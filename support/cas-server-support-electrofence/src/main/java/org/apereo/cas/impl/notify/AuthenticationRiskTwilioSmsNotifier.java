package org.apereo.cas.impl.notify;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.api.AuthenticationRiskNotifier;
import org.apereo.cas.api.AuthenticationRiskScore;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.core.authentication.RiskBasedAuthenticationProperties;
import org.apereo.cas.services.RegisteredService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * This is {@link AuthenticationRiskTwilioSmsNotifier}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class AuthenticationRiskTwilioSmsNotifier implements AuthenticationRiskNotifier {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationRiskTwilioSmsNotifier.class);

    @Autowired
    private CasConfigurationProperties casProperties;

    @Override
    public void notify(final Authentication authentication, final RegisteredService service, final AuthenticationRiskScore score) {

        final RiskBasedAuthenticationProperties.Response.Sms sms =
                casProperties.getAuthn().getAdaptive().getRisk().getResponse().getSms();

        final Principal principal = authentication.getPrincipal();

        if (StringUtils.isBlank(sms.getText()) || StringUtils.isBlank(sms.getFrom()) || StringUtils.isBlank(sms.getTwilio().getToken())
                || StringUtils.isBlank(sms.getTwilio().getAccountId()) || !principal.getAttributes().containsKey(sms.getAttributeName())) {
            LOGGER.debug("Could not send sms {} because either no phones could be found or sms settings are not configured.",
                    principal.getId());
            return;
        }

        try {
            Twilio.init(sms.getTwilio().getAccountId(), sms.getTwilio().getToken());
            Message.creator(
                    new PhoneNumber(principal.getAttributes().get(sms.getAttributeName()).toString()),
                    new PhoneNumber(sms.getFrom()),
                    sms.getText()).create();
        } catch (final Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }
}
