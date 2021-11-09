package org.apereo.cas.logout;

import org.apereo.cas.authentication.principal.AbstractWebApplicationService;
import org.apereo.cas.authentication.principal.WebApplicationServiceFactory;
import org.apereo.cas.logout.slo.DefaultSingleLogoutServiceLogoutUrlBuilder;
import org.apereo.cas.services.AbstractRegisteredService;
import org.apereo.cas.services.DefaultServicesManager;
import org.apereo.cas.services.DefaultServicesManagerRegisteredServiceLocator;
import org.apereo.cas.services.InMemoryServiceRegistry;
import org.apereo.cas.services.RegexMatchingRegisteredServiceProxyPolicy;
import org.apereo.cas.services.RegexRegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.ServicesManagerConfigurationContext;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.web.SimpleUrlValidatorFactoryBean;

import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;

import java.net.URL;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultSingleLogoutServiceLogoutUrlBuilderTests}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Tag("Logout")
public class DefaultSingleLogoutServiceLogoutUrlBuilderTests {
    private ServicesManager servicesManager;

    public static AbstractWebApplicationService getService(final String url) {
        val request = new MockHttpServletRequest();
        request.addParameter("service", url);
        return (AbstractWebApplicationService) new WebApplicationServiceFactory().createService(request);
    }

    @SneakyThrows
    public AbstractRegisteredService getRegisteredService(final String id) {
        val s = new RegexRegisteredService();
        s.setServiceId(id);
        s.setName("Test service " + id);
        s.setDescription("Registered service description");
        s.setProxyPolicy(new RegexMatchingRegisteredServiceProxyPolicy("^https?://.+"));
        s.setId(RandomUtils.getNativeInstance().nextInt());
        servicesManager.save(s);
        return s;
    }

    @BeforeEach
    public void beforeEach() {
        val appCtx = new StaticApplicationContext();
        appCtx.refresh();
        val context = ServicesManagerConfigurationContext.builder()
            .serviceRegistry(new InMemoryServiceRegistry(appCtx))
            .applicationContext(appCtx)
            .environments(new HashSet<>(0))
            .servicesCache(Caffeine.newBuilder().build())
            .registeredServiceLocators(List.of(new DefaultServicesManagerRegisteredServiceLocator()))
            .build();
        this.servicesManager = new DefaultServicesManager(context);
    }

    @Test
    public void verifyLogoutUrlByService() {
        val svc = getRegisteredService("https://www.google.com");
        svc.setLogoutUrl("http://www.example.com/logout");
        val builder = createDefaultSingleLogoutServiceLogoutUrlBuilder(false);
        val url = builder.determineLogoutUrl(svc, getService("https://www.google.com"));
        assertEquals(url.iterator().next().getUrl(), svc.getLogoutUrl());
    }

    @Test
    public void verifyLogoutUrlByDefault() throws Exception {
        val svc = getRegisteredService(".+");
        svc.setLogoutUrl(null);
        val builder = createDefaultSingleLogoutServiceLogoutUrlBuilder(false);
        val url = builder.determineLogoutUrl(svc, getService("https://www.somewhere.com/logout?p=v"));
        assertEquals(new URL("https://www.somewhere.com/logout?p=v").toExternalForm(), url.iterator().next().getUrl());
    }

    @Test
    public void verifyLogoutUrlUnknownUrlProtocol() {
        val svc = getRegisteredService(".+");
        svc.setLogoutUrl(null);
        val builder = createDefaultSingleLogoutServiceLogoutUrlBuilder(false);
        val url = builder.determineLogoutUrl(svc, getService("imaps://etc.example.org"));
        assertTrue(url.isEmpty());
    }

    @Test
    public void verifyLocalLogoutUrlWithLocalUrlNotAllowed() {
        val svc = getRegisteredService(".+");
        svc.setLogoutUrl(null);
        val builder = createDefaultSingleLogoutServiceLogoutUrlBuilder(false);
        val url = builder.determineLogoutUrl(svc, getService("https://localhost/logout?p=v"));
        assertTrue(url.isEmpty());
    }

    @Test
    public void verifyLocalLogoutUrlWithLocalUrlAllowed() throws Exception {
        val svc = getRegisteredService(".+");
        svc.setLogoutUrl(null);
        val builder = createDefaultSingleLogoutServiceLogoutUrlBuilder(true);
        val url = builder.determineLogoutUrl(svc, getService("https://localhost/logout?p=v"));
        assertEquals(new URL("https://localhost/logout?p=v").toExternalForm(), url.iterator().next().getUrl());
    }

    @Test
    public void verifyLocalLogoutUrlWithValidRegExValidationAndLocalUrlNotAllowed() throws Exception {
        val svc = getRegisteredService(".+");
        svc.setLogoutUrl(null);
        val builder = createDefaultSingleLogoutServiceLogoutUrlBuilder(false, "\\w*", true);
        val url = builder.determineLogoutUrl(svc, getService("https://localhost/logout?p=v"));
        assertEquals(new URL("https://localhost/logout?p=v").toExternalForm(), url.iterator().next().getUrl());
    }

    @Test
    public void verifyLocalLogoutUrlWithInvalidRegExValidationAndLocalUrlAllowed() throws Exception {
        val svc = getRegisteredService(".+");
        svc.setLogoutUrl(null);
        val builder = createDefaultSingleLogoutServiceLogoutUrlBuilder(true, "\\d*", true);
        val url = builder.determineLogoutUrl(svc, getService("https://localhost/logout?p=v"));
        assertEquals(new URL("https://localhost/logout?p=v").toExternalForm(), url.iterator().next().getUrl());
    }

    @Test
    public void verifyLocalLogoutUrlWithInvalidRegExValidationAndLocalUrlNotAllowed() {
        val svc = getRegisteredService(".+");
        svc.setLogoutUrl(null);
        val builder = createDefaultSingleLogoutServiceLogoutUrlBuilder(false, "\\d*", true);
        val url = builder.determineLogoutUrl(svc, getService("https://localhost/logout?p=v"));
        assertTrue(url.isEmpty());
    }

    private DefaultSingleLogoutServiceLogoutUrlBuilder createDefaultSingleLogoutServiceLogoutUrlBuilder(final boolean allowLocalLogoutUrls) {
        return createDefaultSingleLogoutServiceLogoutUrlBuilder(allowLocalLogoutUrls, null, true);
    }

    private DefaultSingleLogoutServiceLogoutUrlBuilder createDefaultSingleLogoutServiceLogoutUrlBuilder(final boolean allowLocalLogoutUrls,
                                                                                                        final String authorityValidationRegEx,
                                                                                                        final boolean authorityValidationRegExCaseSensitive) {
        val validator = new SimpleUrlValidatorFactoryBean(allowLocalLogoutUrls, authorityValidationRegEx,
            authorityValidationRegExCaseSensitive).getObject();
        return new DefaultSingleLogoutServiceLogoutUrlBuilder(servicesManager, validator);
    }
}
