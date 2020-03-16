package org.apereo.cas.configuration.model.support.x509;

import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * This is {@link SerialNoDnPrincipalResolverProperties}.
 * @since 6.0.0
 */
@RequiresModule(name = "cas-server-support-x509-webflow")
@Getter
@Setter
@Accessors(chain = true)
public class SerialNoDnPrincipalResolverProperties implements Serializable {
    private static final long serialVersionUID = 1259126639860604739L;
    /**
     * The serial number prefix used for principal resolution
     * when type is set to {@link X509Properties.PrincipalTypes#SERIAL_NO_DN}.
     */
    private String serialNumberPrefix = "SERIALNUMBER=";
    /**
     * Value delimiter used for principal resolution
     * when type is set to {@link X509Properties.PrincipalTypes#SERIAL_NO_DN}.
     */
    private String valueDelimiter = ", ";

}
