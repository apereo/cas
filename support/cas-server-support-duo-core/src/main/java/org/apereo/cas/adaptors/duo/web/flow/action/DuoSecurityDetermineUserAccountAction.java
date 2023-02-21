package org.apereo.cas.adaptors.duo.web.flow.action;

import org.apereo.cas.adaptors.duo.DuoSecurityUserAccountStatus;
import org.apereo.cas.adaptors.duo.authn.DuoSecurityAuthenticationRegistrationCipherExecutor;
import org.apereo.cas.adaptors.duo.authn.DuoSecurityMultifactorAuthenticationProvider;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.token.JwtBuilder;
import org.apereo.cas.util.cipher.CipherExecutorUtils;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.actions.AbstractMultifactorAuthenticationAction;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.core5.net.URIBuilder;
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.Set;
import java.util.UUID;

/**
 * This is {@link DuoSecurityDetermineUserAccountAction}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiredArgsConstructor
@Slf4j
public class DuoSecurityDetermineUserAccountAction extends AbstractMultifactorAuthenticationAction<DuoSecurityMultifactorAuthenticationProvider> {

    private final CasConfigurationProperties casProperties;

    private final ServicesManager servicesManager;

    @Override
    protected Event doExecute(final RequestContext requestContext) throws Exception {
        val authentication = WebUtils.getAuthentication(requestContext);
        val principal = resolvePrincipal(authentication.getPrincipal());

        val duoAuthenticationService = provider.getDuoAuthenticationService();
        val account = duoAuthenticationService.getUserAccount(principal.getId());

        val eventFactorySupport = new EventFactorySupport();
        if (account.getStatus() == DuoSecurityUserAccountStatus.ENROLL) {
            if (StringUtils.isNotBlank(provider.getRegistration().getRegistrationUrl())) {
                val url = buildDuoRegistrationUrlFor(requestContext, provider, principal);
                LOGGER.info("Duo Security registration url for enrollment is [{}]", url);
                requestContext.getFlowScope().put("duoRegistrationUrl", url);
                return eventFactorySupport.event(this, CasWebflowConstants.TRANSITION_ID_ENROLL);
            }
        }
        if (account.getStatus() == DuoSecurityUserAccountStatus.ALLOW) {
            return eventFactorySupport.event(this, CasWebflowConstants.TRANSITION_ID_BYPASS);
        }
        if (account.getStatus() == DuoSecurityUserAccountStatus.DENY) {
            return eventFactorySupport.event(this, CasWebflowConstants.TRANSITION_ID_DENY);
        }
        if (account.getStatus() == DuoSecurityUserAccountStatus.UNAVAILABLE) {
            return eventFactorySupport.event(this, CasWebflowConstants.TRANSITION_ID_UNAVAILABLE);
        }

        return success();
    }

    protected String buildDuoRegistrationUrlFor(final RequestContext requestContext,
                                                final DuoSecurityMultifactorAuthenticationProvider provider,
                                                final Principal principal) throws Exception {
        val cipher = CipherExecutorUtils.newStringCipherExecutor(provider.getRegistration().getCrypto(),
            DuoSecurityAuthenticationRegistrationCipherExecutor.class);
        val builder = new URIBuilder(provider.getRegistration().getRegistrationUrl());
        if (cipher.isEnabled()) {
            val jwtBuilder = new JwtBuilder(cipher, servicesManager, casProperties);
            val jwtRequest = JwtBuilder.JwtRequest
                .builder()
                .serviceAudience(Set.of(builder.getHost()))
                .subject(principal.getId())
                .jwtId(UUID.randomUUID().toString())
                .issuer(casProperties.getServer().getName())
                .build();
            val jwt = jwtBuilder.build(jwtRequest);
            builder.addParameter("principal", jwt);
        }
        return builder.toString();
    }

}
