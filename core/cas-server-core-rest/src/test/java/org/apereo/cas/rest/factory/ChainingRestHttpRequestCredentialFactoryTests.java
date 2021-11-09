package org.apereo.cas.rest.factory;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.util.LinkedMultiValueMap;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link ChainingRestHttpRequestCredentialFactoryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("Authentication")
public class ChainingRestHttpRequestCredentialFactoryTests {

    @Test
    public void verifyOperation() {
        val request = new MockHttpServletRequest();
        val body = new LinkedMultiValueMap<String, String>();
        body.put("username", List.of("casuser"));
        body.put("password", List.of("Mellon"));
        val factory = new ChainingRestHttpRequestCredentialFactory(new UsernamePasswordRestHttpRequestCredentialFactory());
        assertNotNull(factory.fromRequest(request, body));

        assertTrue(factory.fromAuthentication(request, body,
            CoreAuthenticationTestUtils.getAuthentication(),
            mock(MultifactorAuthenticationProvider.class)).isEmpty());
    }

    @Test
    public void verifyDefaultImpl() {
        val request = new MockHttpServletRequest();
        val body = new LinkedMultiValueMap<String, String>();
        val factory = mock(RestHttpRequestCredentialFactory.class);
        when(factory.fromAuthentication(any(), any(), any(), any())).thenCallRealMethod();
        when(factory.getOrder()).thenCallRealMethod();
        
        assertEquals(Integer.MAX_VALUE, factory.getOrder());
        assertTrue(factory.fromAuthentication(request, body,
            CoreAuthenticationTestUtils.getAuthentication(),
            mock(MultifactorAuthenticationProvider.class)).isEmpty());
    }

}
