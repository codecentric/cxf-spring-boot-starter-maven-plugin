cxf-spring-boot-starter-maven-plugin complementing cxf-spring-boot-starter
=============================
[![Build Status](https://travis-ci.org/codecentric/cxf-spring-boot-starter-maven-plugin.svg?branch=master)](https://travis-ci.org/codecentric/cxf-spring-boot-starter-maven-plugin)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/de.codecentric/cxf-spring-boot-starter-maven-plugin/badge.svg)](https://maven-badges.herokuapp.com/maven-central/de.codecentric/cxf-spring-boot-starter-maven-plugin/)
[![License](http://img.shields.io/:license-apache-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)

While a spring-boot-starter like [cxf-spring-boot-starter] generally only serves as a Maven dependency, something that will be executed in the build-section is not delivered in such a way. But the generation of JAX-B Classfiles is a good candidate to run inside the build-process - so the resulting files aren´t checked into source control system. The configuration e.g. of the [jaxws-maven-plugin] is rather complex to work properly and one has to do some research, till all necessary configuration parameters are set properly ([something like this](https://github.com/jonashackt/soap-spring-boot-cxf/blob/master/pom.xml) has to be done - just have a look into the build section of the pom).

### Features include:

* Generating all necessary Java-Classes using JAX-B from your WSDL/XSDs, complementing the [cxf-spring-boot-starter]
* This works also for complex imports of many XSD files, that inherit other XSDs themselfs
* The generated JAX-B Classfiles will be added to your projects classpath - ready to map & transform into whatever you want
* Scanning your resource-Folder for the WSDL and configuring the jaxws-maven-plugin, so that non-absolute paths will be generated into @WebServiceClient-Class
* Extract the targetNamespace from the WSDL, generate the SEI and WebServiceClient annotated classes´ package names from it & write it together with the project´s package name into a cxf-spring-boot-maven.properties to enable Complete automation of Endpoint initialization in the [cxf-spring-boot-starter](https://github.com/codecentric/cxf-spring-boot-starter) ([documentation here](https://github.com/codecentric/cxf-spring-boot-starter#complete-automation-of-endpoint-initialization))

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
            <version>2.0.0.RELEASE</version>
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
* non-absolute paths will be generated into @WebService and @WebServiceClient-Classes (so that one can initialize the Apache CXF endpoint 100% contract-first)
* use jaxws:wsimport-test for testrun



[cxf-spring-boot-starter]:https://github.com/codecentric/cxf-spring-boot-starter
[jaxws-maven-plugin]:http://www.mojohaus.org/jaxws-maven-plugin/
[mojo-executor]:https://github.com/TimMoore/mojo-executor
[stackoverflow:eclipse-m2e-lifecycle]:http://stackoverflow.com/a/26447353/4964553
[bipro.net]:https://www.bipro.net/