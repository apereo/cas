package org.apereo.cas.support.saml.idp.metadata.writer;

import java.io.Writer;

/**
 * A metadata writer that defines how certificates, keys and metadata should be written to a destination.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@FunctionalInterface
public interface SamlIdPCertificateAndKeyWriter {
    /**
     * Write certificate and keys.
     *
     * @param privateKeyWriter  the private key writer
     * @param certificateWriter the certificate writer
     */
    void writeCertificateAndKey(Writer privateKeyWriter, Writer certificateWriter);

}
