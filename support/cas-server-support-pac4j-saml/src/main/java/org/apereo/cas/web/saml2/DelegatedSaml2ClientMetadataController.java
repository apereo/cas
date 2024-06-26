package org.apereo.cas.web.saml2;

import org.apereo.cas.pac4j.client.DelegatedIdentityProviders;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.SamlUtils;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.pac4j.saml.client.SAML2Client;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * This is {@link DelegatedSaml2ClientMetadataController}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Controller("delegatedSaml2ClientMetadataController")
@RequestMapping
@RequiredArgsConstructor
public class DelegatedSaml2ClientMetadataController {

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
        return new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
    }

    /**
     * Gets first service provider metadata.
     *
     * @return the first service provider metadata
     */
    @GetMapping(BASE_ENDPOINT_SERVICE_PROVIDER + "/metadata")
    public ResponseEntity<String> getFirstServiceProviderMetadata() {
        val saml2Client = identityProviders.findAllClients().stream()
            .filter(SAML2Client.class::isInstance)
            .map(SAML2Client.class::cast).findFirst();
        return saml2Client.map(DelegatedSaml2ClientMetadataController::getSaml2ClientServiceProviderMetadataResponseEntity)
            .orElseGet(DelegatedSaml2ClientMetadataController::getNotAcceptableResponseEntity);
    }

    /**
     * Gets first idp metadata.
     *
     * @return the first service provider metadata
     */
    @GetMapping(BASE_ENDPOINT_SERVICE_PROVIDER + "/idp/metadata")
    public ResponseEntity<String> getFirstIdentityProviderMetadata() {
        val saml2Client = identityProviders.findAllClients().stream()
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
    @GetMapping(BASE_ENDPOINT_SERVICE_PROVIDER + "/{client}/metadata")
    public ResponseEntity<String> getServiceProviderMetadataByName(
        @PathVariable("client")
        final String client) {
        val saml2Client = identityProviders.findClient(client);
        return saml2Client.map(value -> getSaml2ClientServiceProviderMetadataResponseEntity((SAML2Client) value))
            .orElseGet(DelegatedSaml2ClientMetadataController::getNotAcceptableResponseEntity);
    }

    /**
     * Gets idp metadata by name.
     *
     * @param client the client
     * @return the service provider metadata by name
     */
    @GetMapping(BASE_ENDPOINT_SERVICE_PROVIDER + "/{client}/idp/metadata")
    public ResponseEntity<String> getIdentityProviderMetadataByName(
        @PathVariable("client")
        final String client) {
        val saml2Client = identityProviders.findClient(client);
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
