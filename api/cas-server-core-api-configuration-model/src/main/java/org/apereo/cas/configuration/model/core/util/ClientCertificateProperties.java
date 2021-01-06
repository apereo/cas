package org.apereo.cas.configuration.model.core.util;

import org.apereo.cas.configuration.model.SpringResourceProperties;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * A client certificate properties.
 *
 * @author Jerome LELEU
 * @since 6.4.0
 */
@RequiresModule(name = "cas-server-core-util", automated = true)
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("ClientCertificateProperties")
public class ClientCertificateProperties implements Serializable {

    private static final long serialVersionUID = -8004292720523993292L;

    /**
     * The location of the client certificate (PKCS12 format).
     */
    @RequiredProperty
    private transient SpringResourceProperties certificate = new SpringResourceProperties();

    /**
     * The passphrase of the client certificate.
     */
    @RequiredProperty
    private String passphrase;
}
