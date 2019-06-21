package de.codecentric.cxf;

import com.sun.tools.xjc.api.XJC;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.twdata.maven.mojoexecutor.MojoExecutor.*;

@Mojo(name = "generate", defaultPhase=LifecyclePhase.GENERATE_SOURCES)
public class BootCxfMojo extends AbstractMojo {

    private static final String GENERATED_SOURCES_TARGET_FOLDER = "target/generated-sources/wsdlimport";
    private static final String TEST_GENERATED_SOURCES_TARGET_FOLDER = "target/test/generated-sources/wsdlimport";
    
    private static final String WSDL_NOT_FOUND_ERROR_MESSAGE = ".wsdl-File not found - is it placed somewhere under /src/main/resources or /src/test/resources?";
    private static final String LOG_PREFIX = "CXF-BOOT-MAVEN-PLUGIN ";
    public static final String CXF_SPRING_BOOT_MAVEN_PROPERTIES_FILE_NAME = "cxf-spring-boot-maven.properties";
    public static final String SEI_IMPLEMENTATION_PACKAGE_NAME_KEY = "sei.implementation.package.name";
    public static final String SEI_AND_WEB_SERVICE_CLIENT_PACKAGE_NAME_KEY = "sei.and.webserviceclient.package.name";

    // (?<=targetNamespace=")[:./a-zA-Z0-9]+(?=")
    private static final String REGEX_FIND_TARGET_NAMESPACE_CONTENT = "(?<=targetNamespace=\")[:._/a-zA-Z0-9-]+(?=\")";
    private static final String TARGET_NAMESPACE_COULDNT_BE_EXTRACTED = "targetNamespace could not be extracted from WSDL file.";


    @Parameter( defaultValue = "${project}", readonly = true )
    private MavenProject mavenProject;

    @Parameter( defaultValue = "${session}", readonly = true )
    private MavenSession mavenSession;

    @Component
    private BuildPluginManager pluginManager;

    public void execute() throws MojoExecutionException {

        logWithPrefix("STEP 0: Scanning for WSDL file in src/main/resources");

        File wsdl = findWsdl(mavenProject.getBasedir());
        String buildDirectory = mavenProject.getBuild().getOutputDirectory();

        logWithPrefix("STEP 1: Found .wsdl-File: " + wsdl.getPath());
        if(isWsdlLocatedInTestResources(wsdl)) {
            logWithPrefix("STEP 2: Generating JAX-B Classfiles for Test purpose.");
            generateJaxbClassFiles(wsdl, "wsimport-test", TEST_GENERATED_SOURCES_TARGET_FOLDER);

            logWithPrefix("STEP 3: Adding the generated Test-Java-Classes to project´s classpath...");
            addGeneratedTestClasses2Cp();

        } else if(isWsdlLocatedInMainResources(wsdl)) {
            logWithPrefix("STEP 2: Generating JAX-B Classfiles.");
            generateJaxbClassFiles(wsdl, "wsimport", GENERATED_SOURCES_TARGET_FOLDER);

            logWithPrefix("STEP 3: Adding the generated Java-Classes to project´s classpath...");
            addGeneratedClasses2Cp();
        }

        logWithPrefix("STEP 4: Guessing SEI implementation´s package name & injecting it into " + CXF_SPRING_BOOT_MAVEN_PROPERTIES_FILE_NAME + " for later Autodetection of Endpoints...");
        // The first writer to cxf-spring-boot-maven.properties should clean the file of old entries
        // Otherwise just appending would lead to bogus properties
        cleanCxfSpringBootMavenProperties(buildDirectory);
        writeSeiImplementationPackageToCxfSpringBootMavenPropterties(buildDirectory, mavenProject.getGroupId());

        logWithPrefix("STEP 5: Extracting targetNamespace from WSDL, generating packageName from it with com.sun.tools.xjc.api.XJC (see wsgen, WSImportTool and WSDLModeler at line 2312 of the JAXWSRI) and injecting it into " + CXF_SPRING_BOOT_MAVEN_PROPERTIES_FILE_NAME + " for later Autodetection of Endpoints...");
        String targetNamespaceFromWsdl = readTargetNamespaceFromWsdl(readWsdlIntoString(wsdl));
        String seiImplementationBasePackageName = generatePackageNameFromTargetNamespaceInWsdl(targetNamespaceFromWsdl);
        writeSeiAndWebServiceClientPackageToCxfSpringBootMavenPropterties(buildDirectory, seiImplementationBasePackageName);
    }

    private void generateJaxbClassFiles(File wsdl, String jaxwsMavenPluginGoal, String dir2PutGeneratedClassesIn) throws MojoExecutionException {
        executeMojo(
                /*
                 * Generate Java-Classes inkl. JAXB-Bindings from WSDL & imported XSD
                 * See Doku at http://www.mojohaus.org/jaxws-maven-plugin/
                 *
                 * Attention: The project has been moved from codehaus to project metro in 2007:
                 * https://jax-ws-commons.java.net/jaxws-maven-plugin/ and then back to codehaus
                 * in 2015, where it is developed further: https://github.com/mojohaus/jaxws-maven-plugin
                 */
                plugin(
                    groupId("com.sun.xml.ws"),
                    artifactId("jaxws-maven-plugin"),
                    version("2.3.2"),
                    dependencies(
                            dependency(
                                    "org.jvnet.jaxb2_commons",
                                    "jaxb2-namespace-prefix",
                                    "1.3"))
                    ),
                goal(jaxwsMavenPluginGoal),
                configuration(
                    /*
                     * See http://www.mojohaus.org/jaxws-maven-plugin/wsimport-mojo.html
                     */
                    element(name("wsdlDirectory"), wsdlPathWithoutFileName(wsdl)),
                    /*
                     * This is very useful to NOT generate something like
                     * wsdlLocation = "file:/Users/myuser/devfolder/cxf-spring-boot-starter/src/test/resources/wsdl/Weather1.0.wsdl"
                     * into the @WebServiceClient generated Class. This could break stuff, e.g. when u build on Jenkins
                     * and then try to deploy on a Linux server, where the path is completely different
                     */
                    element(name("wsdlLocation"), constructWsdlLocation(wsdl)),
                    element(name("sourceDestDir"), dir2PutGeneratedClassesIn),
                    /*
                     * For accessing the imported schema, see https://netbeans.org/bugzilla/show_bug.cgi?id=241570
                     */
                    element("vmArgs",
                            element("vmArg", "-Djavax.xml.accessExternalSchema=all -Djavax.xml.accessExternalDTD=all")),
                    /*
                     * the binding.xml in the given directory is found automatically,
                     * because the directory is scanned for '.xml'-Files
                     */
                    element("bindingDirectory", wsdlPathWithoutFileName(wsdl)),
                    /*
                     * Arguments for JAXB2-Generator behind JAX-WS-Frontend
                     */
                    element("args",
                            element("arg", "-extension"),
                            /*
                             * Thats a tricky parameter: The first '-B' is for passing the following argument
                             * to JAXB2-Generator the second is needed to generate the human readable Namespace-
                             * Prefixes
                             */
                            element("arg", "-B-Xnamespace-prefix"))
                ),
                executionEnvironment(
                    mavenProject,
                    mavenSession,
                    pluginManager
                )
            );
    }


    private String constructWsdlLocation(File wsdl) throws MojoExecutionException {
        String wsdlLocation = "/" + wsdlFolderInResources(wsdl) + wsdlFileName(wsdl);
        logWithPrefix("setting relative wsdlLocation into @WebServiceClient: " + wsdlLocation);
        return wsdlLocation;
    }

    private boolean isWsdlLocatedInTestResources(File wsdl) throws MojoExecutionException {
        return StringUtils.contains(wsdl.getPath(), "/test/") || StringUtils.contains(wsdl.getPath(), "\\test\\");
    }


    private boolean isWsdlLocatedInMainResources(File wsdl) throws MojoExecutionException {
        return StringUtils.contains(wsdl.getPath(), "/main/") || StringUtils.contains(wsdl.getPath(), "\\main\\");
    }

    private String wsdlFileName(File wsdl) throws MojoExecutionException {
        return wsdl.getName();
    }

    private String wsdlFolderInResources(File wsdl) {
        String folderAboveResourceDir = wsdlFileParentFolderName(wsdl, "");
        return folderAboveResourceDir;
    }

    private String wsdlFileParentFolderName(File wsdl, String folderAboveResourceDir) {
        if(!"resources".equals(wsdl.getParentFile().getName())) {
            folderAboveResourceDir = wsdl.getParentFile().getName() + "/" + folderAboveResourceDir;
            return wsdlFileParentFolderName(wsdl.getParentFile(), folderAboveResourceDir);
        } else {
            return folderAboveResourceDir;
        }
    }

    private String wsdlPathWithoutFileName(File wsdl) throws MojoExecutionException {
        return wsdl.getParent();
    }

    protected File findWsdl(File buildDirectory) throws MojoExecutionException {
        String[] extension = {"wsdl"};
        Collection<File> wsdls = FileUtils.listFiles(buildDirectory, extension, true);

        filterOutWsdlsInsideBuildOutputFolder(wsdls);

        Optional<File> wsdl = wsdls.stream().findFirst();

        if(wsdl.isPresent()) {
            return wsdl.get();
        } else {
            throw new MojoExecutionException(WSDL_NOT_FOUND_ERROR_MESSAGE);
        }
    }

    private void filterOutWsdlsInsideBuildOutputFolder(Collection<File> wsdls) {
        if(mavenProject != null) {
            String targetDirectory = mavenProject.getBuild().getOutputDirectory().replaceAll("classes$", "");
            wsdls.removeIf(f -> f.getAbsolutePath().startsWith(targetDirectory));
        }
    }

    private void addGeneratedClasses2Cp() throws MojoExecutionException {
        /*
         * Add the generated Java-Classes to classpath
         */
        executeMojo(
                plugin(
                        groupId("org.codehaus.mojo"),
                        artifactId("build-helper-maven-plugin"),
                        version("3.0.0")
                ),
                goal("add-source"),
                configuration(
                        element("sources",
                                element("source", GENERATED_SOURCES_TARGET_FOLDER))
                        ),
                executionEnvironment(
                        mavenProject,
                        mavenSession,
                        pluginManager
                    )
                );
    }

    private void addGeneratedTestClasses2Cp() throws MojoExecutionException {
        /*
         * Add the generated Java-Classes to classpath
         */
        executeMojo(
                plugin(
                        groupId("org.codehaus.mojo"),
                        artifactId("build-helper-maven-plugin"),
                        version("3.0.0")
                ),
                goal("add-test-source"),
                configuration(
                        element("sources",
                                element("source", TEST_GENERATED_SOURCES_TARGET_FOLDER))
                ),
                executionEnvironment(
                        mavenProject,
                        mavenSession,
                        pluginManager
                )
        );
    }

    private void logWithPrefix(String logMessage) {
        getLog().info(LOG_PREFIX + logMessage);
    }

    protected String readTargetNamespaceFromWsdl(String wsdl) throws MojoExecutionException {

        Matcher matcher = buildMatcher(wsdl, REGEX_FIND_TARGET_NAMESPACE_CONTENT);

        if (matcher.find()) {
            return matcher.group(0);
        } else {
            throw new MojoExecutionException(TARGET_NAMESPACE_COULDNT_BE_EXTRACTED);
        }
    }

    protected static String readWsdlIntoString(File wsdl) throws MojoExecutionException {
        try {
            return FileUtils.readFileToString(wsdl, Charset.defaultCharset());
        } catch (IOException ioEx) {
            throw new MojoExecutionException("Problems in transforming WSDL File to String.", ioEx);
        }
    }

    private static Matcher buildMatcher(String string2SearchIn, String regex) {
        Pattern pattern = Pattern.compile(regex);
        return pattern.matcher(string2SearchIn);
    }

    protected String generatePackageNameFromTargetNamespaceInWsdl(String targetNamespaceFromWsdl) throws MojoExecutionException {
        /*
         * We need to use the same mechanism jaxws-maven-plugin, which itself uses WSimportTool of the JAXWS-RI implementation,
         * to obtain the package-Name from the WSDL file, where the classes are generated to. The WSDL´s targetNamespace is
         * used to generate the package name. If you have targetNamespace="http://www.codecentric.de/namespace/weatherservice/"
         * for example, your package will be de.codecentric.namespace.weatherservice.
         * The code is in WSDLModeler at line 2312:
         */
        return XJC.getDefaultPackageName(targetNamespaceFromWsdl);
    }

    protected void writeSeiAndWebServiceClientPackageToCxfSpringBootMavenPropterties(String outputDirectory, String packageName) throws MojoExecutionException {
        writeCxfSpringBootMavenProperties(outputDirectory, SEI_AND_WEB_SERVICE_CLIENT_PACKAGE_NAME_KEY, packageName);
    }

    protected void writeSeiImplementationPackageToCxfSpringBootMavenPropterties(String outputDirectory, String packageName) throws MojoExecutionException {
        writeCxfSpringBootMavenProperties(outputDirectory, SEI_IMPLEMENTATION_PACKAGE_NAME_KEY, packageName);
    }

    protected void writeCxfSpringBootMavenProperties(String outputDirectory, String propertyKey, String packageName) throws MojoExecutionException {
        try {
            File cxfSpringBootMavenProperties = new File(outputDirectory + "/" + CXF_SPRING_BOOT_MAVEN_PROPERTIES_FILE_NAME);
            FileUtils.writeStringToFile(cxfSpringBootMavenProperties, propertyKey + "=" + packageName + "\n", Charset.defaultCharset(), true);

        } catch (IOException ioExc) {
            throw new MojoExecutionException("Could not inject packageName into " + CXF_SPRING_BOOT_MAVEN_PROPERTIES_FILE_NAME + "." +
                    "Have you set the pom groupId correctly?", ioExc);
        }
    }

    public void cleanCxfSpringBootMavenProperties(String outputDirectory) throws MojoExecutionException {
        try {
            File cxfSpringBootMavenProperties = new File(outputDirectory + "/" + CXF_SPRING_BOOT_MAVEN_PROPERTIES_FILE_NAME);
            FileUtils.writeStringToFile(cxfSpringBootMavenProperties, "", Charset.defaultCharset());

        } catch (IOException ioExc) {
            throw new MojoExecutionException("Could not clean " + CXF_SPRING_BOOT_MAVEN_PROPERTIES_FILE_NAME, ioExc);
        }
    }
}
