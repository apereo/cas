package org.apereo.cas.services;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.util.RegexUtils;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.util.regex.Pattern;

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
public class RegexRegisteredService extends AbstractRegisteredService {

    private static final long serialVersionUID = -8258660210826975771L;

    private transient Pattern servicePattern;

    /**
     * {@inheritDoc}
     * Resets the pattern because we just changed the id.
     * @param id the new service id
     */
    @Override
    public void setServiceId(final String id) {
        this.serviceId = id;
        this.servicePattern = null;
    }
    
    @Override
    public boolean matches(final Service service) {
        return service != null && matches(service.getId());
    }

    @Override
    public boolean matches(final String serviceId) {
        if (this.servicePattern == null) {
            this.servicePattern = RegexUtils.createPattern(this.serviceId);
        }
        return this.servicePattern.matcher(serviceId).matches();
    }

    @Override
    protected AbstractRegisteredService newInstance() {
        return new RegexRegisteredService();
    }

    @JsonIgnore
    @Override
    public String getFriendlyName() {
        return "CAS Client";
    }
}
