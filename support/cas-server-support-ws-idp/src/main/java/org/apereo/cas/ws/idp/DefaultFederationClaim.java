package org.apereo.cas.ws.idp;

import com.google.common.base.Throwables;
import org.apache.cxf.fediz.core.Claim;

import java.net.URI;

/**
 * This is {@link DefaultFederationClaim}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class DefaultFederationClaim extends Claim {
    private static final long serialVersionUID = 79420657771551371L;

    public DefaultFederationClaim(final String claimType) {
        try {
            setClaimType(new URI(claimType));
        } catch (final Exception e) {
            throw Throwables.propagate(e);
        }
    }
}
