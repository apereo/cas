package org.apereo.cas.web.flow.executor;

import java.io.IOException;

/**
 * Provides a strategy pattern interface for transforming an object into a byte array and vice versa.
 * {@link Transcoder} components are used by {@link ClientFlowExecutionRepository} for producing the data stored
 * in a {@link ClientFlowExecutionKey}.
 *
 * @author Marvin S. Addison
 * @see ClientFlowExecutionKey
 * @see ClientFlowExecutionRepository
 * @since 6.1
 */
public interface Transcoder {

    /**
     * Encodes an object into a stream of bytes.
     *
     * @param o Object to encode.
     * @return Object encoded as a byte array.
     * @throws IOException On encoding errors.
     */
    byte[] encode(Object o) throws IOException;

    /**
     * Decodes a stream of bytes produced by {@link #encode(Object)} back into the original object.
     *
     * @param encoded Encoded representation of an object.
     * @return Object decoded from byte array.
     * @throws IOException On decoding errors.
     */
    Object decode(byte[] encoded) throws IOException;
}
