package org.apereo.cas.webauthn.web;

import org.apereo.cas.web.AbstractController;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yubico.internal.util.JacksonCodecs;

/**
 * This is {@link BaseWebAuthnController}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
public abstract class BaseWebAuthnController extends AbstractController {
    /**
     * Base endpoint for all webauthn web resources.
     */
    public static final String BASE_ENDPOINT_WEBAUTHN = "/webauthn";

    private static final ObjectMapper MAPPER = JacksonCodecs.json().findAndRegisterModules();
    
    protected static String writeJson(final Object o) throws Exception {
        return MAPPER.writeValueAsString(o);
    }
}
