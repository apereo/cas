package org.apereo.cas.configuration.model.support.oauth;

import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * This is {@link OAuthRefreshTokenProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiresModule(name = "cas-server-support-oauth")
@Getter
@Setter
@Accessors(chain = true)
public class OAuthRefreshTokenProperties implements Serializable {

    private static final long serialVersionUID = -8328568272835831702L;

    /**
     * Hard timeout beyond which the refresh token is considered expired.
     */
    private String timeToKillInSeconds = "P14D";
}
