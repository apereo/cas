package org.jasig.cas.services;

import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.util.RegexUtils;

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

    @Override
    public void setServiceId(final String id) {
        this.serviceId = id;

        // reset the servicePattern because we just changed the serviceId
        this.servicePattern = null;
    }

    @Override
    public boolean matches(final Service service) {
        if (this.servicePattern == null) {
            this.servicePattern = RegexUtils.createPattern(this.serviceId);
        }
        return service != null && this.servicePattern != null
                && this.servicePattern.matcher(service.getId()).matches();
    }

    @Override
    protected AbstractRegisteredService newInstance() {
        return new RegexRegisteredService();
    }

}
