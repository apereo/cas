package org.apereo.cas.otp.web.flow;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.otp.repository.credentials.OneTimeTokenAccount;
import org.apereo.cas.otp.repository.credentials.OneTimeTokenCredentialRepository;
import org.apereo.cas.web.support.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link OneTimeTokenAccountCheckRegistrationAction}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class OneTimeTokenAccountCheckRegistrationAction extends AbstractAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(OneTimeTokenAccountCheckRegistrationAction.class);

    private final OneTimeTokenCredentialRepository repository;
    private final String label;
    private final String issuer;

    public OneTimeTokenAccountCheckRegistrationAction(final OneTimeTokenCredentialRepository repository,
                                                      final String label,
                                                      final String issuer) {
        this.repository = repository;
        this.label = label;
        this.issuer = issuer;
    }

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        final String uid = WebUtils.getAuthentication(requestContext).getPrincipal().getId();

        final OneTimeTokenAccount acct = repository.get(uid);
        if (acct == null || StringUtils.isBlank(acct.getSecretKey())) {
            final OneTimeTokenAccount keyAccount = this.repository.create(uid);
            final String keyUri = "otpauth://totp/" + this.label + ':' + uid + "?secret=" + keyAccount.getSecretKey() + "&issuer=" + this.issuer;
            requestContext.getFlowScope().put("key", keyAccount);
            requestContext.getFlowScope().put("keyUri", keyUri);
            LOGGER.debug("Registration key URI is [{}]", keyUri);
            return new EventFactorySupport().event(this, "register");
        }
        return success();
    }
}
