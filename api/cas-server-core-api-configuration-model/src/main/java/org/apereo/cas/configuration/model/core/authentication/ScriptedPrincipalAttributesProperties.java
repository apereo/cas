package org.apereo.cas.configuration.model.core.authentication;

import org.apereo.cas.configuration.model.SpringResourceProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link ScriptedPrincipalAttributesProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 * @deprecated Since 6.2
 */
@RequiresModule(name = "cas-server-support-person-directory", automated = true)
@Getter
@Setter
@Deprecated(since = "6.2.0")
@Accessors(chain = true)
@JsonFilter("ScriptedPrincipalAttributesProperties")
public class ScriptedPrincipalAttributesProperties extends SpringResourceProperties {

    private static final long serialVersionUID = 4221139939506528713L;

    /**
     * Script engine name, e.g. groovy, js, python, etc.
     * Required if CAS can't determine based on extension.
     * The file extension of the resource will be used to determine the engineName if not specified.
     * Engines must be on the classpath in order for the engineName to be determined automatically.
     * The first engine found claiming to support the extension of the file specified will be used.
     * @deprecated Since 6.2
     */
    @Deprecated(since = "6.2.0")
    private String engineName;

    /**
     * Whether attribute repository should consider the underlying
     * attribute names in a case-insensitive manner.
     * @deprecated Since 6.2
     */
    @Deprecated(since = "6.2.0")
    private boolean caseInsensitive;

    /**
     * The order of this attribute repository in the chain of repositories.
     * Can be used to explicitly position this source in chain and affects
     * merging strategies.
     * @deprecated Since 6.2
     */
    @Deprecated(since = "6.2.0")
    private int order;

    /**
     * A value can be assigned to this field to uniquely identify this resolver.
     * @deprecated Since 6.2
     */
    @Deprecated(since = "6.2.0")
    private String id;
}
