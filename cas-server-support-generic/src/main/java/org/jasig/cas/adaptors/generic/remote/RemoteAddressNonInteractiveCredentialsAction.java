package org.jasig.cas.adaptors.generic.remote;

import org.jasig.cas.authentication.Credential;
import org.jasig.cas.web.flow.AbstractNonInteractiveCredentialsAction;
import org.jasig.cas.web.support.WebUtils;
import org.springframework.util.StringUtils;
import org.springframework.webflow.execution.RequestContext;

import javax.servlet.http.HttpServletRequest;

/**
 * A webflow action that attrmpts to grab the remote address from the reques,
 * and construct a {@link RemoteAddressCredential} object.
 * @author Scott Battaglia
 * @since 3.2.1
 */
public final class RemoteAddressNonInteractiveCredentialsAction extends AbstractNonInteractiveCredentialsAction {
    @Override
    protected Credential constructCredentialsFromRequest(final RequestContext context) {
        final HttpServletRequest request = WebUtils.getHttpServletRequest(context);
        final String remoteAddress = request.getRemoteAddr();

        if (StringUtils.hasText(remoteAddress)) {
            return new RemoteAddressCredential(remoteAddress);
        }

        logger.debug("No remote address found.");
        return null;
    }
}
