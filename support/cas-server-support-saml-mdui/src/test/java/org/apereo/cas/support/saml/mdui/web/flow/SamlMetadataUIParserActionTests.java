package org.apereo.cas.support.saml.mdui.web.flow;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.config.CasCoreWebflowConfiguration;
import org.apereo.cas.config.CasWebflowContextConfiguration;
import org.apereo.cas.config.SamlMetadataUIConfiguration;
import org.apereo.cas.config.SamlMetadataUIWebflowConfiguration;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.saml.AbstractOpenSamlTests;
import org.apereo.cas.support.saml.SamlProtocolConstants;
import org.apereo.cas.support.saml.mdui.SamlMetadataUIInfo;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.webflow.execution.Action;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SamlMetadataUIParserActionTests}.
 *
 * @author Misagh Moayyed
 * @since 4.1.0
 */
@SpringBootTest(classes = {
    CasCoreWebflowConfiguration.class,
    CasWebflowContextConfiguration.class,
    SamlMetadataUIConfiguration.class,
    SamlMetadataUIWebflowConfiguration.class,
    AbstractOpenSamlTests.SharedTestConfiguration.class
}, properties = {
    "spring.main.allow-bean-definition-overriding=true",
    "cas.saml-metadata-ui.resources=classpath:sample-metadata.xml::classpath:inc-md-pub.pem"
})
@Tag("SAMLMetadata")
class SamlMetadataUIParserActionTests extends AbstractOpenSamlTests {
    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_SAML_METADATA_UI_PARSER)
    private Action samlMetadataUIParserAction;

    @Autowired
    @Qualifier(ServicesManager.BEAN_NAME)
    private ServicesManager servicesManager;

    @Test
    void verifyEntityIdUIInfoExists() throws Throwable {
        val ctx = MockRequestContext.create(applicationContext);
        ctx.setParameter(SamlProtocolConstants.PARAMETER_ENTITY_ID, "https://carmenwiki.osu.edu/shibboleth");
        ctx.getFlowScope().put(CasProtocolConstants.PARAMETER_SERVICE, RegisteredServiceTestUtils.getService());
        samlMetadataUIParserAction.execute(ctx);
        assertNotNull(WebUtils.getServiceUserInterfaceMetadata(ctx, SamlMetadataUIInfo.class));
    }

    @Test
    void verifyEntityIdUIInfoExistsEmbedded() throws Throwable {
        val ctx = MockRequestContext.create(applicationContext);
        val url = "https://google.com?entityId=https://carmenwiki.osu.edu/shibboleth";
        servicesManager.save(RegisteredServiceTestUtils.getRegisteredService("^https://google.com\\?entityId=.+"));

        val service = RegisteredServiceTestUtils.getService(url);
        ctx.setParameter(CasProtocolConstants.PARAMETER_SERVICE, service.getId());
        ctx.getFlowScope().put(CasProtocolConstants.PARAMETER_SERVICE, service);
        samlMetadataUIParserAction.execute(ctx);
        assertNotNull(WebUtils.getServiceUserInterfaceMetadata(ctx, SamlMetadataUIInfo.class));
    }

    @Test
    void verifyEntityIdUIInfoNoParam() throws Throwable {
        val ctx = MockRequestContext.create(applicationContext);
        ctx.setParameter("somethingelse", "https://carmenwiki.osu.edu/shibboleth");
        samlMetadataUIParserAction.execute(ctx);
        assertNull(WebUtils.getServiceUserInterfaceMetadata(ctx, SamlMetadataUIInfo.class));
    }

}
