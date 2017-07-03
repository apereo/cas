package org.apereo.cas.sqrl.storage;

import org.apereo.cas.sqrl.SqrlAccount;
import org.jsqrl.model.SqrlUser;
import org.jsqrl.service.SqrlUserService;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * This is {@link SqrlInMemoryUserService}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class SqrlInMemoryUserService implements SqrlUserService<SqrlUser> {

    private Map<String, SqrlAccount> userTable;

    public SqrlInMemoryUserService() {
        userTable = new HashMap<>();
    }

    @Override
    public SqrlAccount getUserBySqrlKey(final String publicKey) {
        return findUserBySqrlIdentity(publicKey);
    }

    @Override
    public Boolean updateIdentityKey(final String previousIdentityKey, final String identityKey) {
        final SqrlAccount user = findUserBySqrlIdentity(previousIdentityKey);

        if (user != null) {
            user.setIdentityKey(identityKey);
            return true;
        }

        return false;
    }

    @Override
    public SqrlAccount registerSqrlUser(final String identityKey, final String serverUnlockKey,
                                     final String verifyUnlockKey) {
        final String userId = UUID.randomUUID().toString();
        final SqrlAccount newUser = new SqrlAccount(userId, identityKey, serverUnlockKey,
                verifyUnlockKey, true, null, null);
        userTable.put(userId, newUser);
        return newUser;
    }

    @Override
    public Boolean disableSqrlUser(final String identityKey) {
        final SqrlAccount user = getUserBySqrlKey(identityKey);
        if (user != null) {
            user.setSqrlEnabled(false);
            return true;
        }
        return false;
    }

    @Override
    public Boolean enableSqrlUser(final String identityKey) {
        final SqrlAccount user = getUserBySqrlKey(identityKey);
        if (user != null) {
            user.setSqrlEnabled(true);
            return true;
        }
        return false;
    }

    @Override
    public Boolean removeSqrlUser(final String identityKey) {
        final SqrlAccount user = getUserBySqrlKey(identityKey);
        if (user != null) {
            userTable.remove(user.getId());
            return true;
        }
        return false;
    }

    private Boolean updateUserById(final String id, final String firstName, final String email) {
        final SqrlAccount user = userTable.get(id);
        user.setFirstName(firstName);
        user.setEmail(email);
        return true;
    }

    private SqrlAccount findUserBySqrlIdentity(final String publicKey) {
        return userTable
                .entrySet()
                .stream()
                .filter(e -> publicKey.equals(e.getValue().getIdentityKey()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
    }


}
