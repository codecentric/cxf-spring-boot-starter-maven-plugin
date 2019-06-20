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
        generationTestProjectDir = ResourceExtractor.simpleExtractResources( getClass(), "/generation-test-project" );
        verifier = new Verifier( generationTestProjectDir.getAbsolutePath(), "$HOME/.m2/settings.xml");
        verifier.setLogFileName("verifier-output.log");
    }

    @Test
    public void plugin_should_be_executed() throws VerificationException {

        // When
        verifier.executeGoal( "generate-sources" );

        // Then
        verifier.verifyErrorFreeLog();
    }

    @Test
    public void testCXF_BOOT_MAVEN_PLUGIN_STEP_0_is_in_output() throws VerificationException {

        // When
        verifier.executeGoal( "generate-sources" );

        // Then
        verifier.verifyErrorFreeLog();
        verifier.verifyTextInLog("CXF-BOOT-MAVEN-PLUGIN STEP 0: Scanning for WSDL file in src/main/resources");

        // Reset the streams before executing the verifier
        verifier.resetStreams();
    }

    @Test
    public void testCXF_BOOT_MAVEN_PLUGIN_STEP_1_is_in_output() throws VerificationException {

        // When
        verifier.executeGoal( "generate-sources" );

        // Then
        verifier.verifyErrorFreeLog();
        verifier.verifyTextInLog("CXF-BOOT-MAVEN-PLUGIN STEP 1: Found .wsdl-File");

        // Reset the streams before executing the verifier
        verifier.resetStreams();
    }

    @Test
    public void testCXF_BOOT_MAVEN_PLUGIN_STEP_2_is_in_output() throws VerificationException {

        // When
        verifier.executeGoal( "generate-sources" );

        // Then
        verifier.verifyErrorFreeLog();
        verifier.verifyTextInLog("CXF-BOOT-MAVEN-PLUGIN STEP 2: Generating JAX-B Classfiles.");

        // Reset the streams before executing the verifier
        verifier.resetStreams();
    }

    @Test
    public void testCXF_BOOT_MAVEN_PLUGIN_setting_relative_wsdlLocation_is_in_output() throws VerificationException {

        // When
        verifier.executeGoal( "generate-sources" );

        // Then
        verifier.verifyErrorFreeLog();
        verifier.verifyTextInLog("CXF-BOOT-MAVEN-PLUGIN setting relative wsdlLocation into @WebServiceClient:");

        // Reset the streams before executing the verifier
        verifier.resetStreams();
    }

    @Test
    public void testCXF_BOOT_MAVEN_PLUGIN_STEP_4_is_in_output() throws VerificationException {

        // When
        verifier.executeGoal( "generate-sources" );

        // Then
        verifier.verifyErrorFreeLog();
        verifier.verifyTextInLog("CXF-BOOT-MAVEN-PLUGIN STEP 4: Guessing SEI implementation´s package name & injecting it into cxf-spring-boot-maven.properties for later Autodetection of Endpoints...");

        // Reset the streams before executing the verifier
        verifier.resetStreams();
    }

    @Test
    public void testCXF_BOOT_MAVEN_PLUGIN_STEP_5_is_in_output() throws VerificationException {

        // When
        verifier.executeGoal( "generate-sources" );

        // Then
        verifier.verifyErrorFreeLog();
        verifier.verifyTextInLog("CXF-BOOT-MAVEN-PLUGIN STEP 5: Extracting targetNamespace from WSDL, generating packageName from it with com.sun.tools.xjc.api.XJC (see wsgen, WSImportTool and WSDLModeler at line 2312 of the JAXWSRI) and injecting it into cxf-spring-boot-maven.properties for later Autodetection of Endpoints...");

        // Reset the streams before executing the verifier
        verifier.resetStreams();
    }

}