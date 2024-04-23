package org.apereo.cas.configuration.model.support.x509;

import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serial;

/**
 * This is {@link CnEdipiPrincipalResolverProperties}.
 *
 * @since 6.0.0
 */
@RequiresModule(name = "cas-server-support-x509-webflow")
@Getter
@Setter
@Accessors(chain = true)
public class CnEdipiPrincipalResolverProperties extends BaseAlternativePrincipalResolverProperties {
    @Serial
    private static final long serialVersionUID = 2622326703782668141L;

    /**
     * Whether to extract EDIPI as an attribute, regardless of principal resolver type.
     */
    private boolean extractEdipiAsAttribute;
}
