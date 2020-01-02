package org.apereo.cas.impl.notify;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.io.CommunicationsManager;

import lombok.extern.slf4j.Slf4j;
import lombok.val;

/**
 * This is {@link AuthenticationRiskEmailNotifier}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
public class AuthenticationRiskEmailNotifier extends BaseAuthenticationRiskNotifier {
    private final CommunicationsManager communicationsManager;

    public AuthenticationRiskEmailNotifier(final CasConfigurationProperties casProperties,
                                           final CommunicationsManager communicationsManager) {
        super(casProperties);
        this.communicationsManager = communicationsManager;
    }

    @Override
    public void publish() {
        val mail = casProperties.getAuthn().getAdaptive().getRisk().getResponse().getMail();

        val principal = authentication.getPrincipal();
        if (!principal.getAttributes().containsKey(mail.getAttributeName())) {
            LOGGER.debug("Could not send email to [{}]. Either no addresses could be found or email settings are not configured.", principal.getId());
            return;
        }
        val addresses = principal.getAttributes().get(mail.getAttributeName());
        addresses.forEach(address -> this.communicationsManager.email(mail, address.toString(), mail.getFormattedBody()));

    }
}
