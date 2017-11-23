package org.apereo.cas.configuration.model.support.mfa;

import org.apereo.cas.configuration.support.RequiresModule;
import org.apereo.cas.configuration.support.RestEndpointProperties;
import org.apereo.cas.configuration.support.SpringResourceProperties;

import java.io.Serializable;

/**
 * This is {@link MultifactorAuthenticationProviderBypassProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiresModule(name = "cas-server-core-authentication", automated = true)
public class MultifactorAuthenticationProviderBypassProperties implements Serializable {
    private static final long serialVersionUID = -9181362378365850397L;
    
    public enum MultifactorProviderBypassTypes {
        /**
         * Handle multifactor authentication bypass per default CAS rules.
         */
        DEFAULT,
        /**
         * Handle multifactor authentication bypass via a Groovy script.
         */
        GROOVY,
        /**
         * Handle multifactor authentication bypass via a REST endpoint.
         */
        REST
    }

    /**
     * Acceptable values are:
     * <ul>
     *     <li>{@code DEFAULT}: Default bypass rules to skip provider via attributes, etc.</li>
     *     <li>{@code GROOVY}: Handle bypass decisions via a groovy script.</li>
     *     <li>{@code REST}: Handle bypass rules via a REST endpoint</li>
     * </ul>
     */
    private MultifactorProviderBypassTypes type = MultifactorProviderBypassTypes.DEFAULT;
    /**
     * Skip multifactor authentication based on designated principal attribute names.
     */
    private String principalAttributeName;
    /**
     * Optionally, skip multifactor authentication based on designated principal attribute values.
     */
    private String principalAttributeValue;
    /**
     * Skip multifactor authentication based on designated authentication attribute names.
     */
    private String authenticationAttributeName;
    /**
     * Optionally, skip multifactor authentication based on designated authentication attribute values.
     */
    private String authenticationAttributeValue;
    /**
     * Skip multifactor authentication depending on form of primary authentication execution.
     * Specifically, skip multifactor if the a particular authentication handler noted by its name
     * successfully is able to authenticate credentials in the primary factor.
     */
    private String authenticationHandlerName;
    /**
     * Skip multifactor authentication depending on method/form of primary authentication execution.
     * Specifically, skip multifactor if the authentication method attribute collected as part of
     * authentication metadata matches a certain value.
     */
    private String authenticationMethodName;
    /**
     * Skip multifactor authentication depending on form of primary credentials.
     * Value must equal the fully qualified class name of the credential type.
     */
    private String credentialClassType;

    /**
     * Skip multifactor authentication if the http request's remote address or host
     * matches the value defined here. The value may be specified as a regular expression.
     */
    private String httpRequestRemoteAddress;

    /**
     * Skip multifactor authentication if the http request contains the defined header names.
     * Header names may be comma-separated and can be regular expressions; values are ignored.
     */
    private String httpRequestHeaders;
    
    /**
     * Handle bypass using a Groovy resource.
     */
    private Groovy groovy = new Groovy();
    /**
     * Handle bypass using a REST endpoint.
     */
    private Rest rest = new Rest();
    
    public String getCredentialClassType() {
        return credentialClassType;
    }

    public void setCredentialClassType(final String credentialClassType) {
        this.credentialClassType = credentialClassType;
    }

    public String getAuthenticationAttributeName() {
        return authenticationAttributeName;
    }

    public void setAuthenticationAttributeName(final String authenticationAttributeName) {
        this.authenticationAttributeName = authenticationAttributeName;
    }

    public String getAuthenticationAttributeValue() {
        return authenticationAttributeValue;
    }

    public void setAuthenticationAttributeValue(final String authenticationAttributeValue) {
        this.authenticationAttributeValue = authenticationAttributeValue;
    }

    public String getPrincipalAttributeName() {
        return principalAttributeName;
    }

    public void setPrincipalAttributeName(final String principalAttributeName) {
        this.principalAttributeName = principalAttributeName;
    }

    public String getPrincipalAttributeValue() {
        return principalAttributeValue;
    }

    public void setPrincipalAttributeValue(final String principalAttributeValue) {
        this.principalAttributeValue = principalAttributeValue;
    }

    public String getAuthenticationHandlerName() {
        return authenticationHandlerName;
    }

    public void setAuthenticationHandlerName(final String authenticationHandlerName) {
        this.authenticationHandlerName = authenticationHandlerName;
    }

    public String getAuthenticationMethodName() {
        return authenticationMethodName;
    }

    public void setAuthenticationMethodName(final String authenticationMethodName) {
        this.authenticationMethodName = authenticationMethodName;
    }

    public MultifactorProviderBypassTypes getType() {
        return type;
    }

    public void setType(final MultifactorProviderBypassTypes type) {
        this.type = type;
    }

    public Groovy getGroovy() {
        return groovy;
    }

    public void setGroovy(final Groovy groovy) {
        this.groovy = groovy;
    }

    public Rest getRest() {
        return rest;
    }

    public void setRest(final Rest rest) {
        this.rest = rest;
    }

    public String getHttpRequestRemoteAddress() {
        return httpRequestRemoteAddress;
    }

    public void setHttpRequestRemoteAddress(final String httpRequestRemoteAddress) {
        this.httpRequestRemoteAddress = httpRequestRemoteAddress;
    }

    public String getHttpRequestHeaders() {
        return httpRequestHeaders;
    }

    public void setHttpRequestHeaders(final String httpRequestHeaders) {
        this.httpRequestHeaders = httpRequestHeaders;
    }

    @RequiresModule(name = "cas-server-core-authentication", automated = true)
    public static class Groovy extends SpringResourceProperties {
        private static final long serialVersionUID = 8079027843747126083L;
    }

    @RequiresModule(name = "cas-server-core-authentication", automated = true)
    public static class Rest extends RestEndpointProperties {
        private static final long serialVersionUID = 1833594332973137011L;
    }
}
