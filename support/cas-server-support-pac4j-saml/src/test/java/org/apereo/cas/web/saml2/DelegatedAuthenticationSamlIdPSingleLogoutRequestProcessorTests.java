package org.apereo.cas.web.saml2;

import org.apereo.cas.config.CasSamlIdPAutoConfiguration;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.SamlProtocolConstants;
import org.apereo.cas.support.saml.idp.slo.SamlIdPProfileSingleLogoutRequestProcessor;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.saml.common.SAMLObjectBuilder;
import org.opensaml.saml.common.messaging.context.SAMLBindingContext;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.LogoutRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.webflow.execution.Action;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DelegatedAuthenticationSamlIdPSingleLogoutRequestProcessorTests}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
@SpringBootTest(classes = {
    CasSamlIdPAutoConfiguration.class,
    BaseSaml2DelegatedAuthenticationTests.SharedTestConfiguration.class
}, properties = "cas.authn.saml-idp.metadata.file-system.location=${#systemProperties['java.io.tmpdir']}/saml4222")
@Tag("SAML2Web")
@ExtendWith(CasTestExtension.class)
class DelegatedAuthenticationSamlIdPSingleLogoutRequestProcessorTests {
    @Autowired
    @Qualifier("delegatedSaml2IdPSloRequestProcessor")
    private SamlIdPProfileSingleLogoutRequestProcessor delegatedSaml2IdPSloRequestProcessor;

    @Autowired
    @Qualifier(OpenSamlConfigBean.DEFAULT_BEAN_NAME)
    private OpenSamlConfigBean openSamlConfigBean;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    @Qualifier("delegatedSaml2IdPSloRequestCookieGenerator")
    private CasCookieBuilder delegatedSaml2IdPSloRequestCookieGenerator;
    
    @Autowired
    @Qualifier(ServicesManager.BEAN_NAME)
    private ServicesManager servicesManager;
    
    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_DELEGATED_AUTHENTICATION_SAML2_IDP_RESTORE_SLO_REQUEST)
    private Action action;
    
    @Test
    void verifyOperation() throws Exception {
        var builder = (SAMLObjectBuilder) openSamlConfigBean.getBuilderFactory()
            .getBuilder(LogoutRequest.DEFAULT_ELEMENT_NAME);
        val logoutRequest = (LogoutRequest) builder.buildObject();

        builder = (SAMLObjectBuilder) openSamlConfigBean.getBuilderFactory()
            .getBuilder(Issuer.DEFAULT_ELEMENT_NAME);
        val issuer = (Issuer) builder.buildObject();
        issuer.setValue("https://samltest.id/saml/sp");
        logoutRequest.setIssuer(issuer);

        val messageContext = new MessageContext();
        messageContext.ensureSubcontext(SAMLBindingContext.class).setRelayState(UUID.randomUUID().toString());
        messageContext.setMessage(logoutRequest);

        val requestContext = MockRequestContext.create(applicationContext).withUserAgent();
        requestContext.setContextPath("/cas");
        delegatedSaml2IdPSloRequestProcessor.receive(requestContext.getHttpServletRequest(),
            requestContext.getHttpServletResponse(), logoutRequest, messageContext);
        requestContext.setRequestCookiesFromResponse();
        val cookieValue = delegatedSaml2IdPSloRequestCookieGenerator.retrieveCookieValue(requestContext.getHttpServletRequest());
        assertNotNull(cookieValue);
        
        val registeredService = new SamlRegisteredService();
        registeredService.setId(RandomUtils.nextInt());
        registeredService.setName("SAML");
        registeredService.setServiceId(issuer.getValue());
        registeredService.setMetadataLocation(issuer.getValue());
        servicesManager.save(registeredService);

        requestContext.getHttpServletResponse().reset();
        
        assertNull(action.execute(requestContext));
        assertNotNull(WebUtils.getRegisteredService(requestContext.getHttpServletRequest()));
        assertNotNull(WebUtils.getSingleLogoutRequest(requestContext.getHttpServletRequest()));
        assertNotNull(requestContext.getHttpServletRequest().getAttribute(SamlProtocolConstants.PARAMETER_SAML_RELAY_STATE));
        assertTrue(requestContext.getFlowScope().asMap().containsKey(SamlProtocolConstants.PARAMETER_SAML_RELAY_STATE));

        val cookie = requestContext.getHttpServletResponse().getCookie(delegatedSaml2IdPSloRequestCookieGenerator.getCookieName());
        assertNotNull(cookie);
        assertEquals(0, cookie.getMaxAge());
    }

}
