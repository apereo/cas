package org.apereo.cas.support.saml.web.idp.metadata;

import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.saml.SamlIdPConstants;
import org.apereo.cas.support.saml.idp.metadata.generator.SamlIdPMetadataGenerator;
import org.apereo.cas.support.saml.idp.metadata.locator.SamlIdPMetadataLocator;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

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

    private final SamlIdPMetadataGenerator metadataAndCertificatesGenerationService;

    private final SamlIdPMetadataLocator samlIdPMetadataLocator;

    private final ServicesManager servicesManager;

    private final ServiceFactory<WebApplicationService> webApplicationServiceFactory;

    /**
     * Displays the identity provider metadata.
     * Checks to make sure metadata exists, and if not, generates it first.
     *
     * @param service  the service
     * @param response servlet response
     * @throws Exception the exception
     */
    @Operation(summary = "Generate IdP metadata",
        parameters = @Parameter(name = "service", in = ParameterIn.QUERY, required = false, description = "Service identifier"))
    @GetMapping(path = SamlIdPConstants.ENDPOINT_IDP_METADATA, produces = CONTENT_TYPE)
    public void generateMetadataForIdp(
        @RequestParam(value = "service", required = false) final String service,
        final HttpServletResponse response) throws Throwable {

        val registeredService = getRegisteredServiceIfAny(service);
        metadataAndCertificatesGenerationService.generate(registeredService);
        response.setContentType(CONTENT_TYPE);
        response.setStatus(HttpServletResponse.SC_OK);
        try (val md = samlIdPMetadataLocator.resolveMetadata(registeredService).getInputStream()) {
            val contents = IOUtils.toString(md, StandardCharsets.UTF_8);
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
        @RequestParam(value = "service", required = false) final String service) throws Throwable {
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
        @RequestParam(value = "service", required = false) final String service) throws Throwable {
        val registeredService = getRegisteredServiceIfAny(service);
        metadataAndCertificatesGenerationService.generate(registeredService);
        try (val md = samlIdPMetadataLocator.resolveEncryptionCertificate(registeredService).getInputStream()) {
            return IOUtils.toString(md, StandardCharsets.UTF_8);
        }
    }

    private Optional<SamlRegisteredService> getRegisteredServiceIfAny(final String service) {
        if (NumberUtils.isDigits(service)) {
            val svc = servicesManager.findServiceBy(Long.parseLong(service), SamlRegisteredService.class);
            return Optional.ofNullable(svc);
        }
        val svc = StringUtils.isNotBlank(service) ? webApplicationServiceFactory.createService(service) : null;
        val registeredService = servicesManager.findServiceBy(svc, SamlRegisteredService.class);
        return Optional.ofNullable(registeredService);
    }
}
