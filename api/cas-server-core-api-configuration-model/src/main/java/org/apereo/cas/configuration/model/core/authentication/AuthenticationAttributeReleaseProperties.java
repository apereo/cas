package org.apereo.cas.configuration.model.core.authentication;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.configuration.support.RequiresModule;
import java.util.ArrayList;
import java.util.List;

/**
 * Authentication attribute release properties.
 *
 * @author Daniel Frett
 * @since 5.2.0
 */
@RequiresModule(name = "cas-server-support-validation", automated = true)
@Slf4j
@Getter
@Setter
public class AuthenticationAttributeReleaseProperties {

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
