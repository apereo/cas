package org.apereo.cas.uma.web.controllers;

import module java.base;
import org.apereo.cas.AbstractOAuth20Tests;
import org.apereo.cas.config.CasOAuthUmaAutoConfiguration;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.uma.claim.UmaResourceSetClaimPermissionExaminer;
import org.apereo.cas.uma.discovery.UmaServerDiscoverySettings;
import org.apereo.cas.uma.ticket.resource.ResourceSetPolicy;
import org.apereo.cas.uma.ticket.resource.ResourceSetPolicyPermission;
import org.apereo.cas.uma.ticket.resource.repository.ResourceSetRepository;
import org.apereo.cas.uma.web.controllers.permission.UmaPermissionRegistrationRequest;
import org.apereo.cas.uma.web.controllers.resource.UmaResourceRegistrationRequest;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.web.SecurityLogicInterceptor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.hc.core5.http.HttpHeaders;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.core.profile.UserProfile;
import org.pac4j.jee.context.JEEContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link BaseUmaEndpointControllerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@ImportAutoConfiguration(CasOAuthUmaAutoConfiguration.class)
@TestPropertySource(properties = "cas.authn.oauth.uma.requesting-party-token.jwks-file.location=classpath:uma-keystore.jwks")
@Slf4j
@Execution(ExecutionMode.SAME_THREAD)
@SuppressWarnings("unused")
public abstract class BaseUmaEndpointControllerTests extends AbstractOAuth20Tests {
    @Autowired
    @Qualifier("umaRequestingPartyTokenSecurityInterceptor")
    protected SecurityLogicInterceptor umaRequestingPartyTokenSecurityInterceptor;

    @Autowired
    @Qualifier("umaServerDiscoverySettingsFactory")
    protected UmaServerDiscoverySettings discoverySettings;

    @Autowired
    @Qualifier("umaAuthorizationApiTokenSecurityInterceptor")
    protected SecurityLogicInterceptor umaAuthorizationApiTokenSecurityInterceptor;

    @Autowired
    @Qualifier("umaResourceSetClaimPermissionExaminer")
    protected UmaResourceSetClaimPermissionExaminer umaResourceSetClaimPermissionExaminer;

    @Autowired
    @Qualifier("umaResourceSetRepository")
    protected ResourceSetRepository umaResourceSetRepository;

    protected Triple<HttpServletRequest, HttpServletResponse, String> authenticateUmaRequestWithProtectionScope() throws Throwable {
        return authenticateUmaRequestWithScope(OAuth20Constants.UMA_PROTECTION_SCOPE, umaRequestingPartyTokenSecurityInterceptor);
    }

    protected Triple<HttpServletRequest, HttpServletResponse, String> authenticateUmaRequestWithAuthorizationScope() throws Throwable {
        return authenticateUmaRequestWithScope(OAuth20Constants.UMA_AUTHORIZATION_SCOPE, umaAuthorizationApiTokenSecurityInterceptor);
    }

    protected static UmaResourceRegistrationRequest createUmaResourceRegistrationRequest() {
        return createUmaResourceRegistrationRequest(-1);
    }

    protected static UmaResourceRegistrationRequest createUmaResourceRegistrationRequest(final long id) {
        return createUmaResourceRegistrationRequest(id, CollectionUtils.wrapList("read", "write"));
    }

    protected static UmaResourceRegistrationRequest createUmaResourceRegistrationRequest(final long id, final List<String> scopes) {
        val resRequest = new UmaResourceRegistrationRequest();
        resRequest.setUri("http://rs.example.com/alice/myresource");
        resRequest.setName("my-resource");
        resRequest.setType("my-resource-type");
        if (id >= 0) {
            resRequest.setId(id);
        }
        resRequest.setScopes(scopes);
        return resRequest;
    }

    protected static ResourceSetPolicy createUmaPolicyRegistrationRequest(final UserProfile profile) {
        return createUmaPolicyRegistrationRequest(profile, CollectionUtils.wrapHashSet("read", "write"));
    }

    protected static ResourceSetPolicy createUmaPolicyRegistrationRequest(final UserProfile profile, final Collection<String> scopes) {
        val policy = new ResourceSetPolicy();
        val perm = new ResourceSetPolicyPermission();
        perm.setScopes(new HashSet<>(scopes));
        perm.setClaims(new LinkedHashMap<>(CollectionUtils.wrap("givenName", "CAS")));
        perm.setSubject(profile.getId());
        policy.setPermissions(CollectionUtils.wrapHashSet(perm));
        return policy;
    }

    protected static UmaPermissionRegistrationRequest createUmaPermissionRegistrationRequest(final long resourceId) {
        val perm = new UmaPermissionRegistrationRequest();
        perm.setResourceId(resourceId);
        perm.setScopes(CollectionUtils.wrapList("read"));
        perm.setClaims(new LinkedHashMap<>(CollectionUtils.wrap("givenName", "CAS")));
        return perm;
    }

    /**
     * Gets current profile.
     *
     * @param request  the request
     * @param response the response
     * @return the current profile
     */
    protected UserProfile getCurrentProfile(final HttpServletRequest request, final HttpServletResponse response) {
        val ctx = new JEEContext(request, response);
        val manager = new ProfileManager(ctx, oauthDistributedSessionStore);
        val userProfileResult = manager.getProfile();
        if (userProfileResult.isEmpty()) {
            throw new IllegalStateException("Unable to determine the user profile from the context");
        }
        return userProfileResult.get();
    }

    protected MvcResult performUmaRequest(final HttpMethod method, final String path) throws Throwable {
        return performUmaRequest(method, path, null, null, new MockHttpServletRequest(), new MockHttpServletResponse());
    }

    protected MvcResult performUmaRequest(final HttpMethod method, final String path,
                                          final String body) throws Throwable {
        return performUmaRequest(method, path, body, null, new MockHttpServletRequest(), new MockHttpServletResponse());
    }

    protected MvcResult performUmaRequest(final HttpMethod method, final String path,
                                          final HttpServletRequest request,
                                          final HttpServletResponse response) throws Throwable {
        return performUmaRequest(method, path, null, null, request, response);
    }

    protected MvcResult performUmaRequest(final HttpMethod method, final String path,
                                          final String body,
                                          final HttpServletRequest request,
                                          final HttpServletResponse response) throws Throwable {
        return performUmaRequest(method, path, body, null, request, response);
    }

    protected MvcResult performUmaRequest(final HttpMethod method, final String path,
                                          final Map<String, String> parameters,
                                          final HttpServletRequest request,
                                          final HttpServletResponse response) throws Throwable {
        return performUmaRequest(method, path, null, parameters, request, response);
    }

    private MvcResult performUmaRequest(final HttpMethod method, final String path,
                                        @Nullable final String body,
                                        final Map<String, String> parameters,
                                        final HttpServletRequest request,
                                        final HttpServletResponse response) throws Throwable {
        val builder = MockMvcRequestBuilders
            .request(method, "/cas" + CONTEXT + path)
            .with(mockRequest -> {
                mockRequest.setContextPath("/cas");
                mockRequest.setScheme(CAS_SCHEME);
                mockRequest.setServerName(CAS_SERVER);
                mockRequest.setServerPort(CAS_PORT);
                return mockRequest;
            })
            .contentType(MediaType.APPLICATION_JSON_VALUE);
        if (parameters != null) {
            parameters.forEach(builder::param);
        }
        if (body != null) {
            builder.content(body.getBytes(StandardCharsets.UTF_8));
        }
        if (request != null) {
            val headerNames = request.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                val name = headerNames.nextElement();
                builder.header(name, Collections.list(request.getHeaders(name)).toArray());
            }
            if (request.getSession(false) instanceof final MockHttpSession session) {
                builder.session(session);
            }
            if (request.getCookies() != null) {
                builder.cookie(request.getCookies());
            }
        }
        if (response instanceof final MockHttpServletResponse mockResponse && mockResponse.getCookies().length > 0) {
            builder.cookie(mockResponse.getCookies());
        }
        return mockMvc.perform(builder).andReturn();
    }

    protected Map getMappedResponseBody(final MvcResult result) {
        return getModelAndView(result).getModel();
    }
    
    private Triple<HttpServletRequest, HttpServletResponse, String> authenticateUmaRequestWithScope(
        final String scope, final SecurityLogicInterceptor interceptor) throws Throwable {
        val service = addRegisteredService();
        val pair = assertClientOK(service, false, scope);
        assertNotNull(pair.getKey());
        val accessToken = pair.getKey();

        val mockRequest = new MockHttpServletRequest(HttpMethod.POST.name(), CONTEXT + OAuth20Constants.UMA_REGISTRATION_URL);
        mockRequest.addHeader(HttpHeaders.AUTHORIZATION, String.format("%s %s", OAuth20Constants.TOKEN_TYPE_BEARER, accessToken));
        mockRequest.addHeader(HttpHeaders.USER_AGENT, "MSIE");
        val mockResponse = new MockHttpServletResponse();
        interceptor.preHandle(mockRequest, mockResponse, new Object());
        return Triple.of(mockRequest, mockResponse, accessToken);
    }
}
