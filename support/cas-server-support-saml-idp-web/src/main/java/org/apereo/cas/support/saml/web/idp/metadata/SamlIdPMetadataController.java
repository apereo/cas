package org.apereo.cas.support.saml.web.idp.metadata;

import module java.base;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.saml.SamlIdPConstants;
import org.apereo.cas.support.saml.idp.metadata.generator.SamlIdPMetadataGenerator;
import org.apereo.cas.support.saml.idp.metadata.locator.SamlIdPMetadataLocator;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.util.DigestUtils;
import org.apereo.cas.util.function.FunctionUtils;
import com.google.common.primitives.Longs;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * The {@link SamlIdPMetadataController} will attempt
 * to produce saml metadata for CAS as an identity provider.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RestController("samlIdPMetadataController")
@Slf4j
@Tag(name = "SAML2")
@RequiredArgsConstructor
public class SamlIdPMetadataController {
    private static final String CONTENT_TYPE = "text/xml;charset=UTF-8";

    private static final String SAML_METADATA_CONTENT_TYPE = "application/samlmetadata+xml";

    private final SamlIdPMetadataGenerator metadataAndCertificatesGenerationService;

    private final SamlIdPMetadataLocator samlIdPMetadataLocator;

    private final ServicesManager servicesManager;

    private final ServiceFactory<WebApplicationService> webApplicationServiceFactory;

    /**
     * Displays the identity provider metadata.
     * Checks to make sure metadata exists, and if not, generates it first.
     * <p>
     * The document is served as {@code application/samlmetadata+xml}, the media type the SAML metadata
     * specification registers, when the request asks for that type, and as {@code text/xml} otherwise,
     * so that browsers keep showing it and existing consumers see no change. Consumers poll this
     * endpoint: the answer carries an {@code ETag} derived from the document and must be revalidated,
     * and a request whose {@code If-None-Match} names the current document is answered with
     * {@code 304 Not Modified} and no body.
     *
     * @param service  the service
     * @param request  servlet request
     * @param response servlet response
     * @throws Throwable the throwable
     */
    @Operation(summary = "Generate IdP metadata",
        parameters = @Parameter(name = "service", in = ParameterIn.QUERY, required = false, description = "Service identifier"))
    @GetMapping(path = SamlIdPConstants.ENDPOINT_IDP_METADATA, produces = {CONTENT_TYPE, SAML_METADATA_CONTENT_TYPE})
    public void generateMetadataForIdp(
        @RequestParam(value = "service", required = false) final String service,
        final HttpServletRequest request,
        final HttpServletResponse response)
            throws Throwable {

        val registeredService = getRegisteredServiceIfAny(service);
        metadataAndCertificatesGenerationService.generate(registeredService);
        val metadata = samlIdPMetadataLocator.resolveMetadata(registeredService);
        Objects.requireNonNull(metadata, "Unable to resolve and locate identity provider metadata source");
        try (val md = metadata.getInputStream()) {
            val contents = IOUtils.toString(md, StandardCharsets.UTF_8);
            val etag = '"' + DigestUtils.sha256(contents) + '"';
            response.setHeader(HttpHeaders.ETAG, etag);
            response.setHeader(HttpHeaders.CACHE_CONTROL, CacheControl.noCache().getHeaderValue());
            if (isNotModified(request, etag)) {
                response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
                return;
            }
            response.setContentType(isSamlMetadataContentTypeRequested(request) ? SAML_METADATA_CONTENT_TYPE + ";charset=UTF-8" : CONTENT_TYPE);
            response.setStatus(HttpServletResponse.SC_OK);
            try (val writer = response.getWriter()) {
                LOGGER.debug("Producing metadata for the response");
                writer.write(contents);
                writer.flush();
            }
        }
    }

    /**
     * Idp signing certificate.
     *
     * @param service  the service
     * @throws Throwable the throwable
     */
    @GetMapping(path = SamlIdPConstants.ENDPOINT_IDP_METADATA + "/signingCertificate", produces = MediaType.TEXT_PLAIN_VALUE)
    @Operation(summary = "Get IdP signing certificate",
        parameters = @Parameter(name = "service", in = ParameterIn.QUERY, required = false, description = "Service identifier"))
    public String idpSigningCertificate(
        @RequestParam(value = "service", required = false) final String service)
            throws Throwable {
        val registeredService = getRegisteredServiceIfAny(service);
        metadataAndCertificatesGenerationService.generate(registeredService);
        try (val md = samlIdPMetadataLocator.resolveSigningCertificate(registeredService).getInputStream()) {
            return IOUtils.toString(md, StandardCharsets.UTF_8);
        }
    }

    /**
     * Idp encryption certificate.
     *
     * @param service  the service
     * @throws Throwable the throwable
     */
    @Operation(summary = "Get IdP encryption certificate",
        parameters = @Parameter(name = "service", in = ParameterIn.QUERY, required = false, description = "Service identifier"))
    @GetMapping(path = SamlIdPConstants.ENDPOINT_IDP_METADATA + "/encryptionCertificate", produces = MediaType.TEXT_PLAIN_VALUE)
    public String idpEncryptionCertificate(
        @RequestParam(value = "service", required = false) final String service)
            throws Throwable {
        val registeredService = getRegisteredServiceIfAny(service);
        metadataAndCertificatesGenerationService.generate(registeredService);
        try (val md = samlIdPMetadataLocator.resolveEncryptionCertificate(registeredService).getInputStream()) {
            return IOUtils.toString(md, StandardCharsets.UTF_8);
        }
    }

    private Optional<SamlRegisteredService> getRegisteredServiceIfAny(final String service) {
        val registeredServiceId = service != null ? Longs.tryParse(service) : null;
        if (registeredServiceId != null) {
            val svc = servicesManager.findServiceBy(registeredServiceId, SamlRegisteredService.class);
            return Optional.ofNullable(svc);
        }
        val svc = StringUtils.isNotBlank(service) ? webApplicationServiceFactory.createService(service) : null;
        val registeredService = servicesManager.findServiceBy(svc, SamlRegisteredService.class);
        return Optional.ofNullable(registeredService);
    }

    private static boolean isNotModified(final HttpServletRequest request, final String etag) {
        val ifNoneMatch = request.getHeader(HttpHeaders.IF_NONE_MATCH);
        return StringUtils.isNotBlank(ifNoneMatch) && Arrays.stream(ifNoneMatch.split(","))
            .map(String::trim)
            .map(value -> Strings.CS.removeStart(value, "W/"))
            .anyMatch(value -> "*".equals(value) || etag.equals(value));
    }

    private static boolean isSamlMetadataContentTypeRequested(final HttpServletRequest request) {
        val accept = request.getHeader(HttpHeaders.ACCEPT);
        if (StringUtils.isBlank(accept)) {
            return false;
        }
        val samlMetadata = MediaType.parseMediaType(SAML_METADATA_CONTENT_TYPE);
        return FunctionUtils.doAndHandle(() -> MediaType.parseMediaTypes(accept)
            .stream()
            .anyMatch(samlMetadata::equalsTypeAndSubtype), e -> Boolean.FALSE).get();
    }
}
