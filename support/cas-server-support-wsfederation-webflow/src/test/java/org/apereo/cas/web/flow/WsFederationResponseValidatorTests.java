package org.apereo.cas.web.flow;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.wsfederation.WsFederationConfiguration;
import org.apereo.cas.support.wsfederation.WsFederationHelper;
import org.apereo.cas.support.wsfederation.web.WsFederationCookieManager;
import org.apereo.cas.support.wsfederation.web.WsFederationNavigationController;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.util.http.HttpRequestUtils;
import org.apereo.cas.util.spring.beans.BeanContainer;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.webflow.execution.RequestContext;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link WsFederationResponseValidatorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("WSFederation")
@SpringBootTest(classes = BaseWsFederationWebflowTests.SharedTestConfiguration.class,
    properties = {
        "cas.authn.wsfed[0].identity-provider-url=https://example.com/adfs/ls/",
        "cas.authn.wsfed[0].identity-provider-identifier=http://adfs.example.com/adfs/services/trust",
        "cas.authn.wsfed[0].relying-party-identifier=urn:federation:cas",
        "cas.authn.wsfed[0].signing-certificate-resources=classpath:adfs-signing.crt",
        "cas.authn.wsfed[0].identity-attribute=upn"
    })
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class WsFederationResponseValidatorTests {
    @Autowired
    @Qualifier("wsFederationResponseValidator")
    private WsFederationResponseValidator wsFederationResponseValidator;

    @Autowired
    @Qualifier("wsFederationNavigationController")
    private WsFederationNavigationController wsFederationNavigationController;

    @Autowired
    @Qualifier("wsFederationConfigurations")
    private BeanContainer<WsFederationConfiguration> wsFederationConfigurations;

    @Autowired
    @Qualifier(ServicesManager.BEAN_NAME)
    private ServicesManager servicesManager;

    @Autowired
    @Qualifier("wsFederationHelper")
    private WsFederationHelper wsFederationHelper;

    @Autowired
    private ConfigurableApplicationContext applicationContext;
    
    @Test
    @Order(2)
    void verifyOperation() throws Throwable {
        val zdt = ZonedDateTime.of(2014, 2, 26, 22, 51, 10, 0, ZoneOffset.UTC);
        val clock = Clock.fixed(zdt.toInstant(), ZoneOffset.UTC);
        wsFederationHelper.setClock(clock);

        val context = prepareContext();
        assertDoesNotThrow(() -> wsFederationResponseValidator.validateWsFederationAuthenticationRequest(context));
    }

    @Test
    @Order(1)
    void verifyFailedOperation() throws Throwable {
        val context = prepareContext();
        assertThrows(IllegalArgumentException.class, () -> wsFederationResponseValidator.validateWsFederationAuthenticationRequest(context));
    }

    private RequestContext prepareContext() throws Exception {
        val context = MockRequestContext.create(applicationContext);

        context.getHttpServletRequest().setRemoteAddr("185.86.151.11");
        context.getHttpServletRequest().setLocalAddr("185.88.151.11");
        context.getHttpServletRequest().addHeader(HttpRequestUtils.USER_AGENT_HEADER, "Mozilla/5.0 (Windows NT 10.0; WOW64)");
        ClientInfoHolder.setClientInfo(ClientInfo.from(context.getHttpServletRequest()));

        val registeredService = RegisteredServiceTestUtils.getRegisteredService("https://wsfedservice-validate");
        servicesManager.save(registeredService);

        val service = RegisteredServiceTestUtils.getService(registeredService.getServiceId());
        context.setParameter(CasProtocolConstants.PARAMETER_SERVICE, service.getId());
        val wsConfig = wsFederationConfigurations.toList().get(0);
        val id = wsConfig.getId();
        context.setParameter(WsFederationNavigationController.PARAMETER_NAME, id);
        wsFederationNavigationController.redirectToProvider(context.getHttpServletRequest(), context.getHttpServletResponse());
        context.getHttpServletRequest().setCookies(context.getHttpServletResponse().getCookies());

        val wresult = IOUtils.toString(new ClassPathResource("goodTokenResponse.txt").getInputStream(), StandardCharsets.UTF_8);
        context.setParameter(WsFederationResponseValidator.WRESULT, wresult);
        context.setParameter(WsFederationCookieManager.WCTX, id);
        return context;
    }
}
