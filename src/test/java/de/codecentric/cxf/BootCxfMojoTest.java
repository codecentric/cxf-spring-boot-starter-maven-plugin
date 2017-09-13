package de.codecentric.cxf;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.junit.Test;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Optional;

import static de.codecentric.cxf.BootCxfMojo.readWsdlIntoString;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class BootCxfMojoTest {

    private File weatherServiceWsdl = new File("src/test/resources/Weather1.0.wsdl");

    private BootCxfMojo bootCxfMojo = new BootCxfMojo();;
    private File resourcesDirectory = new File("src/test/resources");
    private String buildDirectory = new File("target/classes").getAbsolutePath();

    @Test public void
    find_Wsdl_in_classpath() throws IOException, MojoExecutionException {

        File wsdl = bootCxfMojo.findWsdl(resourcesDirectory);

        assertThat(wsdl.getName(), is(equalTo(weatherServiceWsdl.getName())));
    }

    @Test public void
    read_target_namespace_from_Wsdl() throws MojoExecutionException {

        File wsdl = bootCxfMojo.findWsdl(resourcesDirectory);
        String targetNamespace = bootCxfMojo.readTargetNamespaceFromWsdl(readWsdlIntoString(wsdl));

        assertThat(targetNamespace, is(equalTo("http://www.codecentric.de/namespace/weatherservice/")));
    }

    @Test public void
    extract_correct_package_name_from_target_namespace_in_Wsdl() throws MojoExecutionException {

        File wsdl = bootCxfMojo.findWsdl(resourcesDirectory);
        String targetNamespace = bootCxfMojo.readTargetNamespaceFromWsdl(readWsdlIntoString(wsdl));
        String packageName = bootCxfMojo.generatePackageNameFromTargetNamespaceInWsdl(targetNamespace);

        assertThat(packageName, is(equalTo("de.codecentric.namespace.weatherservice")));
    }

    @Test public void
    extract_correct_package_name_from_target_namespace_containing_numbers_in_Wsdl() throws MojoExecutionException {

        String wsdl = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<wsdl:definitions xmlns:s=\"http://www.w3.org/2001/XMLSchema\"\n" +
                "\t\t\t\t  xmlns:soap12=\"http://schemas.xmlsoap.org/wsdl/soap12/\"\n" +
                "\t\t\t\t  targetNamespace=\"http://www.abc.ch/namespase/filsearch/v5\"\n" +
                "\t\t\t\t  xmlns:wsdl=\"http://schemas.xmlsoap.org/wsdl/\">\n" +
                "</wsdl:definitions>";
        String targetNamespace = bootCxfMojo.readTargetNamespaceFromWsdl(wsdl);
        String packageName = bootCxfMojo.generatePackageNameFromTargetNamespaceInWsdl(targetNamespace);

        assertThat(packageName, is(equalTo("ch.abc.namespase.filsearch.v5")));
    }

    @Test public void
    extract_correct_package_name_from_target_namespace_containing_dash_in_Wsdl() throws MojoExecutionException {

        String wsdl = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<wsdl:definitions xmlns:s=\"http://www.w3.org/2001/XMLSchema\"\n" +
                "\t\t\t\t  xmlns:soap12=\"http://schemas.xmlsoap.org/wsdl/soap12/\"\n" +
                "\t\t\t\t  targetNamespace=\"urn:microsoft-dynamics-schemas/page/customers\"\n" +
                "\t\t\t\t  xmlns:wsdl=\"http://schemas.xmlsoap.org/wsdl/\">\n" +
                "</wsdl:definitions>";
        String targetNamespace = bootCxfMojo.readTargetNamespaceFromWsdl(wsdl);
        String packageName = bootCxfMojo.generatePackageNameFromTargetNamespaceInWsdl(targetNamespace);

        assertThat(packageName, is(equalTo("schemas.dynamics.microsoft.page.customers")));
    }

    @Test public void
    extract_correct_package_name_from_target_namespace_containing_underscores_in_Wsdl() throws MojoExecutionException {

        String wsdl = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<wsdl:definitions xmlns:s=\"http://www.w3.org/2001/XMLSchema\"\n" +
                "\t\t\t\t  xmlns:soap12=\"http://schemas.xmlsoap.org/wsdl/soap12/\"\n" +
                "\t\t\t\t  targetNamespace=\"http://webservice_v26.tag.crm.comarch.com\"\n" +
                "\t\t\t\t  xmlns:wsdl=\"http://schemas.xmlsoap.org/wsdl/\">\n" +
                "</wsdl:definitions>";
        String targetNamespace = bootCxfMojo.readTargetNamespaceFromWsdl(wsdl);
        String packageName = bootCxfMojo.generatePackageNameFromTargetNamespaceInWsdl(targetNamespace);

        assertThat(packageName, is(equalTo("com.comarch.crm.tag.webservice_v26")));
    }

    @Test public void
    does_write_cxfSpringBootMavenProperties() throws MojoExecutionException, IOException {

        bootCxfMojo.writeCxfSpringBootMavenProperties(buildDirectory, "foo.key", "foo.bar");

        File cxfSpringBootMavenProperties = findCxfSpringBootMavenPropertiesInClasspath();
        assertThat(cxfSpringBootMavenProperties.getName(), is(equalTo(BootCxfMojo.CXF_SPRING_BOOT_MAVEN_PROPERTIES_FILE_NAME)));

    }

    @Test public void
    does_clean_cxfSpringBootMavenProperties() throws MojoExecutionException, IOException {

        bootCxfMojo.cleanCxfSpringBootMavenProperties(buildDirectory);

        String content = FileUtils.readFileToString(findCxfSpringBootMavenPropertiesInClasspath(), Charset.defaultCharset());
        assertThat(content.length(), is(0));
    }

    @Test public void
    does_write_Sei_And_WebServiceClient_packageName_into_cxfSpringBootMavenProperties_file() throws MojoExecutionException, IOException {

        String packageNameGeneratedFromTargetNamespace = "de.codecentric.namespace.weatherservice";

        bootCxfMojo.writeSeiAndWebServiceClientPackageToCxfSpringBootMavenPropterties(buildDirectory, packageNameGeneratedFromTargetNamespace);

        String content = FileUtils.readFileToString(findCxfSpringBootMavenPropertiesInClasspath(), Charset.defaultCharset());
        assertThat(content, containsString(BootCxfMojo.SEI_AND_WEB_SERVICE_CLIENT_PACKAGE_NAME_KEY + "=" + packageNameGeneratedFromTargetNamespace));
    }


    @Test public void
    does_write_Sei_Implementation_packageName_into_cxfSpringBootMavenProperties_file() throws Exception {

        String seiImplementationPackageName = "de.codecentric.soap";

        bootCxfMojo.writeSeiImplementationPackageToCxfSpringBootMavenPropterties(buildDirectory, seiImplementationPackageName);

        String content = FileUtils.readFileToString(findCxfSpringBootMavenPropertiesInClasspath(), Charset.defaultCharset());
        assertThat(content, containsString(BootCxfMojo.SEI_IMPLEMENTATION_PACKAGE_NAME_KEY + "=" + seiImplementationPackageName));
    }

    @Test public void
    does_not_overwrite_packages_in_cxfSpringBootMavenProperties_file() throws MojoExecutionException, IOException {
        bootCxfMojo.writeCxfSpringBootMavenProperties(buildDirectory, "foo.key", "foo.bar");
        bootCxfMojo.writeCxfSpringBootMavenProperties(buildDirectory, "bar.key", "bar.boar");

        String content = FileUtils.readFileToString(findCxfSpringBootMavenPropertiesInClasspath(), Charset.defaultCharset());
        assertThat(content, containsString("foo.key" + "=" + "foo.bar"));
        assertThat(content, containsString("bar.key" + "=" + "bar.boar"));
    }

    private File findCxfSpringBootMavenPropertiesInClasspath() throws IOException {
        return findInClasspath("classpath*:**/" + BootCxfMojo.CXF_SPRING_BOOT_MAVEN_PROPERTIES_FILE_NAME).getFile();
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