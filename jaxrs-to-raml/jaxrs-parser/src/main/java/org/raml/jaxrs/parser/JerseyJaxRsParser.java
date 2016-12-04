package org.raml.jaxrs.parser;

import org.raml.jaxrs.model.JaxRsApplication;
import org.raml.jaxrs.parser.analyzers.JerseyAnalyzer;
import org.raml.jaxrs.parser.gatherers.JerseyGatherer;
import org.raml.utilities.format.Joiners;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.Set;

class JerseyJaxRsParser implements JaxRsParser {

    private static final Logger logger = LoggerFactory.getLogger(JerseyJaxRsParser.class);

    private JerseyJaxRsParser() {
    }

    public static JerseyJaxRsParser create() {
        return new JerseyJaxRsParser();
    }

    @Override
    public JaxRsApplication parse(Path jaxRsResource) throws JaxRsParsingException {
        logger.info("parsing JaxRs resource: {}", jaxRsResource);

        Iterable<Class<?>> classes = getJaxRsClassesFor(jaxRsResource);

        return JerseyAnalyzer.create(classes).analyze();
    }

    private static Iterable<Class<?>> getJaxRsClassesFor(Path jaxRsResource) {

        Set<Class<?>> classes = JerseyGatherer.forApplication(jaxRsResource).jaxRsClasses();

        if (logger.isDebugEnabled()) {
            logger.debug("found JaxRs related classes: \n{}", Joiners.squareBracketsPerLineJoiner().join(classes));
        }

        return classes;
    }
}
