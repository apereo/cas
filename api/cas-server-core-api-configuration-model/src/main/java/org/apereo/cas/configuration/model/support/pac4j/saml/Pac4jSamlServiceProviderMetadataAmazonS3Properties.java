package org.apereo.cas.configuration.model.support.pac4j.saml;

import org.apereo.cas.configuration.model.support.aws.BaseAmazonWebServicesProperties;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import java.io.Serial;

/**
 * This is {@link Pac4jSamlServiceProviderMetadataAmazonS3Properties}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@RequiresModule(name = "cas-server-support-pac4j-saml")
@Getter
@Setter
@Accessors(chain = true)

public class Pac4jSamlServiceProviderMetadataAmazonS3Properties extends BaseAmazonWebServicesProperties {
    @Serial
    private static final long serialVersionUID = -1214734720383722585L;
}

