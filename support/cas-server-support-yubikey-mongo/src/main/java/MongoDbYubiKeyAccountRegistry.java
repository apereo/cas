import org.apereo.cas.adaptors.yubikey.YubiKeyAccountValidator;
import org.apereo.cas.adaptors.yubikey.registry.BaseYubiKeyAccountRegistry;

/**
 * This is {@link MongoDbYubiKeyAccountRegistry}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class MongoDbYubiKeyAccountRegistry extends BaseYubiKeyAccountRegistry {

    public MongoDbYubiKeyAccountRegistry(final YubiKeyAccountValidator accountValidator) {
        super(accountValidator);
    }

    @Override
    public boolean isYubiKeyRegisteredFor(final String uid) {
        return true;
    }

    @Override
    public boolean registerAccount(final String uid, final String yubikeyPublicId) {
        return false;
    }

    @Override
    public boolean isYubiKeyRegisteredFor(final String uid, final String yubikeyPublicId) {
        return false;
    }
}
