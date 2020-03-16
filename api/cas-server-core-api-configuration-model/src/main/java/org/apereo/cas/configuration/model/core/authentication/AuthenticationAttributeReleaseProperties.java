package org.apereo.cas.configuration.model.core.authentication;

import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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
    private static final long serialVersionUID = 6123748197108749858L;

    /**
     * Whether CAS authentication/protocol attributes
     * should be released as part of ticket validation.
     */
    private boolean enabled = true;

    /**
     * List of authentication attributes that should never be released.
     */
    private List<String> neverRelease = new ArrayList<>(0);

    /**
     * List of authentication attributes that should be the only ones released. An empty list indicates all attributes
     * should be released.
     */
    private List<String> onlyRelease = new ArrayList<>(0);
}
