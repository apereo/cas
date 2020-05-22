package org.apereo.cas.api;

import org.apereo.cas.authentication.credential.BasicIdentifiableCredential;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * This is {@link PasswordlessUserAccount}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@Builder
public class PasswordlessUserAccount extends BasicIdentifiableCredential implements Serializable {
    private static final long serialVersionUID = 5783908770607793373L;

    @JsonProperty("username")
    private String username;

    @JsonProperty("email")
    private String email;

    @JsonProperty("phoneNumber")
    private String phone;

    @JsonProperty("name")
    private String name;

    @Builder.Default
    @JsonProperty("attributes")
    private Map<String, List<String>> attributes = new LinkedHashMap<>();

    @JsonProperty("multifactorAuthenticationEligible")
    private boolean multifactorAuthenticationEligible;

    @JsonProperty("delegatedAuthenticationEligible")
    private boolean delegatedAuthenticationEligible;

    @JsonProperty("requestPassword")
    private boolean requestPassword;

    @Override
    @JsonIgnore
    public String getId() {
        return getUsername();
    }

    @Override
    @JsonIgnore
    public void setId(final String id) {
        setUsername(id);
    }
}
