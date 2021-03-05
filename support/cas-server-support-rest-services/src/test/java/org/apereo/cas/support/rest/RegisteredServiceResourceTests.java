package org.apereo.cas.support.rest;

import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.AuthenticationManager;
import org.apereo.cas.authentication.AuthenticationTransaction;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.DefaultAuthenticationResultBuilderFactory;
import org.apereo.cas.authentication.DefaultAuthenticationSystemSupport;
import org.apereo.cas.authentication.DefaultAuthenticationTransactionFactory;
import org.apereo.cas.authentication.DefaultAuthenticationTransactionManager;
import org.apereo.cas.authentication.principal.DefaultPrincipalElectionStrategy;
import org.apereo.cas.authentication.principal.WebApplicationServiceFactory;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.util.RegisteredServiceJsonSerializer;
import org.apereo.cas.util.EncodingUtils;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
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
public class RegisteredServiceResourceTests {

    @Mock
    private ServicesManager servicesManager;

    @Test
    public void checkNoCredentials() throws Exception {
        runTest("memberOf", "something", StringUtils.EMPTY, status().isBadRequest());
        runTest("memberOf", "something", ":", status().isBadRequest());
    }

    @Test
    public void checkRegisteredServiceNotAuthorized() throws Exception {
        runTest("memberOf", "something", "test:test", status().isForbidden());
    }

    @Test
    public void checkRegisteredServiceNormal() throws Exception {
        runTest("memberOf", "admin", "test:test", status().isOk());
    }

    @Test
    public void checkRegisteredServiceNoAuthn() throws Exception {
        runTest("memberOf", "something", "testfail:something", status().isUnauthorized());
    }

    @Test
    public void checkRegisteredServiceNoAttributeValue() throws Exception {
        runTest("memberOf", null, "test:test", status().isForbidden());
    }

    @Test
    public void checkRegisteredServiceNoAttribute() throws Exception {
        runTest(null, null, "test:test", status().isForbidden());
    }

    private static MockMvc configureMockMvcFor(final RegisteredServiceResource registeredServiceResource) {
        val sz = new RegisteredServiceJsonSerializer();
        val converter = new MappingJackson2HttpMessageConverter(sz.getObjectMapper());
        return MockMvcBuilders.standaloneSetup(registeredServiceResource)
            .defaultRequest(get("/")
                .contextPath("/cas")
                .contentType(MediaType.APPLICATION_JSON))
            .setMessageConverters(converter)
            .build();
    }

    private RegisteredServiceResource getRegisteredServiceResource(final String attrName, final String attrValue) {
        val mgmr = mock(AuthenticationManager.class);
        lenient().when(mgmr.authenticate(argThat(new AuthenticationCredentialMatcher("test"))))
            .thenReturn(CoreAuthenticationTestUtils.getAuthentication());
        lenient().when(mgmr.authenticate(argThat(new AuthenticationCredentialMatcher("testfail"))))
            .thenThrow(AuthenticationException.class);

        val publisher = mock(ApplicationEventPublisher.class);
        return new RegisteredServiceResource(new DefaultAuthenticationSystemSupport(
            new DefaultAuthenticationTransactionManager(publisher, mgmr),
            new DefaultPrincipalElectionStrategy(), new DefaultAuthenticationResultBuilderFactory(),
            new DefaultAuthenticationTransactionFactory()),
            new WebApplicationServiceFactory(), servicesManager,
            attrName, attrValue);
    }

    private void runTest(final String attrName, final String attrValue, final String credentials,
                         final ResultMatcher result) throws Exception {
        val registeredServiceResource = getRegisteredServiceResource(attrName, attrValue);
        val service = RegisteredServiceTestUtils.getRegisteredService();
        val sz = new RegisteredServiceJsonSerializer();
        try (val writer = new StringWriter()) {
            sz.to(writer, service);
            configureMockMvcFor(registeredServiceResource)
                .perform(post("/cas/v1/services")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Basic " + EncodingUtils.encodeBase64(credentials))
                    .content(writer.toString()))
                .andExpect(result);
        }
    }

    @RequiredArgsConstructor
    private static class AuthenticationCredentialMatcher implements ArgumentMatcher<AuthenticationTransaction> {
        private final String id;

        @Override
        public boolean matches(final AuthenticationTransaction t) {
            return t != null && t.getPrimaryCredential().get().getId().equalsIgnoreCase(this.id);
        }
    }
}
