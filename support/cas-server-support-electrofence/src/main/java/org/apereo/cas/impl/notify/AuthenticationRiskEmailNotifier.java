package org.apereo.cas.impl.notify;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.notifications.CommunicationsManager;
import org.apereo.cas.notifications.mail.EmailMessageBodyBuilder;
import org.apereo.cas.notifications.mail.EmailMessageRequest;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;

import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.List;

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
        mail.getAttributeName().forEach(attributeName -> {
            val resolvedAttribute = SpringExpressionLanguageValueResolver.getInstance().resolve(attributeName);
            if (principal.getAttributes().containsKey(resolvedAttribute)) {
                val addresses = (List) principal.getAttributes().get(resolvedAttribute);
                val body = EmailMessageBodyBuilder.builder()
                    .properties(mail)
                    .build()
                    .get();
                val emailRequest = EmailMessageRequest.builder()
                    .emailProperties(mail)
                    .to(addresses)
                    .body(body)
                    .build();
                addresses.forEach(address -> communicationsManager.email(emailRequest));
            } else {
                LOGGER.debug("Could not send email to [{}]. No email found for [{}] or email settings are not configured.",
                    principal.getId(), resolvedAttribute);
            }
        });

    }
}
