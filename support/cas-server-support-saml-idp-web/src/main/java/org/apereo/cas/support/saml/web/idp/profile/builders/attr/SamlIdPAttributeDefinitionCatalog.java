package org.apereo.cas.support.saml.web.idp.profile.builders.attr;

import lombok.experimental.UtilityClass;

import java.util.stream.Stream;

/**
 * This is {@link SamlIdPAttributeDefinitionCatalog}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@UtilityClass
public class SamlIdPAttributeDefinitionCatalog {

    /**
     * Load stream of known attribute definitions for saml2.
     *
     * @return the stream
     */
    public static Stream<SamlIdPAttributeDefinition> load() {
        return Stream.of(
            SamlIdPAttributeDefinition.builder().urn("urn:oid:0.9.2342.19200300.100.1.1")
                .friendlyName("uid").key("uid").build(),
            SamlIdPAttributeDefinition.builder().urn("urn:oid:2.5.4.12")
                .friendlyName("title").key("title").build(),
            SamlIdPAttributeDefinition.builder().urn("urn:oid:2.5.4.4")
                .friendlyName("sn").key("sn").build(),
            SamlIdPAttributeDefinition.builder().urn("urn:oid:2.5.4.4")
                .friendlyName("surname").key("surname").build(),
            SamlIdPAttributeDefinition.builder().urn("urn:oid:2.5.4.42")
                .friendlyName("givenName").key("givenName").build(),
            SamlIdPAttributeDefinition.builder().urn("urn:oid:2.5.4.20")
                .friendlyName("telephoneNumber").key("telephoneNumber").build(),
            SamlIdPAttributeDefinition.builder().urn("urn:oid:0.9.2342.19200300.100.1.3")
                .friendlyName("email").key("email").build(),
            SamlIdPAttributeDefinition.builder().urn("urn:oid:0.9.2342.19200300.100.1.3")
                .friendlyName("mail").key("mail").build(),
            SamlIdPAttributeDefinition.builder().urn("urn:oid:2.5.4.3")
                .friendlyName("commonName").key("commonName").build(),
            SamlIdPAttributeDefinition.builder().urn("urn:oid:2.16.840.1.113730.3.1.241")
                .friendlyName("displayName").key("displayName").build(),
            SamlIdPAttributeDefinition.builder().urn("urn:oid:2.5.4.10")
                .friendlyName("organizationName").key("organizationName").build(),
            SamlIdPAttributeDefinition.builder().urn("urn:oid:1.3.6.1.4.1.5923.1.1.1.5")
                .friendlyName("eduPersonPrimaryAffiliation").key("eduPersonPrimaryAffiliation").build(),
            SamlIdPAttributeDefinition.builder().urn("urn:oid:1.3.6.1.4.1.5923.1.1.1.1")
                .friendlyName("eduPersonAffiliation").key("eduPersonAffiliation").build(),
            SamlIdPAttributeDefinition.builder().urn("urn:oid:1.3.6.1.4.1.5923.1.1.1.10")
                .friendlyName("eduPersonTargetedID").key("eduPersonTargetedID").persistent(true).build(),
            SamlIdPAttributeDefinition.builder().urn("urn:oid:1.3.6.1.4.1.5923.1.1.1.7")
                .friendlyName("eduPersonEntitlement").key("eduPersonEntitlement").build(),
            SamlIdPAttributeDefinition.builder().urn("urn:oid:1.3.6.1.4.1.5923.1.1.1.6")
                .friendlyName("eduPersonPrincipalName").key("eduPersonPrincipalName").build(),
            SamlIdPAttributeDefinition.builder().urn("urn:oid:1.3.6.1.4.1.5923.1.1.1.9")
                .friendlyName("eduPersonScopedAffiliation").key("eduPersonScopedAffiliation").build(),
            SamlIdPAttributeDefinition.builder().urn("urn:oid:1.3.6.1.4.1.5923.1.1.1.13")
                .friendlyName("eduPersonUniqueId").key("eduPersonUniqueId").build(),
            SamlIdPAttributeDefinition.builder().urn("urn:oid:1.3.6.1.4.1.5923.1.1.1.11")
                .friendlyName("eduPersonAssurance").key("eduPersonAssurance").build(),
            SamlIdPAttributeDefinition.builder().urn("urn:oid:1.3.6.1.4.1.5923.1.1.1.2")
                .friendlyName("eduPersonNickname").key("eduPersonNickname").build(),
            SamlIdPAttributeDefinition.builder().urn("urn:oid:1.3.6.1.4.1.5923.1.1.1.14")
                .friendlyName("eduPersonOrcid").key("eduPersonOrcid").build()
        );
    }
}
