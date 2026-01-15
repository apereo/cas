package org.apereo.cas.support.saml.util;

import module java.base;
import org.apereo.cas.ticket.UniqueTicketIdGenerator;
import org.apereo.cas.util.DigestUtils;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.function.FunctionUtils;
import lombok.Setter;
import lombok.val;
import org.opensaml.saml.common.binding.artifact.AbstractSAMLArtifact;
import org.opensaml.saml.saml1.binding.artifact.SAML1ArtifactType0001;
import org.opensaml.saml.saml2.binding.artifact.SAML2ArtifactType0004;

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
     * Flag to indicate SAML2 compliance. Default is SAML1.1.
     */
    private boolean saml2compliant;

    public SamlCompliantUniqueTicketIdGenerator(final String sourceId) {
        this.sourceIdDigest = FunctionUtils.doUnchecked(() -> DigestUtils.rawDigest("SHA", sourceId.getBytes(StandardCharsets.ISO_8859_1)));
    }

    /**
     * {@inheritDoc}
     * We ignore prefixes for SAML compliance.
     */
    @Override
    public String getNewTicketId(final String prefix) {
        return FunctionUtils.doUnchecked(() -> {
            val artifact = getSAMLArtifactType();
            return prefix + SEPARATOR + artifact.base64Encode();
        });
    }

    private AbstractSAMLArtifact getSAMLArtifactType() {
        if (this.saml2compliant) {
            return new SAML2ArtifactType0004(ENDPOINT_ID, newAssertionHandle(), this.sourceIdDigest);
        }
        return new SAML1ArtifactType0001(this.sourceIdDigest, newAssertionHandle());
    }

    private static byte[] newAssertionHandle() {
        val handle = new byte[ASSERTION_HANDLE_SIZE];
        RandomUtils.getNativeInstance().nextBytes(handle);
        return handle;
    }
}
