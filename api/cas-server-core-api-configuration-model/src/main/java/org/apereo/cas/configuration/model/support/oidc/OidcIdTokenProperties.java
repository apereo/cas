package org.apereo.cas.configuration.model.support.oidc;

import org.apereo.cas.configuration.support.DurationCapable;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * This is {@link OidcIdTokenProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@RequiresModule(name = "cas-server-support-oidc")
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("OidcIdTokenProperties")
public class OidcIdTokenProperties implements Serializable {

    private static final long serialVersionUID = 813328615694269276L;

    /**
     * Hard timeout to kill the id token and expire it.
     */
    @DurationCapable
    private String maxTimeToLiveInSeconds = "PT8H";
}
