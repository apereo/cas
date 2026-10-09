package org.apereo.cas.oidc.vc.issuer.deferred;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.oidc.vc.issuer.deferred.OidcVerifiableCredentialDeferredIssuanceService.DeferredTransaction;
import org.apereo.cas.oidc.vc.issuer.deferred.OidcVerifiableCredentialDeferredIssuanceService.TransactionStatus;
import org.apereo.cas.web.BaseCasActuatorEndpoint;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import lombok.val;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.endpoint.Access;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;
import org.springframework.boot.actuate.endpoint.web.WebEndpointResponse;

/**
 * Lists the pending deferred credential transactions and approves or denies them. An approved transaction lets the wallet
 * collect its credentials from the deferred credential endpoint; a denied one is answered with
 * {@code credential_request_denied}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@Endpoint(id = "oidcVcDeferred", defaultAccess = Access.NONE)
public class OidcVerifiableCredentialDeferredIssuanceEndpoint extends BaseCasActuatorEndpoint {
    private final ObjectProvider<OidcVerifiableCredentialDeferredIssuanceService> deferredIssuanceService;

    public OidcVerifiableCredentialDeferredIssuanceEndpoint(
        final CasConfigurationProperties casProperties,
        final ObjectProvider<OidcVerifiableCredentialDeferredIssuanceService> deferredIssuanceService) {
        super(casProperties);
        this.deferredIssuanceService = deferredIssuanceService;
    }

    /**
     * Deferred transactions that are not expired, optionally for one user only.
     *
     * @param principal the principal identifier, or null for all users
     * @return the transactions
     */
    @ReadOperation
    @Operation(summary = "Get the deferred verifiable credential transactions",
        parameters = @Parameter(name = "principal", in = ParameterIn.QUERY, description = "The principal identifier"))
    public List<DeferredTransaction> getTransactions(final @Nullable String principal) {
        return deferredIssuanceService.getObject().getTransactions(StringUtils.trimToNull(principal));
    }

    /**
     * Approve or deny a deferred transaction.
     *
     * @param transactionId the transaction id
     * @param status        {@code APPROVED} or {@code DENIED}
     * @return the updated transaction, {@code 404} when it is unknown or expired, or {@code 400} when the status is invalid
     * @throws Throwable the throwable
     */
    @WriteOperation
    @Operation(summary = "Approve or deny a deferred verifiable credential transaction",
        parameters = {
            @Parameter(name = "transactionId", required = true, in = ParameterIn.PATH, description = "The transaction id"),
            @Parameter(name = "status", required = true, in = ParameterIn.QUERY, description = "APPROVED or DENIED")
        })
    public WebEndpointResponse<DeferredTransaction> decide(@Selector final String transactionId, final String status) throws Throwable {
        val decision = EnumUtils.getEnum(TransactionStatus.class, StringUtils.upperCase(status, Locale.ENGLISH));
        if (decision == null || decision == TransactionStatus.PENDING) {
            return new WebEndpointResponse<>(WebEndpointResponse.STATUS_BAD_REQUEST);
        }
        return deferredIssuanceService.getObject().decide(transactionId, decision)
            .map(WebEndpointResponse::new)
            .orElseGet(() -> new WebEndpointResponse<>(WebEndpointResponse.STATUS_NOT_FOUND));
    }
}
