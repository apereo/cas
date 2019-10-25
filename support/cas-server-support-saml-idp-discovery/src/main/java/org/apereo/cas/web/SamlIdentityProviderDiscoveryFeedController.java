package org.apereo.cas.web;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.entity.SamlIdentityProviderEntity;
import org.apereo.cas.entity.SamlIdentityProviderEntityParser;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.validation.DelegatedAuthenticationAccessStrategyHelper;
import org.apereo.cas.web.support.ArgumentExtractor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.pac4j.core.client.Clients;
import org.pac4j.core.context.JEEContext;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.saml.client.SAML2Client;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This is {@link SamlIdentityProviderDiscoveryFeedController}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@RestController("identityProviderDiscoveryFeedController")
@Slf4j
@RequiredArgsConstructor
@RequestMapping(path = "/idp/discovery")
public class SamlIdentityProviderDiscoveryFeedController {
    private final CasConfigurationProperties casProperties;

    private final List<SamlIdentityProviderEntityParser> parsers;

    private final Clients clients;

    private final DelegatedAuthenticationAccessStrategyHelper delegatedAuthenticationAccessStrategyHelper;

    private final ArgumentExtractor argumentExtractor;

    private final SessionStore<JEEContext> sessionStore;

    @GetMapping(path = "/feed", produces = MediaType.APPLICATION_JSON_VALUE)
    public Collection<SamlIdentityProviderEntity> getDiscoveryFeed() {
        return parsers
            .stream()
            .map(SamlIdentityProviderEntityParser::getIdentityProviders)
            .flatMap(Set::stream)
            .collect(Collectors.toSet());
    }

    @GetMapping
    public ModelAndView home() {
        val model = new HashMap<String, Object>();

        val entityIds = clients.findAllClients()
            .stream()
            .filter(c -> c instanceof SAML2Client)
            .map(SAML2Client.class::cast)
            .map(SAML2Client::getServiceProviderResolvedEntityId)
            .collect(Collectors.toList());

        LOGGER.debug("Using service provider entity id [{}]", entityIds);
        model.put("entityIds", entityIds);

        model.put("casServerPrefix", casProperties.getServer().getPrefix());
        return new ModelAndView("casSamlIdPDiscoveryView", model);
    }

    @GetMapping(path = "redirect")
    public View redirect(@RequestParam("entityID") final String entityID,
                         final HttpServletRequest httpServletRequest,
                         final HttpServletResponse httpServletResponse) {
        val idp = getDiscoveryFeed().stream()
            .filter(entity -> entity.getEntityID().equals(entityID))
            .findFirst()
            .orElseThrow();
        val samlClient = clients.findAllClients()
            .stream()
            .filter(c -> c instanceof SAML2Client)
            .map(SAML2Client.class::cast)
            .filter(c -> c.getIdentityProviderResolvedEntityId().equalsIgnoreCase(idp.getEntityID()))
            .findFirst()
            .orElseThrow();

        val webContext = new JEEContext(httpServletRequest, httpServletResponse, this.sessionStore);
        val service = this.argumentExtractor.extractService(httpServletRequest);
        if (delegatedAuthenticationAccessStrategyHelper.isDelegatedClientAuthorizedForService(samlClient, service)) {
            val provider = DelegatedClientIdentityProviderConfigurationFactory.builder()
                .service(service)
                .client(samlClient)
                .webContext(webContext)
                .casProperties(casProperties)
                .build()
                .resolve();

            if (provider.isPresent()) {
                return new RedirectView('/' + provider.get().getRedirectUrl(), true, true, true);
            }
        }
        throw new UnauthorizedServiceException(UnauthorizedServiceException.CODE_UNAUTHZ_SERVICE, StringUtils.EMPTY);
    }
}
