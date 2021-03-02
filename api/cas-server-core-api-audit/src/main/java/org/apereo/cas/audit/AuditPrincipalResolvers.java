package org.apereo.cas.audit;

/**
 * This is {@link AuditPrincipalResolvers}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
public interface AuditPrincipalResolvers {
    /**
     * Principal resolver id {@code REQUEST_CHANGE_PASSWORD_PRINCIPAL_RESOLVER}.
     */
    String REQUEST_CHANGE_PASSWORD_PRINCIPAL_RESOLVER = "REQUEST_CHANGE_PASSWORD_PRINCIPAL_RESOLVER";

    /**
     * Principal resolver id {@code REQUEST_FORGOT_USERNAME_PRINCIPAL_RESOLVER}.
     */
    String REQUEST_FORGOT_USERNAME_PRINCIPAL_RESOLVER = "REQUEST_FORGOT_USERNAME_PRINCIPAL_RESOLVER";
}
