package org.apereo.cas.configuration.model.support.oauth;

import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

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
public class OAuthDeviceUserCodeProperties implements Serializable {

    private static final long serialVersionUID = -1232081675586528350L;

    /**
     * Hard timeout to kill the access token and expire it.
     */
    private String maxTimeToLiveInSeconds = "PT1M";

    /**
     * Length of the generated user code.
     */
    private int userCodeLength = 8;
}
