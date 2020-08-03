package org.apereo.cas.web.saml2;

import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.SamlUtils;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.pac4j.core.client.Clients;
import org.pac4j.saml.client.SAML2Client;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.nio.charset.StandardCharsets;

/**
 * This is {@link Saml2ClientMetadataController}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Controller("saml2ClientMetadataController")
@RequestMapping
@RequiredArgsConstructor
public class Saml2ClientMetadataController {

    private final Clients builtClients;

    private final OpenSamlConfigBean openSamlConfigBean;

    @SneakyThrows
    private static ResponseEntity<String> getSaml2ClientServiceProviderMetadataResponseEntity(final SAML2Client saml2Client) {
        val headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_XML);
        saml2Client.init();
        val md = FileUtils.readFileToString(saml2Client.getConfiguration().getServiceProviderMetadataResource().getFile(), StandardCharsets.UTF_8);
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
    @GetMapping("/sp/metadata")
    public ResponseEntity<String> getFirstServiceProviderMetadata() {
        val saml2Client = builtClients.findClient(SAML2Client.class);
        return saml2Client.map(Saml2ClientMetadataController::getSaml2ClientServiceProviderMetadataResponseEntity)
            .orElseGet(Saml2ClientMetadataController::getNotAcceptableResponseEntity);
    }

    /**
     * Gets first idp metadata.
     *
     * @return the first service provider metadata
     */
    @GetMapping("/sp/idp/metadata")
    public ResponseEntity<String> getFirstIdentityProviderMetadata() {
        val saml2Client = builtClients.findClient(SAML2Client.class);
        return saml2Client.map(this::getSaml2ClientIdentityProviderMetadataResponseEntity)
            .orElseGet(Saml2ClientMetadataController::getNotAcceptableResponseEntity);
    }

    /**
     * Gets service provider metadata by name.
     *
     * @param client the client
     * @return the service provider metadata by name
     */
    @GetMapping("/sp/{client}/metadata")
    public ResponseEntity<String> getServiceProviderMetadataByName(@PathVariable("client") final String client) {
        val saml2Client = builtClients.findClient(client);
        return saml2Client.map(value -> getSaml2ClientServiceProviderMetadataResponseEntity(SAML2Client.class.cast(value)))
            .orElseGet(Saml2ClientMetadataController::getNotAcceptableResponseEntity);
    }

    /**
     * Gets idp metadata by name.
     *
     * @param client the client
     * @return the service provider metadata by name
     */
    @GetMapping("/sp/{client}/idp/metadata")
    public ResponseEntity<String> getIdentityProviderMetadataByName(@PathVariable("client") final String client) {
        val saml2Client = builtClients.findClient(client);
        return saml2Client.map(value -> getSaml2ClientIdentityProviderMetadataResponseEntity(SAML2Client.class.cast(value)))
            .orElseGet(Saml2ClientMetadataController::getNotAcceptableResponseEntity);
    }

    private ResponseEntity<String> getSaml2ClientIdentityProviderMetadataResponseEntity(final SAML2Client saml2Client) {
        val headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_XML);
        saml2Client.init();
        val identityProviderMetadataResolver = saml2Client.getIdentityProviderMetadataResolver();
        if (identityProviderMetadataResolver == null) {
            return getNotAcceptableResponseEntity();
        }
        identityProviderMetadataResolver.resolve();
        val entity = identityProviderMetadataResolver.getEntityDescriptorElement();
        val metadata = SamlUtils.transformSamlObject(openSamlConfigBean, entity).toString();
        return new ResponseEntity<>(metadata, headers, HttpStatus.OK);
    }
}
