package org.apereo.cas.webauthn.web;

import org.apereo.cas.web.AbstractController;
import org.apereo.cas.webauthn.WebAuthnUtils;

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

    protected static String writeJson(final Object o) {
        return WebAuthnUtils.getObjectMapper().writeValueAsString(o);
    }
}
