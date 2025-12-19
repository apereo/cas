package org.apereo.cas.configuration.model.core.authentication;

import module java.base;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Authentication attribute release properties.
 *
 * @author Daniel Frett
 * @since 5.2.0
 */
@RequiresModule(name = "cas-server-support-validation", automated = true)
@Getter
@Setter
@Accessors(chain = true)
public class AuthenticationAttributeReleaseProperties implements Serializable {
    @Serial
    private static final long serialVersionUID = 6123748197108749858L;

    /**
     * Whether authentication or protocol attributes
     * should be released to clients. This flag specifically
     * address non-principal attributes, or otherwise attributes
     * that carry metadata about the authentication event itself
     * that are not strictly tied to a principal or person data.
     * The change here should consider such attributes regardless
     * of the specific protocol or authentication flow (CAS, OIDC, etc).
     */
    private boolean enabled = true;

    /**
     * List of authentication attributes that should never be released.
     */
    private List<String> neverRelease = new ArrayList<>();

    /**
     * List of authentication attributes that should be the only ones released. An empty list indicates all attributes
     * should be released.
     */
    private List<String> onlyRelease = new ArrayList<>();
}
