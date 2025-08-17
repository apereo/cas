package org.apereo.cas.impl.notify;

import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.notifications.CommunicationsManager;
import org.apereo.cas.notifications.sms.SmsBodyBuilder;
import org.apereo.cas.notifications.sms.SmsRequest;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;

/**
 * This is {@link AuthenticationRiskSmsNotifier}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
public class AuthenticationRiskSmsNotifier extends BaseAuthenticationRiskNotifier {

    public AuthenticationRiskSmsNotifier(final CasConfigurationProperties casProperties,
                                         final ApplicationContext applicationContext,
                                         final CommunicationsManager communicationsManager,
                                         final ServicesManager servicesManager,
                                         final PrincipalResolver principalResolver,
                                         final CipherExecutor riskVerificationCipherExecutor,
                                         final ServiceFactory serviceFactory,
                                         final TenantExtractor tenantExtractor) {
        super(applicationContext, casProperties, communicationsManager, servicesManager,
            principalResolver, riskVerificationCipherExecutor, serviceFactory, tenantExtractor);
    }

    @Override
    public void publish() throws Throwable {
        val sms = casProperties.getAuthn().getAdaptive().getRisk().getResponse().getSms();
        val principal = authentication.getPrincipal();

        if (StringUtils.isBlank(sms.getText()) || StringUtils.isBlank(sms.getFrom())) {
            LOGGER.debug("Could not send sms [{}] because either no phones could be found or sms settings are not configured.", principal.getId());
        } else {
            val verificationUrl = buildRiskVerificationUrl();
            val parameters = CollectionUtils.<String, Object>wrap(
                "verificationUrl", verificationUrl,
                "registeredService", registeredService,
                "authentication", authentication);
            val text = SmsBodyBuilder.builder()
                .properties(sms)
                .parameters(parameters)
                .build()
                .get();
            val recipients = sms.getAttributeName().stream().map(attribute -> {
                val values = principal.getAttributes().get(SpringExpressionLanguageValueResolver.getInstance().resolve(attribute));
                return CollectionUtils.firstElement(values).orElse(StringUtils.EMPTY).toString();
            }).toList();
            val smsRequest = SmsRequest.builder()
                .principal(principal)
                .from(sms.getFrom())
                .to(recipients)
                .text(text)
                .build();
            communicationsManager.sms(smsRequest);
        }
    }
}
