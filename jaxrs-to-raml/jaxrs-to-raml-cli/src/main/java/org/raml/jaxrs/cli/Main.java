package org.raml.jaxrs.cli;

import org.raml.emitter.RamlEmissionException;
import org.raml.jaxrs.converter.JaxRsToRamlConversionException;
import org.raml.jaxrs.converter.RamlConfiguration;
import org.raml.jaxrs.parser.JaxRsParsingException;
import org.raml.jaxrs.raml.core.DefaultRamlConfiguration;
import org.raml.jaxrs.raml.core.OneStopShop;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {

    public static void main(String[] args) throws JaxRsToRamlConversionException, JaxRsParsingException, RamlEmissionException {
        Path jaxRsResourceFile = Paths.get(args[0]);
        Path ramlOutputFile = Paths.get(args[1]);

        OneStopShop oneStopShop = OneStopShop.create();

        RamlConfiguration ramlConfiguration = DefaultRamlConfiguration.forApplication(jaxRsResourceFile.getFileName().toString());

        oneStopShop.parseJaxRsAndOutputRaml(jaxRsResourceFile, ramlOutputFile, ramlConfiguration);
    }
}
