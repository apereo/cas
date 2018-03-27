package org.apereo.cas.otp.web.flow;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.otp.repository.credentials.OneTimeTokenCredentialRepository;
import org.apereo.cas.otp.repository.credentials.OneTimeTokenAccount;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * Save the registration credential into the repository.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
@AllArgsConstructor
public class OneTimeTokenAccountSaveRegistrationAction extends AbstractAction {
    private final OneTimeTokenCredentialRepository repository;

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        final OneTimeTokenAccount account = requestContext.getFlowScope().get("key", OneTimeTokenAccount.class);

        final String uid = WebUtils.getAuthentication(requestContext).getPrincipal().getId();
        repository.save(uid, account.getSecretKey(), account.getValidationCode(), account.getScratchCodes());
        return success();
    }
}
