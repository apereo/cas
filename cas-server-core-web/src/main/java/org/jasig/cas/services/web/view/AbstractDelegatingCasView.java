package org.jasig.cas.services.web.view;

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
    private final View view;

    /**
     * Instantiates a new Abstract cas jstl view.
     *
     * @param view the view
     */
    protected AbstractDelegatingCasView(final View view) {
        this.view = view;
    }

    @Override
    protected void renderMergedOutputModel(final Map<String, Object> model, final HttpServletRequest request,
                                           final HttpServletResponse response) throws Exception {
        logger.debug("Preparing the output model to render view...");
        prepareMergedOutputModel(model, request, response);

        logger.trace("Prepared output model with objects [{}]. Now rendering view...",
                model.keySet().toArray());
        this.view.render(model, request, response);
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

    public final View getDelegatedView() {
        return view;
    }
}
