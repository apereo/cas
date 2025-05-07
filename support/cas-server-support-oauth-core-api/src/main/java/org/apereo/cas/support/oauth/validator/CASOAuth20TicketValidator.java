package org.apereo.cas.support.oauth.validator;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.client.authentication.AttributePrincipalImpl;
import org.apereo.cas.client.validation.Assertion;
import org.apereo.cas.client.validation.AssertionImpl;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.validation.AuthenticationAttributeReleasePolicy;
import org.apereo.cas.validation.TicketValidator;

import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * This is {@link CASOAuth20TicketValidator}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@RequiredArgsConstructor
public class CASOAuth20TicketValidator implements org.apereo.cas.client.validation.TicketValidator {
    private final TicketValidator validator;

    private final AuthenticationAttributeReleasePolicy authenticationAttributeReleasePolicy;

    @Override
    public Assertion validate(final String ticket, final String service) {
        val validationResult = FunctionUtils.doUnchecked(() -> validator.validate(ticket, service));
        val assertion = validationResult.getAssertion();

        val principalAttributes = new HashMap(validationResult.getPrincipal().getAttributes());
        principalAttributes.put("stateless", validationResult.getAssertion().isStateless());
        
        principalAttributes.putAll(assertion.getContext());
        val originalAttributes = Optional.ofNullable(assertion.getOriginalAuthentication())
            .map(Authentication::getPrincipal)
            .map(Principal::getAttributes)
            .orElseGet(HashMap::new);

        principalAttributes.putAll(originalAttributes);
        val attrPrincipal = new AttributePrincipalImpl(validationResult.getPrincipal().getId(), principalAttributes);
        val registeredService = validationResult.getRegisteredService();

        val authenticationAttributes = authenticationAttributeReleasePolicy.getAuthenticationAttributesForRelease(
            assertion.getPrimaryAuthentication(), assertion, new HashMap<>(0), registeredService);
        return new AssertionImpl(attrPrincipal, (Map) authenticationAttributes, assertion.getContext());
    }
}
