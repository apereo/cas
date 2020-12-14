package org.apereo.cas.configuration.model.support.acme;

import org.apereo.cas.configuration.model.SpringResourceProperties;
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

    @RequiredProperty
    private boolean termsOfUseAccepted;

    @NestedConfigurationProperty
    @RequiredProperty
    private SpringResourceProperties userKey = new SpringResourceProperties();
    
    @NestedConfigurationProperty
    @RequiredProperty
    private SpringResourceProperties domainKey = new SpringResourceProperties();

    @NestedConfigurationProperty
    @RequiredProperty
    private SpringResourceProperties domainCsr = new SpringResourceProperties();

    @NestedConfigurationProperty
    @RequiredProperty
    private SpringResourceProperties domainChain = new SpringResourceProperties();

    private int keySize = 2048;

    /**
     * Use {@code acme://letsencrypt.org} for production.
     */
    @RequiredProperty
    private String serverUrl = "acme://letsencrypt.org/staging";

    @RequiredProperty
    private List<String> domains = new ArrayList<>();

    private int retryAttempts = 3;

    private String retryInternal = "PT2S";
}
