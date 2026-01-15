package org.apereo.cas.impl.token;

import module java.base;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.With;
import lombok.experimental.SuperBuilder;
import lombok.val;
import org.springframework.data.annotation.Id;

/**
 * This is {@link PasswordlessAuthenticationToken}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@ToString
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@SuperBuilder
@With
public class PasswordlessAuthenticationToken implements Serializable {
    @Serial
    private static final long serialVersionUID = 3810773120720229099L;

    @Id
    @JsonProperty
    private String id;

    private String username;

    private String token;

    private ZonedDateTime expirationDate;

    @JsonProperty("properties")
    @Builder.Default
    private Map<String, String> properties = new HashMap<>();
    
    /**
     * Is expired?
     *
     * @return true/false
     */
    @JsonIgnore
    public boolean isExpired() {
        val now = ZonedDateTime.now(ZoneOffset.UTC);
        return now.isAfter(getExpirationDate()) || now.isEqual(getExpirationDate());
    }

    /**
     * Add property.
     *
     * @param key   the key
     * @param value the value
     * @return the passwordless authentication token
     */
    @JsonIgnore
    @CanIgnoreReturnValue
    public PasswordlessAuthenticationToken property(final String key, final String value) {
        this.properties.put(key, value);
        return this;
    }
}
