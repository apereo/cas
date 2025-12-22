package org.apereo.cas.authentication.bypass;

import module java.base;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.configuration.model.support.mfa.MultifactorAuthenticationProviderBypassProperties;
import org.apereo.cas.services.RegisteredService;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import org.springframework.context.ConfigurableApplicationContext;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Multifactor Bypass provider based on Credentials.
 *
 * @author Travis Schmidt
 * @since 6.0
 */
@Slf4j
public class CredentialMultifactorAuthenticationProviderBypassEvaluator extends BaseMultifactorAuthenticationProviderBypassEvaluator {
    @Serial
    private static final long serialVersionUID = -1233888418344342672L;

    private final MultifactorAuthenticationProviderBypassProperties bypassProperties;

    public CredentialMultifactorAuthenticationProviderBypassEvaluator(final MultifactorAuthenticationProviderBypassProperties bypassProperties,
                                                                      final String providerId,
                                                                      final ConfigurableApplicationContext applicationContext) {
        super(providerId, applicationContext);
        this.bypassProperties = bypassProperties;
    }

    protected static boolean locateMatchingCredentialType(final Authentication authentication, final String credentialClassType) {
        return StringUtils.isNotBlank(credentialClassType) && authentication.getCredentials()
            .stream()
            .anyMatch(e -> Objects.nonNull(e.getCredentialMetadata())
                           && e.getCredentialMetadata().getCredentialClass().getName().matches(credentialClassType));
    }

    @Override
    public boolean shouldMultifactorAuthenticationProviderExecuteInternal(final Authentication authentication,
                                                                          @Nullable final RegisteredService registeredService,
                                                                          final MultifactorAuthenticationProvider provider,
                                                                          @Nullable final HttpServletRequest request) {
        val bypassByCredType = locateMatchingCredentialType(authentication, bypassProperties.getCredentialClassType());
        if (bypassByCredType) {
            LOGGER.debug("Bypass rules for credential types [{}] indicate the request may be ignored", bypassProperties.getCredentialClassType());
            return false;
        }
        return true;
    }
}
