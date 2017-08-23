package org.apereo.cas;

/**
 * Class that exposes relevant constants and parameters to
 * the CAS protocol. These include attribute names, pre-defined
 * values and expected request parameter names as is specified
 * by the protocol.
 *
 * @author Misagh Moayyed
 * @since 4.1.0
 */
public interface CasProtocolConstants {

    /* CAS Protocol Validation Fields. **/

    /**
     * Constant representing the PGT in the cas validation model.
     */
    String VALIDATION_CAS_MODEL_PROXY_GRANTING_TICKET = "proxyGrantingTicket";

    /**
     * Constant representing the PGTIOU in the cas validation model.
     */
    String VALIDATION_CAS_MODEL_PROXY_GRANTING_TICKET_IOU = "pgtIou";

    /**
     * Constant representing the remember-me long term token in the validation payload.
     */
    String VALIDATION_REMEMBER_ME_ATTRIBUTE_NAME = "longTermAuthenticationRequestTokenUsed";

    /**
     * Represents the collection of attributes in the view.
     */
    String VALIDATION_CAS_MODEL_ATTRIBUTE_NAME_ATTRIBUTES = "attributes";

    /**
     * Represents the pre-rendered collection of attributes in the view.
     */
    String VALIDATION_CAS_MODEL_ATTRIBUTE_NAME_FORMATTED_ATTRIBUTES = "formattedAttributes";

    /**
     * Represents the authentication date object in the view.
     */
    String VALIDATION_CAS_MODEL_ATTRIBUTE_NAME_AUTHENTICATION_DATE = "authenticationDate";

    /**
     * Represents the flag to note whether assertion is backed by new login.
     */
    String VALIDATION_CAS_MODEL_ATTRIBUTE_NAME_FROM_NEW_LOGIN = "isFromNewLogin";

    /* CAS Protocol Parameters. **/

    /**
     * Constant representing the proxy callback url parameter in the request.
     */
    String PARAMETER_PROXY_CALLBACK_URL = "pgtUrl";

    /**
     * Constant representing the renew parameter in the request.
     */
    String PARAMETER_RENEW = "renew";

    /**
     * Constant representing the gateway parameter in the request.
     */
    String PARAMETER_GATEWAY = "gateway";

    /**
     * Constant representing the service parameter in the request.
     */
    String PARAMETER_SERVICE = "service";

    /**
     * Constant representing the ticket parameter in the request.
     */
    String PARAMETER_TICKET = "ticket";

    /**
     * Constant representing the format parameter in the request.
     */
    String PARAMETER_FORMAT = "format";

    /**
     * Constant representing the targetService parameter in the request.
     */
    String PARAMETER_TARGET_SERVICE = "targetService";

    /**
     * Constant representing the method parameter in the request.
     */
    String PARAMETER_METHOD = "method";

    /**
     * Constant representing the pgtId parameter in the request.
     */
    String PARAMETER_PROXY_GRANTING_TICKET_ID = "pgtId";

    /**
     * Constant representing the pgt parameter in the request.
     */
    String PARAMETER_PROXY_GRANTING_TICKET = "pgt";

    /**
     * Constant representing the pgtIou parameter in the request.
     */
    String PARAMETER_PROXY_GRANTING_TICKET_IOU = "pgtIou";

    /**
     * Constant representing the pgtUrl parameter in the request.
     */
    String PARAMETER_PROXY_GRANTING_TICKET_URL = "pgtUrl";
    
    /* CAS Protocol Error Codes. **/

    /**
     * Constant representing an invalid request for validation.
     */
    String ERROR_CODE_INVALID_REQUEST = "INVALID_REQUEST";

    /**
     * Constant representing an invalid proxy callback for validation.
     */
    String ERROR_CODE_INVALID_PROXY_CALLBACK = "INVALID_PROXY_CALLBACK";

    /**
     * Constant representing an invalid ticket for validation.
     */
    String ERROR_CODE_INVALID_TICKET = "INVALID_TICKET";

    /**
     * Constant representing an invalid pgt request.
     */
    String ERROR_CODE_INVALID_REQUEST_PROXY = "INVALID_REQUEST_PROXY";

    /**
     * Constant representing an invalid service request.
     */
    String ERROR_CODE_UNAUTHORIZED_SERVICE = "UNAUTHORIZED_SERVICE";

    /**
     * Constant representing an invalid service proxy request.
     */
    String ERROR_CODE_UNAUTHORIZED_SERVICE_PROXY = "UNAUTHORIZED_SERVICE_PROXY";

    /**
     * Constant representing login.
     */
    String ENDPOINT_LOGIN = "/login";

    /**
     * Constant representing logout.
     */
    String ENDPOINT_LOGOUT = "/logout";
    
    /**
     * Constant representing proxy validate.
     */
    String ENDPOINT_PROXY_VALIDATE = "/proxyValidate";

    /**
     * Constant representing v3 proxy validate.
     */
    String ENDPOINT_PROXY_VALIDATE_V3 = "/p3/proxyValidate";

    /**
     * Constant representing legacy validate.
     */
    String ENDPOINT_VALIDATE = "/validate";

    /**
     * Constant representing service validate.
     */
    String ENDPOINT_SERVICE_VALIDATE = "/serviceValidate";

    /**
     * Constant representing v3 service validate.
     */
    String ENDPOINT_SERVICE_VALIDATE_V3 = "/p3/serviceValidate";

}
