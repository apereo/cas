package org.apereo.cas.services.web.view;

import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.AuthenticationAttributeReleasePolicy;
import org.apereo.cas.authentication.ProtocolAttributeEncoder;
import org.apereo.cas.services.ServicesManager;
import org.springframework.web.servlet.View;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * Renders and prepares CAS2 views. This view is responsible
 * to simply just prep the base model, and delegates to
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
    protected View view;

    public AbstractDelegatingCasView(final boolean successResponse,
                                     final ProtocolAttributeEncoder protocolAttributeEncoder,
                                     final ServicesManager servicesManager,
                                     final String authenticationContextAttribute,
                                     final View view,
                                     final AuthenticationAttributeReleasePolicy authenticationAttributeReleasePolicy) {
        super(successResponse, protocolAttributeEncoder, servicesManager, authenticationContextAttribute,
            authenticationAttributeReleasePolicy);
        this.view = view;
    }

    @Override
    @SneakyThrows
    protected void renderMergedOutputModel(final Map<String, Object> model, final HttpServletRequest request,
                                           final HttpServletResponse response) {

        LOGGER.debug("Preparing the output model [{}] to render view [{}]", model.keySet(), getClass().getSimpleName());
        prepareMergedOutputModel(model, request, response);
        LOGGER.debug("Prepared output model with objects [{}]. Now rendering view...", model.keySet().toArray());

        if (this.view != null) {
            this.view.render(model, request, response);
        } else {
            LOGGER.warn("No view is available to render the output for [{}]", this.getClass().getName());
        }
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
