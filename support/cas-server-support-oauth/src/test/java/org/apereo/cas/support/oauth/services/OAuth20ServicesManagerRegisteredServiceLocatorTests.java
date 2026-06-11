package org.apereo.cas.support.oauth.services;

import module java.base;
import org.apereo.cas.AbstractOAuth20Tests;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.services.PartialRegexRegisteredServiceMatchingStrategy;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServicesManagerRegisteredServiceLocator;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.util.CollectionUtils;
import lombok.val;
import org.apache.hc.core5.net.URIBuilder;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.Ordered;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OAuth20ServicesManagerRegisteredServiceLocatorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("OAuth")
class OAuth20ServicesManagerRegisteredServiceLocatorTests extends AbstractOAuth20Tests {
    @Autowired
    @Qualifier("oauthServicesManagerRegisteredServiceLocator")
    private ServicesManagerRegisteredServiceLocator oauthServicesManagerRegisteredServiceLocator;

    @Test
    void verifyOperation() {
        assertNotNull(oauthServicesManagerRegisteredServiceLocator);
        assertEquals(Ordered.HIGHEST_PRECEDENCE, oauthServicesManagerRegisteredServiceLocator.getOrder());
        val service = getRegisteredService("clientid123456", UUID.randomUUID().toString());
        service.setMatchingStrategy(new PartialRegexRegisteredServiceMatchingStrategy());
        val svc = serviceFactory.createService(
            String.format("https://oauth.example.org/whatever?%s=%s", OAuth20Constants.CLIENT_ID, service.getClientId()));
        val result = oauthServicesManagerRegisteredServiceLocator.locate(List.of(service), svc);
        assertNotNull(result);
        assertFalse(oauthServicesManagerRegisteredServiceLocator.getRegisteredServiceIndexes().isEmpty());
    }

    @Test
    void verifyWithCallback() throws Throwable {
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

    @Test
    void verifyWithInvalidCallback() {
        val registeredService = RegisteredServiceTestUtils.getRegisteredService("https://casapp.example.org.*");
        servicesManager.save(registeredService);
        val url = "https://casapp.example.org?redirect_uri=http://localhost:9090/callback"
            + "&response_type=code&client_id=iQ6N0nAoS9&scope=FAR&"
            + "code_challenge=6im-DKpTVViPqWih7AU_OISn3QNZoGNkpyIXMjwqtwc&state=xy12AB";
        val request = new MockHttpServletRequest();
        request.setRequestURI("/cas/login");
        request.setParameter(CasProtocolConstants.PARAMETER_SERVICE, url);
        val service = serviceFactory.createService(request);
        val result = servicesManager.findServiceBy(service);
        assertNotNull(result);
        assertEquals(registeredService.getServiceId(), result.getServiceId());
    }

}
