package org.apereo.cas.configuration.model.support.trusted;

import org.apereo.cas.configuration.model.core.authentication.PersonDirectoryPrincipalResolverProperties;
import org.apereo.cas.configuration.support.RegularExpressionCapable;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.stream.Stream;

/**
 * This is {@link TrustedAuthenticationProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-support-trusted-webflow")
@Getter
@Setter
@Accessors(chain = true)

public class TrustedAuthenticationProperties implements Serializable {

    @Serial
    private static final long serialVersionUID = 279410895614233349L;

    /**
     * Indicates the name of the request header that may be extracted from the request
     * as the indicated authenticated userid from the remote authn system.
     */
    private String remotePrincipalHeader;

    /**
     * Indicates the name of the authentication handler.
     */
    private String name;

    /**
     * Order of the authentication handler in the chain.
     */
    private Integer order;

    /**
     * Regular expression that is applied to all request headers
     * to extract them as principal attributes. The comparison is
     * case insensitive. The pattern should use regular expression groups
     * to extract inner matches from a header value.
     */
    @RegularExpressionCapable
    private List<String> attributeHeaderPatterns = Stream.of("(.+)->(.+)").toList();

    /**
     * Principal resolution settings after successful authentication attempts.
     */
    @NestedConfigurationProperty
    private PersonDirectoryPrincipalResolverProperties personDirectory = new PersonDirectoryPrincipalResolverProperties();
}
