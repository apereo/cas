package org.apereo.cas.uma.web.controllers;

import org.apereo.cas.config.CasOAuthUmaComponentSerializationConfiguration;
import org.apereo.cas.config.CasOAuthUmaConfiguration;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.web.AbstractOAuth20Tests;
import org.apereo.cas.util.CollectionUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.apache.http.HttpHeaders;
import org.junit.Test;
import org.pac4j.springframework.web.SecurityInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.LinkedHashMap;

import static org.junit.Assert.*;

/**
 * This is {@link UmaPermissionRegistrationEndpointControllerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Import({CasOAuthUmaConfiguration.class, CasOAuthUmaComponentSerializationConfiguration.class})
public class UmaPermissionRegistrationEndpointControllerTests extends AbstractOAuth20Tests {
    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    @Autowired
    @Qualifier("umaPermissionRegistrationEndpointController")
    protected UmaPermissionRegistrationEndpointController umaPermissionRegistrationEndpointController;

    @Autowired
    @Qualifier("umaSecurityInterceptor")
    protected SecurityInterceptor umaSecurityInterceptor;

    @Test
    public void verifyPermissionRegistrationOperation() throws Exception {
        val service = addRegisteredService();
        val pair = internalVerifyClientOK(service, false, OAuth20Constants.UMA_PROTECTION_SCOPE);
        assertNotNull(pair.getKey());
        val accessToken = pair.getKey();

        val mockRequest = new MockHttpServletRequest(HttpMethod.POST.name(), CONTEXT + OAuth20Constants.UMA_REGISTRATION_URL);
        mockRequest.addHeader(HttpHeaders.AUTHORIZATION, String.format("%s %s", OAuth20Constants.TOKEN_TYPE_BEARER, accessToken));
        val mockResponse = new MockHttpServletResponse();
        umaSecurityInterceptor.preHandle(mockRequest, mockResponse, null);

        val map = new LinkedHashMap<String, Object>();
        map.put("resource_id", "1234567890");
        map.put("resource_scopes", CollectionUtils.wrapList("read"));
        umaPermissionRegistrationEndpointController.handle(MAPPER.writeValueAsString(map), mockRequest, mockResponse);
    }
}
