package org.apereo.cas.support.saml;


import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.shibboleth.utilities.java.support.xml.ParserPool;
import org.opensaml.core.config.ConfigurationService;
import org.opensaml.core.config.InitializationException;
import org.opensaml.core.config.InitializationService;
import org.opensaml.core.xml.XMLObjectBuilderFactory;
import org.opensaml.core.xml.config.XMLObjectProviderRegistry;
import org.opensaml.core.xml.io.MarshallerFactory;
import org.opensaml.core.xml.io.UnmarshallerFactory;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;

/**
 * Load the OpenSAML config context.
 *
 * @author Misagh Moayyed
 * @since 4.1
 */
@Slf4j
@Getter
public class OpenSamlConfigBean {
    private final ParserPool parserPool;

    private XMLObjectBuilderFactory builderFactory;

    private MarshallerFactory marshallerFactory;

    private UnmarshallerFactory unmarshallerFactory;

    /**
     * Instantiates the config bean.
     * @param parserPool the parser pool
     */
    public OpenSamlConfigBean(final ParserPool parserPool) {
        this.parserPool = parserPool;
    }
    

    /**
     * Initialize opensaml.
     */
    @PostConstruct
    public void init() {
        LOGGER.debug("Initializing OpenSaml configuration...");
        Assert.notNull(this.parserPool, "parserPool must not be null");

        try {
            InitializationService.initialize();
        } catch (final InitializationException e) {
            throw new IllegalArgumentException("Exception initializing OpenSAML", e);
        }

        XMLObjectProviderRegistry registry;
        synchronized (ConfigurationService.class) {
            registry = ConfigurationService.get(XMLObjectProviderRegistry.class);
            if (registry == null) {
                LOGGER.debug("XMLObjectProviderRegistry did not exist in ConfigurationService, will be created");
                registry = new XMLObjectProviderRegistry();
                ConfigurationService.register(XMLObjectProviderRegistry.class, registry);
            }
        }

        registry.setParserPool(this.parserPool);

        this.builderFactory = registry.getBuilderFactory();
        this.marshallerFactory = registry.getMarshallerFactory();
        this.unmarshallerFactory = registry.getUnmarshallerFactory();

        LOGGER.debug("Initialized OpenSaml successfully.");
    }
}
