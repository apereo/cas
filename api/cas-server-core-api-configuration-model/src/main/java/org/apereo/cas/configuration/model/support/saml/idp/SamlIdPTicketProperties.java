package org.apereo.cas.configuration.model.support.saml.idp;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * This is {@link SamlIdPTicketProperties.java}.
 *
 * @author Samuel Lyons
 * @since 5.3.0
 */
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