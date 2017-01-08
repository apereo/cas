package org.apereo.cas.impl.notify;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.configuration.model.support.sms.SmsProperties;
import org.apereo.cas.util.io.SmsSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * This is {@link AuthenticationRiskTwilioSmsNotifier}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class AuthenticationRiskTwilioSmsNotifier extends BaseAuthenticationRiskNotifier {

    @Autowired(required = false)
    @Qualifier("smsSender")
    private SmsSender smsSender;

    @Override
    public void publish() {
        final SmsProperties sms = casProperties.getAuthn().getAdaptive().getRisk().getResponse().getSms();
        final Principal principal = authentication.getPrincipal();

        if (StringUtils.isBlank(sms.getText()) || StringUtils.isBlank(sms.getFrom())
                || !principal.getAttributes().containsKey(sms.getAttributeName())) {
            logger.debug("Could not send sms {} because either no phones could be found or sms settings are not configured.",
                    principal.getId());
            return;
        }
        smsSender.send(sms.getFrom(), principal.getAttributes().get(sms.getAttributeName()).toString(), sms.getText());
    }
}
