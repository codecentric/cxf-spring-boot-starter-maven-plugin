package de.codecentric.cxf;

import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.apache.maven.it.util.ResourceExtractor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

public class CxfSpringBootStarterMavenPluginIntegrationTest {

    private File generationTestProjectDir;
    private Verifier verifier;

    @BeforeEach
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