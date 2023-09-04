package org.apereo.cas.adaptors.authy.core.okta.models;


import lombok.*;

import java.io.Serializable;


@ToString
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
public class OktaVerify implements Serializable {
    private static final long serialVersionUID = -7970600701132111037L;

    private String oktaVerify;

    public OktaVerify(final String oktaVerify) {
        this.oktaVerify = oktaVerify;
    }
}