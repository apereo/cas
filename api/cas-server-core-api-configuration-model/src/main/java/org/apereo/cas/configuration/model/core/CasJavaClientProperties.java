package org.apereo.cas.configuration.model.core;

import lombok.Getter;
import lombok.Setter;
import org.apereo.cas.configuration.support.RequiresModule;

import java.io.Serializable;

/**
 * This is {@link CasJavaClientProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RequiresModule(name = "cas-server-core", automated = true)

@Getter
@Setter
public class CasJavaClientProperties implements Serializable {

    private static final long serialVersionUID = -3646242105668747303L;
    /**
     * Prefix of the CAS server used to establish ticket validators for the client.
     * Typically set to {@code https://sso.example.org/cas}
     */
    private String prefix;
}
