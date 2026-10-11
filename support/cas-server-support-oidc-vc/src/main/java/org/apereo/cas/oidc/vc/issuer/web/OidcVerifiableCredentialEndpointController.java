package org.apereo.cas.oidc.vc.issuer.web;

import module java.base;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.oidc.OidcConfigurationContext;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.vc.authz.OidcVerifiableCredentialAuthorizationDetails;
import org.apereo.cas.oidc.vc.issuer.OidcVerifiableCredentialDeferredRequest;
import org.apereo.cas.oidc.vc.issuer.OidcVerifiableCredentialIssuerService;
import org.apereo.cas.oidc.vc.issuer.OidcVerifiableCredentialRequest;
import org.apereo.cas.oidc.vc.issuer.OidcVerifiableCredentialResponse;
import org.apereo.cas.oidc.vc.issuer.OidcVerifiableCredentialValidationContext;
import org.apereo.cas.oidc.vc.issuer.deferred.OidcVerifiableCredentialDeferredIssuanceService;
import org.apereo.cas.oidc.vc.issuer.deferred.OidcVerifiableCredentialDeferredIssuanceService.DeferredTransaction;
import org.apereo.cas.oidc.vc.issuer.encryption.OidcVerifiableCredentialEncryptionException;
import org.apereo.cas.oidc.vc.issuer.encryption.OidcVerifiableCredentialEncryptionService;
import org.apereo.cas.oidc.vc.issuer.notification.OidcVerifiableCredentialNotificationService;
import org.apereo.cas.oidc.vc.issuer.proof.OidcVerifiableCredentialProofException;
import org.apereo.cas.oidc.vc.issuer.proof.OidcVerifiableCredentialProofValidator.VerifiableCredentialProofResult;
import org.apereo.cas.oidc.vc.services.OidcVerifiableCredentialPolicyUtils;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.web.endpoints.BaseOAuth20Controller;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import org.apereo.cas.util.Couplet;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import org.pac4j.jee.context.JEEContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * This is {@link OidcVerifiableCredentialEndpointController}.
 * <p>
 * OpenID4VCI 1.0 has a single credential endpoint. A request carrying several proofs is a batch
 * request, and the response carries one credential per proof; the separate batch credential
 * endpoint of earlier drafts no longer exists.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@Tag(name = "OpenID Connect")
@Slf4j
public class OidcVerifiableCredentialEndpointController extends BaseOAuth20Controller<OidcConfigurationContext> {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false)
        .minimal(true)
        .strictDuplicateDetection(true)
        .build()
        .toObjectMapper();

    private static final Pattern NOTIFICATION_EVENT_DESCRIPTION = Pattern.compile("[\\x20-\\x21\\x23-\\x5B\\x5D-\\x7E]*");

    protected final OidcVerifiableCredentialIssuerService credentialIssuerService;

    protected final OidcVerifiableCredentialNotificationService notificationService;

    protected final OidcVerifiableCredentialEncryptionService encryptionService;

    protected final OidcVerifiableCredentialDeferredIssuanceService deferredIssuanceService;

    public OidcVerifiableCredentialEndpointController(
        final OidcConfigurationContext configurationContext,
        final OidcVerifiableCredentialIssuerService credentialIssuerService,
        final OidcVerifiableCredentialNotificationService notificationService,
        final OidcVerifiableCredentialEncryptionService encryptionService,
        final OidcVerifiableCredentialDeferredIssuanceService deferredIssuanceService) {
        super(configurationContext);
        this.credentialIssuerService = credentialIssuerService;
        this.notificationService = notificationService;
        this.encryptionService = encryptionService;
        this.deferredIssuanceService = deferredIssuanceService;
    }

    /**
     * Handle a credential request, sent as JSON or, encrypted, as {@code application/jwt} (OpenID4VCI 1.0 section 10).
     *
     * @param body         the credential request body
     * @param httpRequest  the http request
     * @param httpResponse the http response
     * @return the response entity
     * @throws Throwable the throwable
     */
    @PostMapping(value = {
        '/' + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.VC_CREDENTIAL_URL,
        "/**/" + OidcConstants.VC_CREDENTIAL_URL
    }, consumes = {MediaType.APPLICATION_JSON_VALUE, OidcConstants.CONTENT_TYPE_JWT})
    @Operation(summary = "Handle OIDC credential request",
        description = "Handles requests for OIDC credential issuance")
    public ResponseEntity handle(
        @RequestBody final String body,
        final HttpServletRequest httpRequest,
        final HttpServletResponse httpResponse)
            throws Throwable {

        val verified = verifyRequest(httpRequest, httpResponse, OidcConstants.VC_CREDENTIAL_URL);
        if (verified.getRight() != null) {
            return verified.getRight();
        }
        val decodedToken = Objects.requireNonNull(verified.getLeft());

        val credentialRequest = readCredentialRequest(body, httpRequest);
        if (credentialRequest.getRight() != null) {
            return credentialRequest.getRight();
        }
        val request = Objects.requireNonNull(credentialRequest.getLeft());

        val identifierError = validateCredentialIdentifiers(request, decodedToken);
        if (identifierError != null) {
            return identifierError;
        }

        val issuanceContext = new OidcVerifiableCredentialValidationContext(decodedToken, request, httpRequest);
        val batchError = validateBatchSize(issuanceContext);
        if (batchError != null) {
            return batchError;
        }

        val issuanceError = validateCredentialIssuance(decodedToken, issuanceContext);
        if (issuanceError != null) {
            return issuanceError;
        }

        val proofs = credentialIssuerService.validateProofs(issuanceContext, new HashSet<>());
        val configurationId = issuanceContext.resolveConfigurationId();
        if (deferredIssuanceService.isDeferred(configurationId)) {
            val transaction = deferredIssuanceService.defer(decodedToken, configurationId, proofs);
            if (transaction.isPresent()) {
                return buildCredentialResponse(request.getCredentialResponseEncryption(), HttpStatus.ACCEPTED,
                    toDeferredResponse(transaction.get()));
            }
        }
        return issueCredentials(issuanceContext, proofs, request.getCredentialResponseEncryption());
    }

    /**
     * Hand out the credentials of a deferred transaction, per OpenID4VCI 1.0 section 9. The access token is checked as it is
     * at the credential endpoint and must belong to the client and the user that started the transaction. A pending
     * transaction is answered with {@code 202} and the same {@code transaction_id}; an approved one with the credentials, built
     * now from the user's attributes, after which its {@code transaction_id} can no longer be used; a denied one with
     * {@code credential_request_denied}. A transaction that is unknown, expired, already used or another client's or user's is
     * {@code invalid_transaction_id}. The request may be encrypted, and the response is encrypted as this request asks,
     * whatever the credential request asked for.
     *
     * @param body         the deferred credential request body
     * @param httpRequest  the http request
     * @param httpResponse the http response
     * @return the response entity
     * @throws Throwable the throwable
     */
    @PostMapping(value = {
        '/' + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.VC_DEFERRED_CREDENTIAL_URL,
        "/**/" + OidcConstants.VC_DEFERRED_CREDENTIAL_URL
    }, consumes = {MediaType.APPLICATION_JSON_VALUE, OidcConstants.CONTENT_TYPE_JWT})
    @Operation(summary = "Handle OIDC deferred credential request",
        description = "Hands out the credentials of a deferred credential issuance")
    public ResponseEntity handleDeferred(
        @RequestBody final String body,
        final HttpServletRequest httpRequest,
        final HttpServletResponse httpResponse)
            throws Throwable {
        val verified = verifyRequest(httpRequest, httpResponse, OidcConstants.VC_DEFERRED_CREDENTIAL_URL);
        if (verified.getRight() != null) {
            return verified.getRight();
        }
        val decodedToken = Objects.requireNonNull(verified.getLeft());
        val deferredRequest = readRequest(body, httpRequest, OidcVerifiableCredentialDeferredRequest.class,
            OidcVerifiableCredentialDeferredRequest::getCredentialResponseEncryption);
        if (deferredRequest.getRight() != null) {
            return deferredRequest.getRight();
        }
        val request = Objects.requireNonNull(deferredRequest.getLeft());
        val transactionId = request.getTransactionId();
        val found = StringUtils.isBlank(transactionId)
            ? Optional.<DeferredTransaction>empty()
            : deferredIssuanceService.find(decodedToken, transactionId);
        if (found.isEmpty()) {
            LOGGER.warn("Deferred transaction [{}] is unknown, expired or was not started by this client and user", transactionId);
            return badRequest(OidcConstants.VC_ERROR_INVALID_TRANSACTION_ID, "Transaction id is unknown, expired or already used");
        }
        val transaction = found.get();
        return switch (transaction.status()) {
            case PENDING -> buildCredentialResponse(request.getCredentialResponseEncryption(), HttpStatus.ACCEPTED,
                toDeferredResponse(transaction));
            case DENIED -> {
                deferredIssuanceService.complete(transaction.transactionId());
                yield badRequest(OidcConstants.VC_ERROR_CREDENTIAL_REQUEST_DENIED, "The credentials of this transaction will not be issued");
            }
            case APPROVED -> deliverDeferredCredentials(decodedToken, transaction, request, httpRequest);
        };
    }

    /**
     * Hand out the credentials of an approved transaction. Whether this token may still obtain them is checked again, the
     * credentials are built, and only then is the transaction removed: a failure to build them, such as an attribute that is
     * still missing, leaves the transaction for a later attempt, and only the request that removes it gets the credentials,
     * so two concurrent requests cannot both obtain them.
     *
     * @param accessToken the access token presented at the deferred credential endpoint
     * @param transaction the approved transaction
     * @param request     the deferred credential request
     * @param httpRequest the http request
     * @return the response entity
     * @throws Throwable the throwable
     */
    protected ResponseEntity deliverDeferredCredentials(final OAuth20AccessToken accessToken, final DeferredTransaction transaction,
                                                        final OidcVerifiableCredentialDeferredRequest request,
                                                        final HttpServletRequest httpRequest)
            throws Throwable {
        val credentialRequest = new OidcVerifiableCredentialRequest();
        credentialRequest.setCredentialConfigurationId(transaction.credentialConfigurationId());
        val issuanceContext = new OidcVerifiableCredentialValidationContext(accessToken, credentialRequest, httpRequest);
        val issuanceError = validateCredentialIssuance(accessToken, issuanceContext);
        if (issuanceError != null) {
            return issuanceError;
        }
        val credentials = encodeCredentials(issuanceContext, transaction.proofs());
        if (!deferredIssuanceService.complete(transaction.transactionId())) {
            return badRequest(OidcConstants.VC_ERROR_INVALID_TRANSACTION_ID, "Transaction id is unknown, expired or already used");
        }
        return respondWithCredentials(issuanceContext, credentials, request.getCredentialResponseEncryption());
    }

    protected ResponseEntity issueCredentials(final OidcVerifiableCredentialValidationContext issuanceContext,
                                              final List<VerifiableCredentialProofResult> proofs,
                                              final OidcVerifiableCredentialRequest.@Nullable CredentialResponseEncryption responseEncryption)
            throws Throwable {
        return respondWithCredentials(issuanceContext, encodeCredentials(issuanceContext, proofs), responseEncryption);
    }

    protected List<OidcVerifiableCredentialResponse.IssuedCredential> encodeCredentials(
        final OidcVerifiableCredentialValidationContext issuanceContext,
        final List<VerifiableCredentialProofResult> proofs)
            throws Throwable {
        return credentialIssuerService.encode(issuanceContext, proofs)
            .stream()
            .<OidcVerifiableCredentialResponse.IssuedCredential>map(issued -> OidcVerifiableCredentialResponse.IssuedCredential
                .builder()
                .credential(issued.credential())
                .build())
            .toList();
    }

    protected ResponseEntity respondWithCredentials(final OidcVerifiableCredentialValidationContext issuanceContext,
                                                    final List<OidcVerifiableCredentialResponse.IssuedCredential> credentials,
                                                    final OidcVerifiableCredentialRequest.@Nullable CredentialResponseEncryption responseEncryption)
            throws Throwable {
        val response = OidcVerifiableCredentialResponse.builder()
            .credentials(credentials)
            .notificationId(notificationService.register(issuanceContext.accessToken(), issuanceContext.resolveConfigurationId()))
            .build();
        return buildCredentialResponse(responseEncryption, HttpStatus.OK, response);
    }

    protected OidcVerifiableCredentialResponse toDeferredResponse(final DeferredTransaction transaction) {
        val interval = Beans.newDuration(getConfigurationContext().getCasProperties().getAuthn().getOidc()
            .getVc().getIssuer().getDeferredIssuance().getInterval());
        return OidcVerifiableCredentialResponse.builder()
            .transactionId(transaction.transactionId())
            .interval(Math.max(1, interval.toSeconds()))
            .build();
    }

    /**
     * Read the credential request, decrypting it when it arrives as {@code application/jwt}, and check its encryption
     * as OpenID4VCI 1.0 section 8.2 asks: a request must be encrypted when the issuer requires it, and whenever it asks
     * for an encrypted response, so that the response key cannot be swapped on the way. The response encryption
     * parameters are checked here, before anything is issued; a missing {@code credential_response_encryption} when the
     * issuer requires encrypted responses is {@code invalid_encryption_parameters}.
     *
     * @param body        the request body
     * @param httpRequest the http request
     * @return the credential request, or an error response
     */
    protected Couplet<@Nullable OidcVerifiableCredentialRequest, @Nullable ResponseEntity> readCredentialRequest(
        final String body, final HttpServletRequest httpRequest) {
        return readRequest(body, httpRequest, OidcVerifiableCredentialRequest.class,
            OidcVerifiableCredentialRequest::getCredentialResponseEncryption);
    }

    protected <T> Couplet<@Nullable T, @Nullable ResponseEntity> readRequest(
        final String body, final HttpServletRequest httpRequest, final Class<T> requestType,
        final Function<T, OidcVerifiableCredentialRequest.@Nullable CredentialResponseEncryption> responseEncryptionOf) {
        val encrypted = isEncryptedRequest(httpRequest);
        if (encrypted && !encryptionService.isEnabled()) {
            return Couplet.right(badRequest(OidcConstants.VC_ERROR_INVALID_CREDENTIAL_REQUEST, "Encrypted credential requests are not supported"));
        }
        if (!encrypted && encryptionService.isRequestEncryptionRequired()) {
            return Couplet.right(badRequest(OidcConstants.VC_ERROR_INVALID_CREDENTIAL_REQUEST, "Credential requests must be encrypted"));
        }
        try {
            val request = MAPPER.readValue(encrypted ? encryptionService.decryptRequest(body) : body, requestType);
            if (request == null) {
                return Couplet.right(badRequest(OidcConstants.VC_ERROR_INVALID_CREDENTIAL_REQUEST, "Credential request is empty"));
            }
            val responseEncryption = responseEncryptionOf.apply(request);
            if (responseEncryption == null && encryptionService.isResponseEncryptionRequired()) {
                throw OidcVerifiableCredentialEncryptionException.invalidParameters("Credential responses must be encrypted");
            }
            if (responseEncryption != null) {
                if (!encryptionService.isEnabled()) {
                    throw OidcVerifiableCredentialEncryptionException.invalidParameters("Credential response encryption is not supported");
                }
                if (!encrypted) {
                    throw OidcVerifiableCredentialEncryptionException.invalidParameters(
                        "A credential request asking for an encrypted response must itself be encrypted");
                }
                encryptionService.validateResponseEncryption(responseEncryption);
            }
            return Couplet.left(request);
        } catch (final OidcVerifiableCredentialEncryptionException e) {
            LOGGER.warn(e.getMessage());
            return Couplet.right(badRequest(e.getError(), e.getMessage()));
        }
    }

    /**
     * The credential or deferred credential response, encrypted as {@code application/jwt} when the wallet asked for it,
     * otherwise as JSON. A deferred answer is encrypted too, whatever its content. Error responses are never encrypted.
     *
     * @param responseEncryption the response encryption parameters of the request, if any
     * @param status             the status, {@code 200} with credentials or {@code 202} with a deferred transaction
     * @param response           the credential response
     * @return the response entity
     */
    protected ResponseEntity buildCredentialResponse(final OidcVerifiableCredentialRequest.@Nullable CredentialResponseEncryption responseEncryption,
                                                     final HttpStatus status,
                                                     final OidcVerifiableCredentialResponse response) {
        if (responseEncryption == null) {
            return ResponseEntity.status(status).body(response);
        }
        val encrypted = encryptionService.encryptResponse(MAPPER.writeValueAsString(response), responseEncryption);
        return ResponseEntity.status(status)
            .contentType(MediaType.parseMediaType(OidcConstants.CONTENT_TYPE_JWT))
            .body(encrypted);
    }

    protected static boolean isEncryptedRequest(final HttpServletRequest request) {
        val contentType = request.getContentType();
        return StringUtils.isNotBlank(contentType)
            && MediaType.parseMediaType(contentType).isCompatibleWith(MediaType.parseMediaType(OidcConstants.CONTENT_TYPE_JWT));
    }

    /**
     * Receive a notification from the wallet about the credentials of a credential response, per OpenID4VCI 1.0
     * section 11. The access token is checked as it is at the credential endpoint. A malformed request, an unknown
     * event or an event description with characters outside the permitted ASCII set is
     * {@code invalid_notification_request}; a notification id that is unknown, expired or was not issued to the
     * client and user of the access token is {@code invalid_notification_id}. Unknown parameters are ignored, and the
     * same notification may be sent again.
     *
     * @param body         the notification request body
     * @param httpRequest  the http request
     * @param httpResponse the http response
     * @return the response entity
     */
    @PostMapping(value = {
        '/' + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.VC_NOTIFICATION_URL,
        "/**/" + OidcConstants.VC_NOTIFICATION_URL
    }, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Handle OIDC credential notification",
        description = "Handles notifications from wallets about issued credentials")
    public ResponseEntity handleNotification(
        @RequestBody final String body,
        final HttpServletRequest httpRequest,
        final HttpServletResponse httpResponse) {
        val verified = verifyRequest(httpRequest, httpResponse, OidcConstants.VC_NOTIFICATION_URL);
        if (verified.getRight() != null) {
            return verified.getRight();
        }
        val notificationRequest = readNotificationRequest(body);
        if (notificationRequest == null) {
            return badRequest(OidcConstants.VC_ERROR_INVALID_NOTIFICATION_REQUEST, "Notification request is invalid");
        }
        try {
            notificationService.notify(Objects.requireNonNull(verified.getLeft()), notificationRequest);
            return ResponseEntity.noContent().build();
        } catch (final OidcVerifiableCredentialNotificationService.InvalidNotificationException e) {
            LOGGER.warn(e.getMessage());
            return badRequest(OidcConstants.VC_ERROR_INVALID_NOTIFICATION_ID, e.getMessage());
        }
    }

    protected static OidcVerifiableCredentialNotificationService.@Nullable NotificationRequest readNotificationRequest(final String body) {
        try {
            val parameters = MAPPER.readValue(body, Map.class);
            if (parameters.get("notification_id") instanceof final String notificationId && StringUtils.isNotBlank(notificationId)
                && parameters.get("event") instanceof final String event
                && OidcVerifiableCredentialNotificationService.EVENTS.contains(event)) {
                val description = parameters.get("event_description");
                if (description == null || (description instanceof final String text && NOTIFICATION_EVENT_DESCRIPTION.matcher(text).matches())) {
                    return new OidcVerifiableCredentialNotificationService.NotificationRequest(notificationId, event, (String) description);
                }
            }
        } catch (final Exception e) {
            LOGGER.debug("Unable to read notification request: [{}]", e.getMessage());
        }
        return null;
    }

    /**
     * The two request parameters are mutually exclusive, and which one is used depends on the token response, per
     * OpenID4VCI 1.0 section 8.2: once it returned {@code credential_identifiers}, the request must name one with
     * {@code credential_identifier} and must not use {@code credential_configuration_id}; otherwise it must name a
     * {@code credential_configuration_id} and must not use a {@code credential_identifier}.
     *
     * @param request     the credential request
     * @param accessToken the access token
     * @return an error response, or null when the request is acceptable
     */
    protected @Nullable ResponseEntity validateCredentialIdentifiers(final OidcVerifiableCredentialRequest request,
                                                                     final OAuth20AccessToken accessToken) {
        val hasIdentifier = StringUtils.isNotBlank(request.getCredentialIdentifier());
        val hasConfigurationId = StringUtils.isNotBlank(request.getCredentialConfigurationId());
        if (hasIdentifier && hasConfigurationId) {
            return badRequest(OidcConstants.VC_ERROR_INVALID_CREDENTIAL_REQUEST,
                "Only one of credential_identifier or credential_configuration_id may be specified");
        }
        val identifiersReturned = isCredentialIdentifiersReturned(accessToken);
        if (hasIdentifier && !identifiersReturned) {
            return badRequest(OidcConstants.VC_ERROR_INVALID_CREDENTIAL_REQUEST,
                "A credential identifier cannot be used when the token response returned no credential identifiers");
        }
        if (!hasIdentifier && identifiersReturned) {
            return badRequest(OidcConstants.VC_ERROR_INVALID_CREDENTIAL_REQUEST,
                "The token response returned credential identifiers, so the request must name one with credential_identifier");
        }
        if (!hasIdentifier && !hasConfigurationId) {
            return badRequest(OidcConstants.VC_ERROR_INVALID_CREDENTIAL_REQUEST,
                "The credential request must name a credential_configuration_id");
        }
        return null;
    }

    /**
     * Whether the token response returned {@code credential_identifiers}: it echoes the authorization details of the access
     * token, each carrying its identifiers, except for the pre-authorized code grant
     * (see {@code OidcVerifiableCredentialAccessTokenResponseCustomizer}).
     *
     * @param accessToken the access token
     * @return true when credential identifiers were returned
     */
    protected boolean isCredentialIdentifiersReturned(final OAuth20AccessToken accessToken) {
        return accessToken.getGrantType() != OAuth20GrantTypes.PRE_AUTHORIZED_CODE && accessToken.hasAuthorizationDetails();
    }

    protected @Nullable ResponseEntity validateBatchSize(final OidcVerifiableCredentialValidationContext context) {
        val maximumBatchSize = Math.max(1, getConfigurationContext().getCasProperties()
            .getAuthn().getOidc().getVc().getIssuer().getBatchSize());
        if (context.resolveProofs().size() > maximumBatchSize) {
            return badRequest(OidcConstants.VC_ERROR_INVALID_CREDENTIAL_REQUEST, "Credential batch size is invalid");
        }
        return null;
    }

    /**
     * Decide whether this token may obtain this credential, and say why not in the vocabulary
     * OpenID4VCI 1.0 section 8.3.1.2 defines for the credential endpoint: a {@code credential_identifier} the token
     * response did not return is {@code unknown_credential_identifier}, a credential configuration the issuer does not
     * publish is {@code unknown_credential_configuration}, while one it publishes but will not hand to this caller is
     * {@code credential_request_denied}. A wallet can act on the difference; a single opaque error
     * tells it only to stop.
     *
     * @param accessToken      the access token
     * @param issuanceContext  the issuance context
     * @return an error response, or null when issuance may proceed
     */
    protected @Nullable ResponseEntity validateCredentialIssuance(final OAuth20AccessToken accessToken,
                                                                   final OidcVerifiableCredentialValidationContext issuanceContext) {
        val credentialIdentifier = issuanceContext.credentialRequest().getCredentialIdentifier();
        if (StringUtils.isNotBlank(credentialIdentifier) && !resolveCredentialIdentifiers(accessToken).contains(credentialIdentifier)) {
            LOGGER.warn("Credential identifier [{}] was not issued with the access token", credentialIdentifier);
            return badRequest(OidcConstants.VC_ERROR_UNKNOWN_CREDENTIAL_IDENTIFIER,
                "Credential identifier %s is unknown".formatted(credentialIdentifier));
        }
        val requestedConfigurationId = issuanceContext.resolveConfigurationId();
        val publishedConfigurationIds = getConfigurationContext().getCasProperties()
            .getAuthn().getOidc().getVc().getIssuer().getCredentialConfigurations().keySet();
        if (!publishedConfigurationIds.contains(requestedConfigurationId)) {
            LOGGER.warn("Credential configuration [{}] is not published by this issuer", requestedConfigurationId);
            return badRequest(OidcConstants.VC_ERROR_UNKNOWN_CREDENTIAL_CONFIGURATION,
                "Credential configuration %s is unknown".formatted(requestedConfigurationId));
        }

        val authorizedConfigurationIds = resolveAuthorizedCredentialConfigurationIds(accessToken);
        if (!authorizedConfigurationIds.contains(requestedConfigurationId)) {
            LOGGER.warn("Access token authorizes [{}] and does not cover [{}]",
                authorizedConfigurationIds, requestedConfigurationId);
            return badRequest(OidcConstants.VC_ERROR_CREDENTIAL_REQUEST_DENIED,
                "Access token does not authorize credential configuration %s".formatted(requestedConfigurationId));
        }

        /*
         * What the token authorizes was decided when the token was minted. The service policy is read
         * again here rather than trusted from then, so that tightening a relying party's verifiable
         * credentials policy takes effect against tokens that are already outstanding.
         */
        val registeredService = OAuth20Utils.getRegisteredOAuthServiceByClientId(
            getConfigurationContext().getServicesManager(), accessToken.getClientId(), OidcRegisteredService.class);
        val allowedConfigurationIds = OidcVerifiableCredentialPolicyUtils.resolveAllowedCredentialConfigurationIds(
            registeredService, publishedConfigurationIds);
        if (!allowedConfigurationIds.contains(requestedConfigurationId)) {
            LOGGER.warn("Client [{}] is not allowed to obtain credential configuration [{}]; allowed: [{}]",
                accessToken.getClientId(), requestedConfigurationId, allowedConfigurationIds);
            return badRequest(OidcConstants.VC_ERROR_CREDENTIAL_REQUEST_DENIED,
                "Credential configuration %s is not permitted for this client".formatted(requestedConfigurationId));
        }
        return null;
    }

    /**
     * Credential configurations the access token is allowed to request. Tokens issued through the
     * pre-authorized code flow, or through the authorization code flow with a credential configuration's
     * scope, carry the identifiers directly; authorization details attached to the token add theirs, so the two
     * are combined. Which request parameter may name them is decided by {@link #validateCredentialIdentifiers}.
     *
     * @param accessToken the access token
     * @return the authorized credential configuration ids, never null
     */
    protected List<String> resolveAuthorizedCredentialConfigurationIds(final OAuth20AccessToken accessToken) {
        val configurationIds = new ArrayList<String>();
        val grantedConfigurationIds = accessToken.getCredentialConfigurationIds();
        if (grantedConfigurationIds != null) {
            configurationIds.addAll(grantedConfigurationIds);
        }
        configurationIds.addAll(resolveAuthorizationDetailsConfigurationIds(accessToken));
        return configurationIds.stream().filter(StringUtils::isNotBlank).distinct().toList();
    }

    /**
     * Credential identifiers the token response returned, one per authorization detail, or none when it returned
     * none. A CAS credential identifier is the credential configuration id of its authorization detail.
     *
     * @param accessToken the access token
     * @return the credential identifiers, never null
     */
    protected List<String> resolveCredentialIdentifiers(final OAuth20AccessToken accessToken) {
        return isCredentialIdentifiersReturned(accessToken) ? resolveAuthorizationDetailsConfigurationIds(accessToken) : List.of();
    }

    private static List<String> resolveAuthorizationDetailsConfigurationIds(final OAuth20AccessToken accessToken) {
        val authorizationDetails = accessToken.getAuthorizationDetails();
        if (authorizationDetails == null) {
            return List.of();
        }
        return authorizationDetails
            .stream()
            .map(OidcVerifiableCredentialEndpointController::toCredentialConfigurationId)
            .filter(StringUtils::isNotBlank)
            .distinct()
            .toList();
    }

    private static String toCredentialConfigurationId(final Serializable authorizationDetails) {
        return switch (authorizationDetails) {
            case final OidcVerifiableCredentialAuthorizationDetails details -> details.getCredentialConfigurationId();
            case final Map<?, ?> details -> Objects.toString(details.get("credential_configuration_id"), StringUtils.EMPTY);
            default -> StringUtils.EMPTY;
        };
    }

    protected Couplet<@Nullable OAuth20AccessToken, @Nullable ResponseEntity> verifyRequest(
        final HttpServletRequest httpRequest,
        final HttpServletResponse httpResponse,
        final String endpoint) {
        val webContext = new JEEContext(httpRequest, httpResponse);
        if (!getConfigurationContext().getIssuerService().validateIssuer(webContext, List.of(endpoint))) {
            LOGGER.warn("CAS cannot accept the request given the issuer is invalid.");
            return Couplet.right(badRequest(OidcConstants.VC_ERROR_INVALID_CREDENTIAL_REQUEST, "Invalid issuer"));
        }

        val presentedAccessToken = getAccessTokenFromRequest(httpRequest);
        if (StringUtils.isBlank(presentedAccessToken.getKey())) {
            LOGGER.warn("The credential request carries no access token in its authorization header.");
            return Couplet.right(unauthorized(httpRequest, null, null));
        }
        val decodedToken = FunctionUtils.doAndHandle(() -> getConfigurationContext().getTicketRegistry()
            .getTicket(presentedAccessToken.getValue(), OAuth20AccessToken.class));
        if (decodedToken == null || decodedToken.isExpired()) {
            LOGGER.warn("The access token presented to the credential endpoint is unknown or expired.");
            return Couplet.right(unauthorized(httpRequest, OAuth20Constants.INVALID_TOKEN,
                "The access token is invalid, revoked or expired"));
        }
        val proofError = verifyProofOfPossession(webContext, presentedAccessToken.getKey(), decodedToken);
        if (proofError != null) {
            return Couplet.right(proofError);
        }
        if (!validateAccessToken(decodedToken)) {
            LOGGER.warn("The access token has an invalid grant type or carries no authorization details.");
            return Couplet.right(badRequest(OidcConstants.VC_ERROR_CREDENTIAL_REQUEST_DENIED,
                "Access token does not authorize credential issuance"));
        }
        return Couplet.left(decodedToken);
    }

    protected static ResponseEntity badRequest(final String error, final String description) {
        return ResponseEntity.badRequest().body(OAuth20Utils.getErrorResponseBody(error, description));
    }

    protected boolean validateAccessToken(@Nullable final OAuth20AccessToken accessToken) {
        return accessToken != null && !accessToken.isExpired()
            && (accessToken.getGrantType() == OAuth20GrantTypes.PRE_AUTHORIZED_CODE
            || accessToken.hasAuthorizationDetails()
            || !resolveAuthorizedCredentialConfigurationIds(accessToken).isEmpty());
    }

    /**
     * Handle errors.
     *
     * @param ex the ex
     * @return the response entity
     */
    @ExceptionHandler(Exception.class)
    @SuppressWarnings("UnusedMethod")
    private static ResponseEntity handleErrors(final Exception ex) {
        LoggingUtils.error(LOGGER, ex);
        /*
         * A failure raised while validating a proof knows which of the two OpenID4VCI proof errors it
         * is, and the distinction matters to the wallet: invalid_nonce is recoverable by fetching a
         * fresh nonce, invalid_proof is not.
         */
        val proofException = findProofException(ex);
        if (proofException != null) {
            return badRequest(proofException.getError(), proofException.getMessage());
        }
        if (ex instanceof final OidcVerifiableCredentialEncryptionException encryptionException) {
            return badRequest(encryptionException.getError(), encryptionException.getMessage());
        }
        if (ex instanceof final ResponseStatusException rse) {
            return ResponseEntity
                .status(rse.getStatusCode())
                .body(OAuth20Utils.getErrorResponseBody(OidcConstants.VC_ERROR_INVALID_CREDENTIAL_REQUEST, rse.getReason()));
        }
        return badRequest(OidcConstants.VC_ERROR_INVALID_CREDENTIAL_REQUEST, ex.getMessage());
    }

    private static @Nullable OidcVerifiableCredentialProofException findProofException(final Throwable throwable) {
        var cause = throwable;
        while (cause != null) {
            if (cause instanceof final OidcVerifiableCredentialProofException proofException) {
                return proofException;
            }
            cause = Objects.equals(cause.getCause(), cause) ? null : cause.getCause();
        }
        return null;
    }
}
