package org.apereo.cas.web.view.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.AuthenticationAttributeReleasePolicy;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.ProtocolAttributeEncoder;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.validation.CasProtocolAttributesRenderer;
import org.apereo.cas.web.view.Cas30ResponseView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
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
@Slf4j
@ToString
@Getter
@Setter
public class Cas30JsonResponseView extends Cas30ResponseView {
    /**
     * Attribute name in the final model representing the service response.
     */
    public static final String ATTRIBUTE_NAME_MODEL_SERVICE_RESPONSE = "serviceResponse";

    public Cas30JsonResponseView(final boolean successResponse,
                                 final ProtocolAttributeEncoder protocolAttributeEncoder,
                                 final ServicesManager servicesManager,
                                 final String authenticationContextAttribute,
                                 final View delegatedView,
                                 final boolean releaseProtocolAttributes,
                                 final AuthenticationAttributeReleasePolicy authenticationAttributeReleasePolicy,
                                 final AuthenticationServiceSelectionPlan serviceSelectionStrategy,
                                 final CasProtocolAttributesRenderer attributesRenderer) {
        super(successResponse, protocolAttributeEncoder, servicesManager, authenticationContextAttribute,
            delegatedView, releaseProtocolAttributes, authenticationAttributeReleasePolicy,
            serviceSelectionStrategy, attributesRenderer);
    }

    public Cas30JsonResponseView(final boolean successResponse,
                                 final ProtocolAttributeEncoder protocolAttributeEncoder,
                                 final ServicesManager servicesManager,
                                 final String authenticationContextAttribute,
                                 final boolean releaseProtocolAttributes,
                                 final AuthenticationAttributeReleasePolicy authenticationAttributeReleasePolicy,
                                 final AuthenticationServiceSelectionPlan serviceSelectionStrategy,
                                 final CasProtocolAttributesRenderer attributesRenderer) {
        this(successResponse, protocolAttributeEncoder, servicesManager, authenticationContextAttribute,
            createDelegatedView(), releaseProtocolAttributes, authenticationAttributeReleasePolicy,
            serviceSelectionStrategy, attributesRenderer);
    }

    private static MappingJackson2JsonView createDelegatedView() {
        final var view = new MappingJackson2JsonView();
        view.setPrettyPrint(true);
        view.getObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL).findAndRegisterModules();
        return view;
    }

    @Override
    protected void prepareMergedOutputModel(final Map<String, Object> model, final HttpServletRequest request, final HttpServletResponse response) {
        final var casResponse = new CasJsonServiceResponse();
        try {
            super.prepareMergedOutputModel(model, request, response);
            if (getAssertionFrom(model) != null) {
                final var success = createAuthenticationSuccess(model);
                casResponse.setAuthenticationSuccess(success);
            } else {
                final var failure = createAuthenticationFailure(model);
                casResponse.setAuthenticationFailure(failure);
            }
        } catch (final Exception e) {
            final var failure = createAuthenticationFailure(model);
            casResponse.setAuthenticationFailure(failure);
        } finally {
            final Map<String, Object> casModel = new HashMap<>();
            casModel.put(ATTRIBUTE_NAME_MODEL_SERVICE_RESPONSE, casResponse);
            model.clear();
            model.putAll(casModel);
        }
    }

    private CasJsonServiceResponseAuthenticationFailure createAuthenticationFailure(final Map<String, Object> model) {
        final var failure = new CasJsonServiceResponseAuthenticationFailure();
        failure.setCode(getErrorCodeFrom(model));
        failure.setDescription(getErrorDescriptionFrom(model));
        return failure;
    }

    private CasJsonServiceResponseAuthenticationSuccess createAuthenticationSuccess(final Map<String, Object> model) {
        final var success = new CasJsonServiceResponseAuthenticationSuccess();
        success.setAttributes(getModelAttributes(model));
        final var principal = getPrincipal(model);
        success.setUser(principal.getId());
        success.setProxyGrantingTicket(getProxyGrantingTicketIou(model));
        final var chainedAuthentications = getChainedAuthentications(model);
        if (chainedAuthentications != null && !chainedAuthentications.isEmpty()) {
            final var proxies = chainedAuthentications.stream().map(authn -> authn.getPrincipal().getId()).collect(Collectors.toList());
            success.setProxies(proxies);
        }
        return success;
    }






}
