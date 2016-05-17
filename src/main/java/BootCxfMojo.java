import static org.twdata.maven.mojoexecutor.MojoExecutor.artifactId;
import static org.twdata.maven.mojoexecutor.MojoExecutor.configuration;
import static org.twdata.maven.mojoexecutor.MojoExecutor.dependencies;
import static org.twdata.maven.mojoexecutor.MojoExecutor.dependency;
import static org.twdata.maven.mojoexecutor.MojoExecutor.element;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executeMojo;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executionEnvironment;
import static org.twdata.maven.mojoexecutor.MojoExecutor.goal;
import static org.twdata.maven.mojoexecutor.MojoExecutor.groupId;
import static org.twdata.maven.mojoexecutor.MojoExecutor.name;
import static org.twdata.maven.mojoexecutor.MojoExecutor.plugin;
import static org.twdata.maven.mojoexecutor.MojoExecutor.version;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

@Mojo(name = "generate", defaultPhase=LifecyclePhase.GENERATE_SOURCES)
public class BootCxfMojo extends AbstractMojo {

    /**
     * WSDL Resource Directory
     */
    @Parameter( property = "generate.wsdl.dir", defaultValue = "src/main/resources/wsdl/" )
    private String wsdldir;
    
    private static final String GENERATED_SOURCES_TARGET_FOLDER = "target/generated-sources/wsdlimport";
    
    /**
     * Test-WSDL Resource Directory
     */
    @Parameter( property = "generate.test.wsdl.dir", defaultValue = "src/test/resources/wsdl/" )
    private String testwsdldir;
    
    private static final String TEST_GENERATED_SOURCES_TARGET_FOLDER = "target/test/generated-sources/wsdlimport";  
    
    private static final String LOG_PREFIX = "CXF-BOOT-MAVEN-PLUGIN STEP ";

    
    @Parameter( defaultValue = "${project}", readonly = true )
    private MavenProject mavenProject;

    @Parameter( defaultValue = "${session}", readonly = true )
    private MavenSession mavenSession;

    @Component
    private BuildPluginManager pluginManager;
    
    public void execute() throws MojoExecutionException {
        getLog().info("cxf-spring-boot-starter-maven-plugin will now process your WSDL. Lean back and enjoy :)");
        
        getLog().info(LOG_PREFIX + "1: Generating JAX-B Classfiles for Test purpose, if there...");
        generateJaxbClassFiles(testwsdldir, TEST_GENERATED_SOURCES_TARGET_FOLDER);
        
        getLog().info(LOG_PREFIX + "2: Adding the generated Java-Classes to project´s classpath...");
        addGeneratedClasses2Cp(TEST_GENERATED_SOURCES_TARGET_FOLDER);
        
        getLog().info(LOG_PREFIX + "3: Generating JAX-B Classfiles, if there...");
        generateJaxbClassFiles(wsdldir, GENERATED_SOURCES_TARGET_FOLDER);
        
        getLog().info(LOG_PREFIX + "4: Adding the generated Java-Classes to project´s classpath...");
        addGeneratedClasses2Cp(GENERATED_SOURCES_TARGET_FOLDER);
    }

    private void generateJaxbClassFiles(String wsdlResourceFolder, String generatedSourcesTargetFolder) throws MojoExecutionException {
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
                goal("wsimport"),
                configuration(
                    /*
                     * See http://www.mojohaus.org/jaxws-maven-plugin/wsimport-mojo.html
                     */
                    element(name("wsdlDirectory"), wsdlResourceFolder),
                    element(name("sourceDestDir"), generatedSourcesTargetFolder),
                    /*
                     * For accessing the imported schema, see https://netbeans.org/bugzilla/show_bug.cgi?id=241570
                     */
                    element("vmArgs",
                            element("vmArg", "-Djavax.xml.accessExternalSchema=all")),
                    /*
                     * the binding.xml in the given directory is found automatically,
                     * because the directory is scanned for '.xml'-Files
                     */
                    element("bindingDirectory", wsdlResourceFolder),
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
    
    private void addGeneratedClasses2Cp(String generatedSourcesTargetFolder) throws MojoExecutionException {
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
                                element("source", generatedSourcesTargetFolder))
                        ),
                executionEnvironment(
                        mavenProject,
                        mavenSession,
                        pluginManager
                    )
                );
    }
}
