package org.apereo.cas.configuration.model.support.x509;

import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link Rfc822EmailPrincipalResolverProperties}.
 * @since 6.1.0
 */
@RequiresModule(name = "cas-server-support-x509-webflow")
@Getter
@Setter
@Accessors(chain = true)
public class Rfc822EmailPrincipalResolverProperties extends BaseAlternativePrincipalResolverProperties {
    private static final long serialVersionUID = -8696449609399074305L;
}
