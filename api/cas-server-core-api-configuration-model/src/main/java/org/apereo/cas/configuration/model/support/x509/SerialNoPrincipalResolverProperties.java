package org.apereo.cas.configuration.model.support.x509;

import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * This is {@link SerialNoPrincipalResolverProperties}.
 * @since 6.0.0
 */
@RequiresModule(name = "cas-server-support-x509-webflow")
@Getter
@Setter
@Accessors(chain = true)
public class SerialNoPrincipalResolverProperties implements Serializable {

    private static final long serialVersionUID = -4935371089672080311L;
    /**
     * Radix used when {@link X509Properties.PrincipalTypes} is {@link X509Properties.PrincipalTypes#SERIAL_NO}.
     */
    private int principalSNRadix;
    /**
     * If radix hex padding should be used when {@link X509Properties.PrincipalTypes} is {@link X509Properties.PrincipalTypes#SERIAL_NO}.
     */
    private boolean principalHexSNZeroPadding;


}
