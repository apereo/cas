package org.apereo.cas.configuration.model.support.aup;

import module java.base;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link InMemoryAcceptableUsagePolicyProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@RequiresModule(name = "cas-server-support-aup-core")
@Getter
@Setter
@Accessors(chain = true)
public class InMemoryAcceptableUsagePolicyProperties implements CasFeatureModule, Serializable {
    @Serial
    private static final long serialVersionUID = 8164227843747126083L;

    /**
     * Scope of map where the aup selection is stored.
     */
    private Scope scope = Scope.GLOBAL;

    /**
     * Scope options for the default aup repository can store flag indicating acceptance.
     * Scope refers to duration that acceptance is kept.
     * Current options are global on the particular server (not replicated across CAS servers)
     * and once per authentication via credentials (not authentication events via TGT).
     */
    public enum Scope {
        /**
         * Store in global in-memory map (for life of server).
         */
        GLOBAL,

        /**
         * Store aup acceptance such that user is prompted when
         * they authenticate via credentials.
         */
        AUTHENTICATION
    }
}
