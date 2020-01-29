package org.apereo.cas.api;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@Builder
public class PasswordlessUserAccount implements Serializable {
    private static final long serialVersionUID = 5783908770607793373L;

    private String username;

    private String email;

    private String phone;

    private String name;

    @Builder.Default
    private Map<String, List<String>> attributes = new LinkedHashMap<>();

    private boolean multifactorAuthenticationEligible;
}
