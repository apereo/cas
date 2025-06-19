package org.apereo.cas.heimdall.authzen;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.With;
import lombok.experimental.SuperBuilder;
import org.springframework.validation.annotation.Validated;
import java.io.Serial;
import java.io.Serializable;

/**
 * This is {@link AuthZenAction}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
@Data
@NoArgsConstructor
@Validated
@ToString
@EqualsAndHashCode
@SuperBuilder
@AllArgsConstructor
@With
public class AuthZenAction implements Serializable {
    @Serial
    private static final long serialVersionUID = -2288573141680320570L;

    private String name;
}
