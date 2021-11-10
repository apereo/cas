package org.apereo.cas.configuration.model.support.oauth;

import org.apereo.cas.configuration.support.DurationCapable;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * This is {@link OAuthDeviceTokenProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@RequiresModule(name = "cas-server-support-oauth")
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("OAuthDeviceTokenProperties")
public class OAuthDeviceTokenProperties implements Serializable {

    private static final long serialVersionUID = -6832081675586528350L;

    /**
     * Hard timeout to kill the access token and expire it.
     */
    @DurationCapable
    private String maxTimeToLiveInSeconds = "PT5M";

    /**
     * The device refresh interval.
     * The client should attempt to acquire an access token every few seconds (at a rate specified by interval)
     * by POSTing to the access token endpoint on the server.
     */
    @DurationCapable
    private String refreshInterval = "PT15S";

    /**
     * The storage object name used and created by CAS to hold OAuth device tokens
     * in the backing ticket registry implementation.
     */
    private String storageName = "oauthDeviceTokensCache";
}
