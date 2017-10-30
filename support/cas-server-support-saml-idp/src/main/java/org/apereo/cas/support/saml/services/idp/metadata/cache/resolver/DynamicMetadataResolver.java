package org.apereo.cas.support.saml.services.idp.metadata.cache.resolver;

import com.google.common.base.Function;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apereo.cas.configuration.model.support.saml.idp.SamlIdPProperties;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.http.HttpClient;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.metadata.resolver.impl.FunctionDrivenDynamicHTTPMetadataResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * This is {@link DynamicMetadataResolver}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class DynamicMetadataResolver extends BaseSamlRegisteredServiceMetadataResolver {
    private static final Logger LOGGER = LoggerFactory.getLogger(DynamicMetadataResolver.class);

    /**
     * The Http client.
     */
    protected final HttpClient httpClient;

    public DynamicMetadataResolver(final SamlIdPProperties samlIdPProperties,
                                   final OpenSamlConfigBean configBean,
                                   final HttpClient httpClient) {
        super(samlIdPProperties, configBean);
        this.httpClient = httpClient;
    }

    @Override
    public List<MetadataResolver> resolve(final SamlRegisteredService service) {
        LOGGER.info("Loading metadata dynamically for [{}]", service.getName());

        final SamlIdPProperties.Metadata md = samlIdPProperties.getMetadata();
        final FunctionDrivenDynamicHTTPMetadataResolver resolver =
                new FunctionDrivenDynamicHTTPMetadataResolver(this.httpClient.getWrappedHttpClient());
        resolver.setMinCacheDuration(TimeUnit.MILLISECONDS.convert(md.getCacheExpirationMinutes(), TimeUnit.MINUTES));
        resolver.setRequireValidMetadata(md.isRequireValidMetadata());

        if (StringUtils.isNotBlank(md.getBasicAuthnPassword()) && StringUtils.isNotBlank(md.getBasicAuthnUsername())) {
            resolver.setBasicCredentials(new UsernamePasswordCredentials(md.getBasicAuthnUsername(), md.getBasicAuthnPassword()));
        }
        if (!md.getSupportedContentTypes().isEmpty()) {
            resolver.setSupportedContentTypes(md.getSupportedContentTypes());
        }

        resolver.setRequestURLBuilder(new Function<String, String>() {
            @Nullable
            @Override
            public String apply(@Nullable final String input) {
                try {
                    if (StringUtils.isNotBlank(input)) {
                        final String metadataLocation = service.getMetadataLocation().replace("{0}", EncodingUtils.urlEncode(input));
                        LOGGER.info("Constructed dynamic metadata query [{}] for [{}]", metadataLocation, service.getName());
                        return metadataLocation;
                    }
                    return null;
                } catch (final Exception e) {
                    throw new IllegalArgumentException(e.getMessage(), e);
                }
            }
        });
        try {
            buildSingleMetadataResolver(resolver, service);
            return CollectionUtils.wrap(resolver);
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return new ArrayList<>(0);
    }

    /**
     * Is dynamic metadata query configured ?
     *
     * @param service the service
     * @return true/false
     */
    protected boolean isDynamicMetadataQueryConfigured(final SamlRegisteredService service) {
        return service.getMetadataLocation().trim().endsWith("/entities/{0}");
    }

    @Override
    public boolean supports(final SamlRegisteredService service) {
        return isDynamicMetadataQueryConfigured(service);
    }
}
