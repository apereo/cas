package org.apereo.cas.api;

import org.apereo.cas.configuration.support.TriStateBoolean;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.With;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
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
@AllArgsConstructor
@NoArgsConstructor
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@SuperBuilder
@With
@Accessors(chain = true)
public class PasswordlessUserAccount implements Serializable {
    @Serial
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
    @Builder.Default
    @JsonDeserialize(using = TriStateBoolean.Deserializer.class)
    private TriStateBoolean multifactorAuthenticationEligible = TriStateBoolean.UNDEFINED;

    @JsonProperty("delegatedAuthenticationEligible")
    @Builder.Default
    @JsonDeserialize(using = TriStateBoolean.Deserializer.class)
    private TriStateBoolean delegatedAuthenticationEligible = TriStateBoolean.UNDEFINED;

    @JsonProperty("allowedDelegatedClients")
    @Builder.Default
    private List<String> allowedDelegatedClients = new ArrayList<>();

    @JsonProperty("requestPassword")
    private boolean requestPassword;

    @JsonProperty("source")
    private String source;
}
