package org.apereo.cas.support.saml.services.idp.metadata.cache.resolver;

import org.apereo.cas.configuration.model.support.saml.idp.SamlIdPProperties;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.support.saml.InMemoryResourceMetadataResolver;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.SamlException;
import org.apereo.cas.support.saml.SamlUtils;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.HttpUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.jooq.lambda.Unchecked;
import org.opensaml.core.criterion.EntityIdCriterion;
import org.opensaml.saml.metadata.resolver.impl.AbstractMetadataResolver;
import org.springframework.http.HttpStatus;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.attribute.UserDefinedFileAttributeView;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Optional;
import java.util.stream.StreamSupport;

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
    protected boolean shouldHttpResponseStatusBeProcessed(final HttpStatus status) {
        return true;
    }

    @Override
    protected AbstractMetadataResolver getMetadataResolverFromResponse(final HttpResponse response, final File backupFile) throws Exception {
        if (!HttpStatus.valueOf(response.getStatusLine().getStatusCode()).is2xxSuccessful()) {
            if (Files.exists(backupFile.toPath())) {
                return new InMemoryResourceMetadataResolver(backupFile, this.configBean);
            }
            throw new Exception("Unable to get entity from MDQ server and a backup file does not exist.");
        }
        val entity = response.getEntity();
        val result = IOUtils.toString(entity.getContent(), StandardCharsets.UTF_8);
        val path = backupFile.toPath();
        LOGGER.trace("Writing metadata to file at [{}]", path);
        try (val output = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            IOUtils.write(result, output);
            output.flush();

            StreamSupport.stream(path.getFileSystem().getFileStores().spliterator(), false)
                .filter(store -> store.supportsFileAttributeView(UserDefinedFileAttributeView.class))
                .forEach(Unchecked.consumer(store -> {
                    val etag = response.getFirstHeader("ETag").getValue();
                    Files.setAttribute(path, "user:ETag",
                        ByteBuffer.wrap(etag.getBytes(StandardCharsets.UTF_8)));
                }));
        }
        EntityUtils.consume(entity);
        return new InMemoryResourceMetadataResolver(backupFile, configBean);
    }

    @Override
    protected HttpResponse fetchMetadata(final String metadataLocation, final CriteriaSet criteriaSet, final File backupFile) {
        val metadata = samlIdPProperties.getMetadata();
        val headers = new LinkedHashMap<String, Object>();
        headers.put("Content-Type", metadata.getSupportedContentTypes());
        headers.put("Accept", "*/*");
        val path = backupFile.toPath();
        if (Files.exists(path)) {
            try {
                val etag = new String((byte[]) Files.getAttribute(path, "user:ETag"), StandardCharsets.UTF_8).trim();
                headers.put("If-None-Match", etag);
            } catch (final Exception e) {
                LOGGER.error("Failed to read ETag Attribute - " + e.getMessage(), e);
            }
        }

        LOGGER.trace("Fetching dynamic metadata via MDQ for [{}]", metadataLocation);
        val response = HttpUtils.executeGet(metadataLocation, metadata.getBasicAuthnUsername(),
            samlIdPProperties.getMetadata().getBasicAuthnPassword(), new HashMap<>(0), headers);
        if (response == null) {
            LOGGER.error("Unable to fetch metadata from [{}]", metadataLocation);
            throw new UnauthorizedServiceException(UnauthorizedServiceException.CODE_UNAUTHZ_SERVICE);
        }
        return response;
    }

    @Override
    protected String getMetadataLocationForService(final SamlRegisteredService service, final CriteriaSet criteriaSet) {
        LOGGER.trace("Getting metadata location dynamically for [{}] based on criteria [{}]", service.getName(), criteriaSet);
        val entityIdCriteria = criteriaSet.get(EntityIdCriterion.class);
        val entityId = Optional.ofNullable(entityIdCriteria).map(EntityIdCriterion::getEntityId).orElseGet(service::getServiceId);
        if (StringUtils.isBlank(entityId)) {
            throw new SamlException("Unable to determine entity id to fetch metadata dynamically via MDQ for service " + service.getName());
        }
        return service.getMetadataLocation().replace("{0}", EncodingUtils.urlEncode(entityId));
    }

    @Override
    public boolean supports(final SamlRegisteredService service) {
        return SamlUtils.isDynamicMetadataQueryConfigured(service.getMetadataLocation());
    }
}
