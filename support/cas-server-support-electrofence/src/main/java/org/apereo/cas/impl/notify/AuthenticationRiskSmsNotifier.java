package org.apereo.cas.impl.notify;

import lombok.RequiredArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.util.io.CommunicationsManager;

/**
 * This is {@link AuthenticationRiskSmsNotifier}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
@Getter
@Setter
@RequiredArgsConstructor
public class AuthenticationRiskSmsNotifier extends BaseAuthenticationRiskNotifier {
    private final CommunicationsManager communicationsManager;

    @Override
    public void publish() {
        final var sms = casProperties.getAuthn().getAdaptive().getRisk().getResponse().getSms();
        final var principal = authentication.getPrincipal();

        if (StringUtils.isBlank(sms.getText()) || StringUtils.isBlank(sms.getFrom())
                || !principal.getAttributes().containsKey(sms.getAttributeName())) {
            LOGGER.debug("Could not send sms [{}] because either no phones could be found or sms settings are not configured.",
                    principal.getId());
            return;
        }
        communicationsManager.sms(sms.getFrom(), principal.getAttributes().get(sms.getAttributeName()).toString(), sms.getText());
    }
}
