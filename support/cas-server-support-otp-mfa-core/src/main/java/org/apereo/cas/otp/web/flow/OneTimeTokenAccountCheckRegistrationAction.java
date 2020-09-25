package org.apereo.cas.otp.web.flow;

import org.apereo.cas.otp.repository.credentials.OneTimeTokenCredentialRepository;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import org.apereo.cas.otp.util.QRUtils;
import java.io.ByteArrayOutputStream;


/**
 * This is {@link OneTimeTokenAccountCheckRegistrationAction}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class OneTimeTokenAccountCheckRegistrationAction extends AbstractAction {
    private final OneTimeTokenCredentialRepository repository;
    private final String label;
    private final String issuer;

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        val uid = WebUtils.getAuthentication(requestContext).getPrincipal().getId();

        val acct = repository.get(uid);
        if (acct == null || StringUtils.isBlank(acct.getSecretKey())) {
            val keyAccount = this.repository.create(uid);
            val keyUri = "otpauth://totp/" + this.label + ':' + uid + "?secret=" + keyAccount.getSecretKey() + "&issuer=" + this.issuer;

            ByteArrayOutputStream o = new ByteArrayOutputStream();
            QRUtils.generateQRCode(o, keyUri, QRUtils.WIDTH_LARGE, QRUtils.WIDTH_LARGE);
            requestContext.getFlowScope().put("key", keyAccount);
            requestContext.getFlowScope().put("QRcode", o.toString());
            return new EventFactorySupport().event(this, "register");
        }
        return success();
    }
}
