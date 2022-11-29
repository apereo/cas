package org.apereo.cas.web.flow.action;

import org.apereo.cas.authentication.SurrogateCredential;
import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link SurrogateInitialAuthenticationAction}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
@RequiredArgsConstructor
public class SurrogateInitialAuthenticationAction extends BaseCasWebflowAction {
    private final String separator;

    @Override
    protected Event doExecute(final RequestContext context) throws Exception {
        val surrogateCredential = WebUtils.getCredential(context, SurrogateCredential.class);
        if (surrogateCredential == null) {
            LOGGER.debug("Provided credentials cannot be found");
            return null;
        }
        val surrogateUsername = surrogateCredential.getSurrogateUsername();
        if (surrogateUsername != null) {
            LOGGER.debug("Provided credentials already contains a surrogate [{}]", surrogateUsername);
            return null;
        }
        if (surrogateCredential.getId().contains(separator)) {
            LOGGER.debug("Credential identifier includes the separator [{}]. Converting to surrogate...", separator);
            addSurrogateInformation(context, surrogateCredential);
        } else {
            removeSurrogateInformation(context, surrogateCredential);
        }
        return null;
    }

    private void addSurrogateInformation(final RequestContext context, final SurrogateCredential surrogateCredential) {
        val givenUserName = surrogateCredential.getId();
        val surrogateUsername = givenUserName.substring(0, givenUserName.indexOf(separator));
        val primaryUserName = givenUserName.substring(givenUserName.indexOf(separator) + separator.length());
        LOGGER.debug("Converting to surrogate credential for username [{}], surrogate username [{}]", primaryUserName, surrogateUsername);

        if (StringUtils.isBlank(surrogateUsername)) {
            surrogateCredential.setId(primaryUserName);
            WebUtils.putCredential(context, surrogateCredential);
            WebUtils.putSurrogateAuthenticationRequest(context, Boolean.TRUE);
            LOGGER.debug("No surrogate username is defined; Signal webflow to request for surrogate credentials");
        } else {
            surrogateCredential.setId(primaryUserName);
            surrogateCredential.setSurrogateUsername(surrogateUsername);
            WebUtils.putCredential(context, surrogateCredential);
            WebUtils.putSurrogateAuthenticationRequest(context, Boolean.FALSE);
            LOGGER.debug("Converted credential to surrogate for username [{}] and assigned it to webflow", primaryUserName);
        }
    }

    private void removeSurrogateInformation(final RequestContext context, final SurrogateCredential surrogateCredential) {
        surrogateCredential.setSurrogateUsername(null);
        WebUtils.putCredential(context, surrogateCredential);
    }
}
