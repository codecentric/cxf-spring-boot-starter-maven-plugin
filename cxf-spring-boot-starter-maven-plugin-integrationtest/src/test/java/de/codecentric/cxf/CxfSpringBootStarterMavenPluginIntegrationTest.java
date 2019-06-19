package de.codecentric.cxf;

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