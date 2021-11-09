package org.apereo.cas.web;

import lombok.AllArgsConstructor;
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

    private boolean autoRedirect;

    private String title;
}
