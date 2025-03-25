package org.apereo.cas.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serial;
import java.io.Serializable;

/**
 * This is {@link SamlIdentityProviderBasicEntity}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class SamlIdentityProviderBasicEntity implements Serializable {
    @Serial
    private static final long serialVersionUID = 8880126090019920635L;

    private String value;

    private String lang;
}
