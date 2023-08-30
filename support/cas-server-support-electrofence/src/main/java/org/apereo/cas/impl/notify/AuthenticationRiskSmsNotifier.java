package org.apereo.cas.impl.notify;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.notifications.CommunicationsManager;
import org.apereo.cas.notifications.sms.SmsBodyBuilder;
import org.apereo.cas.notifications.sms.SmsRequest;
import org.apereo.cas.util.CollectionUtils;

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
    public void publish() throws Throwable {
        val sms = casProperties.getAuthn().getAdaptive().getRisk().getResponse().getSms();
        val principal = authentication.getPrincipal();

        if (StringUtils.isBlank(sms.getText()) || StringUtils.isBlank(sms.getFrom())
            || !principal.getAttributes().containsKey(sms.getAttributeName())) {
            LOGGER.debug("Could not send sms [{}] because either no phones could be found or sms settings are not configured.",
                principal.getId());
            return;
        }
        val to = CollectionUtils.firstElement(principal.getAttributes().get(sms.getAttributeName()))
            .orElse(StringUtils.EMPTY).toString();
        val text = SmsBodyBuilder.builder().properties(sms).build().get();
        val smsRequest = SmsRequest.builder().from(sms.getFrom()).to(to).text(text).build();
        communicationsManager.sms(smsRequest);
    }
}
