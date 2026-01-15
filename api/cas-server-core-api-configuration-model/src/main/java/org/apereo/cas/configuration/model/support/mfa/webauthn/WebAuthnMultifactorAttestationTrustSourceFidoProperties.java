package org.apereo.cas.configuration.model.support.mfa.webauthn;

import module java.base;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jooq.lambda.Unchecked;

/**
 * This is {@link WebAuthnMultifactorAttestationTrustSourceFidoProperties}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@RequiresModule(name = "cas-server-support-webauthn")
@Getter
@Setter
@Accessors(chain = true)
public class WebAuthnMultifactorAttestationTrustSourceFidoProperties implements Serializable {
    /**
     * Default legal text header.
     */
    public static final String DEFAULT_LEGAL_HEADER =
        "Retrieval and use of this BLOB indicates acceptance of the appropriate agreement located at https://fidoalliance.org/metadata/metadata-legal-terms/";

    @Serial
    private static final long serialVersionUID = -6224841263678287815L;

    /**
     * Set legal headers expected in the metadata BLOB.
     * By using the FIDO Metadata Service, you will be subject to its terms of service.
     * This setting serves two purposes:
     * <p>
     * To remind you and any  adopters/reviewers that you need to read those terms of service before using this feature.
     * To help you detect if the legal header changes, so you can take appropriate action.
     * <p>
     * If the legal header in the downloaded BLOB does not equal any of the expected headers,
     * an exception will be thrown in the finalizing configuration step.
     * <p>
     * Note that CAS makes no guarantee that a change to the FIDO Metadata Service
     * terms of service will also cause a change to the legal header in the BLOB.
     * <p>
     * The current legal header is noted by:
     * {@link #DEFAULT_LEGAL_HEADER} which is the following:
     * <p><br>
     * {@code "Retrieval and use of this BLOB indicates acceptance of the appropriate agreement located at https://fidoalliance.org/metadata/metadata-legal-terms/"}.
     */
    @RequiredProperty
    private String legalHeader;

    /**
     * Download the metadata BLOB from the FIDO website.
     * This is the current FIDO Metadata Service BLOB download URL.
     */
    @RequiredProperty
    private String metadataBlobUrl = "https://mds.fidoalliance.org/";

    /**
     * Certificate required for PKI to verify the downloaded blob.
     * This is the current FIDO Metadata Service trust root certificate.
     * If the cert is downloaded, it is also written to the cache File.
     * The certificate will be downloaded if it does not exist in the cache, or if the cached certificate is not currently valid.
     */
    @RequiredProperty
    private String trustRootUrl = "https://secure.globalsign.com/cacert/root-r3.crt";

    /**
     * Certificate SHA-256 hash required for PKI to verify the downloaded certificate.
     * Separate hash values with a comma.
     */
    @RequiredProperty
    private String trustRootHash = "cbb522d7b7f127ad6a0113865bdf1cd4102e7d0759af635a7cf4720dc963c53b";

    /**
     * Cache the trust root certificate in the file cache file.
     * If cache file exists, is a normal file, is readable, matches one of the SHA-256 hashes configured in
     * and contains a currently valid X.509 certificate, then it will be used as the trust root for the FIDO Metadata Service blob.
     * <p>
     * Otherwise, the trust root certificate will be downloaded and written to this file.
     */
    private File trustRootCacheFile = Unchecked.supplier(
        () -> Files.createTempFile("webauthn.fido.trust.root", ".cache").toFile()).get();

    /**
     * Cache metadata BLOB in the file cache file.
     * If cache file exists, is a normal file, is readable, and is not out of date, then it will be used as the FIDO Metadata Service BLOB.
     * <p>
     * Otherwise, the metadata BLOB will be downloaded and written to this file.
     */
    private File blobCacheFile = Unchecked.supplier(
        () -> Files.createTempFile("webauthn.fido.blob", ".cache").toFile()).get();
}
