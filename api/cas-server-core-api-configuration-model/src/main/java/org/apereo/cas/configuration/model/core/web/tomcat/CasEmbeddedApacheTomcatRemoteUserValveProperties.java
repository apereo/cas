package org.apereo.cas.configuration.model.core.web.tomcat;

import org.apereo.cas.configuration.support.RegularExpressionCapable;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import java.io.Serial;
import java.io.Serializable;

/**
 * This is {@link CasEmbeddedApacheTomcatRemoteUserValveProperties}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@RequiresModule(name = "cas-server-webapp-tomcat")
@Getter
@Setter
@Accessors(chain = true)

public class CasEmbeddedApacheTomcatRemoteUserValveProperties implements Serializable {

    @Serial
    private static final long serialVersionUID = -32143821503580896L;

    /**
     * The name of the remote-user header that should be
     * passed onto the http servlet request. Leaving this setting
     * as blank or undefined will deactivate the valve altogether.
     * The header is typically passed down to tomcat via proxies,
     * load balancers, etc.
     */
    @RequiredProperty
    private String remoteUserHeader;

    /**
     * A regular expression (using java.util.regex) that the remote client's IP address is compared to.
     * If this attribute is specified, the remote address MUST match for this request to be accepted.
     * If this attribute is not specified, all requests will be accepted.
     */
    @RegularExpressionCapable
    private String allowedIpAddressRegex = ".+";
    
}
