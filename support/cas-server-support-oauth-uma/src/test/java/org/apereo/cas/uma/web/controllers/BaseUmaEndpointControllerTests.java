package org.apereo.cas.uma.web.controllers;

import org.apereo.cas.config.CasOAuthUmaComponentSerializationConfiguration;
import org.apereo.cas.config.CasOAuthUmaConfiguration;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.web.AbstractOAuth20Tests;
import org.apereo.cas.uma.web.controllers.permission.UmaPermissionRegistrationEndpointController;
import org.apereo.cas.uma.web.controllers.resource.UmaCreateResourceSetRegistrationEndpointController;
import org.apereo.cas.uma.web.controllers.resource.UmaFindResourceSetRegistrationEndpointController;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.http.HttpHeaders;
import org.pac4j.springframework.web.SecurityInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.junit.Assert.*;

/**
 * This is {@link BaseUmaEndpointControllerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Import({CasOAuthUmaConfiguration.class, CasOAuthUmaComponentSerializationConfiguration.class})
public abstract class BaseUmaEndpointControllerTests extends AbstractOAuth20Tests {
    protected static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    @Autowired
    @Qualifier("umaPermissionRegistrationEndpointController")
    protected UmaPermissionRegistrationEndpointController umaPermissionRegistrationEndpointController;

    @Autowired
    @Qualifier("umaCreateResourceSetRegistrationEndpointController")
    protected UmaCreateResourceSetRegistrationEndpointController umaCreateResourceSetRegistrationEndpointController;

    @Autowired
    @Qualifier("umaFindResourceSetRegistrationEndpointController")
    protected UmaFindResourceSetRegistrationEndpointController umaFindResourceSetRegistrationEndpointController;

    @Autowired
    @Qualifier("umaSecurityInterceptor")
    protected SecurityInterceptor umaSecurityInterceptor;

    protected Triple<HttpServletRequest, HttpServletResponse, String> authenticateUmaRequest() throws Exception {
        val service = addRegisteredService();
        val pair = internalVerifyClientOK(service, false, OAuth20Constants.UMA_PROTECTION_SCOPE);
        assertNotNull(pair.getKey());
        val accessToken = pair.getKey();

        val mockRequest = new MockHttpServletRequest(HttpMethod.POST.name(), CONTEXT + OAuth20Constants.UMA_REGISTRATION_URL);
        mockRequest.addHeader(HttpHeaders.AUTHORIZATION, String.format("%s %s", OAuth20Constants.TOKEN_TYPE_BEARER, accessToken));
        val mockResponse = new MockHttpServletResponse();
        umaSecurityInterceptor.preHandle(mockRequest, mockResponse, null);
        return Triple.of(mockRequest, mockResponse, accessToken);
    }
}
