package org.apereo.cas.uma.web.controllers;

import org.apereo.cas.config.CasOAuthUmaComponentSerializationConfiguration;
import org.apereo.cas.config.CasOAuthUmaConfiguration;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.web.AbstractOAuth20Tests;
import org.apereo.cas.uma.ticket.resource.ResourceSetPolicy;
import org.apereo.cas.uma.ticket.resource.ResourceSetPolicyPermission;
import org.apereo.cas.uma.web.controllers.authz.UmaAuthorizationRequestEndpointController;
import org.apereo.cas.uma.web.controllers.permission.UmaPermissionRegistrationEndpointController;
import org.apereo.cas.uma.web.controllers.permission.UmaPermissionRegistrationRequest;
import org.apereo.cas.uma.web.controllers.policy.UmaCreatePolicyForResourceSetEndpointController;
import org.apereo.cas.uma.web.controllers.policy.UmaDeletePolicyForResourceSetEndpointController;
import org.apereo.cas.uma.web.controllers.policy.UmaFindPolicyForResourceSetEndpointController;
import org.apereo.cas.uma.web.controllers.policy.UmaUpdatePolicyForResourceSetEndpointController;
import org.apereo.cas.uma.web.controllers.resource.UmaCreateResourceSetRegistrationEndpointController;
import org.apereo.cas.uma.web.controllers.resource.UmaDeleteResourceSetRegistrationEndpointController;
import org.apereo.cas.uma.web.controllers.resource.UmaFindResourceSetRegistrationEndpointController;
import org.apereo.cas.uma.web.controllers.resource.UmaResourceRegistrationRequest;
import org.apereo.cas.uma.web.controllers.resource.UmaUpdateResourceSetRegistrationEndpointController;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.Pac4jUtils;

import lombok.val;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.http.HttpHeaders;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.springframework.web.SecurityInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.TestPropertySource;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link BaseUmaEndpointControllerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Import({CasOAuthUmaConfiguration.class, CasOAuthUmaComponentSerializationConfiguration.class})
@TestPropertySource(properties = "cas.authn.uma.requestingPartyToken.jwksFile=classpath:uma-keystore.jwks")
public abstract class BaseUmaEndpointControllerTests extends AbstractOAuth20Tests {
    @Autowired
    @Qualifier("umaPermissionRegistrationEndpointController")
    protected UmaPermissionRegistrationEndpointController umaPermissionRegistrationEndpointController;

    @Autowired
    @Qualifier("umaCreateResourceSetRegistrationEndpointController")
    protected UmaCreateResourceSetRegistrationEndpointController umaCreateResourceSetRegistrationEndpointController;

    @Autowired
    @Qualifier("umaDeleteResourceSetRegistrationEndpointController")
    protected UmaDeleteResourceSetRegistrationEndpointController umaDeleteResourceSetRegistrationEndpointController;

    @Autowired
    @Qualifier("umaUpdateResourceSetRegistrationEndpointController")
    protected UmaUpdateResourceSetRegistrationEndpointController umaUpdateResourceSetRegistrationEndpointController;

    @Autowired
    @Qualifier("umaFindResourceSetRegistrationEndpointController")
    protected UmaFindResourceSetRegistrationEndpointController umaFindResourceSetRegistrationEndpointController;

    @Autowired
    @Qualifier("umaCreatePolicyForResourceSetEndpointController")
    protected UmaCreatePolicyForResourceSetEndpointController umaCreatePolicyForResourceSetEndpointController;

    @Autowired
    @Qualifier("umaFindPolicyForResourceSetEndpointController")
    protected UmaFindPolicyForResourceSetEndpointController umaFindPolicyForResourceSetEndpointController;

    @Autowired
    @Qualifier("umaDeletePolicyForResourceSetEndpointController")
    protected UmaDeletePolicyForResourceSetEndpointController umaDeletePolicyForResourceSetEndpointController;

    @Autowired
    @Qualifier("umaUpdatePolicyForResourceSetEndpointController")
    protected UmaUpdatePolicyForResourceSetEndpointController umaUpdatePolicyForResourceSetEndpointController;

    @Autowired
    @Qualifier("umaAuthorizationRequestEndpointController")
    protected UmaAuthorizationRequestEndpointController umaAuthorizationRequestEndpointController;

    @Autowired
    @Qualifier("umaRequestingPartyTokenSecurityInterceptor")
    protected SecurityInterceptor umaRequestingPartyTokenSecurityInterceptor;

    @Autowired
    @Qualifier("umaAuthorizationApiTokenSecurityInterceptor")
    protected SecurityInterceptor umaAuthorizationApiTokenSecurityInterceptor;

    protected Triple<HttpServletRequest, HttpServletResponse, String> authenticateUmaRequestWithProtectionScope() throws Exception {
        return authenticateUmaRequestWithScope(OAuth20Constants.UMA_PROTECTION_SCOPE, umaRequestingPartyTokenSecurityInterceptor);
    }

    protected Triple<HttpServletRequest, HttpServletResponse, String> authenticateUmaRequestWithAuthorizationScope() throws Exception {
        return authenticateUmaRequestWithScope(OAuth20Constants.UMA_AUTHORIZATION_SCOPE, umaAuthorizationApiTokenSecurityInterceptor);
    }

    private Triple<HttpServletRequest, HttpServletResponse, String> authenticateUmaRequestWithScope(
        final String scope, final SecurityInterceptor interceptor) throws Exception {
        val service = addRegisteredService();
        val pair = internalVerifyClientOK(service, false, scope);
        assertNotNull(pair.getKey());
        val accessToken = pair.getKey();

        val mockRequest = new MockHttpServletRequest(HttpMethod.POST.name(), CONTEXT + OAuth20Constants.UMA_REGISTRATION_URL);
        mockRequest.addHeader(HttpHeaders.AUTHORIZATION, String.format("%s %s", OAuth20Constants.TOKEN_TYPE_BEARER, accessToken));
        val mockResponse = new MockHttpServletResponse();
        interceptor.preHandle(mockRequest, mockResponse, null);
        return Triple.of(mockRequest, mockResponse, accessToken);
    }

    protected UmaResourceRegistrationRequest createUmaResourceRegistrationRequest() {
        return createUmaResourceRegistrationRequest(-1);
    }

    protected UmaResourceRegistrationRequest createUmaResourceRegistrationRequest(final long id) {
        val resRequest = new UmaResourceRegistrationRequest();
        resRequest.setUri("http://rs.example.com/alice/myresource");
        resRequest.setName("my-resource");
        resRequest.setType("my-resource-type");
        if (id >= 0) {
            resRequest.setId(id);
        }
        resRequest.setScopes(CollectionUtils.wrapList("read", "write"));
        return resRequest;
    }

    protected ResourceSetPolicy createUmaPolicyRegistrationRequest(final CommonProfile profile) {
        return createUmaPolicyRegistrationRequest(profile, CollectionUtils.wrapHashSet("read", "write"));
    }

    protected ResourceSetPolicy createUmaPolicyRegistrationRequest(final CommonProfile profile, final Collection<String> scopes) {
        val policy = new ResourceSetPolicy();
        val perm = new ResourceSetPolicyPermission();
        perm.setScopes(new HashSet<>(scopes));
        perm.setClaims(new LinkedHashMap<>(CollectionUtils.wrap("givenName", "CAS")));
        perm.setSubject(profile.getId());
        policy.setPermissions(CollectionUtils.wrapHashSet(perm));
        return policy;
    }

    protected UmaPermissionRegistrationRequest createUmaPermissionRegistrationRequest(final long resourceId) {
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
    protected CommonProfile getCurrentProfile(final HttpServletRequest request, final HttpServletResponse response) {
        return (CommonProfile) Pac4jUtils.getPac4jProfileManager(request, response).get(true).get();
    }
}
