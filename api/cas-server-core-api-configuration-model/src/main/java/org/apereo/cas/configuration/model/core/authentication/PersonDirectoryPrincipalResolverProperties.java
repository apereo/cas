package org.apereo.cas.configuration.model.core.authentication;

import org.apereo.cas.configuration.support.RequiresModule;
import org.apereo.cas.configuration.support.TriStateBoolean;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Configuration properties class for Person Directory.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-core-authentication", automated = true)
@Getter
@Setter
@Accessors(chain = true)
public class PersonDirectoryPrincipalResolverProperties implements Serializable {

    @Serial
    private static final long serialVersionUID = 8929912041234879300L;

    /**
     * Attribute name to use to indicate the identifier of the principal constructed.
     * If the attribute is blank or has no values, the default principal id will be used
     * determined by the underlying authentication engine. The principal id attribute
     * usually is removed from the collection of attributes collected, though this behavior
     * depends on the schematics of the underlying authentication strategy.
     */
    private String principalAttribute;

    /**
     * Return a null principal object if no attributes can be found for the principal.
     */
    private TriStateBoolean returnNull = TriStateBoolean.UNDEFINED;

    /**
     * When true, throws an error back indicating that principal resolution
     * has failed and no principal can be found based on the authentication requirements.
     * Otherwise, logs the condition as an error without raising a catastrophic error.
     */
    private TriStateBoolean principalResolutionFailureFatal = TriStateBoolean.UNDEFINED;

    /**
     * Uses an existing principal id that may have already
     * been established in order to run person directory queries.
     * This is generally useful in situations where
     * authentication is delegated to an external identity provider
     * and a principal is first established to then query an attribute source.
     */
    private TriStateBoolean useExistingPrincipalId = TriStateBoolean.UNDEFINED;

    /**
     * Whether attribute repositories should be contacted
     * to fetch person attributes. Defaults to true if not set.
     */
    private TriStateBoolean attributeResolutionEnabled = TriStateBoolean.UNDEFINED;

    /**
     * Activated attribute repository identifiers
     * that should be used for fetching attributes
     * if attribute resolution is enabled.
     * The list here may include identifiers separated by comma.
     */
    private String activeAttributeRepositoryIds = "*";

    /**
     * In the event that the principal resolution engine resolves
     * more than one principal, (especially if such principals in the chain
     * have different identifiers), this setting determines strategy by which
     * the principal id would be chosen from the chain.
     * Accepted values are: {@code last}, {@code first}.
     */
    private String principalResolutionConflictStrategy = "last";

    /**
     * Principal transformation properties.
     */
    @NestedConfigurationProperty
    private PrincipalTransformationProperties principalTransformation = new PrincipalTransformationProperties();

    /**
     * Control the behavior of the attribute repository selection by authentication method or handler.
     * The map here is keyed by the authentication handler name, and the value is the attribute repository
     * identifiers separated by comma. When the authentication handler is executed, the attribute repositories
     * assigned to this handler will be selected to fetch attributes.
     * Note that the resolution engine will always favor attribute repositories assigned to the
     * service definition, if any and as part of its authentication policy, over this global setting.
     */
    private Map<String, String> attributeRepositorySelection = new HashMap<>();
}
