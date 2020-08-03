package org.apereo.cas.rest.factory;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.util.LinkedMultiValueMap;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

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
    }

}
