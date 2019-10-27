cxf-spring-boot-starter-maven-plugin complementing cxf-spring-boot-starter
=============================
[![Build Status](https://travis-ci.org/codecentric/cxf-spring-boot-starter-maven-plugin.svg?branch=master)](https://travis-ci.org/codecentric/cxf-spring-boot-starter-maven-plugin)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/de.codecentric/cxf-spring-boot-starter-maven-plugin/badge.svg)](https://maven-badges.herokuapp.com/maven-central/de.codecentric/cxf-spring-boot-starter-maven-plugin/)
[![License](http://img.shields.io/:license-apache-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)
[![renovateenabled](https://img.shields.io/badge/renovate-enabled-yellow)](https://renovatebot.com)
[![versionjaxb](https://img.shields.io/badge/dynamic/xml?color=brightgreen&url=https://raw.githubusercontent.com/codecentric/cxf-spring-boot-starter-maven-plugin/master/cxf-spring-boot-starter-maven-plugin/pom.xml&query=%2F%2A%5Blocal-name%28%29%3D%27project%27%5D%2F%2A%5Blocal-name%28%29%3D%27properties%27%5D%2F%2A%5Blocal-name%28%29%3D%27jaxb.version%27%5D&label=org.glassfish.jaxb)](github.com/eclipse-ee4j/jaxb-ri)
[![versionjaxws](https://img.shields.io/badge/dynamic/xml?color=brightgreen&url=https://raw.githubusercontent.com/codecentric/cxf-spring-boot-starter-maven-plugin/master/cxf-spring-boot-starter-maven-plugin/pom.xml&query=%2F%2A%5Blocal-name%28%29%3D%27project%27%5D%2F%2A%5Blocal-name%28%29%3D%27properties%27%5D%2F%2A%5Blocal-name%28%29%3D%27jaxws-ri.version%27%5D&label=com.sun.xml.ws.jaxws)](https://mvnrepository.com/artifact/com.sun.xml.ws/jaxws-rt)
[![versionjava](https://img.shields.io/badge/jdk-8,_9,_11-brightgreen.svg?logo=java)](https://www.oracle.com/technetwork/java/javase/downloads/index.html)
[![versionspring](https://img.shields.io/badge/dynamic/xml?color=brightgreen&url=https://raw.githubusercontent.com/codecentric/cxf-spring-boot-starter-maven-plugin/master/cxf-spring-boot-starter-maven-plugin/pom.xml&query=%2F%2A%5Blocal-name%28%29%3D%27project%27%5D%2F%2A%5Blocal-name%28%29%3D%27properties%27%5D%2F%2A%5Blocal-name%28%29%3D%27spring.version%27%5D&label=spring)](https://spring.io/)


While a spring-boot-starter like [cxf-spring-boot-starter] generally only serves as a Maven dependency, something that will be executed in the build-section is not delivered in such a way. But the generation of JAX-B Classfiles is a good candidate to run inside the build-process - so the resulting files aren´t checked into source control system. The configuration e.g. of the [jaxws-maven-plugin] is rather complex to work properly and one has to do some research, till all necessary configuration parameters are set properly ([something like this](https://github.com/jonashackt/soap-spring-boot-cxf/blob/master/pom.xml) has to be done - just have a look into the build section of the pom).

### Features include:

* Generating all necessary Java-Classes using JAX-B from your WSDL/XSDs, complementing the [cxf-spring-boot-starter]
* This works also for complex imports of many XSD files, that inherit other XSDs themselfs
* The generated JAX-B Classfiles will be added to your projects classpath - ready to map & transform into whatever you want
* Scanning your resource-Folder for the WSDL and configuring the jaxws-maven-plugin, so that non-absolute paths will be generated into @WebServiceClient-Class
* non-absolute paths will be generated into @WebService and @WebServiceClient-Classes (so that one can initialize the Apache CXF endpoint 100% contract-first)
* Extract the targetNamespace from the WSDL, generate the SEI and WebServiceClient annotated classes´ package names from it & write it together with the project´s package name into a cxf-spring-boot-maven.properties to enable Complete automation of Endpoint initialization in the [cxf-spring-boot-starter](https://github.com/codecentric/cxf-spring-boot-starter) ([documentation here](https://github.com/codecentric/cxf-spring-boot-starter#complete-automation-of-endpoint-initialization))
* plugin is Eclipse m2e compatible (see [stackoverflow:eclipse-m2e-lifecycle] and [https://wiki.eclipse.org/M2E_compatible_maven_plugins](https://wiki.eclipse.org/M2E_compatible_maven_plugins), so no Plugin execution not covered by lifecycle configuration”-Error should accur

### HowTo

* Put your WSDL into some folder under __/src/main/resources/__ OR __/src/test/resources/__ - and add your XSDs, so they could be imported correct (relatively)
* If you want to tweak your Namespace-Names, put a binding.xml into the same folder, where your WSDL resides
* add this plugin to your pom´s build-section:

```
<build>
    <plugins>
        <plugin>
            <groupId>de.codecentric</groupId>
            <artifactId>cxf-spring-boot-starter-maven-plugin</artifactId>
            <version>2.1.7.RELEASE</version>
            <executions>
                <execution>
                    <goals>
                        <goal>generate</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```


* it will be executed, if you run a standard:

```
mvn clean generate-sources
```

(or directly):

```
mvn cxf-spring-boot-starter:generate
```

* The generated JAX-B Classfiles will be placed in target/generated-sources/wsdlimport 


### Moved from "OLD" jaxb to "NEW" jaxb

As you might noticed there is a lot going on in the Community effort to move JavaEE to JakartaEE from Oracle to Eclipse Foundation. 

One of the impacts for this project was that the JAX-B GitHub repo [github.com/javaee/jaxb-v2](https://github.com/javaee/jaxb-v2) was archived and a new GitHub repo [github.com/eclipse-ee4j/jaxb-ri](https://github.com/eclipse-ee4j/jaxb-ri) has been created.

Therefore we also moved to the new dependencies inside our [pom.xml](pom.xml) - instead of using [the "OLD JAXB" with the `com.sun.xml.bind` Maven groupId](https://mvnrepository.com/artifact/com.sun.xml.bind/jaxb-xjc), we now use the ["NEW JAXB" with the `org.glassfish.jaxb` Maven groupId](https://mvnrepository.com/artifact/org.glassfish.jaxb/jaxb-xjc):

```
		<!-- Generate package Name of SEI and WebServiceClient for later
		Autodetection of Endpoints in cxf-spring-boot-starter -->
		<dependency>
			<groupId>org.glassfish.jaxb</groupId>
			<artifactId>jaxb-xjc</artifactId>
			<version>${jaxb-xjc.version}</version>
		</dependency>

		<dependency>
			<groupId>org.glassfish.jaxb</groupId>
			<artifactId>jaxb-runtime</artifactId>
			<version>${jaxb.version}</version>
		</dependency>
```



### JDK 11 support

As of JDK11 many deprecated packages aren't distributed with the JDK anymore - including `javax.xml.ws` (JAX-WS) and `javax.xml.bind` (JAX-B) (see [this post](https://blog.viadee.de/jaxb-und-soap-in-java-11)).

Therefore we add the JAX-WS runtime to our pom.xml:

```
		<!-- JAXWS for Java 11 -->
		<dependency>
			<groupId>com.sun.xml.ws</groupId>
			<artifactId>jaxws-rt</artifactId>
			<version>${jaxws-ri.version}</version>
			<type>pom</type>
		</dependency>
```

Sadly the jaxws-maven-plugin [isn't JDK11 (nor JDK9) compatible](https://github.com/mojohaus/jaxws-maven-plugin/issues/54) atm! 

__BUT__: Thanks so much to [mickaelbaron](https://github.com/mojohaus/jaxws-maven-plugin/issues/54#issuecomment-434323813) for stating, that the [mojohaus/jaxws-maven-plugin](https://github.com/mojohaus/jaxws-maven-plugin) is just deprecated and moved silently to [eclipse-ee4j/metro-jax-ws/jaxws-ri/jaxws-maven-plugin/](https://github.com/eclipse-ee4j/metro-jax-ws/tree/master/jaxws-ri/jaxws-maven-plugin)!!!

```
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
```

With this maintained version of [com.sun.xml.ws.jaxws-maven-plugin](https://mvnrepository.com/artifact/com.sun.xml.ws/jaxws-maven-plugin) we are also able to run on all major JDK versions like a charm!


##### Build on multiple JDKs locally

On our local machine in most situations we only have one JDK installation. There are solutions like [jenv](https://www.jenv.be/), but as I really like Docker, we can also run our builds with it:

```
# OpenJDK 13 (latest version)
docker run --rm -v "$PWD":/build/our/plugin -w /build/our/plugin maven:3-jdk-13 bash -c "mvn clean install"

# OpenJDK 12 (latest version)
docker run --rm -v "$PWD":/build/our/plugin -w /build/our/plugin maven:3-jdk-12 bash -c "mvn clean install"

# OpenJDK 11 (latest version)
docker run --rm -v "$PWD":/build/our/plugin -w /build/our/plugin maven:3-jdk-11 bash -c "mvn clean install"

# OpenJDK 8 (latest version)
docker run --rm -v "$PWD":/build/our/plugin -w /build/our/plugin maven:3-jdk-8 bash -c "mvn clean install"
``` 

### Integration testing the plugin

> When updating development/release version: don't forget to update [generation-test-project/pom.xml](cxf-spring-boot-starter-maven-plugin-integrationtest/src/test/resources/generation-test-project/pom.xml)!

We want to avoid as many problems as possible, so we should also do integration tests for this plugin. 

The [docs about testing Maven plugins](https://maven.apache.org/plugin-developers/plugin-testing.html) under the headline `Integration/Functional testing` advice us to use the [org.apache.maven.shared.maven-verifier](https://mvnrepository.com/artifact/org.apache.maven.shared/maven-verifier) plugin (__NOT the standard__ [maven-verifier-plugin](https://maven.apache.org/plugins/maven-verifier-plugin/)!!!), mind the groupId):

```
<dependency>
    <groupId>org.apache.maven.shared</groupId>
    <artifactId>maven-verifier</artifactId>
    <version>1.6</version>
    <scope>test</scope>
</dependency>
```

With this dependency we're able to use test the plugin from within JUnit powered Testcases like [CxfSpringBootStarterMavenPluginIntegrationTest.class](cxf-spring-boot-starter-maven-plugin-integrationtest/src/test/java/de/codecentric/cxf/CxfSpringBootStarterMavenPluginIntegrationTest.java):

```java
import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.apache.maven.it.util.ResourceExtractor;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class CxfSpringBootStarterMavenPluginIntegrationTest {

    @Test
    public void testJaxBandJaxWsClassfilesGenerationInclPluginProperties() throws IOException, VerificationException {

        // Given
        File generationTestProjectDir = ResourceExtractor.simpleExtractResources( getClass(), "/generation-test-project" );
        Verifier verifier = new Verifier( generationTestProjectDir.getAbsolutePath() );

        // When
        verifier.executeGoal( "generate-sources" );

        // Then
        verifier.verifyErrorFreeLog();
    }

}
```

For more code examples on how to use the Verifier API, see [this blog post](https://blog.akquinet.de/2011/02/21/testing-maven-plugins-with-the-verifier-approach/). 

As you may notice, we need a example project `generation-test-project` with a working [pom.xml](cxf-spring-boot-starter-maven-plugin-integrationtest/src/test/resources/generation-test-project/pom.xml) inside `src/test/resources/generation-test-project`:

```
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>de.codecentric.soap</groupId>
    <artifactId>generation-test-project</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <packaging>jar</packaging>

    <description>Project solely for integrationtesting the cxf-spring-boot-starter-maven-plugin</description>

    <properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <java.version>1.8</java.version>
    </properties>

    <build>
        <plugins>
            <plugin>
                <groupId>de.codecentric</groupId>
                <artifactId>cxf-spring-boot-starter-maven-plugin</artifactId>
                <version>2.1.6-SNAPSHOT</version>
                <executions>
                    <execution>
                        <goals>
                            <goal>generate</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>

</project>
```

And we need to introduce a parent pom to the project - because before actually running the integration test with `org.apache.maven.shared.maven-verifier` we need to execute the full Maven lifecycle incl. `mvn install` for the standard `cxf-spring-boot-starter-maven-plugin` project. Otherwise we wouldn't have something to integration test - and the second Maven module build would fail!

The parent [pom.xml](pom.xml) therefore has a `modules` section:

```
	<modules>
		<module>cxf-spring-boot-starter-maven-plugin</module>
		<module>cxf-spring-boot-starter-maven-plugin-integrationtest</module>
	</modules>
```

Because we want to release this plugin to Maven central, all configuration needed there has also been placed into the parent pom.xml, which makes the plugin's pom.xml much more readable, since it only contains the configuration and dependencies really needed.

##### Handling the ArtifactResolutionException: Could not find artifact de.codecentric:cxf-spring-boot-starter-maven-plugin:jar:2.1.6-SNAPSHOT error

There is one problem with the [org.apache.maven.shared.maven-verifier plugin](https://mvnrepository.com/artifact/org.apache.maven.shared/maven-verifier. When I first used the plugin, I got this error

```
[INFO] ------------< de.codecentric.soap:generation-test-project >-------------
[INFO] Building generation-test-project 1.0.0-SNAPSHOT
[INFO] --------------------------------[ jar ]---------------------------------
[WARNING] The POM for de.codecentric:cxf-spring-boot-starter-maven-plugin:jar:2.1.6-SNAPSHOT is missing, no dependency information available
[INFO] ------------------------------------------------------------------------
[INFO] BUILD FAILURE
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  1.193 s
[INFO] Finished at: 2019-06-19T15:19:36+02:00
[INFO] ------------------------------------------------------------------------
[ERROR] Plugin de.codecentric:cxf-spring-boot-starter-maven-plugin:2.1.6-SNAPSHOT or one of its dependencies could not be resolved: Could not find artifact de.codecentric:cxf-spring-boot-starter-maven-plugin:jar:2.1.6-SNAPSHOT -> [Help 1]
org.apache.maven.plugin.PluginResolutionException: Plugin de.codecentric:cxf-spring-boot-starter-maven-plugin:2.1.6-SNAPSHOT or one of its dependencies could not be resolved: Could not find artifact de.codecentric:cxf-spring-boot-starter-maven-plugin:jar:2.1.6-SNAPSHOT
    at org.apache.maven.plugin.internal.DefaultPluginDependenciesResolver.resolve (DefaultPluginDependenciesResolver.java:128)
    at org.apache.maven.plugin.internal.DefaultMavenPluginManager.getPluginDescriptor (DefaultMavenPluginManager.java:182)
    at org.apache.maven.plugin.internal.DefaultMavenPluginManager.getMojoDescriptor (DefaultMavenPluginManager.java:286)
    at org.apache.maven.plugin.DefaultBuildPluginManager.getMojoDescriptor (DefaultBuildPluginManager.java:244)
    at org.apache.maven.lifecycle.internal.DefaultLifecycleMappingDelegate.calculateLifecycleMappings (DefaultLifecycleMappingDelegate.java:116)
    at org.apache.maven.lifecycle.internal.DefaultLifecycleExecutionPlanCalculator.calculateLifecycleMappings (DefaultLifecycleExecutionPlanCalculator.java:265)
    at org.apache.maven.lifecycle.internal.DefaultLifecycleExecutionPlanCalculator.calculateMojoExecutions (DefaultLifecycleExecutionPlanCalculator.java:217)
    at org.apache.maven.lifecycle.internal.DefaultLifecycleExecutionPlanCalculator.calculateExecutionPlan (DefaultLifecycleExecutionPlanCalculator.java:126)
    at org.apache.maven.lifecycle.internal.DefaultLifecycleExecutionPlanCalculator.calculateExecutionPlan (DefaultLifecycleExecutionPlanCalculator.java:144)
    at org.apache.maven.lifecycle.internal.builder.BuilderCommon.resolveBuildPlan (BuilderCommon.java:97)
    at org.apache.maven.lifecycle.internal.LifecycleModuleBuilder.buildProject (LifecycleModuleBuilder.java:111)
    at org.apache.maven.lifecycle.internal.LifecycleModuleBuilder.buildProject (LifecycleModuleBuilder.java:81)
    at org.apache.maven.lifecycle.internal.builder.singlethreaded.SingleThreadedBuilder.build (SingleThreadedBuilder.java:56)
    at org.apache.maven.lifecycle.internal.LifecycleStarter.execute (LifecycleStarter.java:128)
    at org.apache.maven.DefaultMaven.doExecute (DefaultMaven.java:305)
    at org.apache.maven.DefaultMaven.doExecute (DefaultMaven.java:192)
    at org.apache.maven.DefaultMaven.execute (DefaultMaven.java:105)
    at org.apache.maven.cli.MavenCli.execute (MavenCli.java:956)
    at org.apache.maven.cli.MavenCli.doMain (MavenCli.java:288)
    at org.apache.maven.cli.MavenCli.main (MavenCli.java:192)
    at jdk.internal.reflect.NativeMethodAccessorImpl.invoke0 (Native Method)
    at jdk.internal.reflect.NativeMethodAccessorImpl.invoke (NativeMethodAccessorImpl.java:62)
    at jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke (DelegatingMethodAccessorImpl.java:43)
    at java.lang.reflect.Method.invoke (Method.java:567)
    at org.codehaus.plexus.classworlds.launcher.Launcher.launchEnhanced (Launcher.java:282)
    at org.codehaus.plexus.classworlds.launcher.Launcher.launch (Launcher.java:225)
    at org.codehaus.plexus.classworlds.launcher.Launcher.mainWithExitCode (Launcher.java:406)
    at org.codehaus.plexus.classworlds.launcher.Launcher.main (Launcher.java:347)
Caused by: org.eclipse.aether.resolution.ArtifactResolutionException: Could not find artifact de.codecentric:cxf-spring-boot-starter-maven-plugin:jar:2.1.6-SNAPSHOT
    at org.eclipse.aether.internal.impl.DefaultArtifactResolver.resolve (DefaultArtifactResolver.java:423)
    at org.eclipse.aether.internal.impl.DefaultArtifactResolver.resolveArtifacts (DefaultArtifactResolver.java:225)
    at org.eclipse.aether.internal.impl.DefaultArtifactResolver.resolveArtifact (DefaultArtifactResolver.java:202)
    at org.eclipse.aether.internal.impl.DefaultRepositorySystem.resolveArtifact (DefaultRepositorySystem.java:257)
    at org.apache.maven.plugin.internal.DefaultPluginDependenciesResolver.resolve (DefaultPluginDependenciesResolver.java:124)
    at org.apache.maven.plugin.internal.DefaultMavenPluginManager.getPluginDescriptor (DefaultMavenPluginManager.java:182)
    at org.apache.maven.plugin.internal.DefaultMavenPluginManager.getMojoDescriptor (DefaultMavenPluginManager.java:286)
    at org.apache.maven.plugin.DefaultBuildPluginManager.getMojoDescriptor (DefaultBuildPluginManager.java:244)
    at org.apache.maven.lifecycle.internal.DefaultLifecycleMappingDelegate.calculateLifecycleMappings (DefaultLifecycleMappingDelegate.java:116)
    at org.apache.maven.lifecycle.internal.DefaultLifecycleExecutionPlanCalculator.calculateLifecycleMappings (DefaultLifecycleExecutionPlanCalculator.java:265)
    at org.apache.maven.lifecycle.internal.DefaultLifecycleExecutionPlanCalculator.calculateMojoExecutions (DefaultLifecycleExecutionPlanCalculator.java:217)
    at org.apache.maven.lifecycle.internal.DefaultLifecycleExecutionPlanCalculator.calculateExecutionPlan (DefaultLifecycleExecutionPlanCalculator.java:126)
    at org.apache.maven.lifecycle.internal.DefaultLifecycleExecutionPlanCalculator.calculateExecutionPlan (DefaultLifecycleExecutionPlanCalculator.java:144)
    at org.apache.maven.lifecycle.internal.builder.BuilderCommon.resolveBuildPlan (BuilderCommon.java:97)
    at org.apache.maven.lifecycle.internal.LifecycleModuleBuilder.buildProject (LifecycleModuleBuilder.java:111)
    at org.apache.maven.lifecycle.internal.LifecycleModuleBuilder.buildProject (LifecycleModuleBuilder.java:81)
    at org.apache.maven.lifecycle.internal.builder.singlethreaded.SingleThreadedBuilder.build (SingleThreadedBuilder.java:56)
    at org.apache.maven.lifecycle.internal.LifecycleStarter.execute (LifecycleStarter.java:128)
    at org.apache.maven.DefaultMaven.doExecute (DefaultMaven.java:305)
    at org.apache.maven.DefaultMaven.doExecute (DefaultMaven.java:192)
    at org.apache.maven.DefaultMaven.execute (DefaultMaven.java:105)
    at org.apache.maven.cli.MavenCli.execute (MavenCli.java:956)
    at org.apache.maven.cli.MavenCli.doMain (MavenCli.java:288)
    at org.apache.maven.cli.MavenCli.main (MavenCli.java:192)
    at jdk.internal.reflect.NativeMethodAccessorImpl.invoke0 (Native Method)
    at jdk.internal.reflect.NativeMethodAccessorImpl.invoke (NativeMethodAccessorImpl.java:62)
    at jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke (DelegatingMethodAccessorImpl.java:43)
    at java.lang.reflect.Method.invoke (Method.java:567)
    at org.codehaus.plexus.classworlds.launcher.Launcher.launchEnhanced (Launcher.java:282)
    at org.codehaus.plexus.classworlds.launcher.Launcher.launch (Launcher.java:225)
    at org.codehaus.plexus.classworlds.launcher.Launcher.mainWithExitCode (Launcher.java:406)
    at org.codehaus.plexus.classworlds.launcher.Launcher.main (Launcher.java:347)
Caused by: org.eclipse.aether.transfer.ArtifactNotFoundException: Could not find artifact de.codecentric:cxf-spring-boot-starter-maven-plugin:jar:2.1.6-SNAPSHOT
    at org.eclipse.aether.internal.impl.DefaultArtifactResolver.resolve (DefaultArtifactResolver.java:413)
    at org.eclipse.aether.internal.impl.DefaultArtifactResolver.resolveArtifacts (DefaultArtifactResolver.java:225)
    at org.eclipse.aether.internal.impl.DefaultArtifactResolver.resolveArtifact (DefaultArtifactResolver.java:202)
    at org.eclipse.aether.internal.impl.DefaultRepositorySystem.resolveArtifact (DefaultRepositorySystem.java:257)
    at org.apache.maven.plugin.internal.DefaultPluginDependenciesResolver.resolve (DefaultPluginDependenciesResolver.java:124)
    at org.apache.maven.plugin.internal.DefaultMavenPluginManager.getPluginDescriptor (DefaultMavenPluginManager.java:182)
    at org.apache.maven.plugin.internal.DefaultMavenPluginManager.getMojoDescriptor (DefaultMavenPluginManager.java:286)
    at org.apache.maven.plugin.DefaultBuildPluginManager.getMojoDescriptor (DefaultBuildPluginManager.java:244)
    at org.apache.maven.lifecycle.internal.DefaultLifecycleMappingDelegate.calculateLifecycleMappings (DefaultLifecycleMappingDelegate.java:116)
    at org.apache.maven.lifecycle.internal.DefaultLifecycleExecutionPlanCalculator.calculateLifecycleMappings (DefaultLifecycleExecutionPlanCalculator.java:265)
    at org.apache.maven.lifecycle.internal.DefaultLifecycleExecutionPlanCalculator.calculateMojoExecutions (DefaultLifecycleExecutionPlanCalculator.java:217)
    at org.apache.maven.lifecycle.internal.DefaultLifecycleExecutionPlanCalculator.calculateExecutionPlan (DefaultLifecycleExecutionPlanCalculator.java:126)
    at org.apache.maven.lifecycle.internal.DefaultLifecycleExecutionPlanCalculator.calculateExecutionPlan (DefaultLifecycleExecutionPlanCalculator.java:144)
    at org.apache.maven.lifecycle.internal.builder.BuilderCommon.resolveBuildPlan (BuilderCommon.java:97)
    at org.apache.maven.lifecycle.internal.LifecycleModuleBuilder.buildProject (LifecycleModuleBuilder.java:111)
    at org.apache.maven.lifecycle.internal.LifecycleModuleBuilder.buildProject (LifecycleModuleBuilder.java:81)
    at org.apache.maven.lifecycle.internal.builder.singlethreaded.SingleThreadedBuilder.build (SingleThreadedBuilder.java:56)
    at org.apache.maven.lifecycle.internal.LifecycleStarter.execute (LifecycleStarter.java:128)
    at org.apache.maven.DefaultMaven.doExecute (DefaultMaven.java:305)
    at org.apache.maven.DefaultMaven.doExecute (DefaultMaven.java:192)
    at org.apache.maven.DefaultMaven.execute (DefaultMaven.java:105)
    at org.apache.maven.cli.MavenCli.execute (MavenCli.java:956)
    at org.apache.maven.cli.MavenCli.doMain (MavenCli.java:288)
    at org.apache.maven.cli.MavenCli.main (MavenCli.java:192)
    at jdk.internal.reflect.NativeMethodAccessorImpl.invoke0 (Native Method)
    at jdk.internal.reflect.NativeMethodAccessorImpl.invoke (NativeMethodAccessorImpl.java:62)
    at jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke (DelegatingMethodAccessorImpl.java:43)
    at java.lang.reflect.Method.invoke (Method.java:567)
    at org.codehaus.plexus.classworlds.launcher.Launcher.launchEnhanced (Launcher.java:282)
    at org.codehaus.plexus.classworlds.launcher.Launcher.launch (Launcher.java:225)
    at org.codehaus.plexus.classworlds.launcher.Launcher.mainWithExitCode (Launcher.java:406)
    at org.codehaus.plexus.classworlds.launcher.Launcher.main (Launcher.java:347)
[ERROR]
[ERROR] Re-run Maven using the -X switch to enable full debug logging.
[ERROR]
[ERROR] For more information about the errors and possible solutions, please read the following articles:
[ERROR] [Help 1] http://cwiki.apache.org/confluence/display/MAVEN/PluginResolutionException

	at org.apache.maven.it.Verifier.executeGoals(Verifier.java:1369)
	at org.apache.maven.it.Verifier.executeGoal(Verifier.java:1254)
	at org.apache.maven.it.Verifier.executeGoal(Verifier.java:1248)
	at de.codecentric.cxf.CxfSpringBootStarterMavenPluginIntegrationTest.testJaxBandJaxWsClassfilesGenerationInclPluginProperties(CxfSpringBootStarterMavenPluginIntegrationTest.java:21)
	at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
	at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
	at java.base/jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
	at java.base/java.lang.reflect.Method.invoke(Method.java:567)
	at org.junit.runners.model.FrameworkMethod$1.runReflectiveCall(FrameworkMethod.java:50)
	at org.junit.internal.runners.model.ReflectiveCallable.run(ReflectiveCallable.java:12)
	at org.junit.runners.model.FrameworkMethod.invokeExplosively(FrameworkMethod.java:47)
	at org.junit.internal.runners.statements.InvokeMethod.evaluate(InvokeMethod.java:17)
	at org.junit.runners.ParentRunner.runLeaf(ParentRunner.java:325)
	at org.junit.runners.BlockJUnit4ClassRunner.runChild(BlockJUnit4ClassRunner.java:78)
	at org.junit.runners.BlockJUnit4ClassRunner.runChild(BlockJUnit4ClassRunner.java:57)
	at org.junit.runners.ParentRunner$3.run(ParentRunner.java:290)
	at org.junit.runners.ParentRunner$1.schedule(ParentRunner.java:71)
	at org.junit.runners.ParentRunner.runChildren(ParentRunner.java:288)
	at org.junit.runners.ParentRunner.access$000(ParentRunner.java:58)
	at org.junit.runners.ParentRunner$2.evaluate(ParentRunner.java:268)
	at org.junit.runners.ParentRunner.run(ParentRunner.java:363)
	at org.apache.maven.surefire.junit4.JUnit4Provider.execute(JUnit4Provider.java:252)
	at org.apache.maven.surefire.junit4.JUnit4Provider.executeTestSet(JUnit4Provider.java:141)
	at org.apache.maven.surefire.junit4.JUnit4Provider.invoke(JUnit4Provider.java:112)
	at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
	at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
	at java.base/jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
	at java.base/java.lang.reflect.Method.invoke(Method.java:567)
	at org.apache.maven.surefire.util.ReflectionUtils.invokeMethodWithArray(ReflectionUtils.java:189)
	at org.apache.maven.surefire.booter.ProviderFactory$ProviderProxy.invoke(ProviderFactory.java:165)
	at org.apache.maven.surefire.booter.ProviderFactory.invokeProvider(ProviderFactory.java:85)
	at org.apache.maven.surefire.booter.ForkedBooter.runSuitesInProcess(ForkedBooter.java:115)
	at org.apache.maven.surefire.booter.ForkedBooter.main(ForkedBooter.java:75)


Results :

Tests in error:
  testJaxBandJaxWsClassfilesGenerationInclPluginProperties(de.codecentric.cxf.CxfSpringBootStarterMavenPluginIntegrationTest): Exit code was non-zero: 1; command line and log = (..)

Tests run: 1, Failures: 0, Errors: 1, Skipped: 0

[INFO] ------------------------------------------------------------------------
[INFO] Reactor Summary for cxf-spring-boot-starter-maven-plugin-reactor 2.1.6-SNAPSHOT:
[INFO]
[INFO] cxf-spring-boot-starter-maven-plugin-reactor ....... SUCCESS [  0.589 s]
[INFO] cxf-spring-boot-starter-maven-plugin ............... SUCCESS [  4.294 s]
[INFO] cxf-spring-boot-starter-maven-plugin-integrationtest FAILURE [  2.959 s]
[INFO] ------------------------------------------------------------------------
[INFO] BUILD FAILURE
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  8.017 s
[INFO] Finished at: 2019-06-19T15:19:36+02:00
[INFO] ------------------------------------------------------------------------
[ERROR] Failed to execute goal org.apache.maven.plugins:maven-surefire-plugin:2.12.4:test (default-test) on project cxf-spring-boot-starter-maven-plugin-integrationtest: There are test failures.
```

After some hours of digging into the problem, I finally went to the source code of the [Verifier.java](https://github.com/apache/maven-verifier/blob/master/src/main/java/org/apache/maven/shared/verifier/Verifier.java#L195):

```
    private void findDefaultMavenHome()
        throws VerificationException
    {
        defaultClasspath = System.getProperty( "maven.bootclasspath" );
        defaultClassworldConf = System.getProperty( "classworlds.conf" );
        defaultMavenHome = System.getProperty( "maven.home" );

        if ( defaultMavenHome == null )
        {
            Properties envVars = CommandLineUtils.getSystemEnvVars();
            defaultMavenHome = envVars.getProperty( "M2_HOME" );
        }

        if ( defaultMavenHome == null )
        {
            File f = new File( System.getProperty( "user.home" ), "m2" );
            if ( new File( f, "bin/mvn" ).isFile() )
            {
                defaultMavenHome = f.getAbsolutePath();
            }
        }
}
```

And there we are - if we havent set either `M2_HOME` or `maven.home`, the Verifier will place an ugly `${user.home)/m2` directory inside the `cxf-spring-boot-starter-maven-plugin-integrationtest` project folder and tries to download our pre-build `de.codecentric:cxf-spring-boot-starter-maven-plugin:jar:2.1.6-SNAPSHOT`, which isn't available on Maven central - since this was only build into our local Maven repository located under `$HOME/.m2/repository`.

Finally the `README` of the [Maven plugin integration testing source](https://github.com/apache/maven-integration-testing) gave me the hint - we simply use a CLI parameter to set the correct Maven repository location:

```
mvn clean install -Dmaven.repo.local=$HOME/.m2/repository
```

__BUT:__ this means, we need to set this `-D` parameter every time we want to run our integration tests, which is quite nasty. Isn't there a way we could set this programmatically, since our tests aren't executable inside our IDEs otherwise. 

Looking into the the [Verifier source at line 137](https://github.com/apache/maven-verifier/blob/master/src/main/java/org/apache/maven/shared/verifier/Verifier.java#L137) we find out, that there are other constructors - e.g. one with a `settingsFile` parameter, where we can inject the current systems configuration (which mainly also inherits the maven repo path). So we initialize the `Verifier` with:

```
        verifier = new Verifier( generationTestProjectDir.getAbsolutePath(), "$HOME/.m2/settings.xml");
```

Now the tests should work also inside our IDEs and without setting the `-D` parameter.


##### Where's the log file?

We configure the `Verifier` to name the log file like this:

```
verifier.setLogFileName("verifier-output.log");
```

But where can I find this file? Have a look into `target/test-classes/yourTestProjectName`. In this plugin's case, the file is located in:

```
cxf-spring-boot-starter-maven-plugin/cxf-spring-boot-starter-maven-plugin-integrationtest/target/test-classes/generation-test-project/verifier-output.log
```

##### Final testcase

Now after all this our Testcase [CxfSpringBootStarterMavenPluginIntegrationTest.class](cxf-spring-boot-starter-maven-plugin-integrationtest/src/test/java/de/codecentric/cxf/CxfSpringBootStarterMavenPluginIntegrationTest.java) looks like:
                       
```java
package de.codecentric.cxf;

import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.apache.maven.it.util.ResourceExtractor;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class CxfSpringBootStarterMavenPluginIntegrationTest {

    private File generationTestProjectDir;
    private Verifier verifier;

    @Before
    public void setUp() throws IOException, VerificationException {
        // Given
        generationTestProjectDir = ResourceExtractor.simpleExtractResources( getClass(), "/generation-test-project" );
        verifier = new Verifier( generationTestProjectDir.getAbsolutePath(), "$HOME/.m2/settings.xml");
        verifier.setLogFileName("verifier-output.log");
    }

    @Test
    public void plugin_output_should_look_good_in_log() throws VerificationException {

        // When
        verifier.executeGoal( "generate-sources" );

        // Then
        verifier.verifyErrorFreeLog();
        verifier.verifyTextInLog("CXF-BOOT-MAVEN-PLUGIN STEP 0: Scanning for WSDL file in src/main/resources");
        verifier.verifyTextInLog("CXF-BOOT-MAVEN-PLUGIN STEP 1: Found .wsdl-File");
        verifier.verifyTextInLog("CXF-BOOT-MAVEN-PLUGIN STEP 2: Generating JAX-B Classfiles.");
        verifier.verifyTextInLog("Processing: file:");
        verifier.verifyTextInLog("jaxws:wsimport args: [-keep, -s, '");
        verifier.verifyTextInLog("CXF-BOOT-MAVEN-PLUGIN setting relative wsdlLocation into @WebServiceClient:");
        verifier.verifyTextInLog("CXF-BOOT-MAVEN-PLUGIN STEP 4: Guessing SEI implementation´s package name & injecting it into cxf-spring-boot-maven.properties for later Autodetection of Endpoints...");
        verifier.verifyTextInLog("CXF-BOOT-MAVEN-PLUGIN STEP 5: Extracting targetNamespace from WSDL, generating packageName from it with com.sun.tools.xjc.api.XJC (see wsgen, WSImportTool and WSDLModeler at line 2312 of the JAXWSRI) and injecting it into cxf-spring-boot-maven.properties for later Autodetection of Endpoints...");

        // Reset the streams before executing the verifier
        verifier.resetStreams();
    }

}
```


[cxf-spring-boot-starter]:https://github.com/codecentric/cxf-spring-boot-starter
[jaxws-maven-plugin]:https://github.com/eclipse-ee4j/metro-jax-ws/tree/master/jaxws-ri/jaxws-maven-plugin
[mojo-executor]:https://github.com/TimMoore/mojo-executor
[stackoverflow:eclipse-m2e-lifecycle]:http://stackoverflow.com/a/26447353/4964553
[bipro.net]:https://www.bipro.net/