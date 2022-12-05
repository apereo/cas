package org.apereo.cas.notifications.mail;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.configuration.model.support.email.EmailProperties;
import org.apereo.cas.util.CollectionUtils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.With;
import lombok.experimental.SuperBuilder;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * This is {@link EmailMessageRequest}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@SuperBuilder
@Getter
@With
@RequiredArgsConstructor
public class EmailMessageRequest {
    private final Principal principal;

    private final String attribute;

    private final EmailProperties emailProperties;

    private final String body;

    private final List<String> to;

    private final Locale locale;

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
    public List<String> getRecipients() {
        if (hasAttributeValue()) {
            val value = getAttributeValue();
            if (value.isPresent()) {
                return CollectionUtils.toCollection(value.get(), ArrayList.class);
            }
        }
        return getTo();
    }
}
