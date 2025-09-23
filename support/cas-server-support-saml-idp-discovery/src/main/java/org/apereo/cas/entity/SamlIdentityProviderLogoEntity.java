package org.apereo.cas.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serial;

/**
 * This is {@link SamlIdentityProviderLogoEntity}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class SamlIdentityProviderLogoEntity extends SamlIdentityProviderBasicEntity {
    @Serial
    private static final long serialVersionUID = -3901349236417720095L;

    private Integer height;

    private Integer width;
}
