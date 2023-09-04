package org.apereo.cas.adaptors.authy.core.okta.models;


import lombok.*;

import java.io.Serializable;


@ToString
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
public class OktaMfaSelect implements Serializable {
    private static final long serialVersionUID = -7970600701132111037L;

    private String identificationMethod;

    public OktaMfaSelect(final String identificationMethod) {
        this.identificationMethod = identificationMethod;
    }
}