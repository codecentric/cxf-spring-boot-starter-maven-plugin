package de.codecentric.cxf;

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
import java.util.Collection;
import java.util.Optional;

import static org.twdata.maven.mojoexecutor.MojoExecutor.*;

@Mojo(name = "generate", defaultPhase=LifecyclePhase.GENERATE_SOURCES)
public class BootCxfMojo extends AbstractMojo {

    private static final String GENERATED_SOURCES_TARGET_FOLDER = "target/generated-sources/wsdlimport";
    private static final String TEST_GENERATED_SOURCES_TARGET_FOLDER = "target/test/generated-sources/wsdlimport";
    
    private static final String WSDL_NOT_FOUND_ERROR_MESSAGE = ".wsdl-File not found - is it placed somewhere under /src/main/resources or /src/test/resources?";
    private static final String LOG_PREFIX = "CXF-BOOT-MAVEN-PLUGIN ";

    
    @Parameter( defaultValue = "${project}", readonly = true )
    private MavenProject mavenProject;

    @Parameter( defaultValue = "${session}", readonly = true )
    private MavenSession mavenSession;


    @Component
    private BuildPluginManager pluginManager;
    
    public void execute() throws MojoExecutionException {
        getLog().info("cxf-spring-boot-starter-maven-plugin will now process your WSDL. Lean back and enjoy :)");

        if(isWsdlLocatedInTestResources()) {
            logWithPrefix("STEP 1: Found .wsdl-File: " + findWsdl().getPath());
            logWithPrefix("STEP 2: Generating JAX-B Classfiles for Test purpose.");
            generateJaxbClassFiles("wsimport-test", TEST_GENERATED_SOURCES_TARGET_FOLDER);

            logWithPrefix("STEP 3: Adding the generated Java-Classes to project´s classpath...");
            addGeneratedTestClasses2Cp();

        } else if(isWsdlLocatedInMainResources()) {
            logWithPrefix("STEP 1: Found .wsdl-File: " + findWsdl().getPath());
            logWithPrefix("STEP 2: Generating JAX-B Classfiles.");
            generateJaxbClassFiles("wsimport", GENERATED_SOURCES_TARGET_FOLDER);

            logWithPrefix("STEP 3: Adding the generated Java-Classes to project´s classpath...");
            addGeneratedClasses2Cp();
        } else {
            logWithPrefix(WSDL_NOT_FOUND_ERROR_MESSAGE);
        }

    }



    private void generateJaxbClassFiles(String jaxwsMavenPluginGoal, String dir2PutGeneratedClassesIn) throws MojoExecutionException {
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
                    groupId("org.codehaus.mojo"),
                    artifactId("jaxws-maven-plugin"),
                    version("2.4.1"),
                    dependencies(
                            dependency(
                                    "org.jvnet.jaxb2_commons",
                                    "jaxb2-namespace-prefix",
                                    "1.1"))
                ),
                goal(jaxwsMavenPluginGoal),
                configuration(
                    /*
                     * See http://www.mojohaus.org/jaxws-maven-plugin/wsimport-mojo.html
                     */
                    element(name("wsdlDirectory"), wsdlPathWithoutFileName()),
                    /*
                     * This is very useful to NOT generate something like
                     * wsdlLocation = "file:/Users/myuser/devfolder/cxf-spring-boot-starter/src/test/resources/wsdl/Weather1.0.wsdl"
                     * into the @WebServiceClient generated Class. This could break stuff, e.g. when u build on Jenkins
                     * and then try to deploy on a Linux server, where the path is completely different
                     */
                    element(name("wsdlLocation"), "/" + wsdlFileParentFolderName() + "/" + wsdlFileName()),
                    element(name("sourceDestDir"), dir2PutGeneratedClassesIn),
                    /*
                     * For accessing the imported schema, see https://netbeans.org/bugzilla/show_bug.cgi?id=241570
                     */
                    element("vmArgs",
                            element("vmArg", "-Djavax.xml.accessExternalSchema=all")),
                    /*
                     * the binding.xml in the given directory is found automatically,
                     * because the directory is scanned for '.xml'-Files
                     */
                    element("bindingDirectory", wsdlPathWithoutFileName()),
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


    private boolean isWsdlLocatedInTestResources() throws MojoExecutionException {
        return StringUtils.contains(findWsdl().getPath(), "/test/");
    }

    private boolean isWsdlLocatedInMainResources() throws MojoExecutionException {
        return StringUtils.contains(findWsdl().getPath(), "/main/");
    }

    private String wsdlFileName() throws MojoExecutionException {
        return findWsdl().getName();
    }

    private String wsdlFileParentFolderName() throws MojoExecutionException {
        return findWsdl().getParentFile().getName();
    }

    private String wsdlPathWithoutFileName() throws MojoExecutionException {

        return findWsdl().getParent();
    }

    private File findWsdl() throws MojoExecutionException {
        File baseDir = mavenProject.getBasedir();
        String[] extension = {"wsdl"};
        Collection<File> wsdls = FileUtils.listFiles(baseDir, extension, true);

        Optional<File> wsdl = wsdls.stream().findFirst();

        if(wsdl.isPresent()) {
            return wsdl.get();
        } else {
            throw new MojoExecutionException(WSDL_NOT_FOUND_ERROR_MESSAGE);
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
                        version("1.10")
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
                        version("1.10")
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
}
