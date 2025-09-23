package org.apereo.cas.web.flow;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.web.flow.actions.ConsumerExecutionAction;
import org.apereo.cas.web.support.WebUtils;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import java.util.Objects;

/**
 * This is {@link SetCredentialMetadataAuthenticationAction}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@Slf4j
public class SetCredentialMetadataAuthenticationAction extends ConsumerExecutionAction {
    public SetCredentialMetadataAuthenticationAction() {
        super(requestContext -> {
            val authentication = WebUtils.getAuthentication(requestContext);
            Objects.requireNonNull(authentication, "Authentication cannot be null");
            val credential = WebUtils.getCredential(requestContext);
            Objects.requireNonNull(credential, "Credential cannot be null");
            credential.getCredentialMetadata().putProperty(Authentication.class.getName(), authentication);
            LOGGER.debug("Credential metadata has been set with authentication [{}]", authentication);
            WebUtils.putCredential(requestContext, credential);
        });
    }
}
