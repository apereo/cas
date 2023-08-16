package org.apereo.cas.support.saml.web.idp.profile.builders.authn;

import org.apereo.cas.support.saml.BaseSamlIdPConfigurationTests;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceMetadataAdaptor;
import org.apereo.cas.support.saml.web.idp.profile.builders.SamlProfileBuilderContext;
import org.apereo.cas.support.saml.web.idp.profile.builders.SamlProfileObjectBuilder;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.core.AuthnStatement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SamlProfileSamlAuthNStatementBuilderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("SAMLResponse")
class SamlProfileSamlAuthNStatementBuilderTests extends BaseSamlIdPConfigurationTests {
    @Autowired
    @Qualifier("samlProfileSamlAuthNStatementBuilder")
    private SamlProfileObjectBuilder<AuthnStatement> samlProfileSamlAuthNStatementBuilder;

    @Test
    void verifyOperation() throws Throwable {
        val request = getAuthnRequestFor(UUID.randomUUID().toString());

        val service = getSamlRegisteredServiceForTestShib(true, true);
        val adaptor = SamlRegisteredServiceMetadataAdaptor.get(
            samlRegisteredServiceCachingMetadataResolver,
            service, service.getServiceId()).get();

        val buildContext = SamlProfileBuilderContext.builder()
            .samlRequest(request)
            .httpRequest(new MockHttpServletRequest())
            .httpResponse(new MockHttpServletResponse())
            .authenticatedAssertion(Optional.of(getAssertion()))
            .registeredService(service)
            .adaptor(adaptor)
            .binding(SAMLConstants.SAML2_POST_BINDING_URI)
            .build();

        val result = samlProfileSamlAuthNStatementBuilder.build(buildContext);
        assertNotNull(result);
    }
}
