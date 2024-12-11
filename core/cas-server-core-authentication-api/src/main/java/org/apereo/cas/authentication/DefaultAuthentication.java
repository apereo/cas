package org.apereo.cas.authentication;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.util.CollectionUtils;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.val;
import java.io.Serial;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Immutable authentication event whose attributes may not change after creation.
 * This class is designed for serialization and is suitable for long-term storage.
 *
 * @author Dmitriy Kopylenko
 * @author Scott Battaglia
 * @author Marvin S. Addison
 * @since 3.0.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@NoArgsConstructor
@Getter
@EqualsAndHashCode
@AllArgsConstructor
@ToString(of = {"principal", "authenticationDate", "attributes"})
public class DefaultAuthentication implements Authentication {
    @Serial
    private static final long serialVersionUID = 3206127526058061391L;

    /**
     * Authentication date stamp.
     */
    private ZonedDateTime authenticationDate;

    /**
     * Authenticated principal.
     */
    private Principal principal;

    /**
     * Authentication messages and warnings.
     */
    private List<MessageDescriptor> warnings = new ArrayList<>();

    /**
     * List of metadata about credentials presented at authentication.
     */
    private List<Credential> credentials = new ArrayList<>();

    /**
     * Authentication metadata attributes.
     */
    private Map<String, List<Object>> attributes = new LinkedHashMap<>();

    /**
     * Map of handler name to handler authentication success event.
     */
    private Map<String, AuthenticationHandlerExecutionResult> successes;

    /**
     * Map of handler name to handler authentication failure cause.
     */
    private Map<String, Throwable> failures = new LinkedHashMap<>();

    @Override
    public void addAttribute(final String name, final Object value) {
        this.attributes.put(name, CollectionUtils.toCollection(value, ArrayList.class));
    }

    @Override
    public boolean containsAttribute(final String name) {
        return this.attributes.containsKey(name);
    }

    @Override
    public void updateAttributes(final Authentication authentication) {
        authenticationDate = authentication.getAuthenticationDate();

        val finalAuthnAttributes = CoreAuthenticationUtils.mergeAttributes(attributes, authentication.getAttributes());
        attributes.clear();
        attributes.putAll(finalAuthnAttributes);

        val finalPrincipalAttributes = CoreAuthenticationUtils.mergeAttributes(principal.getAttributes(), authentication.getPrincipal().getAttributes());
        principal.getAttributes().clear();
        principal.getAttributes().putAll(finalPrincipalAttributes);
    }

    @Override
    public void replaceAttributes(final Authentication authentication) {
        attributes.clear();
        principal.getAttributes().clear();
        updateAttributes(authentication);
    }

    @Override
    public boolean isEqualTo(final Authentication authn) {
        if (this == authn) {
            return true;
        }
        return Objects.equals(getPrincipal(), authn.getPrincipal())
            && Objects.equals(getCredentials(), authn.getCredentials())
            && Objects.equals(getSuccesses(), authn.getSuccesses());
    }

    @Override
    public <T> T getSingleValuedAttribute(final String name, final Class<T> expectedType) {
        if (containsAttribute(name)) {
            val values = getAttributes().get(name);
            return values
                .stream()
                .filter(Objects::nonNull)
                .findFirst()
                .map(expectedType::cast)
                .orElse(null);
        }
        return null;
    }
}
