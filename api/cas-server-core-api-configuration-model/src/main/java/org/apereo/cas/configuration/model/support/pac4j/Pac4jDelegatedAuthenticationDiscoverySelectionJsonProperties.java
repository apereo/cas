package org.apereo.cas.configuration.model.support.pac4j;

import org.apereo.cas.configuration.model.SpringResourceProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link Pac4jDelegatedAuthenticationDiscoverySelectionJsonProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@RequiresModule(name = "cas-server-support-pac4j-webflow")
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("Pac4jDelegatedAuthenticationDiscoverySelectionJsonProperties")
public class Pac4jDelegatedAuthenticationDiscoverySelectionJsonProperties extends SpringResourceProperties {
    private static final long serialVersionUID = -2261947621312270068L;

}
