package org.apereo.cas.ticket.accesstoken;

import org.apereo.cas.AbstractOAuth20Tests;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.token.JwtBuilder;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;

import static org.apereo.cas.util.junit.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Date;

/**
 * This is {@link OAuth20JwtBuilderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("OAuth")
public class OAuth20JwtBuilderTests extends AbstractOAuth20Tests {
    @Test
    public void verifyJwt() throws Exception {
        servicesManager.save(getRegisteredService("clientid-jwt", "secret-jwt"));
        val service = CoreAuthenticationTestUtils.getService("https://service.example.com");
        val request = JwtBuilder.JwtRequest.builder()
            .issueDate(new Date())
            .jwtId(service.getId())
            .serviceAudience("clientid-jwt")
            .subject("casuser")
            .build();
        val jwt = accessTokenJwtBuilder.build(request);
        assertNotNull(jwt);
    }
}
