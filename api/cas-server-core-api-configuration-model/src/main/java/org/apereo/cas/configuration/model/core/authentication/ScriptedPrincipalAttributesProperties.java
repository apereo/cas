package org.apereo.cas.configuration.model.core.authentication;

import org.apereo.cas.configuration.support.RequiresModule;
import org.apereo.cas.configuration.support.SpringResourceProperties;
import lombok.Getter;
import lombok.Setter;

/**
 * This is {@link ScriptedPrincipalAttributesProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiresModule(name = "cas-server-support-person-directory", automated = true)
@Getter
@Setter
public class ScriptedPrincipalAttributesProperties extends SpringResourceProperties {

    private static final long serialVersionUID = 4221139939506528713L;

    /**
     * Script engine name, e.g. groovy, js, python, etc.
     * Required if CAS can't determine based on extension.
     * The file extension of the resource will be used to determine the engineName if not specified.
     * Engines must be on the classpath in order for the engineName to be determined automatically.
     * The first engine found claiming to support the extension of the file specified will be used.
     */
    private String engineName;

    /**
     * Whether attribute repository should consider the underlying
     * attribute names in a case-insensitive manner.
     */
    private boolean caseInsensitive;

    /**
     * The order of this attribute repository in the chain of repositories.
     * Can be used to explicitly position this source in chain and affects
     * merging strategies.
     */
    private int order;
}
