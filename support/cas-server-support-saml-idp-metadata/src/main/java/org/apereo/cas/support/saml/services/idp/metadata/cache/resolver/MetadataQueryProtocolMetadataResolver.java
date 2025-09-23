package org.apereo.cas.support.saml.services.idp.metadata.cache.resolver;

import org.apereo.cas.configuration.model.support.saml.idp.SamlIdPProperties;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.support.saml.InMemoryResourceMetadataResolver;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.SamlException;
import org.apereo.cas.support.saml.SamlUtils;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.http.HttpClient;
import org.apereo.cas.util.http.HttpExecutionRequest;
import org.apereo.cas.util.http.HttpUtils;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.shibboleth.shared.resolver.CriteriaSet;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.core5.http.HttpEntityContainer;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.opensaml.core.criterion.EntityIdCriterion;
import org.opensaml.saml.metadata.resolver.impl.AbstractMetadataResolver;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.attribute.UserDefinedFileAttributeView;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * This is {@link MetadataQueryProtocolMetadataResolver}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
public class MetadataQueryProtocolMetadataResolver extends UrlResourceMetadataResolver {

    public MetadataQueryProtocolMetadataResolver(final HttpClient httpClient,
                                                 final SamlIdPProperties samlIdPProperties,
                                                 final OpenSamlConfigBean configBean) {
        super(httpClient, samlIdPProperties, configBean);
    }

    @Override
    public boolean supports(final SamlRegisteredService service) {
        val locations = org.springframework.util.StringUtils.commaDelimitedListToSet(
            SpringExpressionLanguageValueResolver.getInstance().resolve(service.getMetadataLocation())
        );
        return locations.stream().anyMatch(SamlUtils::isDynamicMetadataQueryConfigured);
    }

    @Override
    protected boolean shouldHttpResponseStatusBeProcessed(final HttpStatus status) {
        return true;
    }

    @Override
    protected AbstractMetadataResolver getMetadataResolverFromResponse(final HttpResponse response, final File backupFile) throws Exception {
        if (!HttpStatus.valueOf(response.getCode()).is2xxSuccessful()) {
            if (Files.exists(backupFile.toPath())) {
                return new InMemoryResourceMetadataResolver(backupFile, this.configBean);
            }
            throw new SamlException("Unable to get entity from MDQ server and a backup file does not exist.");
        }
        val entity = ((HttpEntityContainer) response).getEntity();
        val result = IOUtils.toString(entity.getContent(), StandardCharsets.UTF_8);
        val path = backupFile.toPath();
        LOGGER.trace("Writing metadata to file at [{}]", path);
        try (val output = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            IOUtils.write(result, output);
            output.flush();
            StreamSupport.stream(path.getFileSystem().getFileStores().spliterator(), false)
                .filter(store -> store.supportsFileAttributeView(UserDefinedFileAttributeView.class))
                .forEach(store -> setFileAttribute(response, backupFile));
        }
        EntityUtils.consume(entity);
        return new InMemoryResourceMetadataResolver(backupFile, configBean);
    }

    @Override
    protected HttpResponse fetchMetadata(final SamlRegisteredService service,
                                         final String metadataLocation, final CriteriaSet criteriaSet, final File backupFile) {
        val metadata = samlIdPProperties.getMetadata().getMdq();
        val headers = new LinkedHashMap<String, String>();
        headers.put(HttpHeaders.CONTENT_TYPE, metadata.getSupportedContentType());
        headers.put(HttpHeaders.ACCEPT, "*/*");
        val path = backupFile.toPath();
        FunctionUtils.doAndHandle(p -> {
            if (Files.exists(path)) {
                val etag = new String((byte[]) Files.getAttribute(path, "user:ETag"), StandardCharsets.UTF_8).trim();
                headers.put("If-None-Match", etag);
            }
        });
        LOGGER.trace("Fetching metadata via MDQ for [{}]", metadataLocation);
        val exec = HttpExecutionRequest.builder()
            .basicAuthPassword(metadata.getBasicAuthnPassword())
            .basicAuthUsername(metadata.getBasicAuthnUsername())
            .method(HttpMethod.GET)
            .url(metadataLocation)
            .headers(headers)
            .proxyUrl(service.getMetadataProxyLocation())
            .build();
        val response = HttpUtils.execute(exec);
        if (response == null) {
            LOGGER.error("Unable to fetch metadata from [{}]", metadataLocation);
            throw UnauthorizedServiceException.denied("Rejected: %s".formatted(metadataLocation));
        }
        return response;
    }

    @Override
    protected Set<String> getMetadataLocationsForService(final SamlRegisteredService service, final CriteriaSet criteriaSet) {
        LOGGER.trace("Getting metadata location dynamically for [{}] based on criteria [{}]", service.getName(), criteriaSet);
        val entityIdCriteria = criteriaSet.get(EntityIdCriterion.class);
        val entityId = Optional.ofNullable(entityIdCriteria)
            .map(EntityIdCriterion::getEntityId)
            .orElseGet(service::getServiceId);
        if (StringUtils.isBlank(entityId)) {
            throw new SamlException("Unable to determine entity id to fetch metadata via MDQ for " + service.getName());
        }
        val locations = super.getMetadataLocationsForService(service, criteriaSet);
        return locations
            .stream()
            .map(location -> location.replace("{0}", EncodingUtils.urlEncode(entityId)))
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private static void setFileAttribute(final HttpResponse response, final File backupFile) {
        FunctionUtils.doAndHandle(t -> {
            val path = backupFile.toPath();
            val etag = response.getFirstHeader("ETag").getValue();
            Files.setAttribute(path, "user:ETag", ByteBuffer.wrap(etag.getBytes(StandardCharsets.UTF_8)));
        });
    }
}
