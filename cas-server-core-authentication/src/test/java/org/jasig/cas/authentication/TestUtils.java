package org.jasig.cas.authentication;

import com.google.common.collect.ImmutableList;
import org.jasig.cas.authentication.handler.support.SimpleTestUsernamePasswordAuthenticationHandler;
import org.jasig.cas.authentication.principal.DefaultPrincipalFactory;
import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.services.RegisteredService;
import org.jasig.services.persondir.IPersonAttributeDao;
import org.jasig.services.persondir.support.StubPersonAttributeDao;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;

/**
 * @author Scott Battaglia
 * @since 3.0.0.2
 */
public final class TestUtils {

    public static final String CONST_USERNAME = "test";

    public static final String CONST_TEST_URL = "https://google.com";

    public static final String CONST_TEST_URL2 = "https://example.com";

    public static final String CONST_GOOD_URL = "https://github.com/";

    private static final String CONST_PASSWORD = "test1";

    private TestUtils() {
        // do not instantiate
    }

    public static UsernamePasswordCredential getCredentialsWithSameUsernameAndPassword() {
        return getCredentialsWithSameUsernameAndPassword(CONST_USERNAME);
    }

    public static UsernamePasswordCredential getCredentialsWithSameUsernameAndPassword(
            final String username) {
        return getCredentialsWithDifferentUsernameAndPassword(username,
                username);
    }

    public static UsernamePasswordCredential getCredentialsWithDifferentUsernameAndPassword() {
        return getCredentialsWithDifferentUsernameAndPassword(CONST_USERNAME,
                CONST_PASSWORD);
    }

    public static UsernamePasswordCredential getCredentialsWithDifferentUsernameAndPassword(
            final String username, final String password) {
        final UsernamePasswordCredential usernamePasswordCredentials = new UsernamePasswordCredential();
        usernamePasswordCredentials.setUsername(username);
        usernamePasswordCredentials.setPassword(password);

        return usernamePasswordCredentials;
    }

    public static HttpBasedServiceCredential getHttpBasedServiceCredentials() {
        return getHttpBasedServiceCredentials(CONST_GOOD_URL);
    }

    public static HttpBasedServiceCredential getHttpBasedServiceCredentials(
            final String url) {
        try {
            return new HttpBasedServiceCredential(new URL(url),
                    TestUtils.getRegisteredService(url));
        } catch (final MalformedURLException e) {
            throw new IllegalArgumentException();
        }
    }

    public static Service getService(final String id) {
        final Service svc = mock(Service.class);
        when(svc.getId()).thenReturn(id);
        when(svc.matches(any(Service.class))).thenReturn(true);
        return svc;
    }

    public static Service getService() {
        return getService(CONST_TEST_URL);
    }

    public static IPersonAttributeDao getAttributeRepository() {
        final Map<String, List<Object>>  attributes = new HashMap<>();
        attributes.put("uid", (List) ImmutableList.of(CONST_USERNAME));
        attributes.put("cn", (List) ImmutableList.of(CONST_USERNAME.toUpperCase()));
        attributes.put("givenName", (List) ImmutableList.of(CONST_USERNAME));
        attributes.put("memberOf", (List) ImmutableList.of("system", "admin", "cas"));
        return new StubPersonAttributeDao(attributes);
    }

    public static Principal getPrincipal() {
        return getPrincipal(CONST_USERNAME);
    }

    public static Principal getPrincipal(final String name) {
        return getPrincipal(name, Collections.EMPTY_MAP);
    }

    public static Principal getPrincipal(final String name, final Map<String, Object> attributes) {
        return new DefaultPrincipalFactory().createPrincipal(name, attributes);
    }

    public static Authentication getAuthentication() {
        return getAuthentication(CONST_USERNAME);
    }

    public static Authentication getAuthentication(final String name) {
        return getAuthentication(getPrincipal(name));
    }

    public static Authentication getAuthentication(final Principal principal) {
        return getAuthentication(principal, Collections.<String, Object>emptyMap());
    }

    public static Authentication getAuthentication(final Principal principal, final Map<String, Object> attributes) {
        final AuthenticationHandler handler = new SimpleTestUsernamePasswordAuthenticationHandler();
        final CredentialMetaData meta = new BasicCredentialMetaData(new UsernamePasswordCredential());
        return new DefaultAuthenticationBuilder(principal)
                .addCredential(meta)
                .addSuccess("testHandler", new DefaultHandlerResult(handler, meta))
                .setAttributes(attributes)
                .build();
    }

    public static RegisteredService getRegisteredService() {
        return getRegisteredService(CONST_TEST_URL);
    }

    public static RegisteredService getRegisteredService(final String url) {
        final RegisteredService service = mock(RegisteredService.class);
        when(service.getServiceId()).thenReturn(url);
        when(service.getName()).thenReturn("service name");
        when(service.getId()).thenReturn(Long.MAX_VALUE);
        when(service.getDescription()).thenReturn("service description");
        return service;
    }

    public static AuthenticationContext getAuthenticationContext(final AuthenticationSystemSupport support, final Service service)
            throws AuthenticationException {
        return getAuthenticationContext(support, service, getCredentialsWithSameUsernameAndPassword());
    }

    public static AuthenticationContext getAuthenticationContext(final AuthenticationSystemSupport support)
            throws AuthenticationException {
        return getAuthenticationContext(support, getService(), getCredentialsWithSameUsernameAndPassword());
    }

    public static AuthenticationContext getAuthenticationContext(final AuthenticationSystemSupport support,
                                                                 final Credential... credentials)
            throws AuthenticationException {
        return getAuthenticationContext(support, getService(), credentials);
    }

    public static AuthenticationContext getAuthenticationContext(final AuthenticationSystemSupport support,
                                                                 final Service service,
                                                                 final Credential... credentials)
            throws AuthenticationException {
        final AuthenticationContextBuilder builder = new DefaultAuthenticationContextBuilder(
                support.getPrincipalElectionStrategy());
        final AuthenticationTransaction transaction = AuthenticationTransaction.wrap(credentials);
        support.getAuthenticationTransactionManager()
                .handle(transaction,  builder);
        final AuthenticationContext ctx = builder.build(service);
        return ctx;
    }
}
