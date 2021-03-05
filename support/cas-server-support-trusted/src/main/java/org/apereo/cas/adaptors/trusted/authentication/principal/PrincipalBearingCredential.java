package org.apereo.cas.adaptors.trusted.authentication.principal;

import org.apereo.cas.authentication.credential.AbstractCredential;
import org.apereo.cas.authentication.principal.Principal;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Credential that bear the fully resolved and authenticated Principal, or an
 * indication that there is no such Principal. These Credential are a mechanism
 * to pass into CAS information about an authentication and Principal resolution
 * that has already happened in layers in front of CAS, e.g. by means of a Java
 * Servlet Filter or by means of container authentication in the servlet
 * container or Apache layers. DO NOT accept these Credential from arbitrary
 * web-service calls to CAS. Rather, the code constructing these Credential
 * must be trusted to perform appropriate authentication before issuing these
 * credentials.
 *
 * @author Andrew Petro
 * @since 3.0.0
 */
@Getter
@Setter
@ToString(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
public class PrincipalBearingCredential extends AbstractCredential {
    
    private static final long serialVersionUID = 8866786438439775669L;

    /**
     * The trusted principal.
     */
    private Principal principal;

    @JsonIgnore
    @Override
    public String getId() {
        return this.principal.getId();
    }
}
