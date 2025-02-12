package org.apereo.cas.impl.notify;

import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.notifications.CommunicationsManager;
import org.apereo.cas.notifications.mail.EmailMessageBodyBuilder;
import org.apereo.cas.notifications.mail.EmailMessageRequest;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.ApplicationContext;
import java.util.List;

/**
 * This is {@link AuthenticationRiskEmailNotifier}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
public class AuthenticationRiskEmailNotifier extends BaseAuthenticationRiskNotifier {

    public AuthenticationRiskEmailNotifier(final CasConfigurationProperties casProperties,
                                           final ApplicationContext applicationContext,
                                           final CommunicationsManager communicationsManager,
                                           final ServicesManager servicesManager,
                                           final PrincipalResolver principalResolver,
                                           final CipherExecutor riskVerificationCipherExecutor,
                                           final ServiceFactory serviceFactory,
                                           final TenantExtractor tenantExtractor) {
        super(applicationContext, casProperties, communicationsManager,
            servicesManager, principalResolver, riskVerificationCipherExecutor,
            serviceFactory, tenantExtractor);
    }

    @Override
    public void publish() {
        val mail = casProperties.getAuthn().getAdaptive().getRisk().getResponse().getMail();
        val principal = authentication.getPrincipal();
        mail.getAttributeName().forEach(attributeName -> {
            val resolvedAttribute = SpringExpressionLanguageValueResolver.getInstance().resolve(attributeName);
            if (principal.getAttributes().containsKey(resolvedAttribute)) {
                val verificationUrl = buildRiskVerificationUrl();
                val addresses = (List) principal.getAttributes().get(resolvedAttribute);
                val parameters = CollectionUtils.<String, Object>wrap("authentication", authentication,
                    "registeredService", registeredService,
                    "riskScore", authenticationRiskScore,
                    "verificationUrl", verificationUrl);
                val body = EmailMessageBodyBuilder.builder()
                    .properties(mail)
                    .parameters(parameters)
                    .build()
                    .get();
                val emailRequest = EmailMessageRequest.builder()
                    .emailProperties(mail)
                    .to(addresses)
                    .body(body)
                    .tenant(clientInfo.getTenant())
                    .build();
                addresses.forEach(address -> communicationsManager.email(emailRequest));
            } else {
                LOGGER.debug("Could not send email to [{}]. No email found for [{}] or email settings are not configured.",
                    principal.getId(), resolvedAttribute);
            }
        });

    }
}
