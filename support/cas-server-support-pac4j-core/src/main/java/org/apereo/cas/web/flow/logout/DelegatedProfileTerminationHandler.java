package org.apereo.cas.web.flow.logout;

import org.apereo.cas.logout.SessionTerminationHandler;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.web.support.WebUtils;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.core.util.Pac4jConstants;
import org.pac4j.jee.context.JEEContext;
import org.pac4j.jee.context.session.JEESessionStore;
import org.springframework.webflow.execution.RequestContext;
import java.io.Serializable;
import java.util.List;

/**
 * This is {@link DelegatedProfileTerminationHandler}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Slf4j
public class DelegatedProfileTerminationHandler implements SessionTerminationHandler {
    @Override
    public List<? extends Serializable> beforeSessionTermination(final RequestContext requestContext) {
        LOGGER.trace("Destroying application session...");
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
        val response = WebUtils.getHttpServletResponseFromExternalWebflowContext(requestContext);

        val context = new JEEContext(request, response);
        val manager = new ProfileManager(context, new JEESessionStore());
        manager.removeProfiles();
        val session = request.getSession(false);
        val url = FunctionUtils.doIfNotNull(session, () -> (String) session.getAttribute(Pac4jConstants.REQUESTED_URL));
        return FunctionUtils.doIfNotNull(url, () -> List.of(new Pac4jRequestedUrl(url)), List::<Pac4jRequestedUrl>of).get();
    }

    @Override
    public void afterSessionTermination(final List<? extends Serializable> terminationResults, final RequestContext requestContext) {
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
        terminationResults
            .stream()
            .filter(Pac4jRequestedUrl.class::isInstance)
            .map(Pac4jRequestedUrl.class::cast)
            .filter(req -> StringUtils.isNotBlank(req.url()))
            .forEach(req -> request.getSession(true).setAttribute(Pac4jConstants.REQUESTED_URL, req.url()));
    }

    private record Pac4jRequestedUrl(String url) implements Serializable {
    }
}
