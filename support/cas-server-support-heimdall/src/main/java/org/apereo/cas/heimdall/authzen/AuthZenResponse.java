package org.apereo.cas.heimdall.authzen;

import org.apereo.cas.heimdall.BaseHeimdallEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.With;
import lombok.experimental.SuperBuilder;
import org.springframework.validation.annotation.Validated;
import java.io.Serial;
import java.util.HashMap;
import java.util.Map;

/**
 * This is {@link AuthZenResponse}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
@Data
@NoArgsConstructor
@Validated
@ToString
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@AllArgsConstructor
@With
public class AuthZenResponse extends BaseHeimdallEntity {
    @Serial
    private static final long serialVersionUID = -7726637704182199574L;

    private boolean decision;
    @Builder.Default
    private Map<String, ?> context = new HashMap<>();
}
