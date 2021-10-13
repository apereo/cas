package org.apereo.cas.ws.idp.services;

import org.apereo.cas.BaseCoreWsSecurityIdentityProviderConfigurationTests;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.services.PartialRegexRegisteredServiceMatchingStrategy;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.ServicesManagerRegisteredServiceLocator;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.ws.idp.WSFederationConstants;

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
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link WsFederationServicesManagerRegisteredServiceLocatorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("WSFederation")
public class WsFederationServicesManagerRegisteredServiceLocatorTests extends BaseCoreWsSecurityIdentityProviderConfigurationTests {
    @Autowired
    @Qualifier("wsFederationServicesManagerRegisteredServiceLocator")
    private ServicesManagerRegisteredServiceLocator wsFederationServicesManagerRegisteredServiceLocator;

    @Autowired
    @Qualifier("webApplicationServiceFactory")
    private ServiceFactory<WebApplicationService> webApplicationServiceFactory;

    @Autowired
    @Qualifier(ServicesManager.BEAN_NAME)
    private ServicesManager servicesManager;

    @BeforeEach
    public void setup() {
        servicesManager.deleteAll();
    }

    @Test
    public void verifyOperation() {
        assertNotNull(wsFederationServicesManagerRegisteredServiceLocator);
        assertEquals(Ordered.HIGHEST_PRECEDENCE, wsFederationServicesManagerRegisteredServiceLocator.getOrder());
        val registeredService = getWsFederationRegisteredService("http://app.example.org/wsfed.*", "CAS");
        val service = webApplicationServiceFactory.createService("http://app.example.org/wsfed-whatever");
        service.setAttributes(Map.of(WSFederationConstants.WREPLY, List.of("http://app.example.org/wsfed")));
        val result = wsFederationServicesManagerRegisteredServiceLocator.locate(List.of(registeredService), service);
        assertNotNull(result);
    }

    private static WSFederationRegisteredService getWsFederationRegisteredService(final String serviceId,
                                                                                  final String realm) {
        val service = new WSFederationRegisteredService();
        service.setRealm(realm);
        service.setServiceId(serviceId);
        service.setName("WSFED App");
        service.setId(RandomUtils.nextInt());
        service.setAppliesTo("Example");
        service.setWsdlLocation("classpath:wsdl/ws-trust-1.4-service.wsdl");
        service.setMatchingStrategy(new PartialRegexRegisteredServiceMatchingStrategy());
        return service;
    }

    @Test
    public void verifyWithCallback() throws Exception {
        val callbackUrl = "http://localhost:8443/cas" + WSFederationConstants.ENDPOINT_FEDERATION_REQUEST_CALLBACK;
        
        val service0 = RegisteredServiceTestUtils.getRegisteredService(callbackUrl + ".*");
        service0.setEvaluationOrder(0);

        val service1 = getWsFederationRegisteredService("application1", "CAS");
        service1.setEvaluationOrder(100);

        val service2 = getWsFederationRegisteredService(".*", "CAS");
        service2.setEvaluationOrder(1000);

        val candidateServices = CollectionUtils.wrapList(service0, service1, service2);
        servicesManager.save(candidateServices.toArray(new RegisteredService[0]));

        Collections.sort(candidateServices);

        val url = new URIBuilder(callbackUrl + '?'
            + WSFederationConstants.WTREALM + "=CAS&" + WSFederationConstants.WREPLY + "=application1");
        val request = new MockHttpServletRequest();
        request.setRequestURI(callbackUrl);
        url.getQueryParams().forEach(param -> request.addParameter(param.getName(), param.getValue()));
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request, new MockHttpServletResponse()));
        val service = webApplicationServiceFactory.createService(url.toString());
        val result = servicesManager.findServiceBy(service);
        assertEquals(service1, result);
    }
}
