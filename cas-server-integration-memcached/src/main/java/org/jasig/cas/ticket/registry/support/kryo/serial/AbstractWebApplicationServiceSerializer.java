package org.jasig.cas.ticket.registry.support.kryo.serial;

import org.jasig.cas.authentication.principal.AbstractWebApplicationService;
import org.jasig.cas.ticket.registry.support.kryo.FieldHelper;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

/**
 * Serializer for classes that extend {@link org.jasig.cas.authentication.principal.AbstractWebApplicationService}.
 *
 * @author Marvin S. Addison
 * @since 3.0.0
 */
public abstract class AbstractWebApplicationServiceSerializer<T extends AbstractWebApplicationService>
        extends Serializer<T> {
    /** FieldHelper instance. **/
    protected final FieldHelper fieldHelper;

    /**
     * Instantiates a new abstract web application service serializer.
     *
     * @param helper the helper
     */
    public AbstractWebApplicationServiceSerializer(final FieldHelper helper) {
        this.fieldHelper = helper;
    }

    @Override
    public void write(final Kryo kryo, final Output output, final T service) {
        kryo.writeObject(output, service.getId());
        kryo.writeObject(output, fieldHelper.getFieldValue(service, "originalUrl"));
        kryo.writeObject(output, service.getArtifactId());
    }

    @Override
    public T read(final Kryo kryo, final Input input, final Class<T> type) {
        return createService(kryo, input,
                kryo.readObject(input, String.class),
                kryo.readObject(input, String.class),
                kryo.readObject(input, String.class));
    }

    /**
     * Creates the service.
     *
     * @param kryo the Kryo instance
     * @param input the input stream representing the serialized object
     * @param id the id
     * @param originalUrl the original url
     * @param artifactId the artifact id
     * @return the created service instance.
     */
    protected abstract T createService(Kryo kryo, Input input, String id,
            String originalUrl, String artifactId);
}
