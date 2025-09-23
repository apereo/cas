package org.apereo.cas.support.rest;

import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.AuthenticationManager;
import org.apereo.cas.authentication.AuthenticationTransaction;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.util.RegisteredServiceJsonSerializer;
import org.apereo.cas.util.EncodingUtils;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.io.StringWriter;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for {@link RegisteredServiceResource}.
 *
 * @author Dmitriy Kopylenko
 * @since 4.0.0
 */
@ExtendWith(MockitoExtension.class)
@Tag("RegisteredService")
class RegisteredServiceResourceTests {

    @Mock
    private ServicesManager servicesManager;
    
    @Test
    void checkNoCredentials() throws Throwable {
        runTest("memberOf", "something", StringUtils.EMPTY, status().isBadRequest());
        runTest("memberOf", "something", ":", status().isUnauthorized());
    }

    @Test
    void checkRegisteredServiceNotAuthorized() throws Throwable {
        runTest("memberOf", "something", "test:test", status().isForbidden());
    }

    @Test
    void checkRegisteredServiceNormal() throws Throwable {
        runTest("memberOf", "admin", "test:test", status().isOk());
    }

    @Test
    void checkRegisteredServiceNoAuthn() throws Throwable {
        runTest("memberOf", "something", "testfail:something", status().isUnauthorized());
    }

    @Test
    void checkRegisteredServiceNoAttributeValue() throws Throwable {
        runTest("memberOf", null, "test:test", status().isForbidden());
    }

    @Test
    void checkRegisteredServiceNoAttribute() throws Throwable {
        runTest(null, null, "test:test", status().isForbidden());
    }

    private static MockMvc configureMockMvcFor(final RegisteredServiceResource registeredServiceResource) {
        val appCtx = new StaticApplicationContext();
        appCtx.refresh();
        val sz = new RegisteredServiceJsonSerializer(appCtx);
        val converter = new MappingJackson2HttpMessageConverter(sz.getObjectMapper());
        return MockMvcBuilders.standaloneSetup(registeredServiceResource)
            .defaultRequest(get("/")
                .contextPath("/cas")
                .contentType(MediaType.APPLICATION_JSON))
            .setMessageConverters(converter)
            .build();
    }

    private RegisteredServiceResource getRegisteredServiceResource(final String attrName, final String attrValue) throws Throwable {
        val mgmr = mock(AuthenticationManager.class);
        lenient().when(mgmr.authenticate(argThat(new AuthenticationCredentialMatcher("test"))))
            .thenReturn(CoreAuthenticationTestUtils.getAuthentication());
        lenient().when(mgmr.authenticate(argThat(new AuthenticationCredentialMatcher("testfail"))))
            .thenThrow(AuthenticationException.class);
        val authSystemSupport = CoreAuthenticationTestUtils.getAuthenticationSystemSupport(mgmr, servicesManager);
        return new RegisteredServiceResource(authSystemSupport,
            RegisteredServiceTestUtils.getWebApplicationServiceFactory(),
            servicesManager, attrName, attrValue);
    }

    private void runTest(final String attrName, final String attrValue, final String credentials,
                         final ResultMatcher result) throws Throwable {
        val appCtx = new StaticApplicationContext();
        appCtx.refresh();
        val registeredServiceResource = getRegisteredServiceResource(attrName, attrValue);
        val service = RegisteredServiceTestUtils.getRegisteredService();
        val sz = new RegisteredServiceJsonSerializer(appCtx);
        try (val writer = new StringWriter()) {
            sz.to(writer, service);
            configureMockMvcFor(registeredServiceResource)
                .perform(post("/cas/v1/services")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, "Basic " + EncodingUtils.encodeBase64(credentials))
                    .content(writer.toString()))
                .andExpect(result);
        }
    }

    @SuppressWarnings("UnusedVariable")
    private record AuthenticationCredentialMatcher(String id) implements ArgumentMatcher<AuthenticationTransaction> {
        @Override
        public boolean matches(final AuthenticationTransaction t) {
            return t != null && t.getPrimaryCredential().get().getId().equalsIgnoreCase(this.id);
        }
    }
}
