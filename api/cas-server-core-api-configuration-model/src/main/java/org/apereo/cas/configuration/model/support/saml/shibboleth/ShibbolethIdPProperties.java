package org.apereo.cas.configuration.model.support.saml.shibboleth;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.configuration.support.RequiresModule;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

/**
 * This is {@link ShibbolethIdPProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@RequiresModule(name = "cas-server-support-shibboleth")
@Slf4j
@Getter
@Setter
public class ShibbolethIdPProperties implements Serializable {

    private static final long serialVersionUID = 1741075420882227768L;

    /**
     * The server url of the shibboleth idp deployment.
     */
    private String serverUrl;
}
