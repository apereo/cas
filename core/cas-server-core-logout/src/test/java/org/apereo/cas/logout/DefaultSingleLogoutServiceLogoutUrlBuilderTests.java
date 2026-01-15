package org.apereo.cas.logout;

import module java.base;
import org.apereo.cas.logout.slo.DefaultSingleLogoutServiceLogoutUrlBuilder;
import org.apereo.cas.logout.slo.SingleLogoutServiceLogoutUrlBuilder;
import org.apereo.cas.services.BaseRegisteredService;
import org.apereo.cas.services.CasRegisteredService;
import org.apereo.cas.services.RegexMatchingRegisteredServiceProxyPolicy;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.web.SimpleUrlValidatorFactoryBean;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultSingleLogoutServiceLogoutUrlBuilderTests}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Tag("Logout")
@ExtendWith(CasTestExtension.class)
@SpringBootTest(classes = CasCoreLogoutAutoConfigurationTests.SharedTestConfiguration.class)
class DefaultSingleLogoutServiceLogoutUrlBuilderTests {
    @Autowired
    @Qualifier(ServicesManager.BEAN_NAME)
    protected ServicesManager servicesManager;

    public BaseRegisteredService getRegisteredService(final String id) {
        val registeredService = new CasRegisteredService();
        registeredService.setServiceId(id);
        registeredService.setName("Test service " + id);
        registeredService.setDescription("Registered service description");
        registeredService.setProxyPolicy(new RegexMatchingRegisteredServiceProxyPolicy().setPattern("^https?://.+"));
        registeredService.setId(RandomUtils.getNativeInstance().nextInt());
        servicesManager.save(registeredService);
        return registeredService;
    }

    @Test
    void verifyLogoutUrlByService() {
        val registeredService = getRegisteredService("https://www.google.com");
        registeredService.setLogoutUrl("http://www.example.com/logout");
        val builder = createDefaultSingleLogoutServiceLogoutUrlBuilder(false);
        val url = builder.determineLogoutUrl(registeredService, RegisteredServiceTestUtils.getService("https://www.google.com"));
        assertEquals(url.iterator().next().getUrl(), registeredService.getLogoutUrl());
    }

    @Test
    void verifyLogoutUrlByDefault() throws Throwable {
        val registeredService = getRegisteredService(".+");
        registeredService.setLogoutUrl(null);
        val builder = createDefaultSingleLogoutServiceLogoutUrlBuilder(false);
        val url = builder.determineLogoutUrl(registeredService, RegisteredServiceTestUtils.getService("https://www.somewhere.com/logout?p=v"));
        assertEquals(new URI("https://www.somewhere.com/logout?p=v").toURL().toExternalForm(), url.iterator().next().getUrl());
    }

    @Test
    void verifyLogoutUrlUnknownUrlProtocol() {
        val registeredService = getRegisteredService(".+");
        registeredService.setLogoutUrl(null);
        val builder = createDefaultSingleLogoutServiceLogoutUrlBuilder(false);
        val url = builder.determineLogoutUrl(registeredService, RegisteredServiceTestUtils.getService("imaps://etc.example.org"));
        assertTrue(url.isEmpty());
    }

    @Test
    void verifyLocalLogoutUrlWithLocalUrlNotAllowed() {
        val svc = getRegisteredService(".+");
        svc.setLogoutUrl(null);
        val builder = createDefaultSingleLogoutServiceLogoutUrlBuilder(false);
        val url = builder.determineLogoutUrl(svc, RegisteredServiceTestUtils.getService("https://localhost/logout?p=v"));
        assertTrue(url.isEmpty());
    }

    @Test
    void verifyLocalLogoutUrlWithLocalUrlAllowed() throws Throwable {
        val svc = getRegisteredService(".+");
        svc.setLogoutUrl(null);
        val builder = createDefaultSingleLogoutServiceLogoutUrlBuilder(true);
        val url = builder.determineLogoutUrl(svc, RegisteredServiceTestUtils.getService("https://localhost/logout?p=v"));
        assertEquals(new URI("https://localhost/logout?p=v").toURL().toExternalForm(), url.iterator().next().getUrl());
    }

    @Test
    void verifyLocalLogoutUrlWithValidRegExValidationAndLocalUrlNotAllowed() throws Throwable {
        val svc = getRegisteredService(".+");
        svc.setLogoutUrl(null);
        val builder = createDefaultSingleLogoutServiceLogoutUrlBuilder(false, "\\w*", true);
        val url = builder.determineLogoutUrl(svc, RegisteredServiceTestUtils.getService("https://localhost/logout?p=v"));
        assertEquals(new URI("https://localhost/logout?p=v").toURL().toExternalForm(), url.iterator().next().getUrl());
    }

    @Test
    void verifyLocalLogoutUrlWithInvalidRegExValidationAndLocalUrlAllowed() throws Throwable {
        val svc = getRegisteredService(".+");
        svc.setLogoutUrl(null);
        val builder = createDefaultSingleLogoutServiceLogoutUrlBuilder(true, "\\d*", true);
        val url = builder.determineLogoutUrl(svc, RegisteredServiceTestUtils.getService("https://localhost/logout?p=v"));
        assertEquals(new URI("https://localhost/logout?p=v").toURL().toExternalForm(), url.iterator().next().getUrl());
    }

    @Test
    void verifyLocalLogoutUrlWithInvalidRegExValidationAndLocalUrlNotAllowed() {
        val svc = getRegisteredService(".+");
        svc.setLogoutUrl(null);
        val builder = createDefaultSingleLogoutServiceLogoutUrlBuilder(false, "\\d*", true);
        val url = builder.determineLogoutUrl(svc, RegisteredServiceTestUtils.getService("https://localhost/logout?p=v"));
        assertTrue(url.isEmpty());
    }

    private SingleLogoutServiceLogoutUrlBuilder createDefaultSingleLogoutServiceLogoutUrlBuilder(final boolean allowLocalLogoutUrls) {
        return createDefaultSingleLogoutServiceLogoutUrlBuilder(allowLocalLogoutUrls, null, true);
    }

    private SingleLogoutServiceLogoutUrlBuilder createDefaultSingleLogoutServiceLogoutUrlBuilder(final boolean allowLocalLogoutUrls,
                                                                                                 final String authorityValidationRegEx,
                                                                                                 final boolean authorityValidationRegExCaseSensitive) {
        val validator = new SimpleUrlValidatorFactoryBean(allowLocalLogoutUrls, authorityValidationRegEx,
            authorityValidationRegExCaseSensitive).getObject();
        return new DefaultSingleLogoutServiceLogoutUrlBuilder(servicesManager, validator);
    }
}
