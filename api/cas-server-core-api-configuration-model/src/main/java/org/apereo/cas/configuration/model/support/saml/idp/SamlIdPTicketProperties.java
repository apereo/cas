package org.apereo.cas.configuration.model.support.saml.idp;

import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * This is {@link SamlIdPTicketProperties}.
 *
 * @author Samuel Lyons
 * @since 5.3.0
 */
@RequiresModule(name = "cas-server-support-saml-idp")
@Getter
@Setter
public class SamlIdPTicketProperties implements Serializable {

    private static final long serialVersionUID = 6837089259390742073L;

    /**
     * name that should be given to the saml artifact cache storage name.
     */
    private String samlArtifactsCacheStorageName = "samlArtifactsCache";

    /**
     * The name that should be given to the saml attribute query cache storage name.
     */
    private String samlAttributeQueryCacheStorageName = "samlAttributeQueryCache";
}
