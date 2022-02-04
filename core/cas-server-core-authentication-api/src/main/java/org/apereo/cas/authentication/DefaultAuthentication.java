package org.apereo.cas.authentication;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.util.CollectionUtils;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.builder.EqualsBuilder;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
public class DefaultAuthentication implements Authentication {
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
    private List<CredentialMetaData> credentials = new ArrayList<>();

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
    public void update(final Authentication authn) {
        this.attributes.putAll(authn.getAttributes());
        this.authenticationDate = authn.getAuthenticationDate();
    }

    @Override
    public void updateAll(final Authentication authn) {
        this.attributes.clear();
        update(authn);
    }

    @Override
    public void addAttribute(final String name, final Object value) {
        this.attributes.put(name, CollectionUtils.toCollection(value, ArrayList.class));
    }

    @Override
    public boolean isEqualTo(final Authentication auth2) {
        if (auth2 == null) {
            return false;
        }
        val builder = new EqualsBuilder();
        builder.append(getPrincipal(), auth2.getPrincipal());
        builder.append(getCredentials(), auth2.getCredentials());
        builder.append(getSuccesses(), auth2.getSuccesses());
        return builder.isEquals();
    }
}
