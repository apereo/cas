package org.apereo.cas.oidc.services;

import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.services.PartialRegexRegisteredServiceMatchingStrategy;
import org.apereo.cas.services.RegexRegisteredService;
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
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcServicesManagerRegisteredServiceLocatorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("OIDC")
public class OidcServicesManagerRegisteredServiceLocatorTests extends AbstractOidcTests {
    @Autowired
    @Qualifier("oidcServicesManagerRegisteredServiceLocator")
    private ServicesManagerRegisteredServiceLocator oidcServicesManagerRegisteredServiceLocator;

    @BeforeEach
    public void setup() {
        servicesManager.deleteAll();
    }

    @Test
    public void verifyWithCallback() throws Exception {
        val callbackUrl = "http://localhost:8443/cas"
            + OAuth20Constants.BASE_OAUTH20_URL + '/' + OAuth20Constants.CALLBACK_AUTHORIZE_URL;

        val service0 = RegisteredServiceTestUtils.getRegisteredService(callbackUrl + ".*");
        service0.setEvaluationOrder(0);

        val service1 = getOidcRegisteredService("application1");
        service1.setEvaluationOrder(100);

        val service2 = getOidcRegisteredService("application-catch-all", ".*");
        service2.setEvaluationOrder(1000);

        val candidateServices = CollectionUtils.wrapList(service0, service1, service2);
        servicesManager.save(candidateServices.toArray(new RegisteredService[0]));

        Collections.sort(candidateServices);

        val url = new URIBuilder(callbackUrl + '?' + OAuth20Constants.CLIENT_ID + "=application1");
        val request = new MockHttpServletRequest();
        request.setRequestURI(callbackUrl);
        url.getQueryParams().forEach(param -> request.addParameter(param.getName(), param.getValue()));
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request, new MockHttpServletResponse()));
        val service = webApplicationServiceFactory.createService(url.toString());
        val result = servicesManager.findServiceBy(service);
        assertEquals(result, service1);
    }

    @Test
    public void verifyOperation() {
        assertNotNull(oidcServicesManagerRegisteredServiceLocator);
        assertEquals(OidcServicesManagerRegisteredServiceLocator.DEFAULT_ORDER, oidcServicesManagerRegisteredServiceLocator.getOrder());

        val clientId = UUID.randomUUID().toString();
        val service = getOidcRegisteredService(clientId);
        service.setMatchingStrategy(new PartialRegexRegisteredServiceMatchingStrategy());
        val svc = webApplicationServiceFactory.createService(
            String.format("https://oauth.example.org/whatever?%s=%s", OAuth20Constants.CLIENT_ID, clientId));
        val result = oidcServicesManagerRegisteredServiceLocator.locate(List.of(service), svc);
        assertNotNull(result);
    }

    @Test
    public void verifyReverseOperation() {
        val service1 = RegisteredServiceTestUtils.getRegisteredService(".+");
        service1.setEvaluationOrder(5);

        val oidcClientId = UUID.randomUUID().toString();
        val service2 = getOidcRegisteredService(oidcClientId, ".+", false, false);
        service2.setEvaluationOrder(10);

        val oauthClientId = UUID.randomUUID().toString();
        val service3 = getOAuthRegisteredService(oauthClientId, ".+");
        service3.setEvaluationOrder(15);

        servicesManager.save(service1, service2, service3);

        var svc = webApplicationServiceFactory.createService(
            String.format("https://app.example.org/whatever?%s=%s", OAuth20Constants.CLIENT_ID, oidcClientId));
        var result = servicesManager.findServiceBy(svc);
        assertTrue(result instanceof OidcRegisteredService);

        svc = webApplicationServiceFactory.createService("https://app.example.org/whatever?hello=world");
        result = servicesManager.findServiceBy(svc);
        assertTrue(result instanceof RegexRegisteredService);
    }

}
