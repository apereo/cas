package org.apereo.cas.adaptors.duo;

import module java.base;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.With;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;

/**
 * This is {@link DuoSecurityUserAccount}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@ToString
@Getter
@Setter
@EqualsAndHashCode
@RequiredArgsConstructor
@AllArgsConstructor
@With
@Accessors(chain = true)
@NoArgsConstructor(force = true)
public class DuoSecurityUserAccount implements Serializable {

    @Serial
    private static final long serialVersionUID = 403995883439339241L;

    private final String username;

    @JsonIgnore
    private final Map<String, String> metadata = new LinkedHashMap<>();

    private DuoSecurityUserAccountStatus status = DuoSecurityUserAccountStatus.AUTH;

    private String enrollPortalUrl;

    private String message;

    private List<DuoSecurityUserDevice> devices = new ArrayList<>();

    private List<DuoSecurityBypassCode> bypassCodes = new ArrayList<>();

    private List<DuoSecurityUserAccountGroup> groups = new ArrayList<>();

    private String userId;

    private String providerId;

    /**
     * Add groups.
     *
     * @param group the group
     * @return the duo security user account
     */
    @CanIgnoreReturnValue
    public DuoSecurityUserAccount addGroup(final DuoSecurityUserAccountGroup group) {
        this.groups.add(group);
        return this;
    }

    /**
     * Add bypass code.
     *
     * @param codes the codes
     * @return the duo security user account
     */
    @CanIgnoreReturnValue
    public DuoSecurityUserAccount addBypassCodes(final List<DuoSecurityBypassCode> codes) {
        this.bypassCodes.addAll(codes);
        return this;
    }

    /**
     * Add devices.
     *
     * @param device the device
     * @return the duo security user account
     */
    @CanIgnoreReturnValue
    public DuoSecurityUserAccount addDevice(final DuoSecurityUserDevice device) {
        this.devices.add(device);
        return this;
    }

    /**
     * Add attribute.
     *
     * @param key   the key
     * @param value the value
     * @return the duo security user account
     */
    @CanIgnoreReturnValue
    public DuoSecurityUserAccount addAttribute(final String key, final String value) {
        if (!Strings.CI.equalsAny("null", value) && StringUtils.isNotBlank(value)) {
            this.metadata.put(key, value);
        }
        return this;
    }

    public String getUserId() {
        return this.metadata.get("user_id");
    }

    public String getFirstName() {
        return this.metadata.get("firstname");
    }

    /**
     * Set the first name, as Duo Security names it; a blank name is left out.
     *
     * @param firstName the first name
     * @return the duo security user account
     */
    @JsonSetter("firstName")
    @CanIgnoreReturnValue
    public DuoSecurityUserAccount setFirstName(final String firstName) {
        return addAttribute("firstname", firstName);
    }

    public String getLastName() {
        return this.metadata.get("lastname");
    }

    /**
     * Set the last name, as Duo Security names it; a blank name is left out.
     *
     * @param lastName the last name
     * @return the duo security user account
     */
    @JsonSetter("lastName")
    @CanIgnoreReturnValue
    public DuoSecurityUserAccount setLastName(final String lastName) {
        return addAttribute("lastname", lastName);
    }

    public String getEmail() {
        return this.metadata.get("email");
    }

    /**
     * Set the email address, as Duo Security names it; a blank address is left out.
     *
     * @param email the email address
     * @return the duo security user account
     */
    @JsonSetter("email")
    @CanIgnoreReturnValue
    public DuoSecurityUserAccount setEmail(final String email) {
        return addAttribute("email", email);
    }

    public String getRealName() {
        return this.metadata.get("realname");
    }

    public Instant getLastLogin() {
        return Instant.ofEpochSecond(Long.parseLong(metadata.getOrDefault("last_login", "0")));
    }

    public Instant getCreated() {
        return Instant.ofEpochSecond(Long.parseLong(metadata.getOrDefault("created", "0")));
    }

    public boolean isEnrolled() {
        return BooleanUtils.toBoolean(this.metadata.getOrDefault("is_enrolled", "false"));
    }

    public String getPhone() {
        return this.devices
            .stream()
            .filter(device -> Strings.CI.equals(device.getType(), "phone")
                || Strings.CI.equals(device.getType(), "mobile"))
            .map(DuoSecurityUserDevice::getNumber)
            .filter(StringUtils::isNotBlank)
            .findFirst()
            .orElse(StringUtils.EMPTY);
        
    }
}
