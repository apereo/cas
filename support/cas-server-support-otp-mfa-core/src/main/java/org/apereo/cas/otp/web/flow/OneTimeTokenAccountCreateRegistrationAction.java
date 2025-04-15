package org.apereo.cas.otp.web.flow;

import org.apereo.cas.otp.repository.credentials.OneTimeTokenCredentialRepository;
import org.apereo.cas.otp.util.QRUtils;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.actions.AbstractMultifactorAuthenticationAction;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
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
public class OneTimeTokenAccountCreateRegistrationAction extends AbstractMultifactorAuthenticationAction {
    /**
     * Flow scope attribute name indicating the account.
     */
    public static final String FLOW_SCOPE_ATTR_ACCOUNT = "key";

    /**
     * Flow scope attribute name indicating the account QR code.
     */
    public static final String FLOW_SCOPE_ATTR_QR_IMAGE_BASE64 = "QRcode";

    private final OneTimeTokenCredentialRepository repository;

    private final String label;

    private final String issuer;

    @Override
    protected Event doExecuteInternal(final RequestContext requestContext) throws Exception {
        val principal = resolvePrincipal(WebUtils.getAuthentication(requestContext).getPrincipal(), requestContext);
        val uid = principal.getId();
        val keyAccount = repository.create(uid);
        val keyUri = "otpauth://totp/" + this.label + ':' + uid + "?secret=" + keyAccount.getSecretKey() + "&issuer=" + this.issuer;
        val flowScope = requestContext.getFlowScope();

        flowScope.put(FLOW_SCOPE_ATTR_ACCOUNT, keyAccount);

        val qrCodeBase64 = QRUtils.generateQRCode(keyUri, QRUtils.SIZE, QRUtils.SIZE);
        flowScope.put(FLOW_SCOPE_ATTR_QR_IMAGE_BASE64, qrCodeBase64);

        LOGGER.debug("Registration key URI is [{}]", keyUri);
        return eventFactory.event(this, CasWebflowConstants.TRANSITION_ID_REGISTER);
    }
}
