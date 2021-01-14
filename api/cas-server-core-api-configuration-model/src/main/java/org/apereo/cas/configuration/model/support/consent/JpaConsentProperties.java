package org.apereo.cas.configuration.model.support.consent;

import org.apereo.cas.configuration.model.support.jpa.AbstractJpaProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link JpaConsentProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@RequiresModule(name = "cas-server-support-consent-jdbc")
@Getter
@Setter
@Accessors(chain = true)
public class JpaConsentProperties extends AbstractJpaProperties {
    private static final long serialVersionUID = 1646689616653363554L;
}
