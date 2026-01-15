package org.apereo.cas.authentication.policy;

import module java.base;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationPolicyExecutionResult;
import org.apereo.cas.authentication.CoreAuthenticationUtils;
import org.apereo.cas.util.RegexUtils;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jspecify.annotations.Nullable;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Authentication security policy that is satisfied iff
 * required attributes are found in {@link Authentication}
 * or {@link org.apereo.cas.authentication.principal.Principal}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Slf4j
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@NoArgsConstructor(force = true)
@EqualsAndHashCode(callSuper = true)
@Setter
@Getter
@RequiredArgsConstructor
public class RequiredAttributesAuthenticationPolicy extends BaseAuthenticationPolicy {
    @Serial
    private static final long serialVersionUID = 8901190843828760737L;

    private final Map<String, String> requiredAttributes;

    @Override
    public AuthenticationPolicyExecutionResult isSatisfiedBy(@Nullable final Authentication authentication,
                                                             final Set<AuthenticationHandler> authenticationHandlers,
                                                             final ConfigurableApplicationContext applicationContext,
                                                             final Map<String, ? extends Serializable> context) {
        if (authentication == null) {
            LOGGER.warn("Authentication attempt is null and cannot satisfy policy");
            return AuthenticationPolicyExecutionResult.failure();
        }

        val allAttributes = CoreAuthenticationUtils.mergeAttributes(authentication.getAttributes(),
            authentication.getPrincipal().getAttributes());
        val result = Objects.requireNonNull(requiredAttributes).entrySet().stream().allMatch(entry -> {
            var foundAttribute = allAttributes.containsKey(entry.getKey());
            if (foundAttribute) {
                val attributeValues = allAttributes.get(entry.getKey());
                foundAttribute = RegexUtils.findFirst(entry.getValue(), attributeValues).isPresent();
            }
            return foundAttribute;
        });
        return AuthenticationPolicyExecutionResult.success(result);
    }
}
