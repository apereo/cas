package org.apereo.cas.configuration.model.support.pac4j;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.configuration.support.RequiresModule;
import org.apereo.cas.configuration.support.RequiredProperty;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

/**
 * This is {@link Pac4jGenericClientProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiresModule(name = "cas-server-support-pac4j-webflow")
@Slf4j
@Getter
@Setter
public class Pac4jGenericClientProperties implements Serializable {

    private static final long serialVersionUID = 3007013267786902465L;

    /**
     * The client id.
     */
    @RequiredProperty
    private String id;

    /**
     * The client secret.
     */
    @RequiredProperty
    private String secret;

    /**
     * Name of the client mostly for UI purposes and uniqueness.
     */
    private String clientName;
}
