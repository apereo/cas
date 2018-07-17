package org.apereo.cas.support.saml;

import org.apereo.cas.util.function.FunctionUtils;

import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.shibboleth.utilities.java.support.xml.ParserPool;
import org.opensaml.core.config.ConfigurationService;
import org.opensaml.core.config.InitializationService;
import org.opensaml.core.xml.XMLObjectBuilderFactory;
import org.opensaml.core.xml.config.XMLObjectProviderRegistry;
import org.opensaml.core.xml.io.MarshallerFactory;
import org.opensaml.core.xml.io.UnmarshallerFactory;

/**
 * Load the OpenSAML config context.
 *
 * @author Misagh Moayyed
 * @since 4.1
 */
@Slf4j
@Getter
public class OpenSamlConfigBean {

    @NonNull
    private final ParserPool parserPool;

    private final XMLObjectBuilderFactory builderFactory;
    private final MarshallerFactory marshallerFactory;
    private final UnmarshallerFactory unmarshallerFactory;

    @SneakyThrows
    public OpenSamlConfigBean(final ParserPool parserPool) {
        this.parserPool = parserPool;

        LOGGER.debug("Initializing OpenSaml configuration...");
        InitializationService.initialize();

        val currentProvider = ConfigurationService.get(XMLObjectProviderRegistry.class);
        val registry = FunctionUtils.doIfNull(currentProvider,
            () -> {
                LOGGER.debug("XMLObjectProviderRegistry did not exist in ConfigurationService and it will be created");
                var provider = new XMLObjectProviderRegistry();
                ConfigurationService.register(XMLObjectProviderRegistry.class, provider);
                return provider;
            },
            () -> currentProvider).get();
        registry.setParserPool(this.parserPool);

        this.builderFactory = registry.getBuilderFactory();
        this.marshallerFactory = registry.getMarshallerFactory();
        this.unmarshallerFactory = registry.getUnmarshallerFactory();
        LOGGER.debug("Initialized OpenSaml successfully.");
    }
}
