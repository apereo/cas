package org.apereo.cas.support.oauth.web.audit;

/**
 * Constants encapsulating names of components for auditing facility.
 *
 * @author Dmitriy Kopylenko
 * @since 5.3.0
 */
public interface Oauth2Audits {

    /**
     * User profile audit action name.
     */
    String USER_PROFILE_AUDIT_ACTION = "OAUTH2_USER_PROFILE_DATA";

    /**
     * User profile audit action resolver name.
     */
    String USER_PROFILE_AUDIT_ACTION_RESOLVER_NAME = "OAUTH2_USER_PROFILE_DATA_ACTION_RESOLVER";


    /**
     * User profile audit resource resolver name.
     */
    String USER_PROFILE_AUDIT_RESOURCE_RESOLVER_NAME = "OAUTH2_USER_PROFILE_DATA_RESOURCE_RESOLVER";
}
