package org.apereo.cas.otp.web.flow;

import org.apereo.cas.otp.repository.credentials.OneTimeTokenCredentialRepository;
import org.apereo.cas.otp.util.QRUtils;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.actions.AbstractMultifactorAuthenticationAction;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
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
    protected Event doExecute(final RequestContext requestContext) {
        val principal = resolvePrincipal(WebUtils.getAuthentication(requestContext).getPrincipal());
        val uid = principal.getId();
        val keyAccount = this.repository.create(uid);
        val keyUri = "otpauth://totp/" + this.label + ':' + uid + "?secret=" + keyAccount.getSecretKey() + "&issuer=" + this.issuer;
        val flowScope = requestContext.getFlowScope();

        flowScope.put(FLOW_SCOPE_ATTR_ACCOUNT, keyAccount);

        val qrCodeBase64 = QRUtils.generateQRCode(keyUri, QRUtils.WIDTH_LARGE, QRUtils.WIDTH_LARGE);
        flowScope.put(FLOW_SCOPE_ATTR_QR_IMAGE_BASE64, qrCodeBase64);

        LOGGER.debug("Registration key URI is [{}]", keyUri);
        return new EventFactorySupport().event(this, CasWebflowConstants.TRANSITION_ID_REGISTER);
    }
}
