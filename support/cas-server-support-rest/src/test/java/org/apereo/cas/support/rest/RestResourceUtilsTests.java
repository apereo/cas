package org.apereo.cas.support.rest;

import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.adaptive.UnauthorizedAuthenticationException;
import org.apereo.cas.support.rest.resources.RestResourceUtils;
import org.apereo.cas.util.CollectionUtils;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

/**
 * This is {@link RestResourceUtilsTests}.
 *
 * @author john.j.cool
 * @since 6.1.6
 */
@ExtendWith(MockitoExtension.class)
@Tag("RestfulApi")
public class RestResourceUtilsTests {

    @Mock
    private ApplicationContext context;

    @BeforeEach
    public void before() {
        MockitoAnnotations.initMocks(this);
        doReturn("test").when(context)
                .getMessage(any(), any(), any());
    }

    @Test
    public void verifyNoFrameworkClassSendToClient() {
        val request = mock(HttpServletRequest.class);
        val msg = "test";
        val map = CollectionUtils.<String, Throwable>wrap(
                UnauthorizedAuthenticationException.class.getSimpleName(),
                new UnauthorizedAuthenticationException(msg)
        );

        val response = RestResourceUtils.createResponseEntityForAuthnFailure(
                new AuthenticationException(msg, map, new HashMap<>(0)),
                request,
                context
        );

        assertTrue(response.getStatusCode().isError());
        assertFalse(response.getBody().contains(UnauthorizedAuthenticationException.class.getSimpleName()));
    }
}
