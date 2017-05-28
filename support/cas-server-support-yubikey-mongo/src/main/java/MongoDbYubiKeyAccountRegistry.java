import org.apereo.cas.adaptors.yubikey.registry.BaseYubiKeyAccountRegistry;

/**
 * This is {@link MongoDbYubiKeyAccountRegistry}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class MongoDbYubiKeyAccountRegistry extends BaseYubiKeyAccountRegistry {
    @Override
    public boolean isYubiKeyRegisteredFor(final String uid, final String yubikeyPublicId) {
        return false;
    }
}
