package org.apereo.cas.configuration.model.support.aup;

import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * This is {@link AcceptableUsagePolicyCoreProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-support-aup-webflow")
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("AcceptableUsagePolicyCoreProperties")
public class AcceptableUsagePolicyCoreProperties implements Serializable {

    private static final long serialVersionUID = -7703477581675908899L;

    /**
     * Allows AUP to be turned off on startup.
     */
    @RequiredProperty
    private boolean enabled = true;

    /**
     * AUP attribute to choose in order to determine whether policy
     * has been accepted or not. The attribute is expected to contain
     * a boolean value where {@code true} indicates policy has been
     * accepted and {@code false} indicates otherwise.
     * The attribute is fetched for the principal from configured sources
     * and compared for the right match to determine policy status.
     * If the attribute is not found, the policy status is considered as denied.
     */
    @RequiredProperty
    private String aupAttributeName = "aupAccepted";

    /**
     * AUP attribute to choose whose single value dictates
     * how CAS should fetch the policy terms from
     * the relevant message bundles.
     */
    private String aupPolicyTermsAttributeName;

}
