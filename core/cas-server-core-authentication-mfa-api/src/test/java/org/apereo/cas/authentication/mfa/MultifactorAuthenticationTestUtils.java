package org.apereo.cas.authentication.mfa;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.CredentialMetadata;
import org.apereo.cas.authentication.DefaultMultifactorAuthenticationContextValidator;
import org.apereo.cas.authentication.DefaultRequestedAuthenticationContextValidator;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.MultifactorAuthenticationTriggerSelectionStrategy;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.model.support.mfa.BaseMultifactorAuthenticationProviderProperties;
import org.apereo.cas.configuration.model.support.mfa.MultifactorAuthenticationProviderBypassProperties;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceAccessStrategy;
import org.apereo.cas.services.RegisteredServiceMultifactorPolicy;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.validation.RequestedAuthenticationContextValidator;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.springframework.context.ConfigurableApplicationContext;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import static org.mockito.Mockito.*;

/**
 * This is {@link MultifactorAuthenticationTestUtils}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@UtilityClass
public class MultifactorAuthenticationTestUtils {
    public static WebApplicationService getService(final String id) {
        val svc = mock(WebApplicationService.class);
        when(svc.getId()).thenReturn(id);
        return svc;
    }

    public static Principal getPrincipal(final String id) {
        return getPrincipal(id, new HashMap<>());
    }

    public static Principal getPrincipal(final String id, final Map<String, List<Object>> attributes) {
        val principal = mock(Principal.class);
        when(principal.getAttributes()).thenReturn(attributes);
        when(principal.getId()).thenReturn(id);
        return principal;
    }

    public static Authentication getAuthentication(final String principal) {
        return getAuthentication(getPrincipal(principal), new HashMap<>());
    }

    public static Authentication getAuthentication(final Principal principal) {
        return getAuthentication(principal, new HashMap<>());
    }

    public static Authentication getAuthentication(final Principal principal, final Map<String, List<Object>> attributes) {
        val authentication = mock(Authentication.class);
        when(authentication.getAttributes()).thenReturn(attributes);
        when(authentication.getPrincipal()).thenReturn(principal);

        val credentialMetadata = mock(CredentialMetadata.class);
        when(credentialMetadata.getCredentialClass()).thenReturn((Class) Credential.class);

        val credential = mock(Credential.class);
        when(credential.getCredentialMetadata()).thenReturn(credentialMetadata);
        when(authentication.getCredentials()).thenReturn(CollectionUtils.wrapList(credential));
        return authentication;
    }

    public static RegisteredService getRegisteredService() {
        return getRegisteredService("https://www.github.com/apereo/cas", "UNDEFINED");
    }

    public static RegisteredService getRegisteredService(final String url, final String failureMode) {
        val service = mock(RegisteredService.class);
        when(service.getServiceId()).thenReturn(url);
        when(service.getName()).thenReturn("CAS");
        when(service.getId()).thenReturn(Long.MAX_VALUE);
        when(service.getDescription()).thenReturn("Apereo CAS");
        val access = mock(RegisteredServiceAccessStrategy.class);
        when(access.isServiceAccessAllowed(service, mock(Service.class))).thenReturn(true);
        when(service.getAccessStrategy()).thenReturn(access);
        val mfaPolicy = mock(RegisteredServiceMultifactorPolicy.class);
        when(mfaPolicy.isBypassEnabled()).thenReturn(false);
        when(mfaPolicy.getFailureMode())
            .thenReturn(BaseMultifactorAuthenticationProviderProperties.MultifactorAuthenticationProviderFailureModes.valueOf(failureMode));
        when(service.getMultifactorAuthenticationPolicy()).thenReturn(mfaPolicy);
        return service;
    }

    public static RequestedAuthenticationContextValidator mockRequestAuthnContextValidator(
        final ServicesManager servicesManager,
        final Optional<MultifactorAuthenticationProvider> provider,
        final ConfigurableApplicationContext applicationContext,
        final String failureMode) throws Throwable {
        val multifactorTrigger = mock(MultifactorAuthenticationTriggerSelectionStrategy.class);

        val service = MultifactorAuthenticationTestUtils.getRegisteredService("https://www.github.com/apereo/cas", failureMode);
        when(servicesManager.findServiceBy(any(Service.class))).thenReturn(service);
        when(multifactorTrigger.resolve(any(), any(), any(), any(), any())).thenReturn(provider);

        val multifactorContextValidator = new DefaultMultifactorAuthenticationContextValidator(
            "authn_method", "trusted_authn", applicationContext);
        return new DefaultRequestedAuthenticationContextValidator(servicesManager,
            multifactorTrigger, multifactorContextValidator);
    }

    public static MultifactorAuthenticationProviderBypassProperties getAuthenticationBypassProperties() {
        val props = new MultifactorAuthenticationProviderBypassProperties();
        props.setAuthenticationAttributeName("givenName");
        props.setAuthenticationAttributeValue("CAS");
        return props;
    }

}
