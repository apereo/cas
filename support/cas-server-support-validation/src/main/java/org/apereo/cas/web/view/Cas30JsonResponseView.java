package org.apereo.cas.web.view;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationAttributeReleasePolicy;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.ProtocolAttributeEncoder;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.services.ServicesManager;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Setter;

/**
 * Renders the model prepared by CAS in JSON format.
 * Automatically sets the response type and formats
 * the output for pretty printing. The class relies on
 * {@link MappingJackson2JsonView} to handle most of the
 * model processing and as such, does not do anything special.
 * It is meant and kept to provide a facility for adopters
 * so that the JSON view can be augmented easily in overlays.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
@Slf4j
@ToString
@Getter
@Setter
public class Cas30JsonResponseView extends Cas30ResponseView {

    public Cas30JsonResponseView(final boolean successResponse, final ProtocolAttributeEncoder protocolAttributeEncoder,
                                 final ServicesManager servicesManager, final String authenticationContextAttribute,
                                 final boolean releaseProtocolAttributes, final AuthenticationAttributeReleasePolicy authenticationAttributeReleasePolicy,
                                 final AuthenticationServiceSelectionPlan serviceSelectionStrategy) {
        super(successResponse, protocolAttributeEncoder, servicesManager, authenticationContextAttribute,
            createDelegatedView(), releaseProtocolAttributes, authenticationAttributeReleasePolicy, serviceSelectionStrategy);
    }

    private static MappingJackson2JsonView createDelegatedView() {
        final MappingJackson2JsonView view = new MappingJackson2JsonView();
        view.setPrettyPrint(true);
        view.getObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL).findAndRegisterModules();
        return view;
    }

    @Override
    protected void prepareMergedOutputModel(final Map<String, Object> model, final HttpServletRequest request, final HttpServletResponse response) {
        final CasServiceResponse casResponse = new CasServiceResponse();
        try {
            super.prepareMergedOutputModel(model, request, response);
            if (getAssertionFrom(model) != null) {
                final CasServiceResponseAuthenticationSuccess success = createAuthenticationSuccess(model);
                casResponse.setAuthenticationSuccess(success);
            } else {
                final CasServiceResponseAuthenticationFailure failure = createAuthenticationFailure(model);
                casResponse.setAuthenticationFailure(failure);
            }
        } catch (final Exception e) {
            final CasServiceResponseAuthenticationFailure failure = createAuthenticationFailure(model);
            casResponse.setAuthenticationFailure(failure);
        } finally {
            final Map<String, Object> casModel = new HashMap<>();
            casModel.put("serviceResponse", casResponse);
            model.clear();
            model.putAll(casModel);
        }
    }

    private CasServiceResponseAuthenticationFailure createAuthenticationFailure(final Map<String, Object> model) {
        final CasServiceResponseAuthenticationFailure failure = new CasServiceResponseAuthenticationFailure();
        failure.setCode(getErrorCodeFrom(model));
        failure.setDescription(getErrorDescriptionFrom(model));
        return failure;
    }

    private CasServiceResponseAuthenticationSuccess createAuthenticationSuccess(final Map<String, Object> model) {
        final CasServiceResponseAuthenticationSuccess success = new CasServiceResponseAuthenticationSuccess();
        success.setAttributes(getModelAttributes(model));
        final Principal principal = getPrincipal(model);
        success.setUser(principal.getId());
        success.setProxyGrantingTicket(getProxyGrantingTicketIou(model));
        final Collection<Authentication> chainedAuthentications = getChainedAuthentications(model);
        if (chainedAuthentications != null && !chainedAuthentications.isEmpty()) {
            final List<String> proxies = chainedAuthentications.stream().map(authn -> authn.getPrincipal().getId()).collect(Collectors.toList());
            success.setProxies(proxies);
        }
        return success;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    private static class CasServiceResponse {

        private CasServiceResponseAuthenticationFailure authenticationFailure;

        private CasServiceResponseAuthenticationSuccess authenticationSuccess;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    private static class CasServiceResponseAuthenticationSuccess {

        private String user;

        private String proxyGrantingTicket;

        private List proxies;

        private Map attributes;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    private static class CasServiceResponseAuthenticationFailure {

        private String code;

        private String description;
    }
}
