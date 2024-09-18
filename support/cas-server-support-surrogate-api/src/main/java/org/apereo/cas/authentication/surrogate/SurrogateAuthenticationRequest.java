package org.apereo.cas.authentication.surrogate;

import org.apereo.cas.authentication.MutableCredential;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.apache.commons.lang3.StringUtils;

import java.io.Serial;
import java.io.Serializable;

/**
 * This is {@link SurrogateAuthenticationRequest}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@ToString
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@Getter
@NoArgsConstructor(force = true)
@EqualsAndHashCode
@RequiredArgsConstructor
@SuperBuilder
public class SurrogateAuthenticationRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = -4562646187472556864L;

    private final MutableCredential credential;

    private final String username;

    private final String surrogateUsername;

    private final boolean selectable;
    
    /**
     * Has surrogate username?.
     *
     * @return true/false
     */
    public boolean hasSurrogateUsername() {
        return StringUtils.isNotBlank(this.surrogateUsername);
    }
}
