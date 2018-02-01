package org.apereo.cas.adaptors.trusted.authentication.principal;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.AbstractCredential;
import org.apereo.cas.authentication.principal.Principal;

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
@Slf4j
@Getter
public class PrincipalBearingCredential extends AbstractCredential {

    /** Serialization version marker. */
    private static final long serialVersionUID = 8866786438439775669L;

    /** The trusted principal. */
    private Principal principal;

    /**
     * Instantiates a new principal bearing credential.
     *
     * @param principal the principal
     */
    @JsonCreator
    public PrincipalBearingCredential(@NonNull @JsonProperty("principal") final Principal principal) {
        this.principal = principal;
    }
    
    @JsonIgnore
    @Override
    public String getId() {
        return this.principal.getId();
    }
}
