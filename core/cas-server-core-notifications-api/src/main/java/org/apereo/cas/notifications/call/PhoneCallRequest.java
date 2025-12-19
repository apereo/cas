package org.apereo.cas.notifications.call;

import module java.base;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.function.FunctionUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.With;
import lombok.experimental.SuperBuilder;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

/**
 * This is {@link PhoneCallRequest}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@SuperBuilder
@Getter
@With
@RequiredArgsConstructor
public class PhoneCallRequest {
    private final Principal principal;

    private final String attribute;

    private final String text;

    private final String from;

    private final String to;

    /**
     * Has attribute value for the principal?
     *
     * @return true/false
     */
    public boolean hasAttributeValue() {
        return StringUtils.isNotBlank(attribute) && principal.getAttributes().containsKey(attribute);
    }

    /**
     * Gets the first attribute value from the principal.
     *
     * @return the attribute value
     */
    public Optional<Object> getAttributeValue() {
        val value = principal.getAttributes().get(attribute);
        return CollectionUtils.firstElement(value);
    }

    /**
     * Gets recipients by first using the attribute
     * from the principal. If none found or empty value,
     * uses the provided address.
     *
     * @return the recipients
     */
    public String getRecipient() {
        return FunctionUtils.doIf(hasAttributeValue(),
            () -> getAttributeValue().map(Object::toString).orElseGet(this::getTo),
            this::getTo).get();
    }

    /**
     * Whether this request contains sufficient data to proceed.
     *
     * @return true/false
     */
    public boolean isSufficient() {
        return StringUtils.isNotBlank(getText()) && StringUtils.isNotBlank(getRecipient());
    }
}
