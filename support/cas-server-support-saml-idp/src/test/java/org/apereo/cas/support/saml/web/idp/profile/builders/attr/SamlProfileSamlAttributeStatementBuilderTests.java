package org.apereo.cas.support.saml.web.idp.profile.builders.attr;

import org.apereo.cas.support.saml.BaseSamlIdPConfigurationTests;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;
import org.apereo.cas.support.saml.web.idp.profile.builders.SamlProfileObjectBuilder;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.core.AttributeStatement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SamlProfileSamlAttributeStatementBuilderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("SAML")
@TestPropertySource(properties = "cas.authn.attribute-repository.attribute-definition-store.json.location=classpath:/basic-definitions.json")
public class SamlProfileSamlAttributeStatementBuilderTests extends BaseSamlIdPConfigurationTests {

    @Autowired
    @Qualifier("samlProfileSamlAttributeStatementBuilder")
    private SamlProfileObjectBuilder<AttributeStatement> samlProfileSamlAttributeStatementBuilder;

    @Test
    public void verifyTestAttributeDefns() {
        val service = getSamlRegisteredServiceForTestShib();

        val adaptor = SamlRegisteredServiceServiceProviderMetadataFacade.get(samlRegisteredServiceCachingMetadataResolver, service, service.getServiceId()).get();
        val statement = samlProfileSamlAttributeStatementBuilder.build(getAuthnRequestFor(service), new MockHttpServletRequest(),
            new MockHttpServletResponse(), getAssertion(), service, adaptor, SAMLConstants.SAML2_POST_BINDING_URI,
            new MessageContext());

        assertFalse(statement.getAttributes().isEmpty());
    }

}
