package org.apereo.cas.heimdall.authorizer.resource;

import module java.base;
import org.apereo.cas.heimdall.authorizer.resource.policy.ResourceAuthorizationPolicy;
import org.apereo.cas.heimdall.authzen.AuthZenAction;
import org.apereo.cas.heimdall.authzen.AuthZenResource;
import org.apereo.cas.util.serialization.PatternJsonDeserializer;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.Nulls;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import tools.jackson.databind.annotation.JsonDeserialize;

/**
 * This is {@link AuthorizableResource}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Getter
@Setter
@EqualsAndHashCode
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class AuthorizableResource implements Serializable {
    @Serial
    private static final long serialVersionUID = -1222481042826672523L;

    private long id;
    
    @JsonDeserialize(using = PatternJsonDeserializer.class)
    private Pattern pattern;

    private String method;

    private String resourceType;

    @JsonDeserialize(using = PatternJsonDeserializer.class)
    private Pattern resourceIdPattern;

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private Set<String> actions = new HashSet<>();

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private List<ResourceAuthorizationPolicy> policies = new ArrayList<>();

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private Map<String, Object> properties = new HashMap<>();

    private boolean enforceAllPolicies;

    /**
     * Whether this resource carries policies for the given AuthZEN resource and action.
     * The resource type and action name must match exactly; when a resource id pattern is defined,
     * it must match the entire resource id.
     *
     * @param resource the AuthZEN resource
     * @param action   the AuthZEN action
     * @return true if this resource applies
     */
    @JsonIgnore
    public boolean supports(final AuthZenResource resource, final AuthZenAction action) {
        return resource != null
            && action != null
            && resourceType != null && resourceType.equals(resource.getType())
            && actions.contains(action.getName())
            && (resourceIdPattern == null || (resource.getId() != null && resourceIdPattern.matcher(resource.getId()).matches()));
    }
}
