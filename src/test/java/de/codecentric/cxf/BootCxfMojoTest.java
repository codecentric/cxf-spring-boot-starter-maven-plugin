package de.codecentric.cxf;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class BootCxfMojoTest {

    @Test
    public void does_write_packageName_into_cxfSpringBootMavenProperties_file() throws Exception {

        BootCxfMojo bootCxfMojo = new BootCxfMojo();
        String buildDirectory = new File("target/classes").getAbsolutePath();
        String packageName = "de.codecentric.soap";

        bootCxfMojo.writeCxfSpringBootMavenProperties(buildDirectory, packageName);

        File cxfSpringBootMavenProperties = findCxfSpringBootMavenPropertiesInClasspath();
        assertThat(cxfSpringBootMavenProperties.getName(), is(equalTo(BootCxfMojo.CXF_SPRING_BOOT_MAVEN_PROPERTIES_FILE_NAME)));

        String content = FileUtils.readFileToString(cxfSpringBootMavenProperties, Charset.defaultCharset());
        assertThat(content, is(equalTo("projekt.package.name=de.codecentric.soap")));
    }

    private File findCxfSpringBootMavenPropertiesInClasspath() throws IOException {
        return findInClasspath("classpath*:**/cxf-spring-boot-maven.properties").getFile();
    }

    private Resource findInClasspath(String pattern) throws IOException {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

        Resource[] resources = resolver.getResources(pattern);

        Optional<Resource> first = Arrays.stream(resources).findFirst();

        if(first.isPresent()) {
            return first.get();
        } else {
            throw new FileNotFoundException();
        }
    }

}