package org.apereo.cas.support.oauth.profile;

import module java.base;
import lombok.Getter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;

/**
 * This is {@link DefaultOAuth20UserProfile}, a profile that CAS builds itself rather than reads
 * from the authentication of the request.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@ToString
public class DefaultOAuth20UserProfile implements OAuth20UserProfile {

    @Serial
    private static final long serialVersionUID = -4626453931557154585L;

    private final Map<String, Object> attributes = new HashMap<>();

    private final Map<String, Object> authenticationAttributes = new HashMap<>();

    private final Set<String> roles = new HashSet<>();

    @Getter
    private final String authenticationHandlerName;

    @Getter
    private String id;

    public DefaultOAuth20UserProfile(final String id) {
        this(id, DefaultOAuth20UserProfile.class.getCanonicalName());
    }

    public DefaultOAuth20UserProfile(final String id, final String authenticationHandlerName) {
        this.id = requireNotBlank(id);
        this.authenticationHandlerName = authenticationHandlerName;
    }

    @Override
    public void setId(final String id) {
        this.id = requireNotBlank(id);
    }

    @Override
    public Map<String, Object> getAttributes() {
        return new HashMap<>(attributes);
    }

    @Override
    public @Nullable Object getAttribute(final String name) {
        return attributes.get(name);
    }

    @Override
    public boolean containsAttribute(final String name) {
        return attributes.containsKey(name);
    }

    @Override
    public void addAttribute(final String name, final @Nullable Object value) {
        putAttribute(attributes, name, value);
    }

    @Override
    public Map<String, Object> getAuthenticationAttributes() {
        return new HashMap<>(authenticationAttributes);
    }

    @Override
    public @Nullable Object getAuthenticationAttribute(final String name) {
        return authenticationAttributes.get(name);
    }

    /**
     * Adds an authentication attribute.
     *
     * @param name  the name
     * @param value the value; ignored when null
     */
    public void addAuthenticationAttribute(final String name, final @Nullable Object value) {
        putAttribute(authenticationAttributes, name, value);
    }

    /**
     * Adds authentication attributes.
     *
     * @param attributes the attributes
     */
    public void addAuthenticationAttributes(final Map<String, ?> attributes) {
        attributes.forEach(this::addAuthenticationAttribute);
    }

    @Override
    public Set<String> getRoles() {
        return new LinkedHashSet<>(roles);
    }

    @Override
    public void addRoles(final Collection<String> roles) {
        this.roles.addAll(roles);
    }

    private static String requireNotBlank(final String id) {
        if (StringUtils.isBlank(id)) {
            throw new IllegalArgumentException("Profile identifier cannot be blank");
        }
        return id;
    }

    private static void putAttribute(final Map<String, Object> map, final String name, final @Nullable Object value) {
        if (value != null) {
            if (map.get(name) instanceof final Collection<?> existing && value instanceof final Collection<?> values) {
                map.put(name, Stream.concat(existing.stream(), values.stream()).distinct().collect(Collectors.toList()));
            } else {
                map.put(name, value);
            }
        }
    }
}
