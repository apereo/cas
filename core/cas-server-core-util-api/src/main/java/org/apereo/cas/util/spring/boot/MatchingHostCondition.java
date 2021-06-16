package org.apereo.cas.util.spring.boot;

import org.apereo.cas.util.InetAddressUtils;
import org.apereo.cas.util.RegexUtils;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * This is {@link MatchingHostCondition}.
 *
 * @author Hal Deadman
 * @since 6.4.0
 */
public class MatchingHostCondition extends SpringBootCondition {

    @Override
    public ConditionOutcome getMatchOutcome(final ConditionContext context, final AnnotatedTypeMetadata metadata) {
        val attributes = metadata.getAnnotationAttributes(ConditionalOnMatchingHostname.class.getName());
        if (attributes == null) {
            return ConditionOutcome.match("No annotation attributes found");
        }
        val name = attributes.get("name").toString();
        val hostnameToMatch = context.getEnvironment().getProperty(name);
        if (StringUtils.isBlank(hostnameToMatch)) {
            return ConditionOutcome.match("No hostname set with property: " + name);
        }
        val pattern = RegexUtils.createPattern(hostnameToMatch);
        val matcher = pattern.matcher(InetAddressUtils.getCasServerHostName());
        if (matcher.find()) {
            return ConditionOutcome.match("Hostname matches value for " + name);
        }
        return ConditionOutcome.noMatch("Hostname doesn't match value for " + name);
    }

}
