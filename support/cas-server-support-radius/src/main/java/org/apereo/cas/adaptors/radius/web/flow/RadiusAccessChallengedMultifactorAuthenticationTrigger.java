package org.apereo.cas.adaptors.radius.web.flow;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.MultifactorAuthenticationProviderAbsentException;
import org.apereo.cas.authentication.MultifactorAuthenticationProviderResolver;
import org.apereo.cas.authentication.MultifactorAuthenticationTrigger;
import org.apereo.cas.authentication.MultifactorAuthenticationUtils;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredService;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.jradius.dictionary.Attr_ReplyMessage;
import net.jradius.dictionary.Attr_State;
import org.springframework.context.ApplicationContext;
import org.springframework.core.Ordered;

import javax.servlet.http.HttpServletRequest;

import java.util.Optional;

/**
 * This is {@link RadiusAccessChallengedMultifactorAuthenticationTrigger}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Getter
@Setter
@Slf4j
@RequiredArgsConstructor
public class RadiusAccessChallengedMultifactorAuthenticationTrigger implements MultifactorAuthenticationTrigger {
    private final CasConfigurationProperties casProperties;
    private final MultifactorAuthenticationProviderResolver multifactorAuthenticationProviderResolver;
    private final ApplicationContext applicationContext;

    private int order = Ordered.LOWEST_PRECEDENCE;

    @Override
    public Optional<MultifactorAuthenticationProvider> isActivated(final Authentication authentication, final RegisteredService registeredService,
                                                                   final HttpServletRequest request, final Service service) {
        if (authentication == null) {
            LOGGER.debug("No authentication or service is available to determine event for principal");
            return Optional.empty();
        }
        if (!supports(authentication)) {
            LOGGER.trace("Authentication attempt does not qualify for radius multifactor authentication");
            return Optional.empty();
        }
        
        val providerMap = MultifactorAuthenticationUtils.getAvailableMultifactorAuthenticationProviders(this.applicationContext);
        if (providerMap.isEmpty()) {
            LOGGER.error("No multifactor authentication providers are available in the application context");
            throw new AuthenticationException(new MultifactorAuthenticationProviderAbsentException());
        }

        val id = casProperties.getAuthn().getMfa().getRadius().getId();
        LOGGER.debug("Authentication requires multifactor authentication via provider [{}]", id);
        return MultifactorAuthenticationUtils.resolveProvider(providerMap, id);

    }

    private static boolean supports(final Authentication authentication) {
        val principal = authentication.getPrincipal();
        val attributes = principal.getAttributes();
        LOGGER.debug("Evaluating principal attributes [{}] for multifactor authentication", attributes.keySet());
        return attributes.containsKey(Attr_ReplyMessage.NAME) && attributes.containsKey(Attr_State.NAME);
    }
}
