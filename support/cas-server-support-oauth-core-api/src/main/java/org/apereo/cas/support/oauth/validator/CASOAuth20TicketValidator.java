package org.apereo.cas.support.oauth.validator;

import module java.base;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.CoreAuthenticationUtils;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.profile.DefaultOAuth20UserProfile;
import org.apereo.cas.support.oauth.profile.OAuth20UserProfile;
import org.apereo.cas.validation.AuthenticationAttributeReleasePolicy;
import org.apereo.cas.validation.TicketValidator;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

/**
 * This is {@link CASOAuth20TicketValidator}, which validates the service ticket that the login handshake of the
 * authorization endpoint receives at its callback and builds the profile of the user who signed in.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@RequiredArgsConstructor
public class CASOAuth20TicketValidator {
    private static final String TYPED_ID_PROFILE_CLASS_PREFIX = "org.pac4j.";

    private static final String TYPED_ID_SEPARATOR = "#";

    private final TicketValidator validator;

    private final AuthenticationAttributeReleasePolicy authenticationAttributeReleasePolicy;

    /**
     * Validate the service ticket issued for the service and build the profile of the user. Its attributes are the
     * principal attributes merged into those of the original authentication, with the assertion context and the
     * {@link OAuth20Constants#CAS_OAUTH_STATELESS_PROPERTY} flag; its authentication attributes are those the release
     * policy allows for the service. Single values are unwrapped from their lists and every value is kept as released; the
     * attribute converters that the CAS profile of pac4j applied, which kept only the first value of attributes such as
     * {@code email} and turned others into locales or URIs, no longer run. A principal identifier typed by delegated
     * authentication, such as {@code org.pac4j.oauth.profile.github.GitHubProfile#12345}, loses its profile type, as pac4j
     * removed it, so the subject stays {@code 12345}. The authentication handler name is
     * {@link OAuth20UserProfile#BASIC_PROFILE_HANDLER_NAME}. The result also tells whether the ticket comes from a new login.
     *
     * @param ticket  the service ticket
     * @param service the service the ticket was issued for
     * @return the profile of the user, and whether the ticket comes from a new login
     * @throws Throwable when the ticket cannot be validated for the service
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ValidatedServiceTicket validate(final String ticket, final String service) throws Throwable {
        val validationResult = validator.validate(ticket, service);
        val assertion = validationResult.getAssertion();

        val principalAttributes = new HashMap(validationResult.getPrincipal().getAttributes());
        principalAttributes.put(OAuth20Constants.CAS_OAUTH_STATELESS_PROPERTY, assertion.isStateless());
        principalAttributes.putAll(assertion.getContext());
        val originalAttributes = Optional.ofNullable(assertion.getOriginalAuthentication())
            .map(Authentication::getPrincipal)
            .map(Principal::getAttributes)
            .orElseGet(HashMap::new);
        val finalAttributes = CoreAuthenticationUtils.mergeAttributes(originalAttributes, principalAttributes);
        val registeredService = validationResult.getRegisteredService();
        val authenticationAttributes = authenticationAttributeReleasePolicy.getAuthenticationAttributesForRelease(
            assertion.getPrimaryAuthentication(), assertion, new HashMap<>(), registeredService);

        val profile = new DefaultOAuth20UserProfile(sanitizeIdentifier(validationResult.getPrincipal().getId()),
            OAuth20UserProfile.BASIC_PROFILE_HANDLER_NAME);
        profile.addAttributes(CoreAuthenticationUtils.convertAttributeValuesToObjects(finalAttributes));
        profile.addAuthenticationAttributes(CoreAuthenticationUtils.convertAttributeValuesToObjects(authenticationAttributes));
        return new ValidatedServiceTicket(profile, assertion.isFromNewLogin());
    }

    private static String sanitizeIdentifier(final String identifier) {
        val type = StringUtils.substringBefore(identifier, TYPED_ID_SEPARATOR);
        return identifier.contains(TYPED_ID_SEPARATOR) && type.startsWith(TYPED_ID_PROFILE_CLASS_PREFIX)
            ? identifier.substring(type.length() + 1)
            : identifier;
    }

    /**
     * The profile of the user who signed in, and whether the service ticket comes from a login in which the user presented
     * credentials, rather than from an existing single sign-on session.
     *
     * @param profile      the profile of the user
     * @param fromNewLogin whether the service ticket comes from a new login
     */
    public record ValidatedServiceTicket(OAuth20UserProfile profile, boolean fromNewLogin) {
    }
}
