package org.apereo.cas.configuration.model.support.oidc;

import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * This is {@link OidcClientRegistrationProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-support-oidc")
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("OidcClientRegistrationProperties")
public class OidcClientRegistrationProperties implements Serializable {

    private static final long serialVersionUID = 823028615694269276L;

    /**
     * Whether dynamic registration operates in {@code OPEN} or {@code PROTECTED} mode.
     */
    private String dynamicClientRegistrationMode;
}
