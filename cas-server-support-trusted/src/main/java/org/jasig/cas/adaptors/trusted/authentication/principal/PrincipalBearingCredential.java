package org.jasig.cas.adaptors.trusted.authentication.principal;

import org.jasig.cas.authentication.AbstractCredential;
import org.jasig.cas.authentication.principal.Principal;
import org.springframework.util.Assert;

/**
 * Credential that bear the fully resolved and authenticated Principal, or an
 * indication that there is no such Principal. These Credential are a mechanism
 * to pass into CAS information about an authentication and Principal resolution
 * that has already happened in layers in front of CAS, e.g. by means of a Java
 * Servlet Filter or by means of container authentication in the servlet
 * container or Apache layers. DO NOT accept these Credential from arbitrary
 * web-servicey calls to CAS. Rather, the code constructing these Credential
 * must be trusted to perform appropriate authentication before issuing these
 * credentials.
 *
 * @author Andrew Petro
 * @since 3.0.0
 */
public final class PrincipalBearingCredential extends AbstractCredential {

    /** Serialization version marker. */
    private static final long serialVersionUID = 8866786438439775669L;

    /** The trusted principal. */
    private final Principal principal;

    /**
     * Instantiates a new principal bearing credential.
     *
     * @param principal the principal
     */
    public PrincipalBearingCredential(final Principal principal) {
        Assert.notNull(principal, "principal cannot be null");
        this.principal = principal;
    }

    /**
     * Get the previously authenticated Principal.
     *
     * @return authenticated Principal
     */
    public Principal getPrincipal() {
        return this.principal;
    }

    @Override
    public String getId() {
        return this.principal.getId();
    }

}
