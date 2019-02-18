package org.apereo.cas.services;

import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.util.RegexUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.annotation.Transient;

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
@EqualsAndHashCode(callSuper = true)
public class RegexRegisteredService extends AbstractRegisteredService {

    private static final long serialVersionUID = -8258660210826975771L;

    @JsonIgnore
    @Transient
    @javax.persistence.Transient
    private transient Pattern servicePattern;
    
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
        return !StringUtils.isBlank(serviceId) && this.servicePattern.matcher(serviceId).matches();
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
