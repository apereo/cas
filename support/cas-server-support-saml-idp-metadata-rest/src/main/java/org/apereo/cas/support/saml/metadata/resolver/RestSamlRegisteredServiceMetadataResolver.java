package org.apereo.cas.support.saml.metadata.resolver;

import org.apereo.cas.configuration.model.support.saml.idp.SamlIdPProperties;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlMetadataDocument;
import org.apereo.cas.support.saml.services.idp.metadata.cache.resolver.BaseSamlRegisteredServiceMetadataResolver;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.HttpRequestUtils;
import org.apereo.cas.util.HttpUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.hjson.JsonValue;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.springframework.http.MediaType;

import java.nio.charset.StandardCharsets;
import java.util.Collection;

/**
 * This is {@link RestSamlRegisteredServiceMetadataResolver}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
public class RestSamlRegisteredServiceMetadataResolver extends BaseSamlRegisteredServiceMetadataResolver {
    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    public RestSamlRegisteredServiceMetadataResolver(final SamlIdPProperties samlIdPProperties,
                                                     final OpenSamlConfigBean configBean) {
        super(samlIdPProperties, configBean);
    }

    @Override
    public Collection<? extends MetadataResolver> resolve(final SamlRegisteredService service, final CriteriaSet criteriaSet) {
        HttpResponse response = null;
        try {
            val rest = samlIdPProperties.getMetadata().getRest();
            response = HttpUtils.execute(rest.getUrl(), rest.getMethod(),
                rest.getBasicAuthUsername(), rest.getBasicAuthPassword(),
                CollectionUtils.wrap("entityId", service.getServiceId()),
                CollectionUtils.wrap("Content-Type", MediaType.APPLICATION_XML_VALUE));
            if (response != null && response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                val result = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
                val doc = MAPPER.readValue(JsonValue.readHjson(result).toString(), SamlMetadataDocument.class);
                val resolver = buildMetadataResolverFrom(service, doc);
                return CollectionUtils.wrapList(resolver);
            }
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            HttpUtils.close(response);
        }
        return null;
    }

    @Override
    public boolean supports(final SamlRegisteredService service) {
        try {
            val metadataLocation = service.getMetadataLocation();
            return metadataLocation.trim().startsWith("rest://");
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return false;
    }

    @Override
    public boolean isAvailable(final SamlRegisteredService service) {
        if (supports(service)) {
            val rest = samlIdPProperties.getMetadata().getRest();
            val status = HttpRequestUtils.pingUrl(rest.getUrl());
            return !status.isError();
        }
        return false;
    }
}
