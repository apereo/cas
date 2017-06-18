package org.apereo.cas.services;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apereo.cas.authentication.principal.Principal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Return only the collection of allowed attributes out of what's resolved
 * for the principal.
 *
 * @author Misagh Moayyed
 * @since 4.1.0
 */
public class ReturnAllowedAttributeReleasePolicy extends AbstractRegisteredServiceAttributeReleasePolicy {

    private static final long serialVersionUID = -5771481877391140569L;

    private static final Logger LOGGER = LoggerFactory.getLogger(ReturnAllowedAttributeReleasePolicy.class);

    private List<String> allowedAttributes;

    /**
     * Instantiates a new Return allowed attribute release policy.
     */
    public ReturnAllowedAttributeReleasePolicy() {
        this(new ArrayList<>());
    }

    /**
     * Instantiates a new Return allowed attribute release policy.
     *
     * @param allowedAttributes the allowed attributes
     */
    public ReturnAllowedAttributeReleasePolicy(final List<String> allowedAttributes) {
        setAllowedAttributes(allowedAttributes);
    }

    public void setAllowedAttributes(final List<String> allowed) {
        this.allowedAttributes = allowed;
    }

    public List<String> getAllowedAttributes() {
        return this.allowedAttributes;
    }

    @Override
    protected Map<String, Object> getAttributesInternal(final Principal principal,
                                                        final Map<String, Object> attrs, final RegisteredService service) {
        return authorizeReleaseOfAllowedAttributes(attrs);
    }

    /**
     * Authorize release of allowed attributes map.
     *
     * @param attrs the attrs
     * @return the map
     */
    protected Map<String, Object> authorizeReleaseOfAllowedAttributes(final Map<String, Object> attrs) {
        final Map<String, Object> resolvedAttributes = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        resolvedAttributes.putAll(attrs);

        return getAllowedAttributes().stream()
                .map(attr -> new Object[]{attr, resolvedAttributes.get(attr)})
                .filter(pair -> pair[1] != null)
                .peek(attribute -> LOGGER.debug("Found attribute [{}] in the list of allowed attributes", attribute[0]))
                .collect(Collectors.toMap(attributes -> (String) attributes[0], attributes -> attributes[1]));
    }


    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        final ReturnAllowedAttributeReleasePolicy rhs = (ReturnAllowedAttributeReleasePolicy) obj;
        return new EqualsBuilder()
                .appendSuper(super.equals(obj))
                .append(getAllowedAttributes(), rhs.getAllowedAttributes())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(13, 133)
                .appendSuper(super.hashCode())
                .append(getAllowedAttributes())
                .toHashCode();
    }


    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .append("allowedAttributes", getAllowedAttributes())
                .toString();
    }
}
