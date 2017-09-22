package org.apereo.cas.impl.notify;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.configuration.model.support.sms.SmsProperties;
import org.apereo.cas.util.io.CommunicationsManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is {@link AuthenticationRiskTwilioSmsNotifier}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class AuthenticationRiskTwilioSmsNotifier extends BaseAuthenticationRiskNotifier {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationRiskTwilioSmsNotifier.class);

    private final CommunicationsManager communicationsManager;

    public AuthenticationRiskTwilioSmsNotifier(final CommunicationsManager communicationsManager) {
        this.communicationsManager = communicationsManager;
    }

    @Override
    public void publish() {
        final SmsProperties sms = casProperties.getAuthn().getAdaptive().getRisk().getResponse().getSms();
        final Principal principal = authentication.getPrincipal();

        if (StringUtils.isBlank(sms.getText()) || StringUtils.isBlank(sms.getFrom())
                || !principal.getAttributes().containsKey(sms.getAttributeName())) {
            LOGGER.debug("Could not send sms [{}] because either no phones could be found or sms settings are not configured.",
                    principal.getId());
            return;
        }
        communicationsManager.sms(sms.getFrom(), principal.getAttributes().get(sms.getAttributeName()).toString(), sms.getText());
    }
}
