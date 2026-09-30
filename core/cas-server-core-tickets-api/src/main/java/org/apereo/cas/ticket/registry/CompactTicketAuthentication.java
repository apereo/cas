package org.apereo.cas.ticket.registry;

import module java.base;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationManager;
import org.apereo.cas.authentication.CoreAuthenticationUtils;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.DefaultAuthenticationBuilder;
import org.apereo.cas.authentication.DefaultAuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.RememberMeCredential;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.util.DateTimeUtils;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.apache.commons.lang3.BooleanUtils;
import org.jspecify.annotations.Nullable;

/**
 * Compact form of an authentication, shared by ticket compactors: principal id, authentication date,
 * successful handlers, credential types and remember-me. Principal and authentication attributes are not kept.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@UtilityClass
public class CompactTicketAuthentication {
    /**
     * Number of fields an authentication takes.
     */
    public static final int FIELD_COUNT = 5;

    /**
     * Append the authentication fields.
     *
     * @param fields         the fields
     * @param ticketAuthentication the authentication
     */
    public static void compact(final List<String> fields, final @Nullable Authentication ticketAuthentication) {
        val authentication = Objects.requireNonNull(ticketAuthentication, "Ticket must carry an authentication");
        fields.add(authentication.getPrincipal().getId());
        fields.add(String.valueOf(authentication.getAuthenticationDate().toEpochSecond()));
        fields.add(CompactTicketCodec.encodeValues(authentication.getSuccesses().keySet()));
        fields.add(CompactTicketCodec.encodeValues(getCredentialTypes(authentication)));
        fields.add(BooleanUtils.toString(CoreAuthenticationUtils.isRememberMeAuthentication(authentication), "1", "0"));
    }

    /**
     * Build the authentication from the fields starting at the given position.
     *
     * @param principalFactory the principal factory
     * @param fields           the fields
     * @param start            the position of the first authentication field
     * @return the authentication
     * @throws Throwable the throwable
     */
    public static Authentication expand(final PrincipalFactory principalFactory,
                                        final List<String> fields, final int start) throws Throwable {
        val principal = Objects.requireNonNull(principalFactory.createPrincipal(fields.get(start)));
        val authenticationDate = DateTimeUtils.zonedDateTimeOf(Instant.ofEpochSecond(Long.parseLong(fields.get(start + 1))));
        val handlers = new LinkedHashSet<>(CompactTicketCodec.decodeValues(fields.get(start + 2)));
        val credentialTypes = new LinkedHashSet<>(CompactTicketCodec.decodeValues(fields.get(start + 3)));
        val rememberMe = BooleanUtils.toBoolean(fields.get(start + 4));
        return DefaultAuthenticationBuilder
            .newInstance()
            .setPrincipal(principal)
            .setAuthenticationDate(authenticationDate)
            .addAttribute(RememberMeCredential.AUTHENTICATION_ATTRIBUTE_REMEMBER_ME, rememberMe)
            .addAttribute(Credential.CREDENTIAL_TYPE_ATTRIBUTE, credentialTypes)
            .addAttribute(AuthenticationHandler.SUCCESSFUL_AUTHENTICATION_HANDLERS, handlers)
            .setSuccesses(handlers.stream().collect(Collectors.toMap(Function.identity(),
                name -> new DefaultAuthenticationHandlerExecutionResult(name, principal))))
            .addAttribute(AuthenticationManager.AUTHENTICATION_METHOD_ATTRIBUTE, handlers)
            .build();
    }

    private static Collection<String> getCredentialTypes(final Authentication authentication) {
        if (!authentication.getCredentials().isEmpty()) {
            return authentication.getCredentials().stream()
                .map(credential -> credential.getClass().getSimpleName())
                .collect(Collectors.toCollection(LinkedHashSet::new));
        }
        return Optional.ofNullable(authentication.getAttributes().get(Credential.CREDENTIAL_TYPE_ATTRIBUTE))
            .stream()
            .flatMap(List::stream)
            .map(Object::toString)
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
