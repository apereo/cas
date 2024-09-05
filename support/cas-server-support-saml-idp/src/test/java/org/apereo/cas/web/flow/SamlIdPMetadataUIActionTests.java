package org.apereo.cas.web.flow;

import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.saml.SamlIdPTestUtils;
import org.apereo.cas.support.saml.SamlProtocolConstants;
import org.apereo.cas.support.saml.mdui.SamlMetadataUIInfo;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.execution.Action;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SamlIdPMetadataUIActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("SAMLMetadata")
@TestPropertySource(properties = "cas.authn.saml-idp.metadata.file-system.location=${#systemProperties['java.io.tmpdir']}/saml8858")
class SamlIdPMetadataUIActionTests extends BaseSamlIdPWebflowTests {
    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_SAML_IDP_METADATA_UI_PARSER)
    private Action samlIdPMetadataUIParserAction;

    @Autowired
    @Qualifier(ServicesManager.BEAN_NAME)
    private ServicesManager servicesManager;

    @Test
    void verifyOperation() throws Throwable {
        val context = MockRequestContext.create(applicationContext);

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
    void verifyNoEntity() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
                     
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
