package org.apereo.cas.support.saml.services.idp.metadata.cache.resolver;

import org.apereo.cas.configuration.model.support.saml.idp.SamlIdPProperties;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.support.saml.InMemoryResourceMetadataResolver;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.SamlException;
import org.apereo.cas.support.saml.SamlUtils;
import org.apereo.cas.support.saml.StaticXmlObjectMetadataResolver;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.HttpRequestUtils;
import org.apereo.cas.util.HttpUtils;

import com.google.common.io.ByteStreams;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.opensaml.core.criterion.EntityIdCriterion;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.util.XMLObjectSource;
import org.opensaml.saml.metadata.resolver.impl.AbstractMetadataResolver;
import org.springframework.http.HttpStatus;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Optional;

/**
 * This is {@link MetadataQueryProtocolMetadataResolver}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
public class MetadataQueryProtocolMetadataResolver extends UrlResourceMetadataResolver {

    public MetadataQueryProtocolMetadataResolver(final SamlIdPProperties samlIdPProperties,
                                                 final OpenSamlConfigBean configBean) {
        super(samlIdPProperties, configBean);
    }

    @Override
    protected String getMetadataLocationForService(final SamlRegisteredService service, final CriteriaSet criteriaSet) {
        LOGGER.debug("Getting metadata location dynamically for [{}] based on criteria [{}]", service.getName(), criteriaSet);
        val entityIdCriteria = criteriaSet.get(EntityIdCriterion.class);
        val entityId = Optional.ofNullable(entityIdCriteria).map(EntityIdCriterion::getEntityId).orElseGet(service::getServiceId);
        if (StringUtils.isBlank(entityId)) {
            throw new SamlException("Unable to determine entity id to fetch metadata dynamically via MDQ for service " + service.getName());
        }
        return service.getMetadataLocation().replace("{0}", EncodingUtils.urlEncode(entityId));
    }

    @Override
    protected HttpResponse fetchMetadata(final String metadataLocation, final CriteriaSet criteriaSet) {
        val metadata = samlIdPProperties.getMetadata();
        val headers = new LinkedHashMap<String, Object>();
        headers.put("Content-Type", metadata.getSupportedContentTypes());
        headers.put("Accept", "*/*");

        LOGGER.debug("Fetching dynamic metadata via MDQ for [{}]", metadataLocation);
        val response = HttpUtils.executeGet(metadataLocation, metadata.getBasicAuthnUsername(),
            samlIdPProperties.getMetadata().getBasicAuthnPassword(), new HashMap<>(), headers);
        if (response == null) {
            LOGGER.error("Unable to fetch metadata from [{}]", metadataLocation);
            throw new UnauthorizedServiceException(UnauthorizedServiceException.CODE_UNAUTHZ_SERVICE);
        }
        return response;
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

    @Override
    protected boolean shouldHttpResponseStatusBeProcessed(final HttpStatus status) {
        return super.shouldHttpResponseStatusBeProcessed(status) || status == HttpStatus.NOT_MODIFIED;
    }

    @Override
    protected AbstractMetadataResolver getMetadataResolverFromResponse(final HttpResponse response, final File backupFile) throws Exception {
        if (response.getStatusLine().getStatusCode() == HttpStatus.NOT_MODIFIED.value()) {
            return new InMemoryResourceMetadataResolver(backupFile, this.configBean);
        }
        val entity = response.getEntity();
        val ins = entity.getContent();
        val source = ByteStreams.toByteArray(ins);
        val xmlObject = SamlUtils.transformSamlObject(configBean, source, XMLObject.class);
        xmlObject.getObjectMetadata().put(new XMLObjectSource(source));
        EntityUtils.consume(entity);
        return new StaticXmlObjectMetadataResolver(xmlObject);
    }

    @Override
    public boolean isAvailable(final SamlRegisteredService service) {
        if (supports(service)) {
            val status = HttpRequestUtils.pingUrl(service.getMetadataLocation());
            return !status.isError();
        }
        return false;
    }
}
