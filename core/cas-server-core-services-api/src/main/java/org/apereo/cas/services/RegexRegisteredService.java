package org.apereo.cas.services;

import org.apereo.cas.authentication.principal.Service;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * Mutable registered service that uses Java regular expressions for service matching.
 * Matching is case insensitive, and is successful, if, and only if, the entire region
 * sequence matches the pattern.
 *
 * @author Marvin S. Addison
 * @author Misagh Moayyed
 * @since 3.4
 */
@Entity
@DiscriminatorValue("regex")
@EqualsAndHashCode(callSuper = true)
public class RegexRegisteredService extends AbstractRegisteredService {
    private static final long serialVersionUID = -8258660210826975771L;

    @Override
    public void setServiceId(final String id) {
        this.serviceId = id;
    }

    @Override
    public boolean matches(final Service service) {
        return service != null && matches(service.getId());
    }

    @Override
    public boolean matches(final String serviceId) {
        configureMatchingStrategy();
        return !StringUtils.isBlank(serviceId) && getMatchingStrategy().matches(this, serviceId);
    }

    @JsonIgnore
    @Override
    public String getFriendlyName() {
        return "CAS Client";
    }

    /**
     * Configure matching strategy.
     * If the strategy is undefined, it will default to {@link FullRegexRegisteredServiceMatchingStrategy}.
     */
    protected void configureMatchingStrategy() {
        if (getMatchingStrategy() == null) {
            setMatchingStrategy(new FullRegexRegisteredServiceMatchingStrategy());
        }
    }

    @Override
    protected AbstractRegisteredService newInstance() {
        return new RegexRegisteredService();
    }
}
