package org.apereo.cas.configuration.model.support.mfa.u2f;

import org.apereo.cas.configuration.model.support.jpa.AbstractJpaProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link U2FJpaMultifactorAuthenticationProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@RequiresModule(name = "cas-server-support-u2f-jpa")
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("U2FJpaMultifactorAuthenticationProperties")
public class U2FJpaMultifactorAuthenticationProperties extends AbstractJpaProperties {

    private static final long serialVersionUID = -4334840263678287815L;
}
