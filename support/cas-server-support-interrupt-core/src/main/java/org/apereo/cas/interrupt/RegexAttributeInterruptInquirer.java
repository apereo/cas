package org.apereo.cas.interrupt;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.util.CollectionUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.webflow.execution.RequestContext;

import java.util.HashMap;

/**
 * This is {@link RegexAttributeInterruptInquirer}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RequiredArgsConstructor
@Slf4j
public class RegexAttributeInterruptInquirer extends BaseInterruptInquirer {
    private final String interruptAttributeName;
    private final String interruptAttributeValue;

    @Override
    protected InterruptResponse inquireInternal(final Authentication authentication, final RegisteredService registeredService,
                                                final Service service, final Credential credential,
                                                final RequestContext requestContext) {
        val attributes = new HashMap<String, Object>(authentication.getAttributes());
        attributes.putAll(authentication.getPrincipal().getAttributes());

        LOGGER.debug("Looking for [{}] in attributes [{}]", this.interruptAttributeName, attributes);
        val result = attributes.entrySet()
            .stream()
            .filter(entry -> entry.getKey().matches(this.interruptAttributeName))
            .filter(entry -> {
                val values = CollectionUtils.toCollection(entry.getValue());
                LOGGER.debug("Located attribute [{}] with values [{}]. Checking for match against [{}]",
                    this.interruptAttributeName, values, this.interruptAttributeValue);
                return values.stream().anyMatch(value -> value.toString().matches(this.interruptAttributeValue));
            })
            .findAny();
        if (result.isPresent()) {
            return InterruptResponse.interrupt();
        }
        return InterruptResponse.none();
    }
}
