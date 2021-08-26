package org.apereo.cas.configuration.model.support.mfa;

import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serializable;

/**
 * This is {@link MultifactorAuthenticationProviderBypassProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiresModule(name = "cas-server-core-authentication", automated = true)
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("MultifactorAuthenticationProviderBypassProperties")
public class MultifactorAuthenticationProviderBypassProperties implements Serializable {

    private static final long serialVersionUID = -9181362378365850397L;

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
    @NestedConfigurationProperty
    private GroovyMultifactorAuthenticationProviderBypassProperties groovy = new GroovyMultifactorAuthenticationProviderBypassProperties();

    /**
     * Handle bypass using a REST endpoint.
     */
    @NestedConfigurationProperty
    private RestfulMultifactorAuthenticationProviderBypassProperties rest = new RestfulMultifactorAuthenticationProviderBypassProperties();

}
