package org.apereo.cas.web.flow;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.MultifactorAuthenticationProviderResolver;
import org.apereo.cas.authentication.MultifactorAuthenticationTrigger;
import org.apereo.cas.authentication.MultifactorAuthenticationUtils;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.grouper.GrouperFacade;
import org.apereo.cas.grouper.GrouperGroupField;
import org.apereo.cas.services.RegisteredService;

import edu.internet2.middleware.grouperClientExt.org.apache.commons.lang3.StringUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.ApplicationContext;
import org.springframework.core.Ordered;

import javax.servlet.http.HttpServletRequest;

import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This is {@link GrouperMultifactorAuthenticationTrigger}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Getter
@Setter
@Slf4j
@RequiredArgsConstructor
public class GrouperMultifactorAuthenticationTrigger implements MultifactorAuthenticationTrigger {
    private final CasConfigurationProperties casProperties;

    private final MultifactorAuthenticationProviderResolver multifactorAuthenticationProviderResolver;

    private final GrouperFacade grouperFacade;

    private final ApplicationContext applicationContext;

    private int order = Ordered.LOWEST_PRECEDENCE;

    @Override
    public Optional<MultifactorAuthenticationProvider> isActivated(final Authentication authentication,
                                                                   final RegisteredService registeredService,
                                                                   final HttpServletRequest request, final Service service) {
        val grouperField = casProperties.getAuthn().getMfa().getGrouperGroupField().toUpperCase();
        if (StringUtils.isBlank(grouperField)) {
            LOGGER.debug("No group field is defined to process for Grouper multifactor trigger");
            return Optional.empty();
        }
        if (authentication == null || registeredService == null) {
            LOGGER.debug("No authentication or service is available to determine event for principal");
            return Optional.empty();
        }

        val principal = authentication.getPrincipal();
        val results = grouperFacade.getGroupsForSubjectId(principal.getId());
        if (results.isEmpty()) {
            LOGGER.debug("No groups could be found for [{}] to resolve events for MFA", principal);
            return Optional.empty();
        }

        val providerMap = MultifactorAuthenticationUtils.getAvailableMultifactorAuthenticationProviders(this.applicationContext);
        if (providerMap.isEmpty()) {
            LOGGER.error("No multifactor authentication providers are available in the application context");
            throw new AuthenticationException();
        }

        val groupField = GrouperGroupField.valueOf(grouperField);

        val values = results.stream()
            .map(wsGetGroupsResult -> Stream.of(wsGetGroupsResult.getWsGroups()))
            .flatMap(Function.identity())
            .map(g -> GrouperFacade.getGrouperGroupAttribute(groupField, g))
            .collect(Collectors.toSet());

        return MultifactorAuthenticationUtils.resolveProvider(providerMap, values);
    }
}
