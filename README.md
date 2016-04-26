cxf-spring-boot-starter-maven-plugin complementing cxf-spring-boot-starter
=============================
[![License](http://img.shields.io/:license-apache-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)

While a spring-boot-starter like [cxf-spring-boot-starter] generally only serves as a Maven dependency, something that will be executed in the build-section is not delivered in such a way. But the generation of JAX-B Classfiles is a good candidate to run inside the build-process - so the resulting files aren´t checked into source control system. The configuration e.g. of the [jaxws-maven-plugin] is rather complex to work properly and one has to do some research, till all necessary configuration parameters are set properly. So according to [stackoverflow:maven-plugin-executing-another-plugin](http://stackoverflow.com/questions/526733/maven-plugin-executing-another-plugin) one of the best ways to allow reusage of build-plugins in Maven is to build a custom Maven plugin, that runs the needed commands e.g. via [mojo-executor].

### Features include:

* Generating all necessary Java-Classes using JAX-B from your WSDL/XSDs, complementing the [cxf-spring-boot-starter](https://github.com/jonashackt/cxf-spring-boot-starter)
* This works also for complex imports of many XSD files, that inherit other XSDs themselfs
* The generated JAX-B Classfiles will be added to your projects classpath - ready to map & transform into whatever you want

### Documentation

* Put your WSDL into /src/main/resources/wsdl/ - and add your XSDs, so they could be imported correct (relatively)
* If you want to tweak your Namespace-Names, put a binding.xml also into /src/main/resources/wsdl/
* The generated JAX-B Classfiles will be placed in target/generated-sources/wsdlimport 


[cxf-spring-boot-starter]:https://github.com/jonashackt/cxf-spring-boot-starter
[jaxws-maven-plugin]:https://jax-ws-commons.java.net/jaxws-maven-plugin/
[mojo-executor]:https://github.com/TimMoore/mojo-executor