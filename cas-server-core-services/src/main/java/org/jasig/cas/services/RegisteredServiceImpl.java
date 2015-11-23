package org.jasig.cas.services;

import org.jasig.cas.authentication.principal.Service;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * @deprecated As for 4.1. Consider using {@link org.jasig.cas.services.RegexRegisteredService} instead.
 * Mutable registered service that uses Ant path patterns for service matching.
 *
 * @author Scott Battaglia
 * @author Marvin S. Addison
 * @since 3.1
 */
@Entity
@DiscriminatorValue("ant")
@Deprecated
public class RegisteredServiceImpl extends AbstractRegisteredService {

    /** Unique Id for serialization. */
    private static final long serialVersionUID = -5906102762271197627L;

    private static final PathMatcher PATH_MATCHER = new AntPathMatcher();

    /**
     * @deprecated As of 4.1. Consider using regex patterns instead
     * via {@link org.jasig.cas.services.RegexRegisteredService}.
     * Instantiates a new registered service.
     */
    @Deprecated
    public RegisteredServiceImpl() {
        super();
        logger.info("[{}] is deprecated and will be removed in future CAS releases. Consider using [{}] instead.",
                this.getClass().getSimpleName(), RegexRegisteredService.class.getSimpleName());
    }

    @Override
    public void setServiceId(final String id) {
        this.serviceId = id;
    }

    @Override
    public boolean matches(final Service service) {
        return service != null && PATH_MATCHER.match(serviceId.toLowerCase(), service.getId().toLowerCase());
    }

    @Override
    protected AbstractRegisteredService newInstance() {
        return new RegisteredServiceImpl();
    }
}

