package org.raml.jaxrs.plugin;

import org.apache.commons.io.FilenameUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.raml.emitter.RamlEmissionException;
import org.raml.jaxrs.converter.JaxRsToRamlConversionException;
import org.raml.jaxrs.converter.RamlConfiguration;
import org.raml.jaxrs.parser.JaxRsParsingException;
import org.raml.jaxrs.raml.core.DefaultRamlConfiguration;
import org.raml.jaxrs.raml.core.OneStopShop;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.lang.String.format;

@Mojo(name = "jaxrstoraml", requiresDependencyResolution = ResolutionScope.COMPILE)
public class JaxRsToRamlMojo extends AbstractMojo {

    @Parameter(property = "jaxrs.to.raml.input", defaultValue = "${project.build.outputDirectory}")
    private File input;

    @Parameter(property = "jaxrs.to.raml.sourceDirectory", defaultValue = "${project.build.sourceDirectory}")
    private File sourceDirectory;

    @Parameter(property = "jaxrs.to.raml.outputFileName", defaultValue = "${project.artifactId}.raml")
    private String outputFileName;

    @Parameter(property = "jaxrs.to.raml.outputDirectory", defaultValue = "${project.build.directory}")
    private File outputDirectory;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        PluginConfiguration configuration = createConfiguration();
        confinedExecute(configuration, getLog());
    }

    private static void confinedExecute(PluginConfiguration configuration, Log logger) throws MojoExecutionException {
        checkConfiguration(configuration);
        printConfiguration(configuration, logger);

        Path finalOutputFile = configuration.getOutputDirectory().resolve(configuration.getRamlFileName());
        String applicationName = FilenameUtils.removeExtension(configuration.getRamlFileName().getFileName().toString());


        Path jaxRsUrl = configuration.getInput();

        RamlConfiguration ramlConfiguration = DefaultRamlConfiguration.forApplication(applicationName);
        try {
            OneStopShop.create().parseJaxRsAndOutputRaml(jaxRsUrl, finalOutputFile, ramlConfiguration);
        } catch (JaxRsToRamlConversionException | JaxRsParsingException | RamlEmissionException e) {
            throw new MojoExecutionException(format("unable to generate output raml file: %s", finalOutputFile), e);
        }
    }

    private PluginConfiguration createConfiguration() {
        return PluginConfiguration.create(getInputPath(), getSourceDirectoryPath(), getOutputDirectoryPath(), getRamlFileName());
    }

    private static void printConfiguration(PluginConfiguration configuration, Log logger) {
        logger.info("Configuration");
        logger.info(format("input: %s", configuration.getInput()));
        logger.info(format("source directory: %s", configuration.getSourceDirectory()));
        logger.info(format("output directory: %s", configuration.getOutputDirectory()));
        logger.info(format("output file name: %s", configuration.getRamlFileName()));
    }

    private static void checkConfiguration(PluginConfiguration configuration) throws MojoExecutionException {
        checkInputFile(configuration.getInput());
    }

    private static void checkInputFile(Path inputPath) throws MojoExecutionException {
        //Check that input is an existing file, otherwise fail.
        if (!Files.isRegularFile(inputPath) && !Files.isDirectory(inputPath)) {
            throw new MojoExecutionException(format("invalid input file: %s", inputPath));
        }
    }

    private Path getInputPath() {
        return input.toPath();
    }

    private Path getSourceDirectoryPath() {
        return sourceDirectory.toPath();
    }

    public Path getOutputDirectoryPath() {
        return outputDirectory.toPath();
    }

    public Path getRamlFileName() {
        return Paths.get(outputFileName);
    }
}
