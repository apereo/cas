package org.apereo.cas.support.saml.services.logout;

import org.apereo.cas.logout.DefaultSingleLogoutRequestContext;
import org.apereo.cas.logout.slo.SingleLogoutExecutionRequest;
import org.apereo.cas.logout.slo.SingleLogoutMessageCreator;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.PrincipalAttributeRegisteredServiceUsernameProvider;
import org.apereo.cas.services.RegisteredServiceLogoutType;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.support.saml.BaseSamlIdPConfigurationTests;
import org.apereo.cas.support.saml.SamlIdPTestUtils;
import org.apereo.cas.support.saml.web.idp.profile.slo.SamlIdPSingleLogoutServiceLogoutUrlBuilder;
import org.apereo.cas.util.CollectionUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.TestPropertySource;
import java.net.URI;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SamlIdPProfileSingleLogoutMessageCreatorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Tag("SAMLLogout")
@TestPropertySource(properties = {
    "cas.authn.saml-idp.algs.override-signature-canonicalization-algorithm=http://www.w3.org/2001/10/xml-exc-c14n#",
    "cas.authn.saml-idp.metadata.file-system.location=${#systemProperties['java.io.tmpdir']}/idp-metadata414"
})
class SamlIdPProfileSingleLogoutMessageCreatorTests extends BaseSamlIdPConfigurationTests {

    @Autowired
    @Qualifier("samlLogoutBuilder")
    private SingleLogoutMessageCreator samlLogoutBuilder;
    
    @Test
    void verifyOperation() throws Throwable {
        val samlRegisteredService = SamlIdPTestUtils.getSamlRegisteredService();
        samlRegisteredService.setWhiteListBlackListPrecedence("INCLUDE");
        samlRegisteredService.setSigningKeyAlgorithm("RSA");
        samlRegisteredService.setSigningSignatureCanonicalizationAlgorithm("http://www.w3.org/2001/10/xml-exc-c14n#");
        val logoutRequest = DefaultSingleLogoutRequestContext.builder()
            .logoutUrl(new URI("https://sp.example.org/slo").toURL())
            .registeredService(samlRegisteredService)
            .service(RegisteredServiceTestUtils.getService("https://sp.testshib.org/shibboleth-sp"))
            .ticketId("ST-123456789")
            .executionRequest(SingleLogoutExecutionRequest.builder()
                .ticketGrantingTicket(new MockTicketGrantingTicket("casuser"))
                .build())
            .logoutType(RegisteredServiceLogoutType.BACK_CHANNEL)
            .properties(CollectionUtils.wrap(SamlIdPSingleLogoutServiceLogoutUrlBuilder.PROPERTY_NAME_SINGLE_LOGOUT_BINDING,
                SAMLConstants.SAML2_POST_BINDING_URI))
            .build();

        val result = samlLogoutBuilder.create(logoutRequest);
        assertNotNull(result);
    }

    @Test
    void verifySoapOperation() throws Throwable {
        
        val samlRegisteredService = SamlIdPTestUtils.getSamlRegisteredService();
        val logoutRequest = DefaultSingleLogoutRequestContext.builder()
            .logoutUrl(new URI("https://sp.example.org/slo").toURL())
            .registeredService(samlRegisteredService)
            .service(RegisteredServiceTestUtils.getService("https://sp.testshib.org/shibboleth-sp"))
            .ticketId("ST-123456789")
            .executionRequest(SingleLogoutExecutionRequest.builder()
                .ticketGrantingTicket(new MockTicketGrantingTicket("casuser"))
                .build())
            .logoutType(RegisteredServiceLogoutType.BACK_CHANNEL)
            .properties(CollectionUtils.wrap(SamlIdPSingleLogoutServiceLogoutUrlBuilder.PROPERTY_NAME_SINGLE_LOGOUT_BINDING,
                SAMLConstants.SAML2_SOAP11_BINDING_URI))
            .build();

        val result = samlLogoutBuilder.create(logoutRequest);
        assertNotNull(result);
    }

    @Test
    void verifySignByBasicCredOperation() throws Throwable {
        val samlRegisteredService = SamlIdPTestUtils.getSamlRegisteredService();
        samlRegisteredService.setSigningCredentialType("basic");
        samlRegisteredService.setSkewAllowance(1000);
        val logoutRequest = DefaultSingleLogoutRequestContext.builder()
            .logoutUrl(new URI("https://sp.example.org/slo").toURL())
            .registeredService(samlRegisteredService)
            .service(RegisteredServiceTestUtils.getService("https://sp.testshib.org/shibboleth-sp"))
            .ticketId("ST-123456789")
            .executionRequest(SingleLogoutExecutionRequest.builder()
                .ticketGrantingTicket(new MockTicketGrantingTicket("casuser"))
                .build())
            .logoutType(RegisteredServiceLogoutType.BACK_CHANNEL)
            .properties(CollectionUtils.wrap(SamlIdPSingleLogoutServiceLogoutUrlBuilder.PROPERTY_NAME_SINGLE_LOGOUT_BINDING,
                SAMLConstants.SAML2_POST_BINDING_URI))
            .build();

        val result = samlLogoutBuilder.create(logoutRequest);
        assertNotNull(result);
    }

    @Test
    void verifySignByFingerprintOperation() throws Throwable {
        val samlRegisteredService = SamlIdPTestUtils.getSamlRegisteredService();
        samlRegisteredService.setSigningCredentialFingerprint("badfingerprint");
        val logoutRequest = DefaultSingleLogoutRequestContext.builder()
            .logoutUrl(new URI("https://sp.example.org/slo").toURL())
            .registeredService(samlRegisteredService)
            .service(RegisteredServiceTestUtils.getService("https://sp.testshib.org/shibboleth-sp"))
            .ticketId("ST-123456789")
            .executionRequest(SingleLogoutExecutionRequest.builder()
                .ticketGrantingTicket(new MockTicketGrantingTicket("casuser"))
                .build())
            .logoutType(RegisteredServiceLogoutType.BACK_CHANNEL)
            .properties(CollectionUtils.wrap(SamlIdPSingleLogoutServiceLogoutUrlBuilder.PROPERTY_NAME_SINGLE_LOGOUT_BINDING,
                SAMLConstants.SAML2_POST_BINDING_URI))
            .build();

        assertThrows(IllegalArgumentException.class, () -> samlLogoutBuilder.create(logoutRequest));
    }

    @Test
    void verifyNameIdOperation() throws Throwable {
        val samlRegisteredService = SamlIdPTestUtils.getSamlRegisteredService();
        samlRegisteredService.setWhiteListBlackListPrecedence("INCLUDE");
        samlRegisteredService.setSigningKeyAlgorithm("RSA");
        samlRegisteredService.setSigningSignatureCanonicalizationAlgorithm("http://www.w3.org/2001/10/xml-exc-c14n#");
        samlRegisteredService.setUsernameAttributeProvider(
                new PrincipalAttributeRegisteredServiceUsernameProvider("email"));

        val logoutRequest = DefaultSingleLogoutRequestContext.builder()
                .logoutUrl(new URI("https://sp.example.org/slo").toURL())
                .registeredService(samlRegisteredService)
                .service(RegisteredServiceTestUtils.getService("https://sp.testshib.org/shibboleth-sp"))
                .ticketId("ST-123456789")
                .executionRequest(SingleLogoutExecutionRequest.builder()
                        .ticketGrantingTicket(
                                new MockTicketGrantingTicket("casuser",
                                        Map.of("email", "casuser@example.org"))).build())
                .logoutType(RegisteredServiceLogoutType.BACK_CHANNEL)
                .properties(CollectionUtils.wrap(
                        SamlIdPSingleLogoutServiceLogoutUrlBuilder.PROPERTY_NAME_SINGLE_LOGOUT_BINDING,
                        SAMLConstants.SAML2_POST_BINDING_URI))
                .build();

        val result = samlLogoutBuilder.create(logoutRequest);
        assertNotNull(result);
        assertTrue(result.getPayload().contains("casuser@example.org"));
    }

    @Test
    void verifyPrincipalIdIfNotAttrOperation() throws Throwable {
        val samlRegisteredService = SamlIdPTestUtils.getSamlRegisteredService();
        samlRegisteredService.setWhiteListBlackListPrecedence("INCLUDE");
        samlRegisteredService.setSigningKeyAlgorithm("RSA");
        samlRegisteredService.setSigningSignatureCanonicalizationAlgorithm("http://www.w3.org/2001/10/xml-exc-c14n#");
        samlRegisteredService.setUsernameAttributeProvider(
                new PrincipalAttributeRegisteredServiceUsernameProvider("email"));

        val logoutRequest = DefaultSingleLogoutRequestContext.builder()
                .logoutUrl(new URI("https://sp.example.org/slo").toURL())
                .registeredService(samlRegisteredService)
                .service(RegisteredServiceTestUtils.getService("https://sp.testshib.org/shibboleth-sp"))
                .ticketId("ST-123456789")
                .executionRequest(SingleLogoutExecutionRequest.builder()
                        .ticketGrantingTicket(new MockTicketGrantingTicket("casuser")).build())
                .logoutType(RegisteredServiceLogoutType.BACK_CHANNEL)
                .properties(CollectionUtils.wrap(
                        SamlIdPSingleLogoutServiceLogoutUrlBuilder.PROPERTY_NAME_SINGLE_LOGOUT_BINDING,
                        SAMLConstants.SAML2_POST_BINDING_URI))
                .build();

        val result = samlLogoutBuilder.create(logoutRequest);
        assertNotNull(result);
        assertFalse(result.getPayload().contains("casuser@example.org"));
    }
}
