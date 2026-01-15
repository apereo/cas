package org.apereo.cas.web;

import module java.base;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.model.support.captcha.GoogleRecaptchaProperties;
import org.apereo.cas.services.DefaultRegisteredServiceProperty;
import org.apereo.cas.services.RegisteredServiceProperty;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DefaultCaptchaActivationStrategyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@Tag("Simple")
class DefaultCaptchaActivationStrategyTests {
    @Test
    void verifyByProps() throws Throwable {
        val strategy = new DefaultCaptchaActivationStrategy(mock(ServicesManager.class));
        val context = MockRequestContext.create();

        val properties = new GoogleRecaptchaProperties().setEnabled(true);
        assertTrue(strategy.shouldActivate(context, properties).isPresent());

        properties.setEnabled(false);
        assertTrue(strategy.shouldActivate(context, properties).isEmpty());
    }

    @Test
    void verifyByIpPattern() throws Throwable {
        val strategy = new DefaultCaptchaActivationStrategy(mock(ServicesManager.class));
        val request = new MockHttpServletRequest();
        val context = MockRequestContext.create();

        val properties = new GoogleRecaptchaProperties()
            .setEnabled(true)
            .setActivateForIpAddressPattern("127.+");
        request.setRemoteAddr("185.86.151.11");
        request.setLocalAddr("195.88.151.11");
        ClientInfoHolder.setClientInfo(ClientInfo.from(request));
        assertFalse(strategy.shouldActivate(context, properties).isPresent());
    }

    @Test
    void verifyByIpPatternPerService() throws Throwable {
        val servicesManager = mock(ServicesManager.class);
        val strategy = new DefaultCaptchaActivationStrategy(servicesManager);
        val context = MockRequestContext.create();
        context.setRemoteAddr("185.86.151.99");
        context.setLocalAddr("195.88.151.11");
        context.setClientInfo();

        val service = RegisteredServiceTestUtils.getService(UUID.randomUUID().toString());
        val registeredService = RegisteredServiceTestUtils.getRegisteredService(service.getId());
        registeredService.getProperties().put(RegisteredServiceProperty.RegisteredServiceProperties.CAPTCHA_ENABLED.getPropertyName(),
            new DefaultRegisteredServiceProperty("true"));
        registeredService.getProperties().put(RegisteredServiceProperty.RegisteredServiceProperties.CAPTCHA_IP_ADDRESS_PATTERN.getPropertyName(),
            new DefaultRegisteredServiceProperty("no-match1", "no-match2", "\\d\\d\\.8.+\\.99"));
        when(servicesManager.findServiceBy(any(Service.class))).thenReturn(registeredService);

        WebUtils.putServiceIntoFlowScope(context, service);
        val properties = new GoogleRecaptchaProperties().setEnabled(false);

        assertTrue(strategy.shouldActivate(context, properties).isPresent());
    }

    @Test
    void verifyByService() throws Throwable {
        val servicesManager = mock(ServicesManager.class);

        val strategy = new DefaultCaptchaActivationStrategy(servicesManager);
        val context = MockRequestContext.create();

        val service = RegisteredServiceTestUtils.getService(UUID.randomUUID().toString());
        val registeredService = RegisteredServiceTestUtils.getRegisteredService(service.getId());
        registeredService.getProperties().put(RegisteredServiceProperty.RegisteredServiceProperties.CAPTCHA_ENABLED.getPropertyName(),
            new DefaultRegisteredServiceProperty("true"));
        when(servicesManager.findServiceBy(any(Service.class))).thenReturn(registeredService);

        WebUtils.putServiceIntoFlowScope(context, service);
        val properties = new GoogleRecaptchaProperties().setEnabled(false);
        assertTrue(strategy.shouldActivate(context, properties).isPresent());
    }
}
