package org.apereo.cas.support.oauth.services;

import org.apereo.cas.AbstractOAuth20Tests;
import org.apereo.cas.services.PartialRegexRegisteredServiceMatchingStrategy;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServicesManagerRegisteredServiceLocator;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.util.CollectionUtils;

import lombok.val;
import org.apache.http.client.utils.URIBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.Ordered;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OAuth20ServicesManagerRegisteredServiceLocatorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("OAuth")
public class OAuth20ServicesManagerRegisteredServiceLocatorTests extends AbstractOAuth20Tests {
    @Autowired
    @Qualifier("oauthServicesManagerRegisteredServiceLocator")
    private ServicesManagerRegisteredServiceLocator oauthServicesManagerRegisteredServiceLocator;

    @BeforeEach
    public void setup() {
        super.setup();
        servicesManager.deleteAll();
    }
    
    @Test
    public void verifyOperation() {
        assertNotNull(oauthServicesManagerRegisteredServiceLocator);
        assertEquals(Ordered.HIGHEST_PRECEDENCE, oauthServicesManagerRegisteredServiceLocator.getOrder());
        val service = getRegisteredService("clientid123456", UUID.randomUUID().toString());
        service.setMatchingStrategy(new PartialRegexRegisteredServiceMatchingStrategy());
        val svc = serviceFactory.createService(
            String.format("https://oauth.example.org/whatever?%s=%s", OAuth20Constants.CLIENT_ID, service.getClientId()));
        val result = oauthServicesManagerRegisteredServiceLocator.locate(List.of(service), svc);
        assertNotNull(result);
    }

    @Test
    public void verifyWithCallback() throws Exception {
        val callbackUrl = "http://localhost:8443/cas"
            + OAuth20Constants.BASE_OAUTH20_URL + '/' + OAuth20Constants.CALLBACK_AUTHORIZE_URL;

        val service0 = RegisteredServiceTestUtils.getRegisteredService(callbackUrl + ".*");
        service0.setEvaluationOrder(0);

        val service1 = getRegisteredService("http://localhost:8080/app1", "application1", "secret1");
        service1.setEvaluationOrder(100);

        val service2 = getRegisteredService(".+", "application2", "secret2");
        service2.setEvaluationOrder(1000);

        val candidateServices = CollectionUtils.wrapList(service0, service1, service2);
        servicesManager.save(candidateServices.toArray(new RegisteredService[0]));

        Collections.sort(candidateServices);

        val url = new URIBuilder(callbackUrl + '?' + OAuth20Constants.CLIENT_ID + "=application1");
        val request = new MockHttpServletRequest();
        request.setRequestURI(callbackUrl);
        url.getQueryParams().forEach(param -> request.addParameter(param.getName(), param.getValue()));
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request, new MockHttpServletResponse()));
        val service = serviceFactory.createService(url.toString());
        val result = servicesManager.findServiceBy(service);
        assertEquals(service1, result);
    }

}
