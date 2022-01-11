package org.apereo.cas.web;

import org.apereo.cas.configuration.model.support.delegation.DelegationAutoRedirectTypes;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * This is {@link DelegatedClientIdentityProviderConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@AllArgsConstructor
@Getter
@ToString
@Setter
@SuperBuilder
public class DelegatedClientIdentityProviderConfiguration implements Serializable {
    private static final long serialVersionUID = 6216882278086699364L;

    private final String name;

    private String redirectUrl;

    private final String type;

    private String cssClass;

    @Builder.Default
    private DelegationAutoRedirectTypes autoRedirectType = DelegationAutoRedirectTypes.NONE;

    private String title;
}
