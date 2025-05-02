package org.apereo.cas.authentication;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.attribute.StubPersonAttributeDao;
import org.apereo.cas.authentication.credential.HttpBasedServiceCredential;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.handler.support.SimpleTestUsernamePasswordAuthenticationHandler;
import org.apereo.cas.authentication.metadata.BasicCredentialMetadata;
import org.apereo.cas.authentication.principal.DefaultPrincipalElectionStrategy;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.authentication.principal.resolvers.EchoingPrincipalResolver;
import org.apereo.cas.multitenancy.DefaultTenantsManager;
import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.services.CasModelRegisteredService;
import org.apereo.cas.services.RegisteredServiceAccessStrategy;
import org.apereo.cas.services.RegisteredServiceAuthenticationPolicy;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.function.FunctionUtils;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.springframework.context.support.StaticApplicationContext;
import java.net.URI;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
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
        usernamePasswordCredentials.assignPassword(password);
        return usernamePasswordCredentials;
    }

    public static HttpBasedServiceCredential getHttpBasedServiceCredentials() {
        return getHttpBasedServiceCredentials(CONST_GOOD_URL);
    }

    public static HttpBasedServiceCredential getHttpBasedServiceCredentials(final String url) {
        try {
            return new HttpBasedServiceCredential(new URI(url).toURL(), getRegisteredService(url));
        } catch (final Exception e) {
            throw new IllegalArgumentException();
        }
    }

    public static Service getService(final String id) {
        val svc = mock(Service.class);
        lenient().when(svc.getId()).thenReturn(id);
        lenient().when(svc.getAttributes()).thenReturn(new HashMap<>());
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
        lenient().when(svc.getId()).thenReturn(id);
        lenient().when(svc.getOriginalUrl()).thenReturn(id);
        lenient().when(svc.getSource()).thenReturn(CasProtocolConstants.PARAMETER_SERVICE);
        return svc;
    }

    public static StubPersonAttributeDao getAttributeRepository() {
        val attributes = new HashMap<String, List<Object>>();
        attributes.put("uid", CollectionUtils.wrap(CONST_USERNAME));
        attributes.put("cn", CollectionUtils.wrap(CONST_USERNAME.toUpperCase(Locale.ENGLISH)));
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
        return FunctionUtils.doUnchecked(() -> PrincipalFactoryUtils.newPrincipalFactory().createPrincipal(name, attributes));
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
        return getAuthentication(getPrincipal(name), new HashMap<>(), authnDate);
    }

    public static Authentication getAuthentication(final Principal principal) {
        return getAuthentication(principal, new HashMap<>());
    }

    public static Authentication getAuthentication(final Principal principal, final Map<String, List<Object>> attributes) {
        return getAuthentication(principal, attributes, null);
    }

    public static Authentication getAuthentication(final Map<String, List<Object>> authnAttributes){
        return getAuthentication(getPrincipal(CONST_USERNAME), authnAttributes, null);
    }

    public static Authentication getAuthentication(final Principal principal, final Map<String, List<Object>> attributes, final ZonedDateTime authnDate) {
        val handler = new SimpleTestUsernamePasswordAuthenticationHandler();
        val credential = new UsernamePasswordCredential("casuser", UUID.randomUUID().toString());
        credential.setCredentialMetadata(new BasicCredentialMetadata(credential));
        return new DefaultAuthenticationBuilder(principal)
            .addCredential(credential)
            .setAuthenticationDate(authnDate)
            .addSuccess(handler.getName(), new DefaultAuthenticationHandlerExecutionResult(handler, credential))
            .setAttributes(attributes)
            .build();
    }

    public static CasModelRegisteredService getRegisteredService() {
        return getRegisteredService(CONST_TEST_URL);
    }

    public static CasModelRegisteredService getRegisteredService(final String name, final String url) {
        val service = mock(CasModelRegisteredService.class);
        lenient().when(service.getFriendlyName()).thenCallRealMethod();
        lenient().when(service.getServiceId()).thenReturn(url);
        lenient().when(service.getName()).thenReturn(name);
        lenient().when(service.getId()).thenReturn(Long.MAX_VALUE);
        lenient().when(service.getDescription()).thenReturn("service description");

        val access = mock(RegisteredServiceAccessStrategy.class);
        lenient().when(access.isServiceAccessAllowed(any(), any())).thenReturn(true);
        lenient().when(access.isServiceAccessAllowedForSso(any())).thenReturn(true);
        lenient().when(service.getAccessStrategy()).thenReturn(access);

        val authnPolicy = mock(RegisteredServiceAuthenticationPolicy.class);
        lenient().when(authnPolicy.getRequiredAuthenticationHandlers()).thenReturn(Set.of());
        lenient().when(service.getAuthenticationPolicy()).thenReturn(authnPolicy);
        return service;
    }

    public static CasModelRegisteredService getRegisteredService(final String url) {
        return getRegisteredService("service name", url);
    }

    public static AuthenticationResult getAuthenticationResult(final AuthenticationSystemSupport support,
                                                               final Service service) {
        return getAuthenticationResult(support, service, getCredentialsWithSameUsernameAndPassword());
    }

    public static AuthenticationResult getAuthenticationResult(final AuthenticationSystemSupport support) {
        return getAuthenticationResult(support, getWebApplicationService(), getCredentialsWithSameUsernameAndPassword());
    }

    public static AuthenticationResult getAuthenticationResult(final AuthenticationSystemSupport support,
                                                               final Credential... credentials) {
        return getAuthenticationResult(support, getWebApplicationService(), credentials);
    }

    public static AuthenticationResult getAuthenticationResult(final AuthenticationSystemSupport support, final Service service,
                                                               final Credential... credentials) {
        return FunctionUtils.doUnchecked(() -> support.finalizeAuthenticationTransaction(service, credentials));
    }

    public static AuthenticationResult getAuthenticationResult() {
        return getAuthenticationResult(getWebApplicationService(), getAuthentication());
    }

    public static AuthenticationResult getAuthenticationResult(final Service service) {
        return getAuthenticationResult(service, getAuthentication());
    }

    public static AuthenticationResult getAuthenticationResult(final Authentication authentication) throws AuthenticationException {
        return getAuthenticationResult(getWebApplicationService(), authentication);
    }

    public static AuthenticationResult getAuthenticationResult(final Service service, final Authentication authentication) throws AuthenticationException {
        val result = mock(AuthenticationResult.class);
        when(result.getAuthentication()).thenReturn(authentication);
        when(result.getService()).thenReturn(service);
        return result;
    }

    public static AuthenticationBuilder getAuthenticationBuilder() {
        return getAuthenticationBuilder(getPrincipal());
    }

    public static AuthenticationBuilder getAuthenticationBuilder(final Principal principal) {
        val credential = new UsernamePasswordCredential();
        val handler = new SimpleTestUsernamePasswordAuthenticationHandler();
        return new DefaultAuthenticationBuilder(principal)
            .addCredential(credential)
            .addSuccess(handler.getName(), new DefaultAuthenticationHandlerExecutionResult(handler, credential));
    }

    public static AuthenticationBuilder getAuthenticationBuilder(final Principal principal,
                                                                 final Map<Credential, ? extends AuthenticationHandler> handlers,
                                                                 final Map<String, List<Object>> attributes) {
        val builder = new DefaultAuthenticationBuilder(principal).setAttributes(attributes);
        handlers.forEach((credential, handler) -> {
            builder.addSuccess(handler.getName(), new DefaultAuthenticationHandlerExecutionResult(handler, credential));
            builder.addCredential(credential);
        });
        return builder;
    }

    public static AuthenticationSystemSupport getAuthenticationSystemSupport() {
        return getAuthenticationSystemSupport(mock(AuthenticationManager.class), mock(ServicesManager.class));
    }

    public static AuthenticationSystemSupport getAuthenticationSystemSupport(final AuthenticationManager authenticationManager,
                                                                             final ServicesManager servicesManager) {
        val staticApplicationContext = new StaticApplicationContext();
        staticApplicationContext.refresh();
        
        val principalElectionStrategy = new DefaultPrincipalElectionStrategy();
        val tenantsManager = new DefaultTenantsManager();
        return new DefaultAuthenticationSystemSupport(
            new DefaultAuthenticationTransactionManager(staticApplicationContext, authenticationManager),
            principalElectionStrategy,
            new DefaultAuthenticationResultBuilderFactory(principalElectionStrategy),
            getAuthenticationTransactionFactory(servicesManager),
            servicesManager,
            new EchoingPrincipalResolver(),
            PrincipalFactoryUtils.newPrincipalFactory(),
            mock(TenantExtractor.class), tenantsManager);
    }

    public static AuthenticationTransactionFactory getAuthenticationTransactionFactory(final ServicesManager servicesManager) {
        return new DefaultAuthenticationTransactionFactory(servicesManager);
    }

    public static AuthenticationTransactionFactory getAuthenticationTransactionFactory() {
        return new DefaultAuthenticationTransactionFactory(mock(ServicesManager.class));
    }
}
