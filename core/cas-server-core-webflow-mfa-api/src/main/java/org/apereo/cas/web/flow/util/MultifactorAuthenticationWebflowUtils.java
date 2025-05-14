package org.apereo.cas.web.flow.util;

import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.OneTimeTokenAccount;
import org.apereo.cas.authentication.device.MultifactorAuthenticationRegisteredDevice;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.configurer.CasMultifactorWebflowCustomizer;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.webflow.execution.RequestContext;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This is {@link MultifactorAuthenticationWebflowUtils}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@UtilityClass
public class MultifactorAuthenticationWebflowUtils {
    /**
     * Gets multifactor authentication webflow customizers.
     *
     * @param applicationContext the application context
     * @return the multifactor authentication webflow customizers
     */
    public static List<CasMultifactorWebflowCustomizer> getMultifactorAuthenticationWebflowCustomizers(
        final ConfigurableApplicationContext applicationContext) {
        return applicationContext.getBeansOfType(CasMultifactorWebflowCustomizer.class)
            .values()
            .stream()
            .filter(BeanSupplier::isNotProxy)
            .sorted(AnnotationAwareOrderComparator.INSTANCE)
            .collect(Collectors.toList());
    }

    /**
     * Is multifactor device registration enabled?
     *
     * @param requestContext the request context
     * @return true/false
     */
    public static boolean isMultifactorDeviceRegistrationEnabled(final RequestContext requestContext) {
        val enabled = Objects.requireNonNullElse(requestContext.getFlowScope().getBoolean("mfaDeviceRegistrationEnabled", Boolean.TRUE), Boolean.TRUE);
        return BooleanUtils.toBoolean(enabled);
    }

    /**
     * Put multifactor device registration enabled.
     *
     * @param requestContext the request context
     * @param enabled        the enabled
     */
    public static void putMultifactorDeviceRegistrationEnabled(final RequestContext requestContext, final boolean enabled) {
        requestContext.getFlowScope().put("mfaDeviceRegistrationEnabled", enabled);
    }

    /**
     * Put resolved multifactor authentication providers into scope.
     *
     * @param context the context
     * @param value   the value
     */
    public static void putResolvedMultifactorAuthenticationProviders(final RequestContext context,
                                                                     final Collection<MultifactorAuthenticationProvider> value) {
        val providerIds = value.stream().map(MultifactorAuthenticationProvider::getId).collect(Collectors.toSet());
        context.getConversationScope().put("resolvedMultifactorAuthenticationProviders", providerIds);
    }

    /**
     * Gets resolved multifactor authentication providers.
     *
     * @param context the context
     * @return the resolved multifactor authentication providers
     */
    public static Collection<String> getResolvedMultifactorAuthenticationProviders(final RequestContext context) {
        return context.getConversationScope().get("resolvedMultifactorAuthenticationProviders", Collection.class);
    }

    /**
     * Add the mfa provider id into flow scope.
     *
     * @param context  request context
     * @param provider the mfa provider
     */
    public static void putMultifactorAuthenticationProvider(final RequestContext context, final MultifactorAuthenticationProvider provider) {
        context.getFlowScope().put(CasWebflowConstants.VAR_ID_MFA_PROVIDER_ID, provider.getId());
    }

    /**
     * Get the mfa provider id from flow scope.
     *
     * @param context request context
     * @return provider id
     */
    public static String getMultifactorAuthenticationProvider(final RequestContext context) {
        return context.getFlowScope().get(CasWebflowConstants.VAR_ID_MFA_PROVIDER_ID, String.class);
    }

    /**
     * Put selectable multifactor authentication providers.
     *
     * @param requestContext the request context
     * @param mfaProviders   the mfa providers
     */
    public static void putSelectableMultifactorAuthenticationProviders(final RequestContext requestContext, final List<String> mfaProviders) {
        requestContext.getViewScope().put("mfaSelectableProviders", mfaProviders);
    }

    /**
     * Gets selectable multifactor authentication providers.
     *
     * @param requestContext the request context
     * @return the selectable multifactor authentication providers
     */
    public static List<String> getSelectableMultifactorAuthenticationProviders(final RequestContext requestContext) {
        return requestContext.getViewScope().get("mfaSelectableProviders", List.class);
    }

    /**
     * Put google authenticator multiple device registration enabled.
     *
     * @param requestContext the request context
     * @param enabled        the enabled
     */
    public static void putGoogleAuthenticatorMultipleDeviceRegistrationEnabled(final RequestContext requestContext,
                                                                               final boolean enabled) {
        requestContext.getFlowScope().put("gauthMultipleDeviceRegistrationEnabled", enabled);
    }

    /**
     * Is google authenticator multiple device registration enabled?
     *
     * @param requestContext the request context
     * @return true /false
     */
    public static Boolean isGoogleAuthenticatorMultipleDeviceRegistrationEnabled(final RequestContext requestContext) {
        return requestContext.getFlowScope().get("gauthMultipleDeviceRegistrationEnabled", Boolean.class);
    }

    /**
     * Put yubikey multiple device registration enabled.
     *
     * @param requestContext the request context
     * @param enabled        the enabled
     */
    public static void putYubiKeyMultipleDeviceRegistrationEnabled(final RequestContext requestContext, final boolean enabled) {
        requestContext.getFlowScope().put("yubikeyMultipleDeviceRegistrationEnabled", enabled);
    }

    /**
     * Put simple multifactor authentication token.
     *
     * @param requestContext the request context
     * @param token          the token
     */
    public static void putSimpleMultifactorAuthenticationToken(final RequestContext requestContext, final Ticket token) {
        requestContext.getFlowScope().put("simpleMultifactorAuthenticationToken", token);
    }

    /**
     * Remove simple multifactor authentication token.
     *
     * @param requestContext the request context
     */
    public static void removeSimpleMultifactorAuthenticationToken(final RequestContext requestContext) {
        requestContext.getFlowScope().remove("simpleMultifactorAuthenticationToken");
    }

    /**
     * Gets simple multifactor authentication token.
     *
     * @param <T>            the type parameter
     * @param requestContext the request context
     * @param clazz          the clazz
     * @return the simple multifactor authentication token
     */
    public static <T extends Ticket> T getSimpleMultifactorAuthenticationToken(final RequestContext requestContext,
                                                                               final Class<T> clazz) {
        return requestContext.getFlowScope().get("simpleMultifactorAuthenticationToken", clazz);
    }

    /**
     * Gets multifactor authentication parent credential.
     *
     * @param requestContext the request context
     * @return the multifactor authentication parent credential
     */
    public static Credential getMultifactorAuthenticationParentCredential(final RequestContext requestContext) {
        return requestContext.getFlowScope().get("parentCredential", Credential.class);
    }

    /**
     * Put multifactor authentication parent credential.
     *
     * @param context    the context
     * @param credential the credential
     */
    public static void putMultifactorAuthenticationParentCredential(final RequestContext context, final Credential credential) {
        context.getFlowScope().put("parentCredential", credential);
    }
    
    /**
     * Put multifactor authentication registered devices.
     *
     * @param requestContext the request context
     * @param accounts       the accounts
     */
    public static void putMultifactorAuthenticationRegisteredDevices(final RequestContext requestContext, final Set accounts) {
        val items = ObjectUtils.defaultIfNull(getMultifactorAuthenticationRegisteredDevices(requestContext), new HashSet<>());
        items.addAll(accounts);
        requestContext.getFlowScope().put("multifactorRegisteredAccounts", items);
    }

    /**
     * Gets multifactor authentication registered devices.
     *
     * @param requestContext the request context
     * @return the multifactor authentication registered devices
     */
    public Set<MultifactorAuthenticationRegisteredDevice> getMultifactorAuthenticationRegisteredDevices(final RequestContext requestContext) {
        return requestContext.getFlowScope().get("multifactorRegisteredAccounts", Set.class);
    }

    /**
     * Put one time token account.
     *
     * @param requestContext the request context
     * @param account        the account
     */
    public static void putOneTimeTokenAccount(final RequestContext requestContext, final OneTimeTokenAccount account) {
        requestContext.getFlowScope().put("registeredDevice", account);
    }

    /**
     * Put one time token accounts.
     *
     * @param requestContext the request context
     * @param accounts       the accounts
     */
    public static void putOneTimeTokenAccounts(final RequestContext requestContext, final Collection accounts) {
        requestContext.getFlowScope().put("registeredDevices", accounts);
    }

    /**
     * Gets one time token accounts.
     *
     * @param requestContext the request context
     * @return the one time token accounts
     */
    public static Collection getOneTimeTokenAccounts(final RequestContext requestContext) {
        return requestContext.getFlowScope().get("registeredDevices", Collection.class);
    }

    /**
     * Gets one time token account.
     *
     * @param <T>            the type parameter
     * @param requestContext the request context
     * @param clazz          the clazz
     * @return the one time token account
     */
    public static <T extends OneTimeTokenAccount> T getOneTimeTokenAccount(final RequestContext requestContext, final Class<T> clazz) {
        return requestContext.getFlowScope().get("registeredDevice", clazz);
    }
    
}
