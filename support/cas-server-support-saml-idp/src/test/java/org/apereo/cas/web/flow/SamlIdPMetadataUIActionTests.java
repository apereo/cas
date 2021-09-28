package org.apereo.cas.web.flow;

import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.saml.SamlIdPTestUtils;
import org.apereo.cas.support.saml.SamlProtocolConstants;
import org.apereo.cas.support.saml.mdui.SamlMetadataUIInfo;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.ExternalContextHolder;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.execution.RequestContextHolder;
import org.springframework.webflow.test.MockRequestContext;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SamlIdPMetadataUIActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("SAMLMetadata")
public class SamlIdPMetadataUIActionTests extends BaseSamlIdPWebflowTests {
    @Autowired
    @Qualifier("samlIdPMetadataUIParserAction")
    private Action samlIdPMetadataUIParserAction;

    @Autowired
    @Qualifier(ServicesManager.BEAN_NAME)
    private ServicesManager servicesManager;

    @Test
    public void verifyOperation() throws Exception {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        RequestContextHolder.setRequestContext(context);
        ExternalContextHolder.setExternalContext(context.getExternalContext());

        val registeredService = SamlIdPTestUtils.getSamlRegisteredService();
        val service = RegisteredServiceTestUtils.getService(registeredService.getServiceId());
        service.getAttributes().put(SamlProtocolConstants.PARAMETER_ENTITY_ID, List.of(registeredService.getServiceId()));
        
        WebUtils.putServiceIntoFlowScope(context, service);
        servicesManager.save(registeredService);

        val result = samlIdPMetadataUIParserAction.execute(context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, result.getId());
        assertNotNull(WebUtils.getServiceUserInterfaceMetadata(context, SamlMetadataUIInfo.class));
    }

    @Test
    public void verifyNoEntity() throws Exception {
        
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        RequestContextHolder.setRequestContext(context);
        ExternalContextHolder.setExternalContext(context.getExternalContext());
                     
        val registeredService = SamlIdPTestUtils.getSamlRegisteredService();
        registeredService.setServiceId("something-else");
        val service = RegisteredServiceTestUtils.getService(registeredService.getServiceId());
        WebUtils.putServiceIntoFlowScope(context, service);
        servicesManager.save(registeredService);
                                          
        val result = samlIdPMetadataUIParserAction.execute(context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, result.getId());
        assertNull(WebUtils.getServiceUserInterfaceMetadata(context, SamlMetadataUIInfo.class));

    }

}
