package org.apereo.cas.configuration.model.support.oidc;

import module java.base;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link OidcVerifiableCredentialsMetadataProperties}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@RequiresModule(name = "cas-server-support-oidc-vc")
@Getter
@Setter
@Accessors(chain = true)
public class OidcVerifiableCredentialsMetadataProperties implements Serializable {
    @Serial
    private static final long serialVersionUID = -2220371070424785548L;

    /**
     * Whether claims should be included in the metadata.
     */
    private boolean includeClaims = true;
}
