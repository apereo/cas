package org.apereo.cas.authentication;

import org.apereo.cas.configuration.model.support.mfa.MultifactorAuthenticationProviderBypassProperties;
import org.apereo.cas.services.MultifactorAuthenticationProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.util.HashMap;
import java.util.Map;

/**
 * This is {@link MultifactorAuthenticationUtils}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public final class MultifactorAuthenticationUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(MultifactorAuthenticationUtils.class);

    private MultifactorAuthenticationUtils() {
    }

    /**
     * New multifactor authentication provider bypass multifactor.
     *
     * @param props the props
     * @return the multifactor authentication provider bypass
     */
    public static MultifactorAuthenticationProviderBypass newMultifactorAuthenticationProviderBypass(
        final MultifactorAuthenticationProviderBypassProperties props) {

        final MultifactorAuthenticationProviderBypass bypass;
        switch (props.getType()) {
            case GROOVY:
                bypass = new GroovyMultifactorAuthenticationProviderBypass(props);
                break;
            case REST:
                bypass = new RestMultifactorAuthenticationProviderBypass(props);
                break;
            case DEFAULT:
            default:
                bypass = new DefaultMultifactorAuthenticationProviderBypass(props);
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
}
