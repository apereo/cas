package org.apereo.cas.web.flow.action;

import org.apereo.cas.authentication.RememberMeCredential;
import org.apereo.cas.authentication.SurrogateUsernamePasswordCredential;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.beanutils.BeanUtils;
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
        val up = WebUtils.getCredential(context, UsernamePasswordCredential.class);
        if (up == null) {
            LOGGER.debug("Provided credentials cannot be found, or are already of type [{}]",
                SurrogateUsernamePasswordCredential.class.getName());
            return null;
        }
        if (up.getUsername().contains(separator)) {
            LOGGER.debug("Credential username includes the separator [{}]. Converting to surrogate...", separator);
            convertToSurrogateCredential(context, up);
        } else {
            convertToUsernamePasswordCredential(context, up);
        }
        return null;
    }

    private void convertToSurrogateCredential(final RequestContext context, final UsernamePasswordCredential up) {
        val givenUserName = up.getUsername();
        val surrogateUsername = givenUserName.substring(0, givenUserName.indexOf(separator));
        val primaryUserName = givenUserName.substring(givenUserName.indexOf(separator) + separator.length());
        LOGGER.debug("Converting to surrogate credential for username [{}], surrogate username [{}]", primaryUserName, surrogateUsername);

        if (StringUtils.isBlank(surrogateUsername)) {
            up.setUsername(primaryUserName);
            WebUtils.putSurrogateAuthenticationRequest(context, Boolean.TRUE);
            WebUtils.putCredential(context, up);
            LOGGER.debug("No surrogate username is defined; Signal webflow to request for surrogate credentials");
        } else {
            val sc = new SurrogateUsernamePasswordCredential();
            sc.setUsername(primaryUserName);
            sc.setSurrogateUsername(surrogateUsername);
            sc.assignPassword(up.toPassword());
            if (up instanceof RememberMeCredential) {
                sc.setRememberMe(((RememberMeCredential) up).isRememberMe());
            }
            WebUtils.putSurrogateAuthenticationRequest(context, Boolean.FALSE);
            LOGGER.debug("Converted credential to surrogate for username [{}] and assigned it to webflow", primaryUserName);
            WebUtils.putCredential(context, sc);
        }
    }

    private static void convertToUsernamePasswordCredential(final RequestContext context,
                                                            final UsernamePasswordCredential up) throws Exception {
        if (up instanceof SurrogateUsernamePasswordCredential) {
            val sc = new UsernamePasswordCredential();
            BeanUtils.copyProperties(sc, up);
            WebUtils.putCredential(context, sc);
        }
    }
}
