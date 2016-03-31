package org.jasig.cas.adaptors.trusted.web.flow;

import org.jasig.cas.adaptors.trusted.authentication.principal.PrincipalBearingCredential;
import org.jasig.cas.authentication.Credential;
import org.jasig.cas.web.flow.AbstractNonInteractiveCredentialsAction;
import org.jasig.cas.web.support.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.webflow.execution.RequestContext;

import javax.servlet.http.HttpServletRequest;

/**
 * Implementation of the NonInteractiveCredentialsAction that looks for a remote
 * user that is set in the {@code HttpServletRequest} and attempts to
 * construct a Principal (and thus a PrincipalBearingCredential). If it doesn't
 * find one, this class returns and error event which tells the web flow it
 * could not find any credentials.
 *
 * @author Scott Battaglia
 * @since 3.0.0
 */
@Component("principalFromRemoteUserAction")
public final class PrincipalFromRequestRemoteUserNonInteractiveCredentialsAction
            extends AbstractNonInteractiveCredentialsAction {

    private final transient Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    protected Credential constructCredentialsFromRequest(final RequestContext context) {
        final HttpServletRequest request = WebUtils.getHttpServletRequest(context);
        final String remoteUser = request.getRemoteUser();

        if (StringUtils.hasText(remoteUser)) {
            logger.debug("Remote User [{}] found in HttpServletRequest", remoteUser);
            return new PrincipalBearingCredential(this.principalFactory.createPrincipal(remoteUser));
        }
        return null;
    }
}
