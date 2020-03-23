package org.apereo.cas.configuration.model.support.mfa.trusteddevice;

import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * Common configuration for device fingerprint components.
 *
 * @author Daniel Frett
 * @since 5.3.0
 */
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
@RequiresModule(name = "cas-server-support-trusted-mfa")
public abstract class BaseDeviceFingerprintComponentProperties implements Serializable {
    private static final long serialVersionUID = 46126170193036440L;

    /**
     * Is this component enabled or not.
     */
    private boolean enabled;

    /**
     * Indicates the order of components when generating a device fingerprint.
     */
    private int order;
}
