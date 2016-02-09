package org.jasig.cas.authentication;


import org.jasig.cas.services.MultifactorAuthenticationProvider;
import org.jasig.cas.util.CollectionUtils;
import org.jasig.cas.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.OrderComparator;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

/**
 * The {@link AuthenticationContextValidator} is responsible for evaluating an authentication
 * object to see whether it satisfied a requested authentication context.
 *
 * @author Misagh Moayyed
 * @since 4.3
 */
@Component("authenticationContextValidator")
public final class AuthenticationContextValidator {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${cas.mfa.authn.ctx.attribute:authnContextClass}")
    private String authenticationContextAttribute;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    public String getAuthenticationContextAttribute() {
        return authenticationContextAttribute;
    }

    public void setAuthenticationContextAttribute(final String authenticationContextAttribute) {
        this.authenticationContextAttribute = authenticationContextAttribute;
    }


    /**
     * Validate the authentication context.
     *
     * @param authentication   the authentication
     * @param requestedContext the requested context
     * @return the resulting pair indicates whether context is satisfied, and if so, by which provider.
     */
    public Pair<Boolean, Optional<MultifactorAuthenticationProvider>> validate(final Authentication authentication, final String
            requestedContext) {
        final Object ctxAttr = authentication.getAttributes().get(this.authenticationContextAttribute);
        final Collection<Object> contexts = CollectionUtils.convertValueToCollection(ctxAttr);
        logger.debug("Attempting to match requested authentication context {} against {}", requestedContext, contexts);

        if  (contexts.stream().filter(ctx -> ctx.toString().equals(requestedContext)).count() > 0) {
            logger.debug("Requested authentication context {} is satisfied", requestedContext);
            return new Pair(false, Optional.empty());
        }


        final Map<String, MultifactorAuthenticationProvider> providerMap = getAllMultifactorAuthenticationProvidersFromApplicationContext();

        final Collection<MultifactorAuthenticationProvider> satisfiedProviders =
                getSatisfiedAuthenticationProviders(authentication, providerMap.values());

        if (satisfiedProviders == null) {
            logger.debug("No satisfied multifactor authentication providers are recorded.");
            return new Pair(false, Optional.empty());
        }

        final MultifactorAuthenticationProvider requestedProvider = locateRequestedProvider(providerMap.values(), requestedContext);
        if (requestedProvider == null) {
            logger.debug("Requested authentication provider is not available.");
            return new Pair(false, Optional.empty());
        }

        if (!satisfiedProviders.isEmpty()) {
            final MultifactorAuthenticationProvider[] providersArray =
                    satisfiedProviders.toArray(new MultifactorAuthenticationProvider[]{});
            OrderComparator.sortIfNecessary(providersArray);
            for (final MultifactorAuthenticationProvider provider : providersArray) {
                if (provider.equals(requestedProvider)) {
                    logger.debug("Current provider {} already satisfies the authentication requirements of {}; proceed with flow normally.",
                            provider, requestedProvider);
                    return new Pair(true, Optional.of(requestedProvider));
                }

                if (provider.getOrder() >= requestedProvider.getOrder()) {
                    logger.debug("Provider {} already satisfies the authentication requirements of {}; proceed with flow normally.",
                            provider, requestedProvider);
                    return new Pair(true, Optional.of(requestedProvider));
                }
            }
        }
        return new Pair(false, Optional.empty());
    }

    /**
     * Gets all multifactor authentication providers from application context.
     *
     * @return the all multifactor authentication providers from application context
     */
    private Map<String, MultifactorAuthenticationProvider> getAllMultifactorAuthenticationProvidersFromApplicationContext() {
        try {
            return this.applicationContext.getBeansOfType(MultifactorAuthenticationProvider.class);
        } catch (final Exception e) {
            logger.warn("Could not locate beans of type {} in the application context", MultifactorAuthenticationProvider.class);
        }
        return null;
    }

    private Collection<MultifactorAuthenticationProvider> getSatisfiedAuthenticationProviders(final Authentication authentication,
                                           final Collection<MultifactorAuthenticationProvider> providers) {
        final Collection<Object> contexts = CollectionUtils.convertValueToCollection(
                authentication.getAttributes().get(this.authenticationContextAttribute));

        if (contexts == null || contexts.isEmpty()) {
            return null;
        }

        final Iterator<MultifactorAuthenticationProvider> iterator = providers.iterator();
        for (final Object context : contexts) {
            while (iterator.hasNext()) {
                final MultifactorAuthenticationProvider provider = iterator.next();
                if (!provider.getId().equals(context)) {
                    iterator.remove();
                }
            }
        }
        return providers;
    }


    private static MultifactorAuthenticationProvider locateRequestedProvider(
            final Collection<MultifactorAuthenticationProvider> providersArray, final String requestedProvider) {
        for (final MultifactorAuthenticationProvider provider : providersArray) {
            if (provider.getId().equals(requestedProvider)) {
                return provider;
            }
        }
        return null;
    }

}
