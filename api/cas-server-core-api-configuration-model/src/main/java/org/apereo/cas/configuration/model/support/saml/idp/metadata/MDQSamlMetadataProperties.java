package org.apereo.cas.configuration.model.support.saml.idp.metadata;

import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.http.MediaType;

import java.io.Serial;
import java.io.Serializable;

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
@JsonFilter("MDQSamlMetadataProperties")
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
     * Supported content types in case the metadata instance is connecting to an MDQ server.
     * {@link MediaType#TEXT_XML_VALUE} is supported by default.
     */
    private String supportedContentType = MediaType.TEXT_XML_VALUE;

}
