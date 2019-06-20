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
    }

    @Test
    public void testCXF_BOOT_MAVEN_PLUGIN_STEP_1_is_in_output() throws VerificationException {

        // When
        verifier.executeGoal( "generate-sources" );

        // Then
        verifier.verifyErrorFreeLog();
        verifier.verifyTextInLog("CXF-BOOT-MAVEN-PLUGIN STEP 1: Found .wsdl-File");
    }

    @Test
    public void testCXF_BOOT_MAVEN_PLUGIN_STEP_2_is_in_output() throws VerificationException {

        // When
        verifier.executeGoal( "generate-sources" );

        // Then
        verifier.verifyErrorFreeLog();
        verifier.verifyTextInLog("CXF-BOOT-MAVEN-PLUGIN STEP 2: Generating JAX-B Classfiles.");
    }

    @Test
    public void testCXF_BOOT_MAVEN_PLUGIN_setting_relative_wsdlLocation_is_in_output() throws VerificationException {

        // When
        verifier.executeGoal( "generate-sources" );

        // Then
        verifier.verifyErrorFreeLog();
        verifier.verifyTextInLog("CXF-BOOT-MAVEN-PLUGIN setting relative wsdlLocation into @WebServiceClient:");
    }

}