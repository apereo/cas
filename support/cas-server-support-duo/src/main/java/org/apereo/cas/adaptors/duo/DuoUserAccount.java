package org.apereo.cas.adaptors.duo;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.HashSet;
import java.util.Set;

/**
 * This is {@link DuoUserAccount}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class DuoUserAccount {

    /**
     * Duo account status.
     */
    public enum DuoAccountStatus {
        /**
         * Active duo account status.
         */
        ACTIVE,
        /**
         * Bypass duo account status.
         */
        BYPASS,
        /**
         * Disabled duo account status.
         */
        DISABLED,
        /**
         * Lockedout duo account status.
         */
        LOCKEDOUT
    }

    /**
     * The User id.
     */
    private String userId;
    /**
     * The Real name.
     */
    private String realName;
    /**
     * The Username.
     */
    private String username;
    /**
     * The Email.
     */
    private String email;
    /**
     * The Status.
     */
    private DuoAccountStatus status;
    /**
     * The Groups.
     */
    private Set<String> groups = new HashSet<>();

    /**
     * Instantiates a new Duo user account.
     *
     * @param userId the user id
     */
    public DuoUserAccount(final String userId) {
        this.userId = userId;
    }

    /**
     * Gets user id.
     *
     * @return the user id
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Sets user id.
     *
     * @param userId the user id
     * @return the user id
     */
    public DuoUserAccount setUserId(final String userId) {
        this.userId = userId;
        return this;
    }

    /**
     * Gets real name.
     *
     * @return the real name
     */
    public String getRealName() {
        return realName;
    }

    /**
     * Sets real name.
     *
     * @param realName the real name
     * @return the real name
     */
    public DuoUserAccount setRealName(final String realName) {
        this.realName = realName;
        return this;
    }

    /**
     * Gets username.
     *
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets username.
     *
     * @param username the username
     * @return the username
     */
    public DuoUserAccount setUsername(final String username) {
        this.username = username;
        return this;
    }

    /**
     * Gets email.
     *
     * @return the email
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets email.
     *
     * @param email the email
     * @return the email
     */
    public DuoUserAccount setEmail(final String email) {
        this.email = email;
        return this;
    }

    /**
     * Gets status.
     *
     * @return the status
     */
    public DuoAccountStatus getStatus() {
        return status;
    }

    /**
     * Sets status.
     *
     * @param status the status
     * @return the status
     */
    public DuoUserAccount setStatus(final DuoAccountStatus status) {
        this.status = status;
        return this;
    }

    /**
     * Gets groups.
     *
     * @return the groups
     */
    public Set<String> getGroups() {
        return groups;
    }

    /**
     * Sets groups.
     *
     * @param groups the groups
     * @return the groups
     */
    public DuoUserAccount setGroups(final Set<String> groups) {
        this.groups = groups;
        return this;
    }

    /**
     * Is account status active boolean.
     *
     * @return the boolean
     */
    public boolean isAccountStatusActive() {
        return this.status == DuoAccountStatus.ACTIVE;
    }

    /**
     * Is account status bypass boolean.
     *
     * @return the boolean
     */
    public boolean isAccountStatusBypass() {
        return this.status == DuoAccountStatus.BYPASS;
    }

    /**
     * New instance duo user account.
     *
     * @param uid the uid
     * @return the duo user account
     */
    public static DuoUserAccount newInstance(final String uid) {
        return new DuoUserAccount(uid);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.NO_CLASS_NAME_STYLE)
                .append("userId", userId)
                .append("realName", realName)
                .append("username", username)
                .append("email", email)
                .append("status", status)
                .append("groups", groups)
                .toString();
    }
}
