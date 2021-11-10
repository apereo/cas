package org.apereo.cas.configuration.model.support.oauth;

import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * This is {@link OAuthCodeProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiresModule(name = "cas-server-support-oauth")
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("OAuthCodeProperties")
public class OAuthCodeProperties implements Serializable {

    private static final long serialVersionUID = -7687928082301669359L;

    /**
     * Number of times this code is valid and can be used.
     */
    private int numberOfUses = 1;

    /**
     * Duration in seconds where the code is valid.
     */
    private long timeToKillInSeconds = 30;

    /**
     * The storage object name used and created by CAS to hold OAuth codes in the
     * backing ticket registry implementation.
     */
    private String storageName = "oauthCodesCache";
}
