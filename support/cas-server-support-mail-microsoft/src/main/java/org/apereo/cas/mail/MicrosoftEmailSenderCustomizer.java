package org.apereo.cas.mail;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.notifications.mail.EmailMessageRequest;
import org.apereo.cas.notifications.mail.EmailSenderCustomizer;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;
import com.microsoft.aad.msal4j.ClientCredentialFactory;
import com.microsoft.aad.msal4j.ClientCredentialParameters;
import com.microsoft.aad.msal4j.ConfidentialClientApplication;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

/**
 * This is {@link MicrosoftEmailSenderCustomizer}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@RequiredArgsConstructor
@Slf4j
public class MicrosoftEmailSenderCustomizer implements EmailSenderCustomizer {
    protected final CasConfigurationProperties casProperties;

    @Override
    public void customize(final JavaMailSender mailSender, final EmailMessageRequest messageRequest) {
        val microsoft = casProperties.getEmailProvider().getMicrosoft();
        if (microsoft.isDefined() && mailSender instanceof JavaMailSenderImpl impl) {
            val accessToken = fetchAccessToken();
            LOGGER.debug("Setting accessToken as the password: [{}]", accessToken);
            impl.setPassword(accessToken);
        }
    }

    protected String fetchAccessToken() {
        try {
            val microsoft = casProperties.getEmailProvider().getMicrosoft();
            val clientCredentialParameters = ClientCredentialParameters.builder(microsoft.getScopes()).build();
            val clientApplication = ConfidentialClientApplication
                .builder(SpringExpressionLanguageValueResolver.getInstance().resolve(microsoft.getClientId()),
                    ClientCredentialFactory.createFromSecret(SpringExpressionLanguageValueResolver.getInstance().resolve(microsoft.getClientSecret())))
                .authority("https://login.microsoftonline.com/%s".formatted(SpringExpressionLanguageValueResolver.getInstance().resolve(microsoft.getTenantId())))
                .build();
            return clientApplication.acquireToken(clientCredentialParameters).get().accessToken();
        } catch (final Exception e) {
            throw new RuntimeException("Failed to acquire access token", e);
        }
    }
}
