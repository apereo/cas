package org.apereo.cas.otp.web.flow;

import module java.base;
import org.apereo.cas.authentication.OneTimeTokenAccount;
import org.apereo.cas.otp.repository.credentials.OneTimeTokenCredentialRepository;
import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;
import org.apereo.cas.web.flow.util.MultifactorAuthenticationWebflowUtils;
import org.apereo.cas.web.support.WebUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.jspecify.annotations.Nullable;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link OneTimeTokenAccountConfirmSelectionRegistrationAction}.
 * The user picks one of the devices offered to them, and only those: the device id comes from the request, so a
 * device that was not offered, such as one that belongs to another user or another tenant, is refused and the
 * selection view is shown again.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiredArgsConstructor
@Slf4j
public class OneTimeTokenAccountConfirmSelectionRegistrationAction extends BaseCasWebflowAction {
    /**
     * Account id parameter.
     */
    public static final String REQUEST_PARAMETER_ACCOUNT_ID = "accountId";

    private final OneTimeTokenCredentialRepository repository;

    @Override
    protected @Nullable Event doExecuteInternal(final RequestContext requestContext) {
        val id = NumberUtils.toLong(WebUtils.getRequestParameterOrAttribute(requestContext, REQUEST_PARAMETER_ACCOUNT_ID)
            .orElse(StringUtils.EMPTY), -1);
        val account = findOfferedAccount(requestContext, id)
            .map(offered -> repository.getForCurrentTenant(offered.getUsername(), offered.getId()));
        if (account.isEmpty()) {
            LOGGER.warn("Device [{}] is not one of the devices offered for selection", id);
            return error();
        }
        MultifactorAuthenticationWebflowUtils.putOneTimeTokenAccount(requestContext, account.get());
        return success();
    }

    private static Optional<OneTimeTokenAccount> findOfferedAccount(final RequestContext requestContext, final long id) {
        val offered = (Collection<?>) MultifactorAuthenticationWebflowUtils.getOneTimeTokenAccounts(requestContext);
        return Optional.ofNullable(offered)
            .stream()
            .flatMap(Collection::stream)
            .filter(OneTimeTokenAccount.class::isInstance)
            .map(OneTimeTokenAccount.class::cast)
            .filter(account -> account.getId() == id)
            .findFirst();
    }
}
