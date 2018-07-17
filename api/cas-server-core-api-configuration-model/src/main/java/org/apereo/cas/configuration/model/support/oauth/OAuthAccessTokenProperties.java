package org.apereo.cas.configuration.model.support.oauth;

import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * This is {@link OAuthAccessTokenProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiresModule(name = "cas-server-support-oauth")
@Getter
@Setter
public class OAuthAccessTokenProperties implements Serializable {

    private static final long serialVersionUID = -6832081675586528350L;

    /**
     * Hard timeout to kill the access token and expire it.
     */
    private String maxTimeToLiveInSeconds = "PT28800S";

    /**
     * Sliding window for the access token expiration policy.
     * Essentially, this is an idle time out.
     */
    private String timeToKillInSeconds = "PT7200S";

    /**
     * Whether CAS authentication/protocol attributes
     * should be released as part of this access token's validation.
     */
    private boolean releaseProtocolAttributes = true;
}
