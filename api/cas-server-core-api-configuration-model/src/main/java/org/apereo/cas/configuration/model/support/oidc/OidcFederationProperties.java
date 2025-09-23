package org.apereo.cas.configuration.model.support.oidc;

import org.apereo.cas.configuration.support.ExpressionLanguageCapable;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link OidcFederationProperties}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
@RequiresModule(name = "cas-server-support-oidc")
@Getter
@Setter
@Accessors(chain = true)
public class OidcFederationProperties implements Serializable {

    @Serial
    private static final long serialVersionUID = 113328615694269276L;

    /**
     * Organization name to include in the federation metadata.
     */
    @RequiredProperty
    private String organization = "Apereo CAS";

    /**
     * List of contacts to include in the federation metadata.
     */
    @RequiredProperty
    private List<String> contacts = new ArrayList<>();

    /**
     * The List of federation authorities where this entity is enrolled.
     * This is used to generate the federation metadata and the entries
     * are also considered trust anchors when validating incoming federation
     * requests from relying parties.
     */
    @RequiredProperty
    private List<String> authorityHints = new ArrayList<>();

    /**
     * Path to the JWKS file resource used to handle signing of federation metadata.
     * Note that if the keystore file does not exist at the specified path, one will be generated for you.
     */
    @RequiredProperty
    @ExpressionLanguageCapable
    private String jwksFile = "file:/etc/cas/config/federation-keystore.jwks";
}
