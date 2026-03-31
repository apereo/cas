package org.apereo.cas.configuration.model.support.oidc;

import module java.base;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link OidcVerifiableCredentialClaimProperties}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@Getter
@Setter
@Accessors(chain = true)
@RequiresModule(name = "cas-server-support-oidc-vc")
public class OidcVerifiableCredentialClaimProperties implements Serializable {
    @Serial
    private static final long serialVersionUID = -611478635714944538L;

    private boolean mandatory;
    private String valueType;
}
