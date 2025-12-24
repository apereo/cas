package org.apereo.cas.authentication.metadata;

import module java.base;
import org.apereo.cas.authentication.AuthenticationBuilder;
import org.apereo.cas.authentication.AuthenticationTransaction;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.configuration.model.support.mfa.BaseMultifactorAuthenticationProviderProperties.MultifactorAuthenticationProviderFailureModes;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.function.FunctionUtils;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.util.StringUtils;

/**
 * This is {@link MultifactorAuthenticationProviderMetadataPopulator}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@ToString(callSuper = true)
@RequiredArgsConstructor
public class MultifactorAuthenticationProviderMetadataPopulator extends BaseAuthenticationMetaDataPopulator {
    private final String authenticationContextAttribute;

    private final ObjectProvider<? extends MultifactorAuthenticationProvider> provider;

    private final ServicesManager servicesManager;

    @Override
    public void populateAttributes(final AuthenticationBuilder builder,
                                   final AuthenticationTransaction transaction) {
        val registeredService = servicesManager.findServiceBy(transaction.getService());
        val failureEval = provider.getObject().getFailureModeEvaluator();
        val bypass = failureEval != null
                     && failureEval.evaluate(registeredService, provider.getObject()) == MultifactorAuthenticationProviderFailureModes.PHANTOM
                     && !provider.getObject().isAvailable(registeredService);
        FunctionUtils.doIf(bypass, _ ->
            StringUtils.commaDelimitedListToSet(authenticationContextAttribute).forEach(attribute ->
                builder.mergeAttribute(attribute, provider.getObject().getId()))).accept(provider);
    }

    @Override
    public boolean supports(final Credential credential) {
        return provider.getObject().getFailureModeEvaluator() != null && credential != null;
    }
}
