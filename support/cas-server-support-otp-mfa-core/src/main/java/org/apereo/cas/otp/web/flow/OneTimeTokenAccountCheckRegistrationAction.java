package org.apereo.cas.otp.web.flow;

import org.apereo.cas.otp.repository.credentials.OneTimeTokenCredentialRepository;
import org.apereo.cas.otp.util.QRUtils;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
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
@Slf4j
@RequiredArgsConstructor
public class OneTimeTokenAccountCheckRegistrationAction extends AbstractAction {
    /**
     * Flow scope attribute name indicating the account.
     */
    public static final String FLOW_SCOPE_ATTR_ACCOUNT = "key";
    /**
     * Flow scope attribute name indicating the QR code image in base64 encoding.
     */
    public static final String FLOW_SCOPE_ATTR_QR_IMAGE_BASE64 = "QRcode";


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
            val qrCodeBase64 = QRUtils.generateQRCode(keyUri, QRUtils.WIDTH_LARGE, QRUtils.WIDTH_LARGE);
            val flowScope = requestContext.getFlowScope();
            flowScope.put(FLOW_SCOPE_ATTR_ACCOUNT, keyAccount);
            flowScope.put(FLOW_SCOPE_ATTR_QR_IMAGE_BASE64, qrCodeBase64);
            LOGGER.debug("Registration key URI is [{}]", keyUri);
            return new EventFactorySupport().event(this, CasWebflowConstants.TRANSITION_ID_REGISTER);
        }
        return success();
    }
}
