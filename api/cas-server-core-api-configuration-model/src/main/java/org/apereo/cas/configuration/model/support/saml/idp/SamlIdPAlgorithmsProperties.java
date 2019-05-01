package org.apereo.cas.configuration.model.support.saml.idp;

import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;

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
public class SamlIdPAlgorithmsProperties implements Serializable {

    private static final long serialVersionUID = 6547093517788229284L;

    /**
     * The Override data encryption algorithms.
     */
    private List<String> overrideDataEncryptionAlgorithms = new ArrayList<>();

    /**
     * The Override key encryption algorithms.
     */
    private List<String> overrideKeyEncryptionAlgorithms = new ArrayList<>();

    /**
     * The Override black listed encryption algorithms.
     */
    private List<String> overrideBlackListedEncryptionAlgorithms = new ArrayList<>();

    /**
     * The Override white listed algorithms.
     */
    private List<String> overrideWhiteListedAlgorithms = new ArrayList<>();

    /**
     * The Override signature reference digest methods.
     */
    private List<String> overrideSignatureReferenceDigestMethods = new ArrayList<>();

    /**
     * The Override signature algorithms.
     */
    private List<String> overrideSignatureAlgorithms = new ArrayList<>();

    /**
     * The Override black listed signature signing algorithms.
     */
    private List<String> overrideBlackListedSignatureSigningAlgorithms = new ArrayList<>();

    /**
     * The Override white listed signature signing algorithms.
     */
    private List<String> overrideWhiteListedSignatureSigningAlgorithms = new ArrayList<>();

    /**
     * The Override signature canonicalization algorithm.
     */
    private String overrideSignatureCanonicalizationAlgorithm;
}
