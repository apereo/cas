package org.apereo.cas.ticket.idtoken;

/**
 * This is {@link IdTokenGeneratorService}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@FunctionalInterface
public interface IdTokenGeneratorService {
    /**
     * Generate ID token.
     *
     * @param context the context
     * @return the string
     * @throws Throwable the throwable
     */
    OidcIdToken generate(IdTokenGenerationContext context) throws Throwable;
}
