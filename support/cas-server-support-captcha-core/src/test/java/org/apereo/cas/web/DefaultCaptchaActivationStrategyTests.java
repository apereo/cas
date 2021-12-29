package org.apereo.cas.web;

import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.model.support.captcha.GoogleRecaptchaProperties;
import org.apereo.cas.services.DefaultRegisteredServiceProperty;
import org.apereo.cas.services.RegisteredServiceProperty;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.MockServletContext;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.webflow.context.ExternalContextHolder;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.RequestContextHolder;
import org.springframework.webflow.test.MockRequestContext;

import javax.servlet.http.HttpServletRequest;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DefaultCaptchaActivationStrategyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@Tag("Simple")
public class DefaultCaptchaActivationStrategyTests {

    private static MockRequestContext getRequestContext(final HttpServletRequest request) {
        val context = new MockRequestContext();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        RequestContextHolder.setRequestContext(context);
        ExternalContextHolder.setExternalContext(context.getExternalContext());
        return context;
    }

    @Test
    public void verifyByProps() {
        val strategy = new DefaultCaptchaActivationStrategy(mock(ServicesManager.class));
        val context = getRequestContext(new MockHttpServletRequest());

        val properties = new GoogleRecaptchaProperties().setEnabled(true);
        assertTrue(strategy.shouldActivate(context, properties).isPresent());

        properties.setEnabled(false);
        assertTrue(strategy.shouldActivate(context, properties).isEmpty());
    }

    @Test
    public void verifyByIpPattern() {
        val strategy = new DefaultCaptchaActivationStrategy(mock(ServicesManager.class));
        val request = new MockHttpServletRequest();
        val context = getRequestContext(request);

        val properties = new GoogleRecaptchaProperties()
            .setEnabled(true)
            .setActivateForIpAddressPattern("127.+");
        request.setRemoteAddr("185.86.151.11");
        request.setLocalAddr("195.88.151.11");
        ClientInfoHolder.setClientInfo(new ClientInfo(request));
        assertFalse(strategy.shouldActivate(context, properties).isPresent());
    }

    @Test
    public void verifyByIpPatternPerService() {
        val servicesManager = mock(ServicesManager.class);

        val strategy = new DefaultCaptchaActivationStrategy(servicesManager);
        val request = new MockHttpServletRequest();
        val context = getRequestContext(request);
        request.setRemoteAddr("185.86.151.99");
        request.setLocalAddr("195.88.151.11");
        ClientInfoHolder.setClientInfo(new ClientInfo(request));

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
    public void verifyByService() {
        val servicesManager = mock(ServicesManager.class);

        val strategy = new DefaultCaptchaActivationStrategy(servicesManager);
        val context = getRequestContext(new MockHttpServletRequest());

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
