package org.apereo.cas.support.rest;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.AuthenticationManager;
import org.apereo.cas.authentication.AuthenticationTransaction;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.DefaultAuthenticationSystemSupport;
import org.apereo.cas.authentication.DefaultAuthenticationTransactionManager;
import org.apereo.cas.authentication.DefaultPrincipalElectionStrategy;
import org.apereo.cas.authentication.principal.WebApplicationServiceFactory;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.util.DefaultRegisteredServiceJsonSerializer;
import org.apereo.cas.util.EncodingUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.StringWriter;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Unit tests for {@link RegisteredServiceResource}.
 *
 * @author Dmitriy Kopylenko
 * @since 4.0.0
 */
@RunWith(MockitoJUnitRunner.Silent.class)
@Slf4j
public class RegisteredServiceResourceTests {

    @Mock
    private ServicesManager servicesManager;

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

    private MockMvc configureMockMvcFor(final RegisteredServiceResource registeredServiceResource) {
        final DefaultRegisteredServiceJsonSerializer sz = new DefaultRegisteredServiceJsonSerializer();
        final MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter(sz.getObjectMapper());
        return MockMvcBuilders.standaloneSetup(registeredServiceResource)
            .defaultRequest(get("/")
                .contextPath("/cas")
                .contentType(MediaType.APPLICATION_JSON))
            .setMessageConverters(converter)
            .build();
    }

    private RegisteredServiceResource getRegisteredServiceResource(final String attrName, final String attrValue) {
        final AuthenticationManager mgmr = mock(AuthenticationManager.class);
        when(mgmr.authenticate(argThat(new AuthenticationCredentialMatcher("test")))).thenReturn(CoreAuthenticationTestUtils.getAuthentication());
        when(mgmr.authenticate(argThat(new AuthenticationCredentialMatcher("testfail")))).thenThrow(AuthenticationException.class);
        
        return new RegisteredServiceResource(new DefaultAuthenticationSystemSupport(
            new DefaultAuthenticationTransactionManager(mgmr),
            new DefaultPrincipalElectionStrategy()),
            new WebApplicationServiceFactory(), servicesManager,
            attrName, attrValue);
    }

    private void runTest(final String attrName, final String attrValue, final String credentials, final ResultMatcher result) throws Exception {
        final RegisteredServiceResource registeredServiceResource = getRegisteredServiceResource(attrName, attrValue);
        final RegisteredService service = RegisteredServiceTestUtils.getRegisteredService();
        final DefaultRegisteredServiceJsonSerializer sz = new DefaultRegisteredServiceJsonSerializer();
        try (StringWriter writer = new StringWriter()) {
            sz.to(writer, service);
            configureMockMvcFor(registeredServiceResource)
                .perform(post("/cas/v1/services")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Basic " + EncodingUtils.encodeBase64(credentials))
                    .content(writer.toString()))
                .andExpect(result);
        }
    }

    private static class AuthenticationCredentialMatcher implements ArgumentMatcher<AuthenticationTransaction> {
        private String id;

        AuthenticationCredentialMatcher(final String id) {
            this.id = id;
        }

        @Override
        public boolean matches(final AuthenticationTransaction t) {
            return t != null && t.getCredential().getId().equalsIgnoreCase(this.id);
        }
    }
}
