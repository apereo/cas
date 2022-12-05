package org.apereo.cas.api;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.io.Serial;
import java.io.Serializable;

/**
 * This is {@link PasswordlessRequest}.
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
public class PasswordlessRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = -1992873518520202699L;

    private final String username;
}
