package org.apereo.cas.support.oauth.services;

import module java.base;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.configuration.support.RegularExpressionCapable;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.util.RegexUtils;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

/**
 * This is {@link DefaultRegisteredServiceOAuthTokenExchangePolicy}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@ToString
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@Accessors(chain = true)
@Slf4j
public class DefaultRegisteredServiceOAuthTokenExchangePolicy implements RegisteredServiceOAuthTokenExchangePolicy {
    @Serial
    private static final long serialVersionUID = 1415436756392637729L;

    @RegularExpressionCapable
    private Set<String> allowedResources;

    @RegularExpressionCapable
    private Set<String> allowedAudience;

    @RegularExpressionCapable
    private Set<String> allowedTokenTypes;

    @RegularExpressionCapable
    private Set<String> allowedActorTokenTypes;

    @RegularExpressionCapable
    private Map<String, List<String>> requiredActorTokenAttributes;

    @Override
    public boolean isTokenExchangeAllowed(final RegisteredService registeredService, final Set<String> resources,
                                          final Set<String> audience, final String requestedType) {
        val resourceAllowed = allowedResources == null || allowedResources.stream().anyMatch(resource -> RegexUtils.findFirst(resource, resources).isPresent());
        val audienceAllowed = allowedAudience == null || allowedAudience.stream().anyMatch(aud -> RegexUtils.findFirst(aud, audience).isPresent());
        val tokenTypeAllowed = allowedTokenTypes == null || allowedTokenTypes.stream().anyMatch(type -> RegexUtils.find(type, requestedType));
        val allowedExchange = resourceAllowed && audienceAllowed && tokenTypeAllowed;
        if (!allowedExchange) {
            LOGGER.warn("Token exchange is not allowed for service [{}] for resource [{}], audience [{}] or requested token type[{}]",
                registeredService.getName(), resources, audience, requestedType);
        }
        return allowedExchange;
    }

    @Override
    public boolean canSubjectTokenActAs(final Authentication subject, final Authentication actor, final String actorTokenType) {
        val actorTokenTypeAllowed = allowedActorTokenTypes == null || allowedActorTokenTypes.stream().anyMatch(type -> RegexUtils.find(type, actorTokenType));

        val availableAttributes = new HashMap<>(actor.getAttributes());
        availableAttributes.putAll(actor.getPrincipal().getAttributes());

        val actorTokenAllowed = requiredActorTokenAttributes == null || requiredActorTokenAttributes
            .entrySet()
            .stream()
            .allMatch(entry -> {
                val requiredAttributeValues = entry.getValue();
                val actorAttributeValues = availableAttributes.get(entry.getKey());
                return actorAttributeValues != null && requiredAttributeValues.stream().allMatch(value -> RegexUtils.findFirst(value, actorAttributeValues).isPresent());
            });
        return actorTokenTypeAllowed && actorTokenAllowed;
    }
}
