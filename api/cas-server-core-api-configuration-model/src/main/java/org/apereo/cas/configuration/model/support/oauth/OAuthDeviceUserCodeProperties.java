package org.apereo.cas.configuration.model.support.oauth;

import org.apereo.cas.configuration.support.DurationCapable;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * This is {@link OAuthDeviceUserCodeProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@RequiresModule(name = "cas-server-support-oauth")
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("OAuthDeviceUserCodeProperties")
public class OAuthDeviceUserCodeProperties implements Serializable {

    @Serial
    private static final long serialVersionUID = -1232081675586528350L;

    /**
     * Hard timeout to kill the token and expire it.
     */
    @DurationCapable
    private String maxTimeToLiveInSeconds = "PT1M";

    /**
     * Length of the generated user code.
     */
    private int userCodeLength = 8;

    /**
     * The storage object name used and created by CAS to hold OAuth device user
     * codes in the backing ticket registry implementation.
     */
    private String storageName = "oauthDeviceUserCodesCache";
}
