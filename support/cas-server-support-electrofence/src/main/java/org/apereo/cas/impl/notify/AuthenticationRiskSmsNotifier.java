package org.apereo.cas.impl.notify;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.io.CommunicationsManager;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

/**
 * This is {@link AuthenticationRiskSmsNotifier}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
@Getter
@Setter
public class AuthenticationRiskSmsNotifier extends BaseAuthenticationRiskNotifier {
    private final CommunicationsManager communicationsManager;

    public AuthenticationRiskSmsNotifier(final CasConfigurationProperties casProperties,
                                         final CommunicationsManager communicationsManager) {
        super(casProperties);
        this.communicationsManager = communicationsManager;
    }

    @Override
    public void publish() {
        val sms = casProperties.getAuthn().getAdaptive().getRisk().getResponse().getSms();
        val principal = authentication.getPrincipal();

        if (StringUtils.isBlank(sms.getText()) || StringUtils.isBlank(sms.getFrom())
            || !principal.getAttributes().containsKey(sms.getAttributeName())) {
            LOGGER.debug("Could not send sms [{}] because either no phones could be found or sms settings are not configured.",
                principal.getId());
            return;
        }
        val to = principal.getAttributes().get(sms.getAttributeName()).toString();
        communicationsManager.sms(sms.getFrom(), to, sms.getFormattedText());
    }
}
