package org.apereo.cas.web.view.json;

import org.apereo.cas.CasViewConstants;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.ProtocolAttributeEncoder;
import org.apereo.cas.authentication.attribute.AttributeDefinitionStore;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.validation.AuthenticationAttributeReleasePolicy;
import org.apereo.cas.validation.CasProtocolAttributesRenderer;
import org.apereo.cas.web.view.Cas30ResponseView;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.val;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
@ToString
@Getter
@Setter
public class Cas30JsonResponseView extends Cas30ResponseView {
    /**
     * Attribute name in the final model representing the service response.
     */
    public static final String ATTRIBUTE_NAME_MODEL_SERVICE_RESPONSE = "serviceResponse";

    private static final MappingJackson2JsonView JSON_VIEW;

    static {
        JSON_VIEW = new MappingJackson2JsonView();
        JSON_VIEW.setPrettyPrint(true);
        JSON_VIEW.getObjectMapper()
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .findAndRegisterModules();
    }

    public Cas30JsonResponseView(final boolean successResponse,
                                 final ProtocolAttributeEncoder protocolAttributeEncoder,
                                 final ServicesManager servicesManager,
                                 final View delegatedView,
                                 final AuthenticationAttributeReleasePolicy authenticationAttributeReleasePolicy,
                                 final AuthenticationServiceSelectionPlan serviceSelectionStrategy,
                                 final CasProtocolAttributesRenderer attributesRenderer,
                                 final AttributeDefinitionStore attributeDefinitionStore) {
        super(successResponse, protocolAttributeEncoder, servicesManager,
            delegatedView, authenticationAttributeReleasePolicy,
            serviceSelectionStrategy, attributesRenderer, attributeDefinitionStore);
    }

    public Cas30JsonResponseView(final boolean successResponse,
                                 final ProtocolAttributeEncoder protocolAttributeEncoder,
                                 final ServicesManager servicesManager,
                                 final AuthenticationAttributeReleasePolicy authenticationAttributeReleasePolicy,
                                 final AuthenticationServiceSelectionPlan serviceSelectionStrategy,
                                 final CasProtocolAttributesRenderer attributesRenderer,
                                 final AttributeDefinitionStore attributeDefinitionStore) {
        this(successResponse, protocolAttributeEncoder, servicesManager,
            JSON_VIEW, authenticationAttributeReleasePolicy,
            serviceSelectionStrategy, attributesRenderer, attributeDefinitionStore);
    }

    @Override
    protected void prepareMergedOutputModel(final Map<String, Object> model, final HttpServletRequest request,
                                            final HttpServletResponse response) throws Exception {
        val casResponse = new CasJsonServiceResponse();
        try {
            super.prepareMergedOutputModel(model, request, response);
            if (getAssertionFrom(model) != null) {
                val success = createAuthenticationSuccess(model);
                casResponse.setAuthenticationSuccess(success);
            } else {
                val failure = createAuthenticationFailure(model);
                casResponse.setAuthenticationFailure(failure);
            }
        } catch (final Exception e) {
            val failure = createAuthenticationFailure(model);
            casResponse.setAuthenticationFailure(failure);
        } finally {
            val casModel = new HashMap<String, Object>();
            casModel.put(ATTRIBUTE_NAME_MODEL_SERVICE_RESPONSE, casResponse);
            model.clear();
            model.putAll(casModel);
            if (LoggingUtils.isProtocolMessageLoggerEnabled()) {
                LoggingUtils.protocolMessage("CAS Validation Response",
                    JSON_VIEW.getObjectMapper().writeValueAsString(casModel));
            }
        }
    }

    protected CasJsonServiceResponseAuthenticationFailure createAuthenticationFailure(final Map<String, Object> model) {
        val failure = new CasJsonServiceResponseAuthenticationFailure();
        failure.setCode(getErrorCodeFrom(model));
        failure.setDescription(getErrorDescriptionFrom(model));
        return failure;
    }

    protected CasJsonServiceResponseAuthenticationSuccess createAuthenticationSuccess(final Map<String, Object> model) {
        val success = new CasJsonServiceResponseAuthenticationSuccess();
        val modelAttributes = getModelAttributes(model);
        val processedAttributes = modelAttributes
            .entrySet()
            .stream()
            .collect(Collectors.toMap(Map.Entry::getKey, entry -> {
                val attributeDefinition = attributeDefinitionStore.locateAttributeDefinition(entry.getKey())
                    .or(() -> attributeDefinitionStore.locateAttributeDefinitionByName(entry.getKey()));
                return attributeDefinition.map(definition -> definition.toAttributeValue(entry.getValue())).orElseGet(entry::getValue);
            }));

        success.setAttributes(processedAttributes);
        val principal = getPrincipal(model);
        success.setUser(principal.getId());
        success.setProxyGrantingTicket(getProxyGrantingTicketIou(model));
        success.setProxies((List) model.get(CasViewConstants.MODEL_ATTRIBUTE_NAME_PROXIES));
        return success;
    }

}
