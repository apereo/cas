package org.apereo.cas.configuration.model.support.saml.idp;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * This is {@link SamlIdPAlgorithmsProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Getter
@Setter
public class SamlIdPAlgorithmsProperties implements Serializable {

    private static final long serialVersionUID = 6547093517788229284L;

    /**
     * The Override data encryption algorithms.
     */
    private List overrideDataEncryptionAlgorithms;

    /**
     * The Override key encryption algorithms.
     */
    private List overrideKeyEncryptionAlgorithms;

    /**
     * The Override black listed encryption algorithms.
     */
    private List overrideBlackListedEncryptionAlgorithms;

    /**
     * The Override white listed algorithms.
     */
    private List overrideWhiteListedAlgorithms;

    /**
     * The Override signature reference digest methods.
     */
    private List overrideSignatureReferenceDigestMethods;

    /**
     * The Override signature algorithms.
     */
    private List overrideSignatureAlgorithms;

    /**
     * The Override black listed signature signing algorithms.
     */
    private List overrideBlackListedSignatureSigningAlgorithms;

    /**
     * The Override white listed signature signing algorithms.
     */
    private List overrideWhiteListedSignatureSigningAlgorithms;

    /**
     * The Override signature canonicalization algorithm.
     */
    private String overrideSignatureCanonicalizationAlgorithm;
}
