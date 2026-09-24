package org.apereo.cas.configuration.model.support.saml.idp.metadata;

import module java.base;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link MDQSamlMetadataProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@RequiresModule(name = "cas-server-support-saml-idp")
@Getter
@Setter
@Accessors(chain = true)
public class MDQSamlMetadataProperties implements Serializable {
    @Serial
    private static final long serialVersionUID = -1311568960413770598L;

    /**
     * Basic auth username in case the metadata instance is connecting to an MDQ server.
     */
    private String basicAuthnUsername;

    /**
     * Basic auth password in case the metadata instance is connecting to an MDQ server.
     */
    private String basicAuthnPassword;

    /**
     * The media type this instance asks an MDQ server for, sent as the {@code Accept} header.
     * The SAML profile for the Metadata Query Protocol requires a conforming client to request
     * {@code application/samlmetadata+xml}, which is the default. Override it only for a server
     * that serves SAML metadata under a different media type.
     */
    private String supportedContentType = "application/samlmetadata+xml";

}
