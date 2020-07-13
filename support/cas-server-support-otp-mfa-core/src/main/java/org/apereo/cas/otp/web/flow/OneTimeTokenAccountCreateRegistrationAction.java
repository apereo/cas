package org.apereo.cas.otp.web.flow;

import org.apereo.cas.otp.repository.credentials.OneTimeTokenCredentialRepository;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link OneTimeTokenAccountCreateRegistrationAction}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class OneTimeTokenAccountCreateRegistrationAction extends AbstractAction {
    /**
     * Flow scope attribute name indicating the account.
     */
    public static final String FLOW_SCOPE_ATTR_ACCOUNT = "key";

    /**
     * Flow scope attribute name indicating the account uri.
     */
    public static final String FLOW_SCOPE_ATTR_ACCOUNT_URI = "keyUri";

    private final OneTimeTokenCredentialRepository repository;

    private final String label;

    private final String issuer;

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        val uid = WebUtils.getAuthentication(requestContext).getPrincipal().getId();
        val keyAccount = this.repository.create(uid);
        val keyUri = "otpauth://totp/" + this.label + ':' + uid + "?secret=" + keyAccount.getSecretKey() + "&issuer=" + this.issuer;
        val flowScope = requestContext.getFlowScope();
        flowScope.put(FLOW_SCOPE_ATTR_ACCOUNT, keyAccount);
        flowScope.put(FLOW_SCOPE_ATTR_ACCOUNT_URI, keyUri);
        LOGGER.debug("Registration key URI is [{}]", keyUri);
        return new EventFactorySupport().event(this, CasWebflowConstants.TRANSITION_ID_REGISTER);
    }
}
