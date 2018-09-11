package org.apereo.cas.web.saml2;

import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.SamlUtils;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.opensaml.core.xml.XMLObject;
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
@Slf4j
@AllArgsConstructor
public class Saml2ClientMetadataController {

    private final Clients builtClients;
    private final OpenSamlConfigBean openSamlConfigBean;

    /**
     * Gets first service provider metadata.
     *
     * @return the first service provider metadata
     */
    @GetMapping("/sp/metadata")
    public ResponseEntity<String> getFirstServiceProviderMetadata() {
        final SAML2Client saml2Client = builtClients.findClient(SAML2Client.class);
        if (saml2Client != null) {
            return getSaml2ClientServiceProviderMetadataResponseEntity(saml2Client);
        }
        return getNotAcceptableResponseEntity();
    }

    /**
     * Gets first idp metadata.
     *
     * @return the first service provider metadata
     */
    @GetMapping("/sp/idp/metadata")
    public ResponseEntity<String> getFirstIdentityProviderMetadata() {
        final SAML2Client saml2Client = builtClients.findClient(SAML2Client.class);
        if (saml2Client != null) {
            return getSaml2ClientIdentityProviderMetadataResponseEntity(saml2Client);
        }
        return getNotAcceptableResponseEntity();
    }

    /**
     * Gets service provider metadata by name.
     *
     * @param client the client
     * @return the service provider metadata by name
     */
    @GetMapping("/sp/{client}/metadata")
    public ResponseEntity<String> getServiceProviderMetadataByName(@PathVariable("client") final String client) {
        final SAML2Client saml2Client = (SAML2Client) builtClients.findClient(client);
        if (saml2Client != null) {
            return getSaml2ClientServiceProviderMetadataResponseEntity(saml2Client);
        }
        return getNotAcceptableResponseEntity();
    }

    /**
     * Gets idp metadata by name.
     *
     * @param client the client
     * @return the service provider metadata by name
     */
    @GetMapping("/sp/{client}/idp/metadata")
    public ResponseEntity<String> getIdentityProviderMetadataByName(@PathVariable("client") final String client) {
        final SAML2Client saml2Client = (SAML2Client) builtClients.findClient(client);
        if (saml2Client != null) {
            saml2Client.init();
            return getSaml2ClientIdentityProviderMetadataResponseEntity(saml2Client);
        }
        return getNotAcceptableResponseEntity();
    }

    @SneakyThrows
    private static ResponseEntity<String> getSaml2ClientServiceProviderMetadataResponseEntity(final SAML2Client saml2Client) {
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_XML);
        saml2Client.init();
        final String md = FileUtils.readFileToString(saml2Client.getConfiguration().getServiceProviderMetadataResource().getFile(), StandardCharsets.UTF_8);
        return new ResponseEntity<>(md, headers, HttpStatus.OK);
    }

    private ResponseEntity<String> getSaml2ClientIdentityProviderMetadataResponseEntity(final SAML2Client saml2Client) {
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_XML);
        saml2Client.getIdentityProviderMetadataResolver().resolve();
        final XMLObject entity = saml2Client.getIdentityProviderMetadataResolver().getEntityDescriptorElement();
        final String metadata = SamlUtils.transformSamlObject(openSamlConfigBean, entity).toString();
        return new ResponseEntity<>(metadata, headers, HttpStatus.OK);
    }

    private static ResponseEntity<String> getNotAcceptableResponseEntity() {
        return new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
    }
}
