package org.apereo.cas.configuration.model.support.surrogate;

import org.apereo.cas.configuration.model.support.couchdb.BaseCouchDbProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link SurrogateCouchDbAuthenticationProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@RequiresModule(name = "cas-server-support-surrogate-authentication-couchdb")
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("SurrogateCouchDbAuthenticationProperties")
public class SurrogateCouchDbAuthenticationProperties extends BaseCouchDbProperties {

    private static final long serialVersionUID = 8378399979559955402L;

    /**
     * Use user profiles instead of surrogate/principal pairs. If +true+, a list of of
     * principals the user is an authorized surrogate of is stored in the
     * user profile in CouchDb. Most useful with CouchDb authentication or AUP.
     */
    private boolean profileBased;

    /**
     * Attribute with list of principals the user may surrogate
     * when user surrogates are stored in user profiles.
     */
    private String surrogatePrincipalsAttribute = "surrogateFor";

    public SurrogateCouchDbAuthenticationProperties() {
        this.setDbName("surrogates");
    }
}
