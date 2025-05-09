package org.apereo.cas.services;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.AcceptUsersAuthenticationHandler;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationResult;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.DefaultAuthenticationBuilder;
import org.apereo.cas.authentication.DefaultAuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.DefaultAuthenticationResultBuilder;
import org.apereo.cas.authentication.PrincipalElectionStrategy;
import org.apereo.cas.authentication.credential.HttpBasedServiceCredential;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.handler.support.SimpleTestUsernamePasswordAuthenticationHandler;
import org.apereo.cas.authentication.principal.AbstractWebApplicationService;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.WebApplicationServiceFactory;
import org.apereo.cas.authentication.principal.cache.CachingPrincipalAttributesRepository;
import org.apereo.cas.authentication.principal.merger.MultivaluedAttributeMerger;
import org.apereo.cas.configuration.model.core.authentication.PrincipalAttributesCoreProperties;
import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.services.consent.DefaultRegisteredServiceConsentPolicy;
import org.apereo.cas.services.support.RegisteredServiceRegexAttributeFilter;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.function.FunctionUtils;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.jooq.lambda.Unchecked;
import org.springframework.mock.web.MockHttpServletRequest;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import static org.mockito.Mockito.*;

/**
 * This is {@link RegisteredServiceTestUtils}.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
@UtilityClass
public class RegisteredServiceTestUtils {
    public static final String CONST_USERNAME = "test";

    public static final String CONST_TEST_URL = "https://google.com";

    public static final String CONST_TEST_URL2 = "https://example.com";

    public static final String CONST_TEST_URL3 = "https://another.example.com";

    public static HttpBasedServiceCredential getHttpBasedServiceCredentials() {
        return getHttpBasedServiceCredentials(CONST_TEST_URL);
    }

    public static HttpBasedServiceCredential getHttpBasedServiceCredentials(final String url) {
        try {
            val service = (CasModelRegisteredService) RegisteredServiceTestUtils.getRegisteredService(url);
            return new HttpBasedServiceCredential(new URI(url).toURL(), service);
        } catch (final Exception e) {
            throw new IllegalArgumentException();
        }
    }

    public static UsernamePasswordCredential getCredentialsWithSameUsernameAndPassword(final String username) {
        val usernamePasswordCredentials = new UsernamePasswordCredential();
        usernamePasswordCredentials.setUsername(username);
        usernamePasswordCredentials.assignPassword(username);

        return usernamePasswordCredentials;
    }

    public static UsernamePasswordCredential getCredentialsWithDifferentUsernameAndPassword(final String username, final String password) {
        val usernamePasswordCredentials = new UsernamePasswordCredential();
        usernamePasswordCredentials.setUsername(username);
        usernamePasswordCredentials.assignPassword(password);

        return usernamePasswordCredentials;
    }

    public static AbstractWebApplicationService getService() {
        return getService(CONST_TEST_URL);
    }

    public static AbstractWebApplicationService getService(final String name) {
        val request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, name);
        return (AbstractWebApplicationService) new WebApplicationServiceFactory(mock(TenantExtractor.class)).createService(request);
    }

    public static Service getService2() {
        return getService(CONST_TEST_URL2);
    }

    public static Map<String, Set<String>> getTestAttributes() {
        return getTestAttributes("CASUser");
    }

    public static Map<String, Set<String>> getTestAttributes(final String username) {
        val attributes = new HashMap<String, Set<String>>();
        Set<String> attributeValues = new HashSet<>();
        attributeValues.add("uid");

        attributes.put("uid", attributeValues);

        attributeValues = new HashSet<>();
        attributeValues.add(username);

        attributes.put("givenName", attributeValues);

        attributeValues = new HashSet<>();
        attributeValues.add("admin");
        attributeValues.add("system");
        attributeValues.add("cas");

        attributes.put("memberOf", attributeValues);
        return attributes;
    }

    public static BaseWebBasedRegisteredService getRegisteredService() {
        return getRegisteredService(CONST_TEST_URL);
    }

    public static BaseWebBasedRegisteredService getRegisteredService(final Map requiredAttributes) {
        return getRegisteredService(CONST_TEST_URL, requiredAttributes);
    }

    public static <T extends BaseWebBasedRegisteredService> T getRegisteredService(final String id, final Class<T> clazz,
                                                                                   final boolean uniq) {
        return Unchecked.supplier(() -> getRegisteredService(id, clazz, uniq, getTestAttributes())).get();
    }

    public static <T extends BaseRegisteredService> T getRegisteredService(
        final String id, final Class<T> clazz,
        final boolean uniq, final Map requiredAttributes) throws Exception {
        val baseRegisteredService = (BaseRegisteredService) clazz.getDeclaredConstructor().newInstance();
        baseRegisteredService.setServiceId(id);
        baseRegisteredService.setEvaluationOrder(1);
        if (uniq) {
            val uuid = Iterables.get(Splitter.on('-').split(UUID.randomUUID().toString()), 0);
            baseRegisteredService.setName("TestService" + uuid);
        } else {
            baseRegisteredService.setName(id);
        }
        baseRegisteredService.setDescription("Registered service description");
        baseRegisteredService.setId(RandomUtils.nextInt());
        baseRegisteredService.setTheme("exampleTheme");
        baseRegisteredService.setUsernameAttributeProvider(new PrincipalAttributeRegisteredServiceUsernameProvider("uid"));
        val accessStrategy = new DefaultRegisteredServiceAccessStrategy(true, true);
        accessStrategy.setRequireAllAttributes(true);
        accessStrategy.setRequiredAttributes(requiredAttributes);
        accessStrategy.setUnauthorizedRedirectUrl(new URI("https://www.github.com"));
        baseRegisteredService.setAccessStrategy(accessStrategy);
        baseRegisteredService.setLogo("https://logo.example.org/logo.png");
        baseRegisteredService.setLogoutType(RegisteredServiceLogoutType.BACK_CHANNEL);
        baseRegisteredService.setLogoutUrl("https://sys.example.org/logout.png");

        if (baseRegisteredService instanceof final CasRegisteredService casRegisteredService) {
            val policy = new RegexMatchingRegisteredServiceProxyPolicy();
            policy.setPattern("^http.+");
            casRegisteredService.setProxyPolicy(policy);
        }
        baseRegisteredService.setPublicKey(new RegisteredServicePublicKeyImpl("classpath:RSA1024Public.key", "RSA"));

        val policy = new ReturnAllowedAttributeReleasePolicy();
        policy.setAuthorizedToReleaseCredentialPassword(true);
        policy.setAuthorizedToReleaseProxyGrantingTicket(true);

        val repo = new CachingPrincipalAttributesRepository(TimeUnit.SECONDS.name(), 10);
        repo.setMergingStrategy(PrincipalAttributesCoreProperties.MergingStrategyTypes.ADD);
        policy.setPrincipalAttributesRepository(repo);
        policy.setAttributeFilter(new RegisteredServiceRegexAttributeFilter("https://.+"));
        policy.setAllowedAttributes(new ArrayList<>(getTestAttributes().keySet()));
        baseRegisteredService.setAttributeReleasePolicy(policy);

        return (T) baseRegisteredService;
    }

    public static <T extends BaseWebBasedRegisteredService> T getRegisteredService(final String id, final Class<T> clazz) {
        return getRegisteredService(id, clazz, true);
    }

    public static CasRegisteredService getRegisteredService(final String id) {
        return getRegisteredService(id, CasRegisteredService.class, true);
    }

    public static CasRegisteredService getRegisteredService(final String id, final boolean uniq) {
        return getRegisteredService(id, CasRegisteredService.class, uniq);
    }

    public static CasRegisteredService getRegisteredService(final String id, final Map requiredAttributes) {
        return Unchecked.supplier(() -> getRegisteredService(id, CasRegisteredService.class, true, requiredAttributes)).get();
    }

    public static Principal getPrincipal(final Map<String, List<Object>> attributes) {
        return getPrincipal(CONST_USERNAME, attributes);
    }

    public static Principal getPrincipal() {
        return getPrincipal(CONST_USERNAME);
    }

    public static Principal getPrincipal(final String name) {
        return getPrincipal(name, new HashMap<>());
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
        return getAuthentication(getPrincipal(name), attributes);
    }

    public static Authentication getAuthentication(final Principal principal) {
        return getAuthentication(principal, new HashMap<>());
    }

    public static Authentication getAuthentication(final Principal principal, final Map<String, List<Object>> attributes) {
        val handler = new SimpleTestUsernamePasswordAuthenticationHandler();
        val credential = new UsernamePasswordCredential();
        return new DefaultAuthenticationBuilder(principal)
            .addCredential(credential)
            .addSuccess("testHandler", new DefaultAuthenticationHandlerExecutionResult(handler, credential))
            .setAttributes(attributes)
            .build();
    }

    public static Authentication getAuthentication(final Credential credential) {
        return new DefaultAuthenticationBuilder(getPrincipal(credential.getId())).addCredential(credential).build();
    }
    
    public static Authentication getAuthentication(final String principal, final AuthenticationHandler handler,
                                                   final Credential credential, final Map<String, List<Object>> attributes) {
        return new DefaultAuthenticationBuilder(getPrincipal(principal))
            .addCredential(credential)
            .addSuccess(handler.getName(), new DefaultAuthenticationHandlerExecutionResult(handler, credential))
            .setAttributes(attributes)
            .build();
    }

    public static List<RegisteredService> getRegisteredServicesForTests() throws Exception {
        val list = new ArrayList<RegisteredService>();
        val svc = RegisteredServiceTestUtils.getRegisteredService("testencryption$");
        val policy = new ReturnAllowedAttributeReleasePolicy();
        policy.setAuthorizedToReleaseCredentialPassword(true);
        policy.setAuthorizedToReleaseProxyGrantingTicket(true);
        val publicKey = new RegisteredServicePublicKeyImpl();
        publicKey.setLocation("classpath:keys/RSA1024Public.key");
        svc.setPublicKey(publicKey);
        svc.setAttributeReleasePolicy(policy);
        list.add(svc);

        val svc2 = RegisteredServiceTestUtils.getRegisteredService("testDefault");
        svc2.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy(new HashMap<>()));
        svc2.setUsernameAttributeProvider(new DefaultRegisteredServiceUsernameProvider());
        list.add(svc2);

        val svc3 = RegisteredServiceTestUtils.getRegisteredService("https://example\\.com/normal/.*");
        svc3.setEvaluationOrder(10);
        svc3.setAttributeReleasePolicy(new ReturnAllAttributeReleasePolicy());
        svc3.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy(new HashMap<>()));
        svc3.setAuthenticationPolicy(new DefaultRegisteredServiceAuthenticationPolicy()
            .setCriteria(new AnyAuthenticationHandlerRegisteredServiceAuthenticationPolicyCriteria()));
        list.add(svc3);

        val svc4 = RegisteredServiceTestUtils.getRegisteredService("https://example\\.com/high/.*");
        svc4.setEvaluationOrder(20);
        svc4.setAttributeReleasePolicy(new ReturnAllAttributeReleasePolicy());
        val handlers = CollectionUtils.wrapHashSet(AcceptUsersAuthenticationHandler.class.getSimpleName(), "TestOneTimePasswordAuthenticationHandler");
        svc4.setAuthenticationPolicy(new DefaultRegisteredServiceAuthenticationPolicy()
            .setRequiredAuthenticationHandlers(handlers)
            .setCriteria(new AnyAuthenticationHandlerRegisteredServiceAuthenticationPolicyCriteria()));
        svc4.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy(new HashMap<>()));
        list.add(svc4);

        val svc5 = RegisteredServiceTestUtils.getRegisteredService("(https://)*google.com$");
        svc5.setEvaluationOrder(1);
        svc5.setProxyPolicy(new RegexMatchingRegisteredServiceProxyPolicy().setPattern(".+"));
        svc5.setPublicKey(new RegisteredServicePublicKeyImpl("classpath:keys/RSA4096Public.key", "RSA"));
        val policy1 = new ReturnAllowedAttributeReleasePolicy();
        policy1.setAuthorizedToReleaseCredentialPassword(true);
        policy1.setAuthorizedToReleaseProxyGrantingTicket(true);
        policy1.setAllowedAttributes(CollectionUtils.wrap("binaryAttribute"));
        svc5.setAttributeReleasePolicy(policy1);
        svc5.setUsernameAttributeProvider(new DefaultRegisteredServiceUsernameProvider());
        svc5.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy(new HashMap<>()));
        list.add(svc5);

        val svc6 = RegisteredServiceTestUtils.getRegisteredService("eduPersonTest");
        svc6.setUsernameAttributeProvider(new PrincipalAttributeRegisteredServiceUsernameProvider("eduPersonAffiliation"));
        svc6.setAttributeReleasePolicy(new ReturnAllAttributeReleasePolicy());
        svc6.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy(new HashMap<>()));
        svc6.setProxyPolicy(new RefuseRegisteredServiceProxyPolicy());
        list.add(svc6);

        val svc7 = RegisteredServiceTestUtils.getRegisteredService("testencryption$");
        val policy2 = new ReturnAllowedAttributeReleasePolicy();
        policy2.setAuthorizedToReleaseCredentialPassword(true);
        policy2.setAuthorizedToReleaseProxyGrantingTicket(true);
        svc7.setAttributeReleasePolicy(policy2);
        svc7.setPublicKey(new RegisteredServicePublicKeyImpl("classpath:keys/RSA1024Public.key", "RSA"));
        svc7.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy(new HashMap<>()));
        svc7.setUsernameAttributeProvider(new DefaultRegisteredServiceUsernameProvider());
        list.add(svc7);

        val svc8 = RegisteredServiceTestUtils.getRegisteredService("^TestServiceAttributeForAuthzFails");
        svc8.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy(CollectionUtils.wrap("cn", CollectionUtils.wrapSet("cnValue"),
            "givenName", CollectionUtils.wrapSet("gnameValue"))));
        svc8.setUsernameAttributeProvider(new DefaultRegisteredServiceUsernameProvider());
        list.add(svc8);

        val svc9 = RegisteredServiceTestUtils.getRegisteredService("^TestSsoFalse");
        svc9.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy(true, false));
        svc9.setUsernameAttributeProvider(new DefaultRegisteredServiceUsernameProvider());
        list.add(svc9);

        val svc10 = RegisteredServiceTestUtils.getRegisteredService("TestServiceAttributeForAuthzPasses");
        svc10.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy(CollectionUtils.wrap("groupMembership", CollectionUtils.wrapSet("adopters"))));
        svc10.setAttributeReleasePolicy(new ReturnAllAttributeReleasePolicy());
        svc10.setUsernameAttributeProvider(new DefaultRegisteredServiceUsernameProvider());
        list.add(svc10);

        val svc11 = RegisteredServiceTestUtils.getRegisteredService("eduPersonTestInvalid");
        svc11.setUsernameAttributeProvider(new PrincipalAttributeRegisteredServiceUsernameProvider("nonExistentAttributeName"));
        svc11.setAttributeReleasePolicy(new ReturnAllowedAttributeReleasePolicy(CollectionUtils.wrap("groupMembership")));
        svc11.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy(new HashMap<>()));
        list.add(svc11);

        val svc12 = RegisteredServiceTestUtils.getRegisteredService("testAnonymous");
        svc12.setUsernameAttributeProvider(new AnonymousRegisteredServiceUsernameAttributeProvider());
        svc12.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy(new HashMap<>()));
        list.add(svc12);

        val svc13 = RegisteredServiceTestUtils.getRegisteredService("^http://www.jasig.org.+");
        svc13.setProxyPolicy(new RegexMatchingRegisteredServiceProxyPolicy().setPattern(".+"));
        svc13.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy(new HashMap<>()));
        svc13.setUsernameAttributeProvider(new DefaultRegisteredServiceUsernameProvider());
        list.add(svc13);

        val svc14 = RegisteredServiceTestUtils.getRegisteredService("usernameAttributeProviderService");
        svc14.setUsernameAttributeProvider(new PrincipalAttributeRegisteredServiceUsernameProvider("cn"));
        svc14.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy(new HashMap<>()));
        list.add(svc14);

        val svc15 = RegisteredServiceTestUtils.getRegisteredService("proxyService");
        svc15.setProxyPolicy(new RegexMatchingRegisteredServiceProxyPolicy().setPattern("^https://.+"));
        svc15.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy(new HashMap<>()));
        svc15.setUsernameAttributeProvider(new DefaultRegisteredServiceUsernameProvider());
        list.add(svc15);

        val svc16 = RegisteredServiceTestUtils.getRegisteredService("^test.*");
        svc16.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy(new HashMap<>()));
        svc16.setUsernameAttributeProvider(new DefaultRegisteredServiceUsernameProvider());
        svc16.setEvaluationOrder(1000);
        list.add(svc16);

        val svc17 = RegisteredServiceTestUtils.getRegisteredService("https://localhost.*");
        svc17.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy(new HashMap<>()));
        svc17.setUsernameAttributeProvider(new DefaultRegisteredServiceUsernameProvider());
        svc17.setEvaluationOrder(100);
        list.add(svc17);

        val svc18 = RegisteredServiceTestUtils.getRegisteredService("https://github.com/apereo/cas");
        val accessStrategy = new DefaultRegisteredServiceAccessStrategy();
        accessStrategy.setUnauthorizedRedirectUrl(new URI("https://www.github.com"));
        svc18.setAccessStrategy(accessStrategy);
        svc18.setUsernameAttributeProvider(new DefaultRegisteredServiceUsernameProvider());
        svc18.setEvaluationOrder(98);
        list.add(svc18);

        val svc19 = RegisteredServiceTestUtils.getRegisteredService("https://carmenwiki.osu.edu.*");
        svc19.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy(new HashMap<>()));
        svc19.setUsernameAttributeProvider(new DefaultRegisteredServiceUsernameProvider());
        svc19.setEvaluationOrder(99);
        list.add(svc19);

        val svc20 = RegisteredServiceTestUtils.getRegisteredService("consentService");
        svc20.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy(new HashMap<>()));
        svc20.setUsernameAttributeProvider(new DefaultRegisteredServiceUsernameProvider());
        val attrPolicy = new ReturnAllAttributeReleasePolicy();
        attrPolicy.setConsentPolicy(new DefaultRegisteredServiceConsentPolicy());
        svc20.setAttributeReleasePolicy(attrPolicy);
        svc20.setEvaluationOrder(88);
        list.add(svc20);

        val svc21 = RegisteredServiceTestUtils.getRegisteredService("jwtservice");
        svc21.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy(new HashMap<>()));
        svc21.setUsernameAttributeProvider(new DefaultRegisteredServiceUsernameProvider());
        val prop = new DefaultRegisteredServiceProperty();
        prop.setValues(CollectionUtils.wrapSet(Boolean.TRUE.toString()));
        svc21.getProperties().put(RegisteredServiceProperty.RegisteredServiceProperties.TOKEN_AS_SERVICE_TICKET.getPropertyName(), prop);
        svc21.setEvaluationOrder(2000);
        list.add(svc21);

        val svc22 = RegisteredServiceTestUtils.getRegisteredService("cas-access-disabled");
        val strategy = new DefaultRegisteredServiceAccessStrategy();
        strategy.setEnabled(false);
        strategy.setUnauthorizedRedirectUrl(new URI("https://www.github.com"));
        svc22.setAccessStrategy(strategy);
        list.add(svc22);

        val svc23 = RegisteredServiceTestUtils.getRegisteredService("cas-access-delegation");
        val strategy23 = new DefaultRegisteredServiceAccessStrategy();
        strategy23.setEnabled(true);
        val delegate = new DefaultRegisteredServiceDelegatedAuthenticationPolicy();
        delegate.setExclusive(true);
        strategy23.setDelegatedAuthenticationPolicy(delegate);
        svc23.setAccessStrategy(strategy23);
        list.add(svc23);

        val svc24 = RegisteredServiceTestUtils.getRegisteredService("https://www.casinthecloud.com");
        svc24.setProxyPolicy(new RegexMatchingRegisteredServiceProxyPolicy().setPattern(".+"));
        svc24.setPublicKey(new RegisteredServicePublicKeyImpl("classpath:keys/RSA4096Public.key", "RSA"));
        val policy24 = new ReturnAllowedAttributeReleasePolicy();
        policy24.setAuthorizedToReleaseCredentialPassword(true);
        policy24.setAuthorizedToReleaseProxyGrantingTicket(false);
        policy24.setAllowedAttributes(CollectionUtils.wrap("binaryAttribute"));
        svc24.setAttributeReleasePolicy(policy24);
        svc24.setUsernameAttributeProvider(new DefaultRegisteredServiceUsernameProvider());
        svc24.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy(new HashMap<>()));
        list.add(svc24);

        val svc25 = RegisteredServiceTestUtils.getRegisteredService("accessStrategyMapped");
        svc25.setAttributeReleasePolicy(new ReturnMappedAttributeReleasePolicy()
            .setAllowedAttributes(Map.of("sAMAccountName", "uid",
                "mail", "groovy { return attributes['sAMAccountName'][0] + '@example.org'}")));
        svc25.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy(
            Map.of("mail", Set.of(".*"))));
        list.add(svc25);

        return list;
    }

    /**
     * Gets authentication result.
     *
     * @return the authentication result
     */
    public static AuthenticationResult getAuthenticationResult(final String username) {
        return FunctionUtils.doUnchecked(() -> {
            val authentication = getAuthentication(username);
            val strategy = mock(PrincipalElectionStrategy.class);
            when(strategy.getAttributeMerger()).thenReturn(new MultivaluedAttributeMerger());
            when(strategy.nominate(anyCollection(), anyMap())).thenReturn(authentication.getPrincipal());
            return new DefaultAuthenticationResultBuilder(strategy).collect(authentication).build();
        });
    }
}
