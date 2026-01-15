package org.apereo.cas.configuration.model.support.wsfed;

import module java.base;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link WsFederationIdentityProviderProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Getter
@Setter
@Accessors(chain = true)
@RequiresModule(name = "cas-server-support-ws-idp")

public class WsFederationIdentityProviderProperties implements Serializable {
    @Serial
    private static final long serialVersionUID = 5190493517277610788L;

    /**
     * At this point, by default security token service’s endpoint operate using a single
     * realm configuration and identity provider configuration is only able to recognize and request tokens for a single realm.
     * Registration of clients need to ensure this value is matched.
     */
    @RequiredProperty
    private String realm = "urn:org:apereo:cas:ws:idp:realm-CAS";

    /**
     * Realm name.
     */
    @RequiredProperty
    private String realmName = "CAS";
}
