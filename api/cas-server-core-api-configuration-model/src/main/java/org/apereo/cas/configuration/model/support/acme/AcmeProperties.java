package org.apereo.cas.configuration.model.support.acme;

import org.apereo.cas.configuration.model.SpringResourceProperties;
import org.apereo.cas.configuration.support.DurationCapable;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link AcmeProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@RequiresModule(name = "cas-server-support-acme")
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("AcmeProperties")
public class AcmeProperties implements Serializable {
    private static final long serialVersionUID = -561637865919944706L;

    /**
     * Flag that indicates ACME terms of use
     * has been accepted by the user.
     */
    @RequiredProperty
    private boolean termsOfUseAccepted;

    /**
     * Define the user's key file as a resource.
     */
    @NestedConfigurationProperty
    @RequiredProperty
    private SpringResourceProperties userKey = new SpringResourceProperties();

    /**
     * Define the domain's key file as a resource.
     */
    @NestedConfigurationProperty
    @RequiredProperty
    private SpringResourceProperties domainKey = new SpringResourceProperties();

    /**
     * Define the domains's CSR file as a resource.
     */
    @NestedConfigurationProperty
    @RequiredProperty
    private SpringResourceProperties domainCsr = new SpringResourceProperties();

    /**
     * Define the domain's chain certificate file as a resource.
     */
    @NestedConfigurationProperty
    @RequiredProperty
    private SpringResourceProperties domainChain = new SpringResourceProperties();

    /**
     * Indicate the key length/size used when requesting/generating keys.
     */
    private int keySize = 2048;

    /**
     * Server url to contact, when requesting certificates.
     * Use {@code acme://letsencrypt.org} for production.
     */
    @RequiredProperty
    private String serverUrl = "acme://letsencrypt.org/staging";

    /**
     * List of domains or sub domains
     * that are requesting a certificate renewal.
     */
    @RequiredProperty
    private List<String> domains = new ArrayList<>();

    /**
     * Number of attempts to retry when executing certificate orders
     * or checking for status of an existing order or challenge acknowledgement.
     */
    private int retryAttempts = 3;

    /**
     * Delay interval between to retry attempts when executing certificate orders
     * or checking for status of an existing order or challenge acknowledgement.
     */
    @DurationCapable
    private String retryInternal = "PT2S";
}
