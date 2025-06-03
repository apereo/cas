package org.apereo.cas.heimdall.authzen;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.With;
import lombok.experimental.SuperBuilder;
import net.minidev.json.annotate.JsonIgnore;
import org.springframework.validation.annotation.Validated;
import java.io.Serial;
import java.io.Serializable;
import java.util.Map;
import java.util.Optional;

/**
 * This is {@link AuthZenResource}.
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
public class AuthZenResource implements Serializable {
    @Serial
    private static final long serialVersionUID = -5988573141680320570L;

    private String type;
    private String id;
    private Map<String, Object> properties;
}
