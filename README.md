cxf-spring-boot-starter-maven-plugin complementing cxf-spring-boot-starter
=============================
[![Build Status](https://travis-ci.org/codecentric/cxf-spring-boot-starter-maven-plugin.svg?branch=master)](https://travis-ci.org/codecentric/cxf-spring-boot-starter-maven-plugin)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/de.codecentric/cxf-spring-boot-starter-maven-plugin/badge.svg)](https://maven-badges.herokuapp.com/maven-central/de.codecentric/cxf-spring-boot-starter-maven-plugin/)
[![Dependency Status](https://www.versioneye.com/user/projects/5720e321fcd19a004544247d/badge.svg?style=flat)](https://www.versioneye.com/user/projects/5720e321fcd19a004544247d)
[![License](http://img.shields.io/:license-apache-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)

While a spring-boot-starter like [cxf-spring-boot-starter] generally only serves as a Maven dependency, something that will be executed in the build-section is not delivered in such a way. But the generation of JAX-B Classfiles is a good candidate to run inside the build-process - so the resulting files aren´t checked into source control system. The configuration e.g. of the [jaxws-maven-plugin] is rather complex to work properly and one has to do some research, till all necessary configuration parameters are set properly. Something like this has to be done:


```
<build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
	        <!-- Generate Java-Classes inkl. JAXB-Bindings from WSDL & imported XSD -->
	        <!-- See Doku at https://jax-ws-commons.java.net/jaxws-maven-plugin/usage.html -->
			<plugin>
				<groupId>org.codehaus.mojo</groupId>
				<artifactId>jaxws-maven-plugin</artifactId>
				<version>2.4.1</version>
				<configuration>
					<!-- See https://jax-ws-commons.java.net/jaxws-maven-plugin/wsimport-mojo.html -->
					<wsdlDirectory>src/main/resources/wsdl/</wsdlDirectory>
					<sourceDestDir>target/generated-sources/wsdlimport</sourceDestDir>
					<!-- For accessing the imported schema, see https://netbeans.org/bugzilla/show_bug.cgi?id=241570 -->
					<vmArgs>
			            <vmArg>-Djavax.xml.accessExternalSchema=all</vmArg>
			        </vmArgs>
			        <!-- the binding.xml in the given directory is found automatically, because the directory is scanned for '.xml'-Files -->
			       	<bindingDirectory>src/main/resources/wsdl</bindingDirectory>
			       	<!-- Arguments for JAXB2-Generator behind JAX-WS-Frontend -->
			       	<args>
			            <arg>-extension</arg>
			            <!-- Thats a tricky parameter: The first '-B' is for passing the following argument to JAXB2-Generator
			                 the second is needed to generate the human readable Namespace-Prefixes -->
			            <arg>-B-Xnamespace-prefix</arg>
			        </args>
				</configuration>
				<dependencies>
			    	<dependency>
			            <groupId>org.jvnet.jaxb2_commons</groupId>
			            <artifactId>jaxb2-namespace-prefix</artifactId>
			            <version>1.1</version>
			        </dependency>
				</dependencies>
				<executions>
					<execution>
						<goals>
							<goal>wsimport</goal>
						</goals>
					</execution>
				</executions>
			</plugin>
			<plugin>
		        <groupId>org.codehaus.mojo</groupId>
		        <artifactId>build-helper-maven-plugin</artifactId>
		        <executions>
		          <execution>
		            <id>add-source</id>
		            <phase>generate-sources</phase>
		            <goals>
		              <goal>add-source</goal>
		            </goals>
		            <configuration>
		              <sources>
		                <source>target/generated-sources/wsdlimport</source>
		              </sources>
		            </configuration>
		          </execution>
		        </executions>
			</plugin>
		</plugins>
        <pluginManagement>
        	<!-- Really ugly - neede to solve Maven Eclipse Plugin m2e issue... Argh! -->
        	<plugins>
        		<!--This plugin's configuration is used to store Eclipse m2e settings only. It has no influence on the Maven build itself.-->
        		<plugin>
        			<groupId>org.eclipse.m2e</groupId>
        			<artifactId>lifecycle-mapping</artifactId>
        			<version>1.0.0</version>
        			<configuration>
        				<lifecycleMappingMetadata>
        					<pluginExecutions>
        						<pluginExecution>
        							<pluginExecutionFilter>
        								<groupId>
        									org.jvnet.jax-ws-commons
        								</groupId>
        								<artifactId>
        									jaxws-maven-plugin
        								</artifactId>
        								<versionRange>
        									[2.3,)
        								</versionRange>
        								<goals>
        									<goal>wsimport</goal>
        								</goals>
        							</pluginExecutionFilter>
        							<action>
        								<execute/>
        							</action>
        						</pluginExecution>
        					</pluginExecutions>
        				</lifecycleMappingMetadata>
        			</configuration>
        		</plugin>
        	</plugins>
        </pluginManagement>
    </build>
```


So according to [stackoverflow:maven-plugin-executing-another-plugin](http://stackoverflow.com/questions/526733/maven-plugin-executing-another-plugin) one of the best ways to allow reusage of build-plugins in Maven is to build a custom Maven plugin, that runs the needed commands e.g. via [mojo-executor] (the other way would have been a Maven parent, but this couldn´t be delivered that well - because you then had to stick to that parent and you´re not allowed to use your own).

### Features include:

* Generating all necessary Java-Classes using JAX-B from your WSDL/XSDs, complementing the [cxf-spring-boot-starter](https://github.com/jonashackt/cxf-spring-boot-starter)
* This works also for complex imports of many XSD files, that inherit other XSDs themselfs
* The generated JAX-B Classfiles will be added to your projects classpath - ready to map & transform into whatever you want

### HowTo

* Put your WSDL into /src/main/resources/wsdl/ - and add your XSDs, so they could be imported correct (relatively)
* If you want to tweak your Namespace-Names, put a binding.xml also into /src/main/resources/wsdl/
* add this plugin to your pom´s build-section:

```
<build>
    <plugins>
        <plugin>
            <groupId>de.codecentric</groupId>
            <artifactId>cxf-spring-boot-starter-maven-plugin</artifactId>
            <version>1.0.4.RELEASE</version>
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

### Dones

* made the plugin Eclipse m2e compatible (see [stackoverflow:eclipse-m2e-lifecycle] and [https://wiki.eclipse.org/M2E_compatible_maven_plugins](https://wiki.eclipse.org/M2E_compatible_maven_plugins), so no Plugin execution not covered by lifecycle configuration”-Error should accur anymore


[cxf-spring-boot-starter]:https://github.com/jonashackt/cxf-spring-boot-starter
[jaxws-maven-plugin]:http://www.mojohaus.org/jaxws-maven-plugin/
[mojo-executor]:https://github.com/TimMoore/mojo-executor
[stackoverflow:eclipse-m2e-lifecycle]:http://stackoverflow.com/a/26447353/4964553