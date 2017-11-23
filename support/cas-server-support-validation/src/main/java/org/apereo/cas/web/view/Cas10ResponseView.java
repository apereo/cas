package org.apereo.cas.web.view;

import org.apereo.cas.authentication.ProtocolAttributeEncoder;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.web.support.AuthenticationAttributeReleasePolicy;
import org.apereo.cas.services.web.view.AbstractCasView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * Custom View to Return the CAS 1.0 Protocol Response. Implemented as a view
 * class rather than a JSP (like CAS 2.0 spec) because of the requirement of the
 * line feeds to be "\n".
 *
 * @author Scott Battaglia
 * @since 3.0.0
 */
public class Cas10ResponseView extends AbstractCasView {

    public Cas10ResponseView(final boolean successResponse,
                             final ProtocolAttributeEncoder protocolAttributeEncoder,
                             final ServicesManager servicesManager,
                             final String authenticationContextAttribute,
                             final AuthenticationAttributeReleasePolicy authenticationAttributeReleasePolicy) {
        super(successResponse, protocolAttributeEncoder, servicesManager, authenticationContextAttribute,
                authenticationAttributeReleasePolicy);
    }

    @Override
    protected void renderMergedOutputModel(final Map model, final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        if (this.successResponse) {
            response.getWriter().print("yes\n" + getPrimaryAuthenticationFrom(model).getPrincipal().getId() + '\n');
        } else {
            response.getWriter().print("no\n\n");
        }
    }
}
