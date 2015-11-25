package org.jasig.cas.web.view;

import org.jasig.cas.services.web.view.AbstractDelegatingCasView;
import org.jasig.cas.CasViewConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
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
public class Cas20ResponseView extends AbstractDelegatingCasView {

    /**
     * Instantiates a new Abstract cas jstl view.
     *
     * @param view the view
     */
    protected Cas20ResponseView(final View view) {
        super(view);
    }

    @Override
    protected void prepareMergedOutputModel(final Map<String, Object> model, final HttpServletRequest request,
                                            final HttpServletResponse response) throws Exception {
        super.putIntoModel(model, CasViewConstants.MODEL_ATTRIBUTE_NAME_PRINCIPAL, getPrincipal(model));
        super.putIntoModel(model, CasViewConstants.MODEL_ATTRIBUTE_NAME_CHAINED_AUTHENTICATIONS, getChainedAuthentications(model));
        super.putIntoModel(model, CasViewConstants.MODEL_ATTRIBUTE_NAME_PRIMARY_AUTHENTICATION, getPrimaryAuthenticationFrom(model));
    }


    /**
     * The type Success.
     */
    @Component("cas2ServiceSuccessView")
    public static class Success extends Cas20ResponseView {
        /**
         * Instantiates a new Success.
         * @param view the view
         */
        @Autowired
        public Success(@Qualifier("cas2JstlSuccessView")
                       final View view) {
            super(view);
            super.setSuccessResponse(true);
        }
    }
}
