package org.apereo.cas.authentication.principal;

/**
 * This is {@link ClientCustomPropertyConstants}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
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

    /**
     * CSS class assigned to this client to be used in the UI.
     */
    String CLIENT_CUSTOM_PROPERTY_CSS_CLASS = "cssClass";
}
