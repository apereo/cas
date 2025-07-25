package org.apereo.cas.notifications.sms;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.util.CollectionUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.With;
import lombok.experimental.SuperBuilder;
import lombok.val;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link SmsRequest}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@SuperBuilder
@Getter
@With
@RequiredArgsConstructor
public class SmsRequest {
    private final Principal principal;

    private final String attribute;

    private final String text;

    private final String from;

    private final List<String> to;

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
    public List<String> getAttributeValue() {
        val value = principal.getAttributes().get(attribute);
        return CollectionUtils.toCollection(value, ArrayList.class);
    }

    /**
     * Gets recipients by first using the attribute
     * from the principal. If none found or empty value,
     * uses the provided address.
     *
     * @return the recipients
     */
    public List<String> getRecipients() {
        return hasAttributeValue() ? getAttributeValue() : ObjectUtils.getIfNull(getTo(), List.of());
    }

    /**
     * Whether this request contains sufficient data to proceed.
     *
     * @return true/false
     */
    public boolean isSufficient() {
        return StringUtils.isNotBlank(getText()) && !getRecipients().isEmpty();
    }
    
}
