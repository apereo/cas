package org.apereo.cas.web.view;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.ProtocolAttributeEncoder;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.web.support.AuthenticationAttributeReleasePolicy;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
public class Cas30JsonResponseView extends Cas30ResponseView {

    public Cas30JsonResponseView(final boolean successResponse,
                                 final ProtocolAttributeEncoder protocolAttributeEncoder,
                                 final ServicesManager servicesManager,
                                 final String authenticationContextAttribute,
                                 final boolean releaseProtocolAttributes,
                                 final AuthenticationAttributeReleasePolicy authenticationAttributeReleasePolicy,
                                 final AuthenticationServiceSelectionPlan serviceSelectionStrategy) {
        super(successResponse, protocolAttributeEncoder, servicesManager, authenticationContextAttribute,
                createDelegatedView(), releaseProtocolAttributes, authenticationAttributeReleasePolicy,
                serviceSelectionStrategy);
    }

    private static MappingJackson2JsonView createDelegatedView() {
        final MappingJackson2JsonView view = new MappingJackson2JsonView();
        view.setPrettyPrint(true);
        view.getObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL).findAndRegisterModules();
        return view;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }

    @Override
    protected void prepareMergedOutputModel(final Map<String, Object> model, final HttpServletRequest request,
                                            final HttpServletResponse response) {
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
            final List<String> proxies = chainedAuthentications.stream()
                    .map(authn -> authn.getPrincipal().getId()).collect(Collectors.toList());
            success.setProxies(proxies);
        }
        return success;
    }

    private static class CasServiceResponse {
        private CasServiceResponseAuthenticationFailure authenticationFailure;
        private CasServiceResponseAuthenticationSuccess authenticationSuccess;

        public CasServiceResponseAuthenticationFailure getAuthenticationFailure() {
            return this.authenticationFailure;
        }

        public void setAuthenticationFailure(final CasServiceResponseAuthenticationFailure authenticationFailure) {
            this.authenticationFailure = authenticationFailure;
        }

        public CasServiceResponseAuthenticationSuccess getAuthenticationSuccess() {
            return this.authenticationSuccess;
        }

        public void setAuthenticationSuccess(final CasServiceResponseAuthenticationSuccess authenticationSuccess) {
            this.authenticationSuccess = authenticationSuccess;
        }
    }

    private static class CasServiceResponseAuthenticationSuccess {
        private String user;
        private String proxyGrantingTicket;
        private List proxies;
        private Map attributes;

        public String getUser() {
            return this.user;
        }

        public void setUser(final String user) {
            this.user = user;
        }

        public String getProxyGrantingTicket() {
            return this.proxyGrantingTicket;
        }

        public void setProxyGrantingTicket(final String proxyGrantingTicket) {
            this.proxyGrantingTicket = proxyGrantingTicket;
        }

        public List getProxies() {
            return this.proxies;
        }

        public void setProxies(final List proxies) {
            this.proxies = proxies;
        }

        public Map getAttributes() {
            return this.attributes;
        }

        public void setAttributes(final Map attributes) {
            this.attributes = attributes;
        }
    }

    private static class CasServiceResponseAuthenticationFailure {
        private String code;
        private String description;

        public String getCode() {
            return this.code;
        }

        public void setCode(final String code) {
            this.code = code;
        }

        public String getDescription() {
            return this.description;
        }

        public void setDescription(final String description) {
            this.description = description;
        }
    }
}
