package org.apereo.cas.configuration.model.support.pac4j;

import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link Pac4jDelegatedAuthenticationFacebookProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@RequiresModule(name = "cas-server-support-pac4j-webflow")
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("Pac4jDelegatedAuthenticationFacebookProperties")
public class Pac4jDelegatedAuthenticationFacebookProperties extends Pac4jIdentifiableClientProperties {

    private static final long serialVersionUID = -2737594266552466076L;

    /**
     * The requested scope.
     */
    private String scope;

    /**
     * Custom fields to include in the request.
     */
    private String fields;

    public Pac4jDelegatedAuthenticationFacebookProperties() {
        setClientName("Facebook");
    }
}
