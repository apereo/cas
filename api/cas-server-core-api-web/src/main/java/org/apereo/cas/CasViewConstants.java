package org.apereo.cas;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Principal;

/**
 * Constants interface to host fields
 * related to view rendering and validation model.
 *
 * @author Misagh Moayyed
 * @since 4.1
 */
public interface CasViewConstants {

    /**
     * Represents the flag to note the principal credential used to establish
     * a successful authentication event.
     */
    String MODEL_ATTRIBUTE_NAME_PRINCIPAL_CREDENTIAL = "credential";

    /**
     * Represents the
     * {@link Principal} object in the view.
     */
    String MODEL_ATTRIBUTE_NAME_PRINCIPAL = "principal";

    /**
     * Represents the chained authentication objects
     * in the view for proxying.
     */
    String MODEL_ATTRIBUTE_NAME_CHAINED_AUTHENTICATIONS = "chainedAuthentications";

    /**
     * Represents the
     * {@link Authentication} object in the view.
     **/
    String MODEL_ATTRIBUTE_NAME_PRIMARY_AUTHENTICATION = "primaryAuthentication";

    /**
     * Constant representing the Assertion in the cas validation model.
     */
    String MODEL_ATTRIBUTE_NAME_ASSERTION = "assertion";

    /**
     * The constant representing the error code in the response.
     */
    String MODEL_ATTRIBUTE_NAME_ERROR_CODE = "code";

    /**
     * The constant representing the error description in the response.
     */
    String MODEL_ATTRIBUTE_NAME_ERROR_DESCRIPTION = "description";

    /**
     * The constant representing the validated service in the response.
     */
    String MODEL_ATTRIBUTE_NAME_SERVICE = "service";

    /**
     * The constant representing the PGTIOU in the response.
     */
    String MODEL_ATTRIBUTE_NAME_PROXY_GRANTING_TICKET_IOU = "pgtIou";

    /**
     * The constant representing the PGT in the response.
     */
    String MODEL_ATTRIBUTE_NAME_PROXY_GRANTING_TICKET = "proxyGrantingTicket";
}
