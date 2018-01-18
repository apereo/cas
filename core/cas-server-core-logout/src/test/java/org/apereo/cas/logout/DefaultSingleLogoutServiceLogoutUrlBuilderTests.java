package org.apereo.cas.logout;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.principal.AbstractWebApplicationService;
import org.apereo.cas.authentication.principal.WebApplicationServiceFactory;
import org.apereo.cas.services.AbstractRegisteredService;
import org.apereo.cas.services.RegexMatchingRegisteredServiceProxyPolicy;
import org.apereo.cas.services.RegexRegisteredService;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.web.SimpleUrlValidatorFactoryBean;
import org.apereo.cas.web.UrlValidator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.springframework.mock.web.MockHttpServletRequest;

import java.net.URL;

import static org.junit.Assert.*;

/**
 * This is {@link DefaultSingleLogoutServiceLogoutUrlBuilderTests}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@RunWith(JUnit4.class)
@Slf4j
public class DefaultSingleLogoutServiceLogoutUrlBuilderTests {

    @Test
    public void verifyLogoutUrlByService() throws Exception {
        final AbstractRegisteredService svc = getRegisteredService("https://www.google.com");
        svc.setLogoutUrl(new URL("http://www.example.com/logout"));
        final DefaultSingleLogoutServiceLogoutUrlBuilder builder = createDefaultSingleLogoutServiceLogoutUrlBuilder(false);
        final URL url = builder.determineLogoutUrl(svc, getService("https://www.google.com"));
        assertEquals(url, svc.getLogoutUrl());
    }

    @Test
    public void verifyLogoutUrlByDefault() throws Exception {
        final AbstractRegisteredService svc = getRegisteredService(".+");
        svc.setLogoutUrl(null);
        final DefaultSingleLogoutServiceLogoutUrlBuilder builder = createDefaultSingleLogoutServiceLogoutUrlBuilder(false);
        final URL url = builder.determineLogoutUrl(svc, getService("https://www.somewhere.com/logout?p=v"));
        assertEquals(url, new URL("https://www.somewhere.com/logout?p=v"));
    }

    @Test
    public void verifyLogoutUrlUnknownUrlProtocol() throws Exception {
        final AbstractRegisteredService svc = getRegisteredService(".+");
        svc.setLogoutUrl(null);
        final DefaultSingleLogoutServiceLogoutUrlBuilder builder = createDefaultSingleLogoutServiceLogoutUrlBuilder(false);
        final URL url = builder.determineLogoutUrl(svc, getService("imaps://etc.example.org"));
        assertNull(url);
    }

    @Test
    public void verifyLocalLogoutUrlWithLocalUrlNotAllowed() throws Exception {
        final AbstractRegisteredService svc = getRegisteredService(".+");
        svc.setLogoutUrl(null);
        final DefaultSingleLogoutServiceLogoutUrlBuilder builder = createDefaultSingleLogoutServiceLogoutUrlBuilder(false);
        final URL url = builder.determineLogoutUrl(svc, getService("https://localhost/logout?p=v"));
        assertNull(url);
    }

    @Test
    public void verifyLocalLogoutUrlWithLocalUrlAllowed() throws Exception {
        final AbstractRegisteredService svc = getRegisteredService(".+");
        svc.setLogoutUrl(null);
        final DefaultSingleLogoutServiceLogoutUrlBuilder builder = createDefaultSingleLogoutServiceLogoutUrlBuilder(true);
        final URL url = builder.determineLogoutUrl(svc, getService("https://localhost/logout?p=v"));
        assertEquals(url, new URL("https://localhost/logout?p=v"));
    }

    @Test
    public void verifyLocalLogoutUrlWithValidRegExValidationAndLocalUrlNotAllowed() throws Exception {
        final AbstractRegisteredService svc = getRegisteredService(".+");
        svc.setLogoutUrl(null);
        final DefaultSingleLogoutServiceLogoutUrlBuilder builder = createDefaultSingleLogoutServiceLogoutUrlBuilder(false, "\\w*", true);
        final URL url = builder.determineLogoutUrl(svc, getService("https://localhost/logout?p=v"));
        assertEquals(url, new URL("https://localhost/logout?p=v"));
    }

    @Test
    public void verifyLocalLogoutUrlWithInvalidRegExValidationAndLocalUrlAllowed() throws Exception {
        final AbstractRegisteredService svc = getRegisteredService(".+");
        svc.setLogoutUrl(null);
        final DefaultSingleLogoutServiceLogoutUrlBuilder builder = createDefaultSingleLogoutServiceLogoutUrlBuilder(true, "\\d*", true);
        final URL url = builder.determineLogoutUrl(svc, getService("https://localhost/logout?p=v"));
        assertEquals(url, new URL("https://localhost/logout?p=v"));
    }

    @Test
    public void verifyLocalLogoutUrlWithInvalidRegExValidationAndLocalUrlNotAllowed() throws Exception {
        final AbstractRegisteredService svc = getRegisteredService(".+");
        svc.setLogoutUrl(null);
        final DefaultSingleLogoutServiceLogoutUrlBuilder builder = createDefaultSingleLogoutServiceLogoutUrlBuilder(false, "\\d*", true);
        final URL url = builder.determineLogoutUrl(svc, getService("https://localhost/logout?p=v"));
        assertNull(url);
    }

    private DefaultSingleLogoutServiceLogoutUrlBuilder createDefaultSingleLogoutServiceLogoutUrlBuilder(final boolean allowLocalLogoutUrls) throws Exception {
        return createDefaultSingleLogoutServiceLogoutUrlBuilder(allowLocalLogoutUrls, null, true);
    }

    private DefaultSingleLogoutServiceLogoutUrlBuilder createDefaultSingleLogoutServiceLogoutUrlBuilder(final boolean allowLocalLogoutUrls,
                                                                                                        final String authorityValidationRegEx,
                                                                                                        final boolean authorityValidationRegExCaseSensitive) {
        final UrlValidator validator = new SimpleUrlValidatorFactoryBean(allowLocalLogoutUrls, authorityValidationRegEx,
            authorityValidationRegExCaseSensitive).getObject();
        return new DefaultSingleLogoutServiceLogoutUrlBuilder(validator);
    }

    @SneakyThrows
    public static AbstractRegisteredService getRegisteredService(final String id) {
        final RegexRegisteredService s = new RegexRegisteredService();
        s.setServiceId(id);
        s.setName("Test service " + id);
        s.setDescription("Registered service description");
        s.setProxyPolicy(new RegexMatchingRegisteredServiceProxyPolicy("^https?://.+"));
        s.setId(RandomUtils.getInstanceNative().nextInt(Math.abs(s.hashCode())));
        return s;
    }

    public static AbstractWebApplicationService getService(final String url) {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("service", url);
        return (AbstractWebApplicationService) new WebApplicationServiceFactory().createService(request);
    }
}
