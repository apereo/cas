package org.apereo.cas.support.saml.web.idp.profile.builders.attr;

import java.util.stream.Stream;

/**
 * This is {@link SamlIdPAttributeDefinitionCatalog}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
class SamlIdPAttributeDefinitionCatalog {

    public static Stream<SamlIdPAttributeDefinition> load() {
        return Stream.of(
            SamlIdPAttributeDefinition.builder().urn("urn:oid:0.9.2342.19200300.100.1.1").friendlyName("uid").key("urn:oid:0.9.2342.19200300.100.1.1").build(),
            SamlIdPAttributeDefinition.builder().urn("urn:oid:2.5.4.4").friendlyName("sn").key("urn:oid:2.5.4.4").build(),
            SamlIdPAttributeDefinition.builder().urn("urn:oid:2.5.4.4").friendlyName("surname").key("urn:oid:2.5.4.4").build(),
            SamlIdPAttributeDefinition.builder().urn("urn:oid:2.5.4.42").friendlyName("givenName").key("urn:oid:2.5.4.42").build(),
            SamlIdPAttributeDefinition.builder().urn("urn:oid:2.5.4.20").friendlyName("telephoneNumber").key("urn:oid:2.5.4.20").build(),
            SamlIdPAttributeDefinition.builder().urn("urn:oid:0.9.2342.19200300.100.1.3").friendlyName("email").key("urn:oid:0.9.2342.19200300.100.1.3").build(),
            SamlIdPAttributeDefinition.builder().urn("urn:oid:0.9.2342.19200300.100.1.3").friendlyName("mail").key("urn:oid:0.9.2342.19200300.100.1.3").build(),
            SamlIdPAttributeDefinition.builder().urn("urn:oid:2.5.4.3").friendlyName("commonName").key("urn:oid:2.5.4.3").build(),
            SamlIdPAttributeDefinition.builder().urn("urn:oid:2.16.840.1.113730.3.1.241").friendlyName("displayName").key("urn:oid:2.16.840.1.113730.3.1.241").build(),
            SamlIdPAttributeDefinition.builder().urn("urn:oid:2.5.4.10").friendlyName("organizationName").key("urn:oid:2.5.4.10").build(),
            SamlIdPAttributeDefinition.builder().urn("urn:oid:1.3.6.1.4.1.5923.1.1.1.5").friendlyName("eduPersonPrimaryAffiliation").key("urn:oid:1.3.6.1.4.1.5923.1.1.1.5").build(),
            SamlIdPAttributeDefinition.builder().urn("urn:oid:1.3.6.1.4.1.5923.1.1.1.1").friendlyName("eduPersonAffiliation").key("urn:oid:1.3.6.1.4.1.5923.1.1.1.1").build(),
            SamlIdPAttributeDefinition.builder().urn("urn:oid:1.3.6.1.4.1.5923.1.1.1.10").friendlyName("eduPersonTargetedID").key("urn:oid:1.3.6.1.4.1.5923.1.1.1.10").build(),
            SamlIdPAttributeDefinition.builder().urn("urn:oid:1.3.6.1.4.1.5923.1.1.1.7").friendlyName("eduPersonEntitlement").key("urn:oid:1.3.6.1.4.1.5923.1.1.1.7").build(),
            SamlIdPAttributeDefinition.builder().urn("urn:oid:1.3.6.1.4.1.5923.1.1.1.6").friendlyName("eduPersonPrincipalName").key("urn:oid:1.3.6.1.4.1.5923.1.1.1.6").build(),
            SamlIdPAttributeDefinition.builder().urn("urn:oid:1.3.6.1.4.1.5923.1.1.1.9").friendlyName("eduPersonScopedAffiliation").key("urn:oid:1.3.6.1.4.1.5923.1.1.1.9").build()
        );
    }
}
