package org.apereo.cas.services.web.view;

import org.springframework.web.servlet.View;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * Renders and prepares CAS2 views. This view is responsible
 * to simply just prep the base model, and delegates to
 * a the real view to render the final output.
 * @author Misagh Moayyed
 * @since 4.1.0
 */
public abstract class AbstractDelegatingCasView extends AbstractCasView {
    /** View to delegate. */
    private View view;

    /**
     * Instantiates a new Abstract cas view.
     */
    protected AbstractDelegatingCasView() {
        
    }

    @Override
    protected void renderMergedOutputModel(final Map<String, Object> model, final HttpServletRequest request,
                                           final HttpServletResponse response) throws Exception {
        logger.debug("Preparing the output model to render view...");
        prepareMergedOutputModel(model, request, response);

        logger.trace("Prepared output model with objects [{}]. Now rendering view...",
                model.keySet().toArray());
    
        if (this.view != null) {
            this.view.render(model, request, response);
        } else {
            logger.warn("No view is available to render the output for {}", this.getClass().getName());
        }
    }

    /**
     * Prepare merged output model before final rendering.
     *
     * @param model the model
     * @param request the request
     * @param response the response
     * @throws Exception the exception
     */
    protected abstract void prepareMergedOutputModel(Map<String, Object> model, HttpServletRequest request,
                                                     HttpServletResponse response) throws Exception;

    public void setView(final View view) {
        this.view = view;
    }

    public View getView() {
        return this.view;
    }
}
