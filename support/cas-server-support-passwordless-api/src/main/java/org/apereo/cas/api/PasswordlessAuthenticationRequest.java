package org.apereo.cas.api;

import module java.base;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.With;
import lombok.experimental.SuperBuilder;

/**
 * This is {@link PasswordlessAuthenticationRequest}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Getter
@Setter
@ToString(callSuper = true)
@RequiredArgsConstructor
@NoArgsConstructor(force = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@SuperBuilder
@AllArgsConstructor
@With
public class PasswordlessAuthenticationRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = -1992873518520202699L;

    private final String username;

    private final String providedUsername;

    @Builder.Default
    private Map<String, String> properties = new HashMap<>();

}
