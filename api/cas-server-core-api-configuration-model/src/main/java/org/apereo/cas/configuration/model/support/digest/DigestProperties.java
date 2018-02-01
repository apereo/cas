package org.apereo.cas.configuration.model.support.digest;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.configuration.support.RequiresModule;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

/**
 * This is {@link DigestProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-support-digest-authentication")
@Slf4j
@Getter
@Setter
public class DigestProperties implements Serializable {

    private static final long serialVersionUID = -7920128284733546444L;

    /**
     * The digest realm to use.
     */
    private String realm = "CAS";

    /**
     * Authentication method used when creating digest header.
     */
    private String authenticationMethod = "auth";

    /**
     * Static/stub list of username and passwords to accept
     * if no other account store is defined.
     */
    private Map<String, String> users = new HashMap<>();

    /**
     * Name of the authentication handler.
     */
    private String name;
}
