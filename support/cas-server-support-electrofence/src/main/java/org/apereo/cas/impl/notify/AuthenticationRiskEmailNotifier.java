package org.apereo.cas.impl.notify;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.configuration.model.support.email.EmailProperties;
import org.apereo.cas.util.io.CommunicationsManager;

/**
 * This is {@link AuthenticationRiskEmailNotifier}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
@AllArgsConstructor
public class AuthenticationRiskEmailNotifier extends BaseAuthenticationRiskNotifier {
    private final CommunicationsManager communicationsManager;

    @Override
    public void publish() {
        final EmailProperties mail =
                casProperties.getAuthn().getAdaptive().getRisk().getResponse().getMail();

        final Principal principal = authentication.getPrincipal();
        if (!principal.getAttributes().containsKey(mail.getAttributeName())) {
            LOGGER.debug("Could not send email [{}] because either no addresses could be found or email settings are not configured.",
                    principal.getId());
            return;
        }
        final String to = principal.getAttributes().get(mail.getAttributeName()).toString();
        this.communicationsManager.email(mail.getText(), mail.getFrom(), mail.getSubject(), to, mail.getCc(), mail.getBcc());
    }
}
