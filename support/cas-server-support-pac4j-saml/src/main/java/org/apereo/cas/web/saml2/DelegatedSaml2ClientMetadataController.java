package org.apereo.cas.web.saml2;

import org.apereo.cas.pac4j.client.DelegatedIdentityProviders;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.SamlUtils;
import org.apereo.cas.web.AbstractController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.pac4j.jee.context.JEEContext;
import org.pac4j.saml.client.SAML2Client;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * This is {@link DelegatedSaml2ClientMetadataController}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RequestMapping
@RequiredArgsConstructor
@Tag(name = "Delegated Authentication")
public class DelegatedSaml2ClientMetadataController extends AbstractController {

    /**
     * Base endpoint url.
     */
    public static final String BASE_ENDPOINT_SERVICE_PROVIDER = "/sp";

    private final DelegatedIdentityProviders identityProviders;

    private final OpenSamlConfigBean openSamlConfigBean;

    private static ResponseEntity<String> getSaml2ClientServiceProviderMetadataResponseEntity(final SAML2Client saml2Client) {
        val headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_XML);
        saml2Client.init();
        val md = saml2Client.getServiceProviderMetadataResolver().getMetadata();
        return new ResponseEntity<>(md, headers, HttpStatus.OK);
    }

    private static ResponseEntity<String> getNotAcceptableResponseEntity() {
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    /**
     * Gets first service provider metadata.
     *
     * @return the first service provider metadata
     */
    @GetMapping(value = BASE_ENDPOINT_SERVICE_PROVIDER + "/metadata",
        consumes = MediaType.ALL_VALUE,
        produces = MediaType.APPLICATION_XML_VALUE)
    @Operation(summary = "Get service provider metadata")
    public ResponseEntity<String> getFirstServiceProviderMetadata(final HttpServletRequest request,
                                                                  final HttpServletResponse response) {
        val webContext = new JEEContext(request, response);
        val saml2Client = identityProviders.findAllClients(webContext)
            .stream()
            .filter(SAML2Client.class::isInstance)
            .map(SAML2Client.class::cast).findFirst();
        return saml2Client.map(DelegatedSaml2ClientMetadataController::getSaml2ClientServiceProviderMetadataResponseEntity)
            .orElseGet(DelegatedSaml2ClientMetadataController::getNotAcceptableResponseEntity);
    }

    /**
     * Gets first idp metadata.
     *
     * @param request  the request
     * @param response the response
     * @return the first service provider metadata
     */
    @GetMapping(path = BASE_ENDPOINT_SERVICE_PROVIDER + "/idp/metadata", consumes = MediaType.ALL_VALUE,
        produces = MediaType.APPLICATION_XML_VALUE)
    @Operation(summary = "Get identity provider metadata")
    public ResponseEntity<String> getFirstIdentityProviderMetadata(final HttpServletRequest request,
                                                                   final HttpServletResponse response) {
        val webContext = new JEEContext(request, response);
        val saml2Client = identityProviders.findAllClients(webContext)
            .stream()
            .filter(SAML2Client.class::isInstance)
            .map(SAML2Client.class::cast).findFirst();
        return saml2Client.map(this::getSaml2ClientIdentityProviderMetadataResponseEntity)
            .orElseGet(DelegatedSaml2ClientMetadataController::getNotAcceptableResponseEntity);
    }

    /**
     * Gets service provider metadata by name.
     *
     * @param client the client
     * @return the service provider metadata by name
     */
    @GetMapping(path = BASE_ENDPOINT_SERVICE_PROVIDER + "/{client}/metadata", consumes = MediaType.ALL_VALUE,
        produces = MediaType.APPLICATION_XML_VALUE)
    @Operation(summary = "Get service provider metadata by name",
        parameters = @Parameter(name = "client", in = ParameterIn.PATH, required = true, description = "The client name"))
    public ResponseEntity<String> getServiceProviderMetadataByName(
        @PathVariable("client") final String client,
        final HttpServletRequest request, final HttpServletResponse response) {
        val webContext = new JEEContext(request, response);
        val saml2Client = identityProviders.findClient(client, webContext);
        return saml2Client.map(value -> getSaml2ClientServiceProviderMetadataResponseEntity((SAML2Client) value))
            .orElseGet(DelegatedSaml2ClientMetadataController::getNotAcceptableResponseEntity);
    }

    /**
     * Gets idp metadata by name.
     *
     * @param client the client
     * @return the service provider metadata by name
     */
    @GetMapping(path = BASE_ENDPOINT_SERVICE_PROVIDER + "/{client}/idp/metadata", consumes = MediaType.ALL_VALUE,
        produces = MediaType.APPLICATION_XML_VALUE)
    @Operation(summary = "Get identity provider metadata by name",
        parameters = @Parameter(name = "client", in = ParameterIn.PATH, required = true, description = "The client name"))
    public ResponseEntity<String> getIdentityProviderMetadataByName(
        @PathVariable("client")
        final String client,
        final HttpServletRequest request, final HttpServletResponse response) {
        val webContext = new JEEContext(request, response);
        val saml2Client = identityProviders.findClient(client, webContext);
        return saml2Client.map(value -> getSaml2ClientIdentityProviderMetadataResponseEntity((SAML2Client) value))
            .orElseGet(DelegatedSaml2ClientMetadataController::getNotAcceptableResponseEntity);
    }

    private ResponseEntity<String> getSaml2ClientIdentityProviderMetadataResponseEntity(final SAML2Client saml2Client) {
        val headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_XML);
        saml2Client.init();
        val identityProviderMetadataResolver = saml2Client.getIdentityProviderMetadataResolver();
        identityProviderMetadataResolver.resolve(true);
        val entity = identityProviderMetadataResolver.getEntityDescriptorElement();
        val metadata = SamlUtils.transformSamlObject(openSamlConfigBean, entity).toString();
        return new ResponseEntity<>(metadata, headers, HttpStatus.OK);
    }
}
