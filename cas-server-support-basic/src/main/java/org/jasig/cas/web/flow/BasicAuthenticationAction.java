package org.jasig.cas.web.flow;

import org.jasig.cas.authentication.Credential;
import org.jasig.cas.authentication.UsernamePasswordCredential;
import org.jasig.cas.web.support.WebUtils;
import org.pac4j.core.context.J2EContext;
import org.pac4j.core.context.WebContext;
import org.pac4j.http.credentials.UsernamePasswordCredentials;
import org.pac4j.http.credentials.extractor.BasicAuthExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.webflow.execution.RequestContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This is {@link BasicAuthenticationAction} that extracts basic authN credentials from the request.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
@Component("basicAuthenticationAction")
public class BasicAuthenticationAction extends AbstractNonInteractiveCredentialsAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(BasicAuthenticationAction.class);


    @Override
    protected Credential constructCredentialsFromRequest(final RequestContext requestContext) {
        final HttpServletRequest request = WebUtils.getHttpServletRequest(requestContext);
        final HttpServletResponse response = WebUtils.getHttpServletResponse(requestContext);
        final BasicAuthExtractor extractor = new BasicAuthExtractor(this.getClass().getSimpleName());
        final WebContext webContext = new J2EContext(request, response);
        try {
            final UsernamePasswordCredentials credentials = extractor.extract(webContext);
            if (credentials != null) {
                LOGGER.debug("Received basic authentication request from credentials {} ", credentials);
                return new UsernamePasswordCredential(credentials.getUsername(), credentials.getPassword());
            }
        } catch (final Exception e) {
            LOGGER.warn(e.getMessage(), e);

        }
        return null;
    }
}
