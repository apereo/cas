package org.apereo.cas.support.rest;

import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.adaptive.UnauthorizedAuthenticationException;
import org.apereo.cas.support.rest.resources.RestResourceUtils;
import org.apereo.cas.util.CollectionUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link RestResourceUtilsTests}.
 *
 * @author john.j.cool
 * @since 6.1.6
 */
@Tag("RestfulApi")
public class RestResourceUtilsTests {
    @Test
    public void verifyCreateResponseEntityForAuthnFailure() {
        val request = new MockHttpServletRequest();
        val map = CollectionUtils.<String, Throwable>wrap(
            UnauthorizedAuthenticationException.class.getSimpleName(),
            new UnauthorizedAuthenticationException("test")
        );
        val applicationContext = new StaticApplicationContext();
        applicationContext.refresh();
        val response = RestResourceUtils.createResponseEntityForAuthnFailure(
            new AuthenticationException("test", map, new HashMap<>(0)),
            request, applicationContext);

        assertTrue(response.getStatusCode().isError());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().contains(UnauthorizedAuthenticationException.class.getSimpleName()));
    }
}
