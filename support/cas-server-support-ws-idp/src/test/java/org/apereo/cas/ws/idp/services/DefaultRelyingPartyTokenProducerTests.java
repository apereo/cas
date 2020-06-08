package org.apereo.cas.ws.idp.services;

import org.apereo.cas.BaseCoreWsSecurityIdentityProviderConfigurationTests;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.SecurityTokenServiceClient;
import org.apereo.cas.authentication.SecurityTokenServiceClientBuilder;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ws.idp.WSFederationClaims;
import org.apereo.cas.ws.idp.web.WSFederationRequest;

import lombok.val;
import org.apache.cxf.ws.security.tokenstore.SecurityToken;
import org.jasig.cas.client.authentication.AttributePrincipalImpl;
import org.jasig.cas.client.validation.Assertion;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.mock.web.MockHttpServletRequest;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.HashMap;
import java.util.List;

import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.AfterEach;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import static org.apereo.cas.util.junit.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultRelyingPartyTokenProducerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@SpringBootTest(classes = {
    DefaultRelyingPartyTokenProducerTests.DefaultRelyingPartyTokenProducerTestConfiguration.class,
    BaseCoreWsSecurityIdentityProviderConfigurationTests.SharedTestConfiguration.class
},
    properties = {
        "cas.authn.wsfed-idp.idp.realm=urn:org:apereo:cas:ws:idp:realm-CAS",
        "cas.authn.wsfed-idp.idp.realm-name=CAS",

        "cas.authn.wsfed-idp.sts.signing-keystore-file=classpath:ststrust.jks",
        "cas.authn.wsfed-idp.sts.signing-keystore-password=storepass",

        "cas.authn.wsfed-idp.sts.encryption-keystore-file=classpath:stsencrypt.jks",
        "cas.authn.wsfed-idp.sts.encryption-keystore-password=storepass",

        "cas.authn.wsfed-idp.sts.subject-name-id-format=unspecified",
        "cas.authn.wsfed-idp.sts.encrypt-tokens=true",

        "cas.authn.wsfed-idp.sts.custom-claims=uid,cn",

        "cas.authn.wsfed-idp.sts.realm.keystore-file=stsrealm_a.jks",
        "cas.authn.wsfed-idp.sts.realm.keystore-password=storepass",
        "cas.authn.wsfed-idp.sts.realm.keystore-alias=realma",
        "cas.authn.wsfed-idp.sts.realm.key-password=realma",
        "cas.authn.wsfed-idp.sts.realm.issuer=CAS"
    })
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Tag("WSFederation")
public class DefaultRelyingPartyTokenProducerTests {
    @Autowired
    @Qualifier("wsFederationRelyingPartyTokenProducer")
    private WSFederationRelyingPartyTokenProducer wsFederationRelyingPartyTokenProducer;

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @Test
    public void verifyOperation() {
        val request = new MockHttpServletRequest();

        val registeredService = new WSFederationRegisteredService();
        registeredService.setRealm("CAS");
        registeredService.setServiceId("http://app.example.org/wsfed-idp");
        registeredService.setName("WSFED App");
        registeredService.setId(100);
        registeredService.setAppliesTo("CAS");
        registeredService.setWsdlLocation("classpath:wsdl/ws-trust-1.4-service.wsdl");
        servicesManager.save(registeredService);

        val attributes = CoreAuthenticationTestUtils.getAttributes();
        attributes.put(WSFederationClaims.COMMON_NAME.name(), List.of("common-name-wsfed"));
        attributes.put(WSFederationClaims.GIVEN_NAME.getUri(), List.of("common-name-wsfed"));

        val principal = new AttributePrincipalImpl("casuser", attributes);
        val assertion = mock(Assertion.class);
        when(assertion.getPrincipal()).thenReturn(principal);

        val securityToken = mock(SecurityToken.class);
        val result = wsFederationRelyingPartyTokenProducer.produce(securityToken, registeredService,
            WSFederationRequest.of(request), request, assertion);
        assertEquals("<SecurityToken id=\"abcdefgh123456\"/>", result);
    }

    @TestConfiguration("DefaultRelyingPartyTokenProducerTestConfiguration")
    public static class DefaultRelyingPartyTokenProducerTestConfiguration {
        @Bean
        public SecurityTokenServiceClientBuilder securityTokenServiceClientBuilder() throws Exception {
            val client = mock(SecurityTokenServiceClient.class);

            val builder = mock(SecurityTokenServiceClientBuilder.class);
            when(builder.buildClientForRelyingPartyTokenResponses(any(SecurityToken.class), any(WSFederationRegisteredService.class)))
                .thenReturn(client);
            when(client.getProperties()).thenReturn(new HashMap<>(0));

            val dbf = DocumentBuilderFactory.newInstance();
            val db = dbf.newDocumentBuilder();
            val elementResponse = db.newDocument().createElement("SecurityToken");
            elementResponse.setAttribute("id", "abcdefgh123456");
            when(client.requestSecurityTokenResponse(anyString())).thenReturn(elementResponse);
            return builder;
        }
    }
}
