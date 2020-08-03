package org.apereo.cas.support.saml.util;

import org.apereo.cas.ticket.UniqueTicketIdGenerator;
import org.apereo.cas.util.DigestUtils;
import org.apereo.cas.util.RandomUtils;

import lombok.Setter;
import lombok.SneakyThrows;
import lombok.val;
import org.opensaml.saml.common.binding.artifact.AbstractSAMLArtifact;
import org.opensaml.saml.saml1.binding.artifact.SAML1ArtifactType0001;
import org.opensaml.saml.saml2.binding.artifact.SAML2ArtifactType0004;

import java.security.SecureRandom;

/**
 * Unique Ticket Id Generator compliant with the SAML 1.1 specification for
 * artifacts. This should also be compliant with the SAML 2 specification.
 * <p>
 * Default to SAML 1.1 Compliance.
 *
 * @author Scott Battaglia
 * @since 3.0.0
 */
@Setter
public class SamlCompliantUniqueTicketIdGenerator implements UniqueTicketIdGenerator {

    /**
     * Assertion handles are randomly-generated 20-byte identifiers.
     */
    private static final int ASSERTION_HANDLE_SIZE = 20;

    /**
     * SAML 2 Type 0004 endpoint ID is 0x0001.
     */
    private static final byte[] ENDPOINT_ID = {0, 1};

    /**
     * SAML defines the source id as the server name.
     */
    private final byte[] sourceIdDigest;
    /**
     * Random generator to construct the AssertionHandle.
     */
    private final SecureRandom random;
    /**
     * Flag to indicate SAML2 compliance. Default is SAML1.1.
     */
    private boolean saml2compliant;

    /**
     * Instantiates a new SAML compliant unique ticket id generator.
     *
     * @param sourceId the source id
     */
    public SamlCompliantUniqueTicketIdGenerator(final String sourceId) {
        try {
            this.sourceIdDigest = DigestUtils.rawDigest("SHA", sourceId.getBytes("8859_1"));
        } catch (final Exception e) {
            throw new IllegalStateException("Exception generating digest of source ID.", e);
        }
        this.random = RandomUtils.getNativeInstance();
    }

    /**
     * {@inheritDoc}
     * We ignore prefixes for SAML compliance.
     */
    @Override
    @SneakyThrows
    public String getNewTicketId(final String prefix) {
        val artifact = getSAMLArtifactType();
        return prefix + SEPARATOR + artifact.base64Encode();
    }

    private AbstractSAMLArtifact getSAMLArtifactType() {
        if (this.saml2compliant) {
            return new SAML2ArtifactType0004(ENDPOINT_ID, newAssertionHandle(), this.sourceIdDigest);
        }
        return new SAML1ArtifactType0001(this.sourceIdDigest, newAssertionHandle());
    }

    /**
     * New assertion handle.
     *
     * @return the byte[] array of size {@link #ASSERTION_HANDLE_SIZE}
     */
    private byte[] newAssertionHandle() {
        val handle = new byte[ASSERTION_HANDLE_SIZE];
        this.random.nextBytes(handle);
        return handle;
    }
}
