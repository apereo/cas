package org.apereo.cas.support.saml;

import org.apereo.cas.util.function.FunctionUtils;

import com.codahale.metrics.MetricRegistry;
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
import org.opensaml.xmlsec.config.DecryptionParserPool;

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
    private final XMLObjectBuilderFactory builderFactory;
    private final MarshallerFactory marshallerFactory;
    private final UnmarshallerFactory unmarshallerFactory;
    private final XMLObjectProviderRegistry xmlObjectProviderRegistry;

    @SneakyThrows
    public OpenSamlConfigBean(final @NonNull ParserPool parserPool) {
        this.parserPool = parserPool;

        LOGGER.trace("Initializing OpenSaml configuration...");
        InitializationService.initialize();

        val currentProvider = ConfigurationService.get(XMLObjectProviderRegistry.class);
        this.xmlObjectProviderRegistry = FunctionUtils.doIfNull(currentProvider,
            () -> {
                LOGGER.trace("XMLObjectProviderRegistry did not exist in ConfigurationService and it will be created");
                var provider = new XMLObjectProviderRegistry();
                ConfigurationService.register(XMLObjectProviderRegistry.class, provider);
                return provider;
            },
            () -> currentProvider).get();

        xmlObjectProviderRegistry.setParserPool(this.parserPool);

        ConfigurationService.register(DecryptionParserPool.class, new DecryptionParserPool(this.parserPool));
        ConfigurationService.register(MetricRegistry.class, new MetricRegistry());

        this.builderFactory = xmlObjectProviderRegistry.getBuilderFactory();
        this.marshallerFactory = xmlObjectProviderRegistry.getMarshallerFactory();
        this.unmarshallerFactory = xmlObjectProviderRegistry.getUnmarshallerFactory();
        LOGGER.debug("Initialized OpenSaml successfully.");
    }
}
