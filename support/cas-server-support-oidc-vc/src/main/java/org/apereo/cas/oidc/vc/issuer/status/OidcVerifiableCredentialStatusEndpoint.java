package org.apereo.cas.oidc.vc.issuer.status;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.BaseCasActuatorEndpoint;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import lombok.val;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.endpoint.Access;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;
import org.springframework.boot.actuate.endpoint.web.WebEndpointResponse;

/**
 * Lists the status list entries of the verifiable credentials issued to a user and changes their status, so a credential
 * can be revoked, suspended or reinstated.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@Endpoint(id = "oidcVcStatus", defaultAccess = Access.NONE)
public class OidcVerifiableCredentialStatusEndpoint extends BaseCasActuatorEndpoint {
    private final ObjectProvider<OidcVerifiableCredentialStatusListService> statusListService;

    public OidcVerifiableCredentialStatusEndpoint(final CasConfigurationProperties casProperties,
                                                  final ObjectProvider<OidcVerifiableCredentialStatusListService> statusListService) {
        super(casProperties);
        this.statusListService = statusListService;
    }

    /**
     * Status list entries of the credentials issued to a user.
     *
     * @param principal the principal identifier
     * @return the entries
     */
    @ReadOperation
    @Operation(summary = "Get the status list entries of the verifiable credentials issued to a user",
        parameters = @Parameter(name = "principal", required = true, in = ParameterIn.PATH, description = "The principal identifier"))
    public List<OidcVerifiableCredentialStatusListService.StatusEntry> getEntries(@Selector final String principal) {
        return statusListService.getObject().getEntries(principal);
    }

    /**
     * Change the status of a credential's status list entry.
     *
     * @param statusListId the status list identifier
     * @param index        the index
     * @param status       the status, one of {@code VALID}, {@code INVALID} or {@code SUSPENDED}
     * @return the updated entry, {@code 404} when the entry is unknown, or {@code 400} when the status is invalid
     * @throws Throwable the throwable
     */
    @WriteOperation
    @Operation(summary = "Change the status of a verifiable credential",
        parameters = {
            @Parameter(name = "statusListId", required = true, in = ParameterIn.PATH, description = "The status list identifier"),
            @Parameter(name = "index", required = true, in = ParameterIn.PATH, description = "The index in the status list"),
            @Parameter(name = "status", required = true, in = ParameterIn.QUERY, description = "VALID, INVALID or SUSPENDED")
        })
    public WebEndpointResponse<OidcVerifiableCredentialStatusListService.StatusEntry> updateStatus(
        @Selector final String statusListId, @Selector final long index, final String status)
            throws Throwable {
        val statusType = EnumUtils.getEnum(OidcVerifiableCredentialStatusListService.StatusType.class,
            StringUtils.upperCase(status, Locale.ENGLISH));
        if (statusType == null) {
            return new WebEndpointResponse<>(WebEndpointResponse.STATUS_BAD_REQUEST);
        }
        return statusListService.getObject().updateStatus(statusListId, index, statusType)
            .map(WebEndpointResponse::new)
            .orElseGet(() -> new WebEndpointResponse<>(WebEndpointResponse.STATUS_NOT_FOUND));
    }
}
