package org.apereo.cas.services;

import module java.base;
import org.apereo.cas.configuration.support.ExpressionLanguageCapable;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * This is {@link WebBasedRegisteredService}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public interface WebBasedRegisteredService extends RegisteredService {
    /**
     * Gets interrupt policy that is assigned to this service.
     *
     * @return the interrupt policy
     */
    RegisteredServiceWebflowInterruptPolicy getWebflowInterruptPolicy();

    /**
     * Gets passwordless authentication policy that is assigned to this service.
     *
     * @return the passwordless policy
     */
    RegisteredServicePasswordlessPolicy getPasswordlessPolicy();

    /**
     * Gets surrogate/impersonation authentication policy that is assigned to this service.
     *
     * @return the passwordless policy
     */
    RegisteredServiceSurrogatePolicy getSurrogatePolicy();

    /**
     * Get the acceptable usage policy linked to this application.
     *
     * @return an instance of {@link RegisteredServiceAcceptableUsagePolicy}
     */
    RegisteredServiceAcceptableUsagePolicy getAcceptableUsagePolicy();

    /**
     * Gets SSO participation strategy.
     *
     * @return the service ticket expiration policy
     */
    RegisteredServiceSingleSignOnParticipationPolicy getSingleSignOnParticipationPolicy();

    /**
     * Gets the logo image associated with this service.
     * The image mostly is served on the user interface
     * to identify this requesting service during authentication.
     *
     * @return URL of the image
     * @since 4.1
     */
    String getLogo();

    /**
     * Describes the canonical information url
     * where this service is advertised and may provide
     * help/guidance.
     *
     * @return the info url.
     */
    String getInformationUrl();

    /**
     * Links to the privacy policy of this service, if any.
     *
     * @return the link to privacy policy
     */
    String getPrivacyUrl();

    /**
     * Returns the logout type of the service.
     *
     * @return the logout type of the service.
     */
    RegisteredServiceLogoutType getLogoutType();

    /**
     * Identifies the logout url that will be invoked
     * upon sending single-logout callback notifications.
     * This is an optional setting. When undefined, the service
     * url as is defined by {@link #getServiceId()} will be used
     * to handle logout invocations.
     *
     * @return the logout url for this service
     * @since 4.1
     */
    String getLogoutUrl();

    /**
     * Returns a short theme name. Services do not need to have unique theme
     * names.
     *
     * @return the theme name associated with this service.
     */
    @ExpressionLanguageCapable
    String getTheme();

    /**
     * Returns a locale name to be activated when this service is used.
     *
     * @return the locale name associated with this service.
     */
    @ExpressionLanguageCapable
    String getLocale();

}
