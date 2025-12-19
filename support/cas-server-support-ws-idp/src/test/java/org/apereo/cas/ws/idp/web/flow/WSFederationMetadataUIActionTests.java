package org.apereo.cas.ws.idp.web.flow;

import module java.base;
import org.apereo.cas.BaseCoreWsSecurityIdentityProviderConfigurationTests;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;
import org.apereo.cas.ws.idp.WSFederationConstants;
import org.apereo.cas.ws.idp.services.WSFederationRegisteredService;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.webflow.execution.Action;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link WSFederationMetadataUIActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("WebflowActions")
class WSFederationMetadataUIActionTests extends BaseCoreWsSecurityIdentityProviderConfigurationTests {
    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_WSFEDERATION_METADATA_UI)
    private Action wsFederationMetadataUIAction;

    @Autowired
    @Qualifier(ServicesManager.BEAN_NAME)
    private ServicesManager servicesManager;

    @Test
    void verifyOperation() throws Throwable {
        val context = MockRequestContext.create(applicationContext);

        val registeredService = new WSFederationRegisteredService();
        registeredService.setRealm("CAS");
        registeredService.setServiceId("http://app.example5.org/wsfed-idp");
        registeredService.setName("WSFED App");
        registeredService.setId(100);
        registeredService.setAppliesTo("CAS");
        registeredService.setWsdlLocation("classpath:wsdl/ws-trust-1.4-service.wsdl");
        servicesManager.save(registeredService);

        val service = RegisteredServiceTestUtils.getService("http://app.example5.org/wsfed-idp");
        service.getAttributes().put(WSFederationConstants.WREPLY, List.of(registeredService.getServiceId()));
        
        WebUtils.putServiceIntoFlowScope(context, service);
        val event = wsFederationMetadataUIAction.execute(context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, event.getId());
    }
}
