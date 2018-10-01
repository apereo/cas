package org.apereo.cas.authentication;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.configuration.model.support.mfa.MultifactorAuthenticationProviderBypassProperties;
import org.apereo.cas.services.MultifactorAuthenticationProvider;
import org.apereo.cas.services.VariegatedMultifactorAuthenticationProvider;
import org.apereo.cas.util.CollectionUtils;
import org.springframework.context.ApplicationContext;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * This is {@link MultifactorAuthenticationUtils}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
@UtilityClass
public class MultifactorAuthenticationUtils {
    /**
     * New multifactor authentication provider bypass multifactor.
     *
     * @param props the props
     * @return the multifactor authentication provider bypass
     */
    public static MultifactorAuthenticationProviderBypass newMultifactorAuthenticationProviderBypass(
        final MultifactorAuthenticationProviderBypassProperties props) {

        final ChainingMultifactorAuthenticationProviderBypass bypass
                = new ChainingMultifactorAuthenticationProviderBypass();
        bypass.addBypass(new DefaultMultifactorAuthenticationProviderBypass(props));

        switch (props.getType()) {
            case GROOVY:
                bypass.addBypass(new GroovyMultifactorAuthenticationProviderBypass(props));
                break;
            case REST:
                bypass.addBypass(new RestMultifactorAuthenticationProviderBypass(props));
                break;
            case DEFAULT:
            default:
                break;
        }
        return bypass;
    }

    /**
     * Gets all multifactor authentication providers from application context.
     *
     * @param applicationContext the application context
     * @return the all multifactor authentication providers from application context
     */
    public static Map<String, MultifactorAuthenticationProvider> getAvailableMultifactorAuthenticationProviders(
        final ApplicationContext applicationContext) {
        try {
            return applicationContext.getBeansOfType(MultifactorAuthenticationProvider.class, false, true);
        } catch (final Exception e) {
            LOGGER.debug("No beans of type [{}] are available in the application context. "
                    + "CAS may not be configured to handle multifactor authentication requests in absence of a provider",
                MultifactorAuthenticationProvider.class);
        }
        return new HashMap<>(0);
    }

    /**
     * Gets multifactor authentication providers by ids.
     *
     * @param ids                the ids
     * @param applicationContext the application context
     * @return the multifactor authentication providers by ids
     */
    public static Collection<MultifactorAuthenticationProvider> getMultifactorAuthenticationProvidersByIds(final Collection<String> ids,
                                                                                                           final ApplicationContext applicationContext) {
        final Map<String, MultifactorAuthenticationProvider> available = getAvailableMultifactorAuthenticationProviders(applicationContext);
        final Collection<MultifactorAuthenticationProvider> values = available.values();
        return values.stream()
            .filter(p -> ids.contains(p.getId()))
            .collect(Collectors.toSet());
    }

    /**
     * Method returns an Optional that will contain a MultifactorAuthenticationProvider that has the
     * same id as the passed providerId parameter.
     *
     * @param providerId - the id to match
     * @param context - ApplicationContext
     * @return - Optional
     */
    public static Optional<MultifactorAuthenticationProvider> getMultifactorAuthenticationProviderById(final String providerId,
                                                                                                       final ApplicationContext context) {
        return getAvailableMultifactorAuthenticationProviders(context).values().stream()
            .filter(p -> p.getId().equals(providerId)).findFirst();
    }

    /**
     * Consolidate providers collection.
     * If the provider is multi-instance in the collection, consolidate and flatten.
     *
     * @param providers the providers
     * @return the collection
     */
    public static Collection<MultifactorAuthenticationProvider> flattenProviders(final Collection<? extends MultifactorAuthenticationProvider> providers) {
        final Collection<MultifactorAuthenticationProvider> flattenedProviders = new HashSet<>();
        providers.forEach(p -> flattenedProviders.addAll(flattenProvider(p)));
        return flattenedProviders;
    }

    /**
     * Returns the collection of providers in a VariegatedMultifactorAuthenticationProvider or wraps the passed provider
     * into a Collection.
     *
     * @param provider the provider
     * @return - the collection
     */
    public static Collection<MultifactorAuthenticationProvider> flattenProvider(final MultifactorAuthenticationProvider provider) {
        if (provider instanceof VariegatedMultifactorAuthenticationProvider) {
            return ((VariegatedMultifactorAuthenticationProvider) provider).getProviders();
        }
        return CollectionUtils.wrap(provider);
    }
}
