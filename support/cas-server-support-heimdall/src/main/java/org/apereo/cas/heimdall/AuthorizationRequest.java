package org.apereo.cas.heimdall;

import module java.base;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.heimdall.authzen.AuthZenAction;
import org.apereo.cas.heimdall.authzen.AuthZenResource;
import org.apereo.cas.heimdall.authzen.AuthZenSubject;
import org.apereo.cas.util.CollectionUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.SuperBuilder;
import lombok.ToString;
import lombok.With;
import org.jspecify.annotations.Nullable;
import org.springframework.validation.annotation.Validated;

/**
 * This is {@link AuthorizationRequest}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@Validated
@ToString
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@AllArgsConstructor
@With
public class AuthorizationRequest extends BaseHeimdallEntity {
    @Serial
    private static final long serialVersionUID = -3826637704182099574L;

    private static final String SUBJECT_PROPERTIES = "subject.properties.";

    private static final String RESOURCE_PROPERTIES = "resource.properties.";

    private static final String ACTION_PROPERTIES = "action.properties.";

    private static final String CONTEXT = "context.";

    private String method;
    private String uri;
    private String namespace;
    
    private AuthZenSubject subject;
    private AuthZenResource resource;
    private AuthZenAction action;

    @Builder.Default
    private Map<String, ?> context = new HashMap<>();

    @JsonIgnore
    private Principal principal;

    /**
     * Whether this is an AuthZEN request, which carries a subject, a resource and an action.
     *
     * @return true if subject, resource and action are all present
     */
    @JsonIgnore
    public boolean isAuthZen() {
        return subject != null && resource != null && action != null;
    }

    /**
     * Resolve the values of an attribute by name for policy evaluation. Qualified names read the request:
     * {@code subject.id}, {@code subject.type}, {@code resource.id}, {@code resource.type}, {@code action.name},
     * {@code subject.properties.<name>}, {@code resource.properties.<name>}, {@code action.properties.<name>}
     * and {@code context.<name>}. Any other name is a principal attribute.
     *
     * @param name the attribute name
     * @return the values, or an empty collection when absent
     */
    @JsonIgnore
    public Collection<Object> resolveAttributeValues(final String name) {
        final Object value = switch (name) {
            case "subject.id" -> subject == null ? null : subject.getId();
            case "subject.type" -> subject == null ? null : subject.getType();
            case "resource.id" -> resource == null ? null : resource.getId();
            case "resource.type" -> resource == null ? null : resource.getType();
            case "action.name" -> action == null ? null : action.getName();
            default -> resolveQualifiedAttribute(name);
        };
        return value == null ? List.of() : CollectionUtils.toCollection(value);
    }

    private @Nullable Object resolveQualifiedAttribute(final String name) {
        if (name.startsWith(SUBJECT_PROPERTIES)) {
            return subject == null ? null : propertyOf(subject.getProperties(), name.substring(SUBJECT_PROPERTIES.length()));
        }
        if (name.startsWith(RESOURCE_PROPERTIES)) {
            return resource == null ? null : propertyOf(resource.getProperties(), name.substring(RESOURCE_PROPERTIES.length()));
        }
        if (name.startsWith(ACTION_PROPERTIES)) {
            return action == null ? null : propertyOf(action.getProperties(), name.substring(ACTION_PROPERTIES.length()));
        }
        if (name.startsWith(CONTEXT)) {
            return propertyOf(context, name.substring(CONTEXT.length()));
        }
        return principal == null ? null : principal.getAttributes().get(name);
    }

    private static @Nullable Object propertyOf(final @Nullable Map<String, ?> properties, final String name) {
        return properties == null ? null : properties.get(name);
    }
}

