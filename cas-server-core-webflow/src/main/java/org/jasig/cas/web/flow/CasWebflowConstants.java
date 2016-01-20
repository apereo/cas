package org.jasig.cas.web.flow;

/**
 * This is {@link CasWebflowConstants}.
 *
 * @author Misagh Moayyed
 * @since 4.3.0
 */
public interface CasWebflowConstants {
    /**
     * The transition state 'success'.
     */
    String TRANSITION_ID_SUCCESS = "success";

    /**
     * The transition state 'realSubmit'.
     */
    String TRANSITION_ID_REAL_SUBMIT = "realSubmit";

    /**
     * The transition state 'yes'.
     */
    String TRANSITION_ID_YES = "yes";

    /**
     * The transition state 'no'.
     */
    String TRANSITION_ID_NO = "no";

    /**
     * The transition state 'submit'.
     */
    String TRANSITION_ID_SUBMIT = "submit";
    /**
     * The transition state 'generated'.
     */
    String TRANSITION_ID_GENERATED = "generated";
    /**
     * The transition state 'error'.
     */
    String TRANSITION_ID_ERROR = "error";

    /**
     * The transition state 'authenticationFailure'.
     */
    String TRANSITION_ID_AUTHENTICATION_FAILURE = "authenticationFailure";

    /**
     * The transition state 'warn'.
     */
    String TRANSITION_ID_WARN = "warn";

    /**
     * The transition state 'sendTicketGrantingTicket'.
     */
    String TRANSITION_ID_SEND_TICKET_GRANTING_TICKET = "sendTicketGrantingTicket";

    /**
     * The transition state 'viewLoginForm'.
     */
    String TRANSITION_ID_VIEW_LOGIN_FORM = "viewLoginForm";


    /**
     * The transition state 'ticketGrantingTicketCheck'.
     */
    String TRANSITION_ID_TICKET_GRANTING_TICKET_CHECK = "ticketGrantingTicketCheck";

    /**
     * The action state 'generateLoginTicket'.
     */
    String STATE_ID_GENERATE_LOGIN_TICKET = "generateLoginTicket";
}
