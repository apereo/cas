package org.apereo.cas.ticket.registry.compact;

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
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.DateTimeUtils;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;

/**
 * Compact form of an authentication, shared by ticket compactors: principal id, authentication date,
 * successful handlers, credential types, remember-me and the authentication attributes that applications
 * rely on during ticket validation (the delegated identity provider name and the multifactor authentication context),
 * kept as strings. Principal attributes and other authentication attributes are not kept.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@UtilityClass
public class CompactTicketAuthentication {
    /**
     * Number of fields an authentication takes.
     */
    public static final int FIELD_COUNT = 6;

    /**
     * Authentication attribute that carries the name of the delegated identity provider.
     */
    public static final String CLIENT_NAME_ATTRIBUTE = "clientName";

    private static final int RETAINED_ATTRIBUTES_OFFSET = 5;

    /**
     * Names of the authentication attributes kept in the compact form: the delegated identity provider name,
     * and the multifactor authentication context and trusted device attributes as configured.
     *
     * @param casProperties the cas properties
     * @return the attribute names
     */
    public static Set<String> getRetainedAuthenticationAttributes(final CasConfigurationProperties casProperties) {
        val mfa = casProperties.getAuthn().getMfa();
        val names = new LinkedHashSet<String>();
        names.add(CLIENT_NAME_ATTRIBUTE);
        names.addAll(org.springframework.util.StringUtils.commaDelimitedListToSet(mfa.getCore().getAuthenticationContextAttribute()));
        names.add(mfa.getTrusted().getCore().getAuthenticationContextAttribute());
        names.removeIf(StringUtils::isBlank);
        return names;
    }

    /**
     * Append the authentication fields.
     *
     * @param fields               the fields
     * @param ticketAuthentication the authentication
     * @param retainedAttributes   the names of the authentication attributes to keep
     */
    public static void compact(final List<String> fields, final @Nullable Authentication ticketAuthentication,
                               final Collection<String> retainedAttributes) {
        val authentication = Objects.requireNonNull(ticketAuthentication, "Ticket must carry an authentication");
        fields.add(authentication.getPrincipal().getId());
        fields.add(String.valueOf(authentication.getAuthenticationDate().toEpochSecond()));
        fields.add(CompactTicketCodec.encodeValues(authentication.getSuccesses().keySet()));
        fields.add(CompactTicketCodec.encodeValues(getCredentialTypes(authentication)));
        fields.add(BooleanUtils.toString(CoreAuthenticationUtils.isRememberMeAuthentication(authentication), "1", "0"));
        val attributes = new ArrayList<String>();
        retainedAttributes.forEach(name -> {
            val values = authentication.getAttributes().get(name);
            if (values != null && !values.isEmpty()) {
                attributes.add(name);
                attributes.add(CompactTicketCodec.encodeValues(values.stream().map(String::valueOf).toList()));
            }
        });
        fields.add(CompactTicketCodec.encodeValues(attributes));
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
                                        final List<String> fields, final int start)
            throws Throwable {
        val principal = Objects.requireNonNull(principalFactory.createPrincipal(fields.get(start)));
        val authenticationDate = DateTimeUtils.zonedDateTimeOf(Instant.ofEpochSecond(Long.parseLong(fields.get(start + 1))));
        val handlers = new LinkedHashSet<>(CompactTicketCodec.decodeValues(fields.get(start + 2)));
        val credentialTypes = new LinkedHashSet<>(CompactTicketCodec.decodeValues(fields.get(start + 3)));
        val rememberMe = BooleanUtils.toBoolean(fields.get(start + 4));
        val retainedAttributes = CompactTicketCodec.decodeValues(fields.get(start + RETAINED_ATTRIBUTES_OFFSET));
        if (retainedAttributes.size() % 2 != 0) {
            throw new IllegalArgumentException("Invalid compact authentication attributes");
        }
        val builder = DefaultAuthenticationBuilder.newInstance();
        for (var index = 0; index < retainedAttributes.size(); index += 2) {
            builder.addAttribute(retainedAttributes.get(index),
                new ArrayList<>(CompactTicketCodec.decodeValues(retainedAttributes.get(index + 1))));
        }
        return builder
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
