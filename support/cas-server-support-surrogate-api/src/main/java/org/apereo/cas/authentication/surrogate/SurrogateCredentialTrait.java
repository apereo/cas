package org.apereo.cas.authentication.surrogate;

import org.apereo.cas.authentication.CredentialTrait;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.io.Serial;

/**
 * This is {@link SurrogateCredentialTrait}.
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
public class SurrogateCredentialTrait implements CredentialTrait {
    @Serial
    private static final long serialVersionUID = -3264159627200534296L;

    private final String surrogateUsername;
}
