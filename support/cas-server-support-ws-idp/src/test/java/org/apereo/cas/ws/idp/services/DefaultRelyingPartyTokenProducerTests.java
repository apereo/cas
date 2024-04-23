package org.apereo.cas.ws.idp.services;

import org.apereo.cas.BaseCoreWsSecurityIdentityProviderConfigurationTests;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.SecurityTokenServiceClient;
import org.apereo.cas.authentication.SecurityTokenServiceClientBuilder;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.validation.TicketValidationResult;
import org.apereo.cas.ws.idp.WSFederationClaims;
import org.apereo.cas.ws.idp.web.WSFederationRequest;

import lombok.val;
import org.apache.cxf.binding.soap.SoapFault;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.ws.security.tokenstore.SecurityToken;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.TestPropertySource;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DefaultRelyingPartyTokenProducerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("WSFederation")
@Import(DefaultRelyingPartyTokenProducerTests.DefaultRelyingPartyTokenProducerTestConfiguration.class)
@TestPropertySource(properties = "cas.authn.wsfed-idp.sts.custom-claims=my-custom-claim,second-custom-claim")
class DefaultRelyingPartyTokenProducerTests extends BaseCoreWsSecurityIdentityProviderConfigurationTests {
    @Autowired
    @Qualifier("wsFederationRelyingPartyTokenProducer")
    private WSFederationRelyingPartyTokenProducer wsFederationRelyingPartyTokenProducer;

    @Autowired
    @Qualifier(ServicesManager.BEAN_NAME)
    private ServicesManager servicesManager;

    @Test
    void verifyFailsOperation() throws Throwable {
        val request = new MockHttpServletRequest();

        val registeredService = new WSFederationRegisteredService();
        registeredService.setRealm("CAS");
        registeredService.setServiceId("http://app.example.org/wsfed-idp");
        registeredService.setName("WSFED App");
        registeredService.setId(100);
        registeredService.setAppliesTo("FatalError");
        registeredService.setWsdlLocation("classpath:wsdl/ws-trust-1.4-service.wsdl");
        servicesManager.save(registeredService);

        val assertion = getValidationResult();
        val securityToken = mock(SecurityToken.class);
        assertThrows(SoapFault.class, () -> wsFederationRelyingPartyTokenProducer.produce(securityToken, registeredService,
            WSFederationRequest.of(request), request, assertion));
    }

    private static TicketValidationResult getValidationResult(final Map<String, List<Object>> attributes) throws Throwable {
        val principal = PrincipalFactoryUtils.newPrincipalFactory().createPrincipal("casuser", attributes);
        val assertion = mock(TicketValidationResult.class);
        when(assertion.getPrincipal()).thenReturn(principal);
        return assertion;
    }

    private static TicketValidationResult getValidationResult() throws Throwable {
        return getValidationResult(CoreAuthenticationTestUtils.getAttributes());
    }

    @Test
    void verifyRequestFailsOperation() throws Throwable {
        val request = new MockHttpServletRequest();

        val registeredService = new WSFederationRegisteredService();
        registeredService.setRealm("CAS");
        registeredService.setServiceId("http://app.example.org/wsfed-idp");
        registeredService.setName("WSFED App");
        registeredService.setId(100);
        registeredService.setAppliesTo("RequestFailed");
        registeredService.setWsdlLocation("classpath:wsdl/ws-trust-1.4-service.wsdl");
        servicesManager.save(registeredService);

        val assertion = getValidationResult();
        val securityToken = mock(SecurityToken.class);
        assertThrows(IllegalArgumentException.class, () -> wsFederationRelyingPartyTokenProducer.produce(securityToken, registeredService,
            WSFederationRequest.of(request), request, assertion));
    }


    @Test
    void verifyOperation() throws Throwable {
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
        attributes.put("my-custom-claim", List.of("custom-claim-value"));

        val assertion = getValidationResult(attributes);

        val securityToken = mock(SecurityToken.class);
        val result = wsFederationRelyingPartyTokenProducer.produce(securityToken, registeredService,
            WSFederationRequest.of(request), request, assertion);
        assertEquals("<SecurityToken id=\"abcdefgh123456\"/>", result);
    }

    @TestConfiguration(value = "DefaultRelyingPartyTokenProducerTestConfiguration", proxyBeanMethods = false)
    static class DefaultRelyingPartyTokenProducerTestConfiguration {
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
            when(client.requestSecurityTokenResponse(ArgumentMatchers.eq("CAS"))).thenReturn(elementResponse);
            when(client.requestSecurityTokenResponse(ArgumentMatchers.eq("FatalError")))
                .thenThrow(new SoapFault("error", Fault.FAULT_CODE_SERVER));
            when(client.requestSecurityTokenResponse(ArgumentMatchers.eq("RequestFailed")))
                .thenThrow(new SoapFault("error", new QName("RequestFailed")));

            return builder;
        }
    }
}
