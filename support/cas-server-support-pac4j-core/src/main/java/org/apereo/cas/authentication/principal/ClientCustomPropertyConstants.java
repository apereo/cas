package org.apereo.cas.authentication.principal;

/**
 * This is {@link ClientCustomPropertyConstants}.
 *
 * @author Misagh Moayyed
 * @since 5.3.4
 */
public interface ClientCustomPropertyConstants {
    /**
     * Property to indicate the principal attribute used for profile creation.
     */
    String CLIENT_CUSTOM_PROPERTY_PRINCIPAL_ATTRIBUTE_ID = "principalAttributeId";
    /**
     * Property to indicate whether auto-redirect should execute for this client.
     */
    String CLIENT_CUSTOM_PROPERTY_AUTO_REDIRECT = "autoRedirect";
}
