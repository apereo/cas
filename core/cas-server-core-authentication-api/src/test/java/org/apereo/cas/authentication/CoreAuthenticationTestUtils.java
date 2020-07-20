package org.apereo.cas.authentication;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.credential.HttpBasedServiceCredential;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.handler.support.SimpleTestUsernamePasswordAuthenticationHandler;
import org.apereo.cas.authentication.metadata.BasicCredentialMetaData;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceAccessStrategy;
import org.apereo.cas.services.RegisteredServiceAuthenticationPolicy;
import org.apereo.cas.util.CollectionUtils;

import lombok.experimental.UtilityClass;
import lombok.val;
import org.apereo.services.persondir.support.StubPersonAttributeDao;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.mockito.Mockito.*;

/**
 * @author Scott Battaglia
 * @since 3.0.0.2
 */
@UtilityClass
public class CoreAuthenticationTestUtils {

    public static final String CONST_USERNAME = "test";

    public static final String CONST_TEST_URL = "https://google.com";

    public static final String CONST_GOOD_URL = "https://github.com/";

    private static final String CONST_PASSWORD = "test1";

    public static UsernamePasswordCredential getCredentialsWithSameUsernameAndPassword() {
        return getCredentialsWithSameUsernameAndPassword(CONST_USERNAME);
    }

    public static UsernamePasswordCredential getCredentialsWithSameUsernameAndPassword(final String username) {
        return getCredentialsWithDifferentUsernameAndPassword(username, username);
    }

    public static UsernamePasswordCredential getCredentialsWithDifferentUsernameAndPassword() {
        return getCredentialsWithDifferentUsernameAndPassword(CONST_USERNAME, CONST_PASSWORD);
    }

    public static UsernamePasswordCredential getCredentialsWithDifferentUsernameAndPassword(final String username, final String password) {
        val usernamePasswordCredentials = new UsernamePasswordCredential();
        usernamePasswordCredentials.setUsername(username);
        usernamePasswordCredentials.setPassword(password);

        return usernamePasswordCredentials;
    }

    public static HttpBasedServiceCredential getHttpBasedServiceCredentials() {
        return getHttpBasedServiceCredentials(CONST_GOOD_URL);
    }

    public static HttpBasedServiceCredential getHttpBasedServiceCredentials(final String url) {
        try {
            return new HttpBasedServiceCredential(new URL(url), getRegisteredService(url));
        } catch (final MalformedURLException e) {
            throw new IllegalArgumentException();
        }
    }

    public static Service getService(final String id) {
        val svc = mock(Service.class);
        lenient().when(svc.getId()).thenReturn(id);
        return svc;
    }

    public static Service getService() {
        return getService(CONST_TEST_URL);
    }

    public static WebApplicationService getWebApplicationService() {
        return getWebApplicationService("https://github.com/apereo/cas");
    }

    public static WebApplicationService getWebApplicationService(final String id) {
        val svc = mock(WebApplicationService.class);
        when(svc.getId()).thenReturn(id);
        when(svc.getOriginalUrl()).thenReturn(id);
        when(svc.getSource()).thenReturn(CasProtocolConstants.PARAMETER_SERVICE);
        return svc;
    }

    public static StubPersonAttributeDao getAttributeRepository() {
        val attributes = new HashMap<String, List<Object>>();
        attributes.put("uid", CollectionUtils.wrap(CONST_USERNAME));
        attributes.put("cn", CollectionUtils.wrap(CONST_USERNAME.toUpperCase()));
        attributes.put("givenName", CollectionUtils.wrap(CONST_USERNAME));
        attributes.put("mail", CollectionUtils.wrap(CONST_USERNAME + "@example.org"));
        attributes.put("memberOf", CollectionUtils.wrapList("system", "admin", "cas", "staff"));
        return new StubPersonAttributeDao(attributes);
    }

    public static Map getAttributes() {
        return getAttributeRepository().getBackingMap();
    }

    public static Principal getPrincipal() {
        return getPrincipal(CONST_USERNAME);
    }

    public static Principal getPrincipal(final Map<String, List<Object>> attributes) {
        return getPrincipal(CONST_USERNAME, attributes);
    }

    public static Principal getPrincipal(final String name) {
        val backingMap = getAttributeRepository().getBackingMap();
        return getPrincipal(name, backingMap);
    }

    public static Principal getPrincipal(final String name, final Map<String, List<Object>> attributes) {
        return PrincipalFactoryUtils.newPrincipalFactory().createPrincipal(name, attributes);
    }

    public static Authentication getAuthentication() {
        return getAuthentication(CONST_USERNAME);
    }

    public static Authentication getAuthentication(final String name) {
        return getAuthentication(getPrincipal(name));
    }

    public static Authentication getAuthentication(final String name, final Map<String, List<Object>> attributes) {
        return getAuthentication(getPrincipal(name), attributes, null);
    }

    public static Authentication getAuthentication(final String name, final ZonedDateTime authnDate) {
        return getAuthentication(getPrincipal(name), new HashMap<>(0), authnDate);
    }

    public static Authentication getAuthentication(final Principal principal) {
        return getAuthentication(principal, new HashMap<>(0));
    }

    public static Authentication getAuthentication(final Principal principal, final Map<String, List<Object>> attributes) {
        return getAuthentication(principal, attributes, null);
    }

    public static Authentication getAuthentication(final Principal principal, final Map<String, List<Object>> attributes, final ZonedDateTime authnDate) {
        val handler = new SimpleTestUsernamePasswordAuthenticationHandler();
        val meta = new BasicCredentialMetaData(new UsernamePasswordCredential());
        return new DefaultAuthenticationBuilder(principal)
            .addCredential(meta)
            .setAuthenticationDate(authnDate)
            .addSuccess(handler.getName(), new DefaultAuthenticationHandlerExecutionResult(handler, meta))
            .setAttributes(attributes)
            .build();
    }

    public static RegisteredService getRegisteredService() {
        return getRegisteredService(CONST_TEST_URL);
    }

    public static RegisteredService getRegisteredService(final String url) {
        val service = mock(RegisteredService.class);
        when(service.getServiceId()).thenReturn(url);
        when(service.getName()).thenReturn("service name");
        when(service.getId()).thenReturn(Long.MAX_VALUE);
        when(service.getDescription()).thenReturn("service description");

        val access = mock(RegisteredServiceAccessStrategy.class);
        when(access.isServiceAccessAllowed()).thenReturn(true);
        when(service.getAccessStrategy()).thenReturn(access);

        val authnPolicy = mock(RegisteredServiceAuthenticationPolicy.class);
        when(authnPolicy.getRequiredAuthenticationHandlers()).thenReturn(Set.of());
        when(service.getAuthenticationPolicy()).thenReturn(authnPolicy);
        return service;
    }

    public static AuthenticationResult getAuthenticationResult(final AuthenticationSystemSupport support, final Service service)
        throws AuthenticationException {
        return getAuthenticationResult(support, service, getCredentialsWithSameUsernameAndPassword());
    }

    public static AuthenticationResult getAuthenticationResult(final AuthenticationSystemSupport support) throws AuthenticationException {
        return getAuthenticationResult(support, getService(), getCredentialsWithSameUsernameAndPassword());
    }

    public static AuthenticationResult getAuthenticationResult(final AuthenticationSystemSupport support, final Credential... credentials)
        throws AuthenticationException {
        return getAuthenticationResult(support, getService(), credentials);
    }

    public static AuthenticationResult getAuthenticationResult(final AuthenticationSystemSupport support, final Service service,
                                                               final Credential... credentials) throws AuthenticationException {

        return support.handleAndFinalizeSingleAuthenticationTransaction(service, credentials);
    }

    public static AuthenticationResult getAuthenticationResult() throws AuthenticationException {
        return getAuthenticationResult(getService(), getAuthentication());
    }

    public static AuthenticationResult getAuthenticationResult(final Service service) {
        return getAuthenticationResult(service, getAuthentication());
    }

    public static AuthenticationResult getAuthenticationResult(final Authentication authentication) throws AuthenticationException {
        return getAuthenticationResult(getService(), authentication);
    }

    public static AuthenticationResult getAuthenticationResult(final Service service, final Authentication authentication) throws AuthenticationException {
        val result = mock(AuthenticationResult.class);
        when(result.getAuthentication()).thenReturn(authentication);
        when(result.getService()).thenReturn(service);
        return result;
    }

    public static Principal mockPrincipal(final String attrName, final String... attrValues) {
        val attributes = (Map) Collections.singletonMap(attrName, CollectionUtils.toCollection(attrValues, ArrayList.class));
        return PrincipalFactoryUtils.newPrincipalFactory().createPrincipal("user", attributes);
    }

    public static AuthenticationBuilder getAuthenticationBuilder() {
        return getAuthenticationBuilder(getPrincipal());
    }

    public static AuthenticationBuilder getAuthenticationBuilder(final Principal principal) {
        val meta = new BasicCredentialMetaData(new UsernamePasswordCredential());
        val handler = new SimpleTestUsernamePasswordAuthenticationHandler();
        return new DefaultAuthenticationBuilder(principal)
            .addCredential(meta)
            .addSuccess(handler.getName(), new DefaultAuthenticationHandlerExecutionResult(handler, meta));
    }

    public static AuthenticationBuilder getAuthenticationBuilder(final Principal principal,
                                                                 final Map<Credential, ? extends AuthenticationHandler> handlers,
                                                                 final Map<String, List<Object>> attributes) {
        val builder = new DefaultAuthenticationBuilder(principal).setAttributes(attributes);
        handlers.forEach((credential, handler) -> {
            builder.addSuccess(handler.getName(), new DefaultAuthenticationHandlerExecutionResult(handler, new BasicCredentialMetaData(credential)));
            builder.addCredential(new BasicCredentialMetaData(credential));
        });
        return builder;
    }
}
