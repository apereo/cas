package org.apereo.cas.web.flow.action;

import org.apereo.cas.authentication.MutableCredential;
import org.apereo.cas.authentication.surrogate.SurrogateCredentialTrait;
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
    protected Event doExecute(final RequestContext context) {
        val credential = WebUtils.getCredential(context, MutableCredential.class);
        if (credential == null) {
            LOGGER.debug("Provided credentials cannot be found");
        } else if (credential.getId().contains(separator)) {
            LOGGER.debug("Credential identifier includes the separator [{}]. Converting to surrogate...", separator);
            addSurrogateInformation(context, credential);
        } else {
            removeSurrogateInformation(context, credential);
        }
        return null;
    }

    private void addSurrogateInformation(final RequestContext context, final MutableCredential credential) {
        val givenUserName = credential.getId();
        val surrogateUsername = givenUserName.substring(0, givenUserName.indexOf(separator));
        val primaryUserName = givenUserName.substring(givenUserName.indexOf(separator) + separator.length());
        LOGGER.debug("Converting to surrogate credential for username [{}], surrogate username [{}]", primaryUserName, surrogateUsername);

        if (StringUtils.isBlank(surrogateUsername)) {
            credential.setId(primaryUserName);
            WebUtils.putCredential(context, credential);
            WebUtils.putSurrogateAuthenticationRequest(context, Boolean.TRUE);
            LOGGER.debug("No surrogate username is defined; Signal webflow to request for surrogate credentials");
        } else {
            credential.setId(primaryUserName);
            credential.getCredentialMetadata().addTrait(new SurrogateCredentialTrait(surrogateUsername));
            WebUtils.putCredential(context, credential);
            WebUtils.putSurrogateAuthenticationRequest(context, Boolean.FALSE);
            LOGGER.debug("Converted credential to surrogate for username [{}] and assigned it to webflow", primaryUserName);
        }
    }

    private static void removeSurrogateInformation(final RequestContext context, final MutableCredential credential) {
        credential.getCredentialMetadata().removeTrait(SurrogateCredentialTrait.class);
        WebUtils.putCredential(context, credential);
    }
}
