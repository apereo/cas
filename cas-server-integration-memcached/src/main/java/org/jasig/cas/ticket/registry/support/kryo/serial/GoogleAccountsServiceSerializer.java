package org.jasig.cas.ticket.registry.support.kryo.serial;

import java.lang.reflect.Constructor;
import java.security.PrivateKey;
import java.security.PublicKey;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import org.jasig.cas.support.saml.authentication.principal.GoogleAccountsService;
import org.jasig.cas.ticket.registry.support.kryo.FieldHelper;

/**
 * Serializer for {@link GoogleAccountsService}.
 *
 * @author Marvin S. Addison
 * @since 3.0.0
 */
public final class GoogleAccountsServiceSerializer extends AbstractWebApplicationServiceSerializer<GoogleAccountsService> {

    private static final Constructor CONSTRUCTOR;

    private final PrivateKey privateKey;
    private final PublicKey publicKey;
    private final String alternateUsername;

    static {
        try {
            CONSTRUCTOR = GoogleAccountsService.class.getDeclaredConstructor(
                    String.class,
                    String.class,
                    String.class,
                    String.class,
                    String.class,
                    PrivateKey.class,
                    PublicKey.class,
                    String.class);
            CONSTRUCTOR.setAccessible(true);
        } catch (final NoSuchMethodException e) {
            throw new IllegalStateException("Expected constructor signature not found.", e);
        }
    }

    /**
     * Instantiates a new google accounts service serializer.
     *
     * @param helper the helper
     * @param publicKey the public key
     * @param privateKey the private key
     * @param alternateUsername the alternate username
     */
    public GoogleAccountsServiceSerializer(
            final FieldHelper helper,
            final PublicKey publicKey,
            final PrivateKey privateKey,
            final String alternateUsername) {

        super(helper);
        this.publicKey = publicKey;
        this.privateKey = privateKey;
        this.alternateUsername = alternateUsername;
    }

    @Override
    public void write(final Kryo kryo, final Output output, final GoogleAccountsService service) {
        super.write(kryo, output, service);
        kryo.writeObject(output, fieldHelper.getFieldValue(service, "requestId"));
        kryo.writeObject(output, fieldHelper.getFieldValue(service, "relayState"));
    }

    @Override
    protected GoogleAccountsService createService(final Kryo kryo, final Input input, final String id,
            final String originalUrl, final String artifactId) {

        final String requestId = kryo.readObject(input, String.class);
        final String relayState = kryo.readObject(input, String.class);
        try {
            return (GoogleAccountsService) CONSTRUCTOR.newInstance(
                    id,
                    originalUrl,
                    artifactId,
                    relayState,
                    requestId,
                    privateKey,
                    publicKey,
                    alternateUsername);
        } catch (final Exception e) {
            throw new IllegalStateException("Error creating SamlService", e);
        }
    }
}
