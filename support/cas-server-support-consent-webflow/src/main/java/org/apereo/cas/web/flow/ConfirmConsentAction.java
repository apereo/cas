package org.apereo.cas.web.flow;

import module java.base;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.attribute.AttributeDefinitionStore;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.consent.ConsentEngine;
import org.apereo.cas.consent.ConsentReminderOptions;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.web.support.WebUtils;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jspecify.annotations.Nullable;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import jakarta.servlet.http.HttpServletRequest;

/**
 * This is {@link ConfirmConsentAction}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
public class ConfirmConsentAction extends AbstractConsentAction {

    public ConfirmConsentAction(final ServicesManager servicesManager,
                                final AuthenticationServiceSelectionPlan strategies,
                                final ConsentEngine consentEngine,
                                final CasConfigurationProperties casProperties,
                                final AttributeDefinitionStore attributeDefinitionStore) {
        super(casProperties, servicesManager, strategies, consentEngine, attributeDefinitionStore);
    }

    @Override
    protected @Nullable Event doExecuteInternal(final RequestContext requestContext) throws Throwable {
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
        val webService = WebUtils.getService(requestContext);
        val service = this.authenticationRequestServiceSelectionStrategies.resolveService(webService);
        val registeredService = getRegisteredServiceForConsent(requestContext, service);
        val authentication = WebUtils.getAuthentication(requestContext);

        val consentProperties = casProperties.getConsent().getCore();
        val option = resolveConsentReminderOptions(request);
        var unit = resolveReminderTimeUnit(request);
        var reminder = resolveReminder(request);
        if (!canApplyReminder(reminder, unit)) {
            LOGGER.warn("Consent reminder [{}] with time unit [{}] cannot be applied to a consent decision; "
                + "falling back on [{}] with time unit [{}]", reminder, unit,
                consentProperties.getReminder(), consentProperties.getReminderTimeUnit());
            reminder = consentProperties.getReminder();
            unit = consentProperties.getReminderTimeUnit();
        }

        LOGGER.debug("Storing consent decision for service [{}]", service);
        consentEngine.storeConsentDecision(service, registeredService, authentication, reminder, unit, option);
        return eventFactory.success(this);
    }

    /**
     * Resolve the consent reminder options submitted with the request.
     * The value arrives as a request parameter and is not trusted; anything that is not a known
     * option falls back on the default, rather than failing the consent submission outright.
     *
     * @param request the request
     * @return the consent reminder options
     */
    protected ConsentReminderOptions resolveConsentReminderOptions(final HttpServletRequest request) {
        val parameter = request.getParameter("option");
        try {
            return ConsentReminderOptions.valueOf(Integer.parseInt(parameter));
        } catch (final Exception e) {
            LOGGER.warn("Unable to determine consent reminder options from [{}]; falling back on [{}]",
                parameter, ConsentReminderOptions.ATTRIBUTE_NAME);
            return ConsentReminderOptions.ATTRIBUTE_NAME;
        }
    }

    /**
     * Resolve the reminder time unit submitted with the request.
     * The value arrives as a request parameter and is not trusted; anything that is not a
     * {@link ChronoUnit} falls back on the globally configured time unit.
     *
     * @param request the request
     * @return the reminder time unit
     */
    protected ChronoUnit resolveReminderTimeUnit(final HttpServletRequest request) {
        val parameter = request.getParameter("reminderTimeUnit");
        try {
            return ChronoUnit.valueOf(parameter.toUpperCase(Locale.ENGLISH));
        } catch (final Exception e) {
            val configured = casProperties.getConsent().getCore().getReminderTimeUnit();
            LOGGER.warn("Unable to determine consent reminder time unit from [{}]; falling back on [{}]", parameter, configured);
            return configured;
        }
    }

    /**
     * Resolve the reminder amount submitted with the request.
     * The value arrives as a request parameter and is not trusted; anything that is not a number
     * falls back on the globally configured reminder.
     *
     * @param request the request
     * @return the reminder
     */
    protected long resolveReminder(final HttpServletRequest request) {
        val parameter = request.getParameter("reminder");
        try {
            return Long.parseLong(parameter);
        } catch (final Exception e) {
            val configured = casProperties.getConsent().getCore().getReminder();
            LOGGER.warn("Unable to determine consent reminder from [{}]; falling back on [{}]", parameter, configured);
            return configured;
        }
    }

    /**
     * Can the reminder be applied to a consent decision?
     * A reminder is only usable if it moves a decision's creation date forward into a real date;
     * a negative amount, an amount that overflows the calendar, or a unit such as {@code FOREVER}
     * or {@code ERAS} would be stored happily and then fail on every subsequent login.
     *
     * @param reminder the reminder
     * @param unit     the reminder time unit
     * @return true/false
     */
    protected static boolean canApplyReminder(final long reminder, final ChronoUnit unit) {
        if (reminder < 0) {
            return false;
        }
        try {
            LocalDateTime.now(ZoneOffset.UTC).plus(reminder, unit);
            return true;
        } catch (final Exception e) {
            return false;
        }
    }
}
