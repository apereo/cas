package org.apereo.cas.mfa.accepto.web.flow;

import lombok.experimental.UtilityClass;

/**
 * This is {@link AccepttoWebflowUtils}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@UtilityClass
public class AccepttoWebflowUtils {
    /**
     * Session attribute to hold the authentication channel.
     */
    public static final String SESSION_ATTRIBUTE_CHANNEL = "acceptoMfaChannel";

    /**
     * Session attribute to hold original authn.
     */
    public static final String SESSION_ATTRIBUTE_ORIGINAL_AUTHENTICATION = "acceptoMfaOriginalAuthN";
}
