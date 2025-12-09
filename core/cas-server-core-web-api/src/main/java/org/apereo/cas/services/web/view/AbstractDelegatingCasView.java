package org.apereo.cas.services.web.view;

import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.ProtocolAttributeEncoder;
import org.apereo.cas.authentication.attribute.AttributeDefinitionStore;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.validation.AuthenticationAttributeReleasePolicy;
import org.apereo.cas.validation.CasProtocolAttributesRenderer;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jspecify.annotations.NonNull;
import org.springframework.web.servlet.View;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * Renders and prepares CAS2 views. This view is responsible
 * to just prep the base model, and delegates to
 * a the real view to render the final output.
 *
 * @author Misagh Moayyed
 * @since 4.1.0
 */
@Slf4j
@Getter
public abstract class AbstractDelegatingCasView extends AbstractCasView {
    /**
     * View to delegate.
     */
    protected final View view;

    protected AbstractDelegatingCasView(final boolean successResponse,
                                        final ProtocolAttributeEncoder protocolAttributeEncoder,
                                        final ServicesManager servicesManager,
                                        final View view,
                                        final AuthenticationAttributeReleasePolicy authenticationAttributeReleasePolicy,
                                        final AuthenticationServiceSelectionPlan authenticationRequestServiceSelectionStrategies,
                                        final CasProtocolAttributesRenderer attributesRenderer,
                                        final AttributeDefinitionStore attributeDefinitionStore) {
        super(successResponse, protocolAttributeEncoder, servicesManager,
            authenticationAttributeReleasePolicy, authenticationRequestServiceSelectionStrategies,
            attributesRenderer, attributeDefinitionStore);
        this.view = view;
    }

    @Override
    protected void renderMergedOutputModel(final @NonNull Map<String, Object> model,
                                           @NonNull final HttpServletRequest request,
                                           @NonNull final HttpServletResponse response) {
        FunctionUtils.doAndHandle(_ -> {
            val requestWrapper = new ContentCachingRequestWrapper(request, 0);
            val responseWrapper = new ContentCachingResponseWrapper(response);
            LOGGER.debug("Preparing the output model [{}] to render view [{}]", model.keySet(), getClass().getSimpleName());
            prepareMergedOutputModel(model, request, response);
            LOGGER.trace("Prepared output model with objects [{}]. Now rendering view...", model.keySet().toArray());
            try {
                getView().render(model, requestWrapper, responseWrapper);
            } finally {
                val responseArray = responseWrapper.getContentAsByteArray();
                val output = new String(responseArray, responseWrapper.getCharacterEncoding());
                val message = String.format("Final CAS response for [%s] is:%n%s%n", getView().toString(), output);
                LOGGER.debug(message);
                responseWrapper.copyBodyToResponse();
            }
        });
    }

    /**
     * Prepare merged output model before final rendering.
     *
     * @param model    the model
     * @param request  the request
     * @param response the response
     * @throws Exception the exception
     */
    protected abstract void prepareMergedOutputModel(Map<String, Object> model, HttpServletRequest request,
                                                     HttpServletResponse response) throws Exception;

}
