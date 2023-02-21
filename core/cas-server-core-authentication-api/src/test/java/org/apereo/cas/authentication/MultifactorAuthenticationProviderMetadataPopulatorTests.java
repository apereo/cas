package org.apereo.cas.authentication;

import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.metadata.MultifactorAuthenticationProviderMetadataPopulator;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.mfa.BaseMultifactorAuthenticationProviderProperties.MultifactorAuthenticationProviderFailureModes;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.spring.DirectObjectProvider;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link MultifactorAuthenticationProviderMetadataPopulatorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@Tag("AuthenticationMetadata")
public class MultifactorAuthenticationProviderMetadataPopulatorTests {
    @Test
    public void verifyOperation() throws Exception {
        val provider = mock(MultifactorAuthenticationProvider.class);
        when(provider.getFailureMode()).thenReturn(MultifactorAuthenticationProviderFailureModes.PHANTOM);
        when(provider.getId()).thenReturn("mfa-dummy");
        when(provider.getFailureModeEvaluator()).thenReturn(
            new DefaultMultifactorAuthenticationFailureModeEvaluator(new CasConfigurationProperties()));
        when(provider.isAvailable(any())).thenReturn(false);

        val servicesManager = mock(ServicesManager.class);
        val populator = new MultifactorAuthenticationProviderMetadataPopulator("contextClass",
            new DirectObjectProvider<>(provider), servicesManager);
        assertTrue(populator.supports(CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword()));

        val builder = CoreAuthenticationTestUtils.getAuthenticationBuilder();
        populator.populateAttributes(builder, new DefaultAuthenticationTransactionFactory()
            .newTransaction(new UsernamePasswordCredential()));
        val authn = builder.build();
        assertTrue(authn.getAttributes().containsKey("contextClass"));
    }
}
