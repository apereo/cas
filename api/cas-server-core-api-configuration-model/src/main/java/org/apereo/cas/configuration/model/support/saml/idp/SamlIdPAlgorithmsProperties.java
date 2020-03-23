package org.apereo.cas.configuration.model.support.saml.idp;

import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link SamlIdPAlgorithmsProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RequiresModule(name = "cas-server-support-saml-idp")
@Getter
@Setter
@Accessors(chain = true)
public class SamlIdPAlgorithmsProperties implements Serializable {

    private static final long serialVersionUID = 6547093517788229284L;

    /**
     * The Override data encryption algorithms.
     */
    private List<String> overrideDataEncryptionAlgorithms = new ArrayList<>(0);

    /**
     * The Override key encryption algorithms.
     */
    private List<String> overrideKeyEncryptionAlgorithms = new ArrayList<>(0);

    /**
     * The Override black listed encryption algorithms.
     */
    private List<String> overrideBlackListedEncryptionAlgorithms = new ArrayList<>(0);

    /**
     * The Override white listed algorithms.
     */
    private List<String> overrideWhiteListedAlgorithms = new ArrayList<>(0);

    /**
     * The Override signature reference digest methods.
     */
    private List<String> overrideSignatureReferenceDigestMethods = new ArrayList<>(0);

    /**
     * The Override signature algorithms.
     */
    private List<String> overrideSignatureAlgorithms = new ArrayList<>(0);

    /**
     * The Override black listed signature signing algorithms.
     */
    private List<String> overrideBlackListedSignatureSigningAlgorithms = new ArrayList<>(0);

    /**
     * The Override white listed signature signing algorithms.
     */
    private List<String> overrideWhiteListedSignatureSigningAlgorithms = new ArrayList<>(0);

    /**
     * The Override signature canonicalization algorithm.
     */
    private String overrideSignatureCanonicalizationAlgorithm;
}
