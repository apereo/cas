package org.apereo.cas.ticket.registry.support.kryo.serial;

import java.lang.reflect.Constructor;

import org.apereo.cas.util.http.HttpClient;
import org.apereo.cas.util.http.SimpleHttpClientFactoryBean;
import org.apereo.cas.support.saml.authentication.principal.SamlService;
import org.apereo.cas.ticket.registry.support.kryo.FieldHelper;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

/**
 * Serializer for {@link SamlService} class.
 *
 * @author Marvin S. Addison
 * @since 3.0.0
 */
public class SamlServiceSerializer extends AbstractWebApplicationServiceSerializer<SamlService> {
    private static final Constructor CONSTRUCTOR;

    static {
        try {
            CONSTRUCTOR = SamlService.class.getDeclaredConstructor(
                    String.class, String.class, String.class, HttpClient.class, String.class);
            CONSTRUCTOR.setAccessible(true);
        } catch (final NoSuchMethodException e) {
            throw new IllegalStateException("Expected constructor signature not found.", e);
        }
    }

    /**
     * Instantiates a new SAML service serializer.
     *
     * @param helper the helper
     */
    public SamlServiceSerializer(final FieldHelper helper) {
        super(helper);
    }

    @Override
    public void write(final Kryo kryo, final Output output, final SamlService service) {
        super.write(kryo, output, service);
        kryo.writeObject(output, service.getRequestID());
    }

    @Override
    protected SamlService createService(final Kryo kryo, final Input input, final String id,
            final String originalUrl, final String artifactId) {

        final String requestId = kryo.readObject(input, String.class);
        try {
            return (SamlService) CONSTRUCTOR.newInstance(id, originalUrl, artifactId, new SimpleHttpClientFactoryBean().getObject(),
                    requestId);
        } catch (final Exception e) {
            throw new IllegalStateException("Error creating SamlService", e);
        }
    }
}
