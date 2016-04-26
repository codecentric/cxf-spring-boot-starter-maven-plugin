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
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.project.MavenProject;

@Mojo(name = "process-wsdl")
public class BootCxfMojo extends AbstractMojo {
    
    @Component
    private MavenProject mavenProject;

    @Component
    private MavenSession mavenSession;

    @Component
    private BuildPluginManager pluginManager;
    
    public void execute() throws MojoExecutionException {
        getLog().info("cxf-spring-boot-starter-maven-plugin will now process your WSDL. Lean back and enjoy :)");
        
        executeMojo(
                /*
                 * Generate Java-Classes inkl. JAXB-Bindings from WSDL & imported XSD
                 * See Doku at https://jax-ws-commons.java.net/jaxws-maven-plugin/usage.html
                 */
                plugin(
                    groupId("org.jvnet.jax-ws-commons"),
                    artifactId("jaxws-maven-plugin"),
                    version("2.3"),
                    dependencies(
                            dependency(
                                    "org.jvnet.jaxb2_commons",
                                    "jaxb2-namespace-prefix",
                                    "1.1"))
                ),
                goal("wsimport"),
                configuration(
                    /*
                     * See https://jax-ws-commons.java.net/jaxws-maven-plugin/wsimport-mojo.html
                     */
                    element(name("wsdlDirectory"), "${project.directory}/src/main/resources/wsdl/"),
                    element(name("sourceDestDir"), "${project.directory}/target/generated-sources/wsdlimport"),
                    /*
                     * For accessing the imported schema, see https://netbeans.org/bugzilla/show_bug.cgi?id=241570
                     */
                    element("vmArgs",
                            element("vmArg", "-Djavax.xml.accessExternalSchema=all")),
                    /*
                     * the binding.xml in the given directory is found automatically,
                     * because the directory is scanned for '.xml'-Files
                     */
                    element("bindingDirectory", "src/main/resources/wsdl"),
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
        
        /*
         * Add the generated Java-Classes to classpath
         */
        executeMojo(
                plugin(
                        groupId("org.codehaus.mojo"),
                        artifactId("build-helper-maven-plugin")
                ),
                goal("add-source"),
                configuration(
                        element("sources", 
                                element("source", "target/generated-sources/wsdlimport"))
                        ),
                executionEnvironment(
                        mavenProject,
                        mavenSession,
                        pluginManager
                    )
                );
    }
}
