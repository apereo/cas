package org.apereo.cas.sqrl;

import org.jsqrl.model.SqrlUser;

/**
 * This is {@link SqrlAccount}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class SqrlAccount implements SqrlUser {

    private String id;

    private String identityKey;
    private String serverUnlockKey;
    private String verifyUnlockKey;
    private Boolean sqrlEnabled;

    private String firstName;
    private String email;

    public SqrlAccount(final String id, final String identityKey,
                       final String serverUnlockKey,
                       final String verifyUnlockKey, final Boolean sqrlEnabled,
                       final String firstName, final String email) {
        this.id = id;
        this.identityKey = identityKey;
        this.serverUnlockKey = serverUnlockKey;
        this.verifyUnlockKey = verifyUnlockKey;
        this.sqrlEnabled = sqrlEnabled;
        this.firstName = firstName;
        this.email = email;
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public void setIdentityKey(final String identityKey) {
        this.identityKey = identityKey;
    }

    public void setServerUnlockKey(final String serverUnlockKey) {
        this.serverUnlockKey = serverUnlockKey;
    }

    public void setVerifyUnlockKey(final String verifyUnlockKey) {
        this.verifyUnlockKey = verifyUnlockKey;
    }

    public Boolean getSqrlEnabled() {
        return sqrlEnabled;
    }

    public void setSqrlEnabled(final Boolean sqrlEnabled) {
        this.sqrlEnabled = sqrlEnabled;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(final String firstName) {
        this.firstName = firstName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(final String email) {
        this.email = email;
    }

    @Override
    public String getIdentityKey() {
        return this.identityKey;
    }

    @Override
    public String getServerUnlockKey() {
        return this.serverUnlockKey;
    }

    @Override
    public String getVerifyUnlockKey() {
        return this.verifyUnlockKey;
    }

    @Override
    public Boolean sqrlEnabled() {
        return sqrlEnabled;
    }
}
