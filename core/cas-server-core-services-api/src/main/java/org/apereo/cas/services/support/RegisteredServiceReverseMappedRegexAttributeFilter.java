package org.apereo.cas.services.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * A filtering policy that selectively applies patterns to attributes mapped in the config.
 * If an attribute is mapped, it's only allowed to be released if it does not match the linked pattern.
 * If an attribute is not mapped, it may optionally be excluded from the released set of attributes.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class RegisteredServiceReverseMappedRegexAttributeFilter extends RegisteredServiceMappedRegexAttributeFilter {

    private static final long serialVersionUID = 852145306984610128L;

    private static final Logger LOGGER = LoggerFactory.getLogger(RegisteredServiceReverseMappedRegexAttributeFilter.class);

    @Override
    protected List<Object> filterAttributeValuesByPattern(final Set<Object> attributeValues, final Pattern pattern) {
        return attributeValues.stream()
                .filter(v -> {
                    LOGGER.debug("Matching attribute value [{}] against pattern [{}]", v, pattern.pattern());
                    final Matcher matcher = pattern.matcher(v.toString());
                    if (isCompleteMatch()) {
                        return !matcher.matches();
                    }
                    return !matcher.find();
                })
                .collect(Collectors.toList());
    }
}
