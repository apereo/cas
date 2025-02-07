package org.apereo.cas.support.inwebo.web.flow.actions;

/**
 * The webflow constants.
 *
 * @author Jerome LELEU
 * @since 6.4.0
 */
public interface InweboWebflowConstants {
    /**
     * The pending transition.
     */
    String PENDING = "pending";
    /**
     * The push transition.
     */
    String PUSH = "push";
    /**
     * The Virtual Authenticator transition.
     */
    String VA = "va";
    /**
     * The mAccessWeb transition.
     */
    String MA = "ma";
    /**
     * The select transition.
     */
    String SELECT = "select";

    /**
     * The mustEnroll action.
     */
    String MUST_ENROLL = "mustEnroll";

    /**
     * The browser authentication activation status.
     */
    long BROWSER_AUTHENTICATION_STATUS = 4L;
    /**
     * The push and browser authentication activation status.
     */
    long PUSH_AND_BROWSER_AUTHENTICATION_STATUS = 5L;

    /**
     * The inweboSessionId flow scope parameter.
     */
    String INWEBO_SESSION_ID = "inweboSessionId";
    /**
     * The siteAlias flow scope parameter.
     */
    String SITE_ALIAS = "siteAlias";
    /**
     * The siteDescription flow scope parameter.
     */
    String SITE_DESCRIPTION = "siteDescription";
    /**
     * The login flow scope parameter.
     */
    String LOGIN = "login";
    /**
     * The browser authenticator flow scope parameter.
     */
    String BROWSER_AUTHENTICATOR = "browserAuthenticator";
    /**
     * The otp request parameter.
     */
    String OTP = "otp";
}
