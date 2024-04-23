package org.apereo.cas.configuration.model.support.spnego;

import org.apereo.cas.configuration.model.support.ldap.AbstractLdapSearchProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serial;

/**
 * This is {@link SpnegoLdapProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@RequiresModule(name = "cas-server-support-spnego-webflow")
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("SpnegoLdapProperties")
public class SpnegoLdapProperties extends AbstractLdapSearchProperties {
    @Serial
    private static final long serialVersionUID = -8835216200501334936L;
}
