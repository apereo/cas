package org.apereo.cas.configuration.model.support.uma;

import org.apereo.cas.configuration.model.SpringResourceProperties;
import org.apereo.cas.configuration.support.DurationCapable;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.core.io.FileSystemResource;

import java.io.Serializable;

/**
 * This is {@link UmaRequestingPartyTokenProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@RequiresModule(name = "cas-server-support-oauth-uma")
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("UmaRequestingPartyTokenProperties")
public class UmaRequestingPartyTokenProperties implements Serializable {
    private static final long serialVersionUID = 3988708361481340920L;

    /**
     * Hard timeout to kill the access token and expire it.
     */
    @DurationCapable
    private String maxTimeToLiveInSeconds = "PT3M";

    /**
     * Path to the JWKS file that is used to sign the rpt token.
     */
    @NestedConfigurationProperty
    private SpringResourceProperties jwksFile = new SpringResourceProperties()
        .setLocation(new FileSystemResource("/etc/cas/uma-keystore.jwks"));
}
