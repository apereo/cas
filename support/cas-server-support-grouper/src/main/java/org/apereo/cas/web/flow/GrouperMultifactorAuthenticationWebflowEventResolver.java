package org.apereo.cas.web.flow;

import com.google.common.collect.ImmutableSet;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetGroupsResult;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.grouper.GrouperFacade;
import org.apereo.cas.grouper.GrouperGroupField;
import org.apereo.cas.services.MultifactorAuthenticationProvider;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.web.flow.resolver.impl.AbstractCasWebflowEventResolver;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * This is {@link GrouperMultifactorAuthenticationWebflowEventResolver}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class GrouperMultifactorAuthenticationWebflowEventResolver extends AbstractCasWebflowEventResolver {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Override
    protected Set<Event> resolveInternal(final RequestContext context) {
        final RegisteredService service = WebUtils.getRegisteredService(context);
        final Authentication authentication = WebUtils.getAuthentication(context);

        if (StringUtils.isBlank(casProperties.getAuthn().getMfa().getGrouperGroupField())) {
            logger.debug("No group field is defined to process for Grouper multifactor trigger");
            return null;
        }
        if (authentication == null) {
            logger.debug("No authentication is available to determine event for principal");
            return null;
        }

        final Principal principal = authentication.getPrincipal();
        final List<WsGetGroupsResult> results = GrouperFacade.getGroupsForSubjectId(principal.getId());
        if (results.isEmpty()) {
            logger.debug("No groups could be found for {} to resolve events for MFA", principal);
            return null;
        }

        final Map<String, MultifactorAuthenticationProvider> providerMap =
                WebUtils.getAllMultifactorAuthenticationProviders(this.applicationContext);
        if (providerMap == null || providerMap.isEmpty()) {
            logger.warn("No multifactor authentication providers are available in the application context");
            throw new AuthenticationException();
        }

        final GrouperGroupField groupField =
                GrouperGroupField.valueOf(casProperties.getAuthn().getMfa().getGrouperGroupField().toUpperCase());

        final Optional<MultifactorAuthenticationProvider> providerFound =
                providerMap.values().stream()
                        .filter(provider -> results.stream().filter(wr -> Arrays.stream(wr.getWsGroups()).filter(g -> {
                            final String value = GrouperFacade.getGrouperGroupAttribute(groupField, g);
                            logger.debug("Evaluating group {} against provider id {}", value, provider.getId());
                            return provider.getId().matches(value);
                        }).findAny().isPresent()).findAny().isPresent())
                        .findFirst();

        if (providerFound.isPresent()) {
            if (providerFound.get().isAvailable(service)) {
                logger.debug("Attempting to build event based on the authentication provider [{}] and service [{}]",
                        providerFound.get(), service.getName());
                final Event event = validateEventIdForMatchingTransitionInContext(providerFound.get().getId(), context,
                        buildEventAttributeMap(authentication.getPrincipal(), service, providerFound.get()));
                return ImmutableSet.of(event);
            }
            logger.warn("Located multifactor provider {}, yet the provider cannot be reached or verified", providerFound.get());
            return null;
        }
        logger.debug("No multifactor provider could be found based on {}'s Grouper groups", principal.getId());
        return null;
    }
}
