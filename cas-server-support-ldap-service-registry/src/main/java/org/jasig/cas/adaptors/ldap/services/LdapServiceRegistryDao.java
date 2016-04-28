package org.jasig.cas.adaptors.ldap.services;

import org.apache.commons.lang3.StringUtils;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.ServiceRegistryDao;
import org.jasig.cas.util.LdapUtils;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.LdapEntry;
import org.ldaptive.LdapException;
import org.ldaptive.Response;
import org.ldaptive.SearchFilter;
import org.ldaptive.SearchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;
import java.util.LinkedList;
import java.util.List;

/**
 * Implementation of the ServiceRegistryDao interface which stores the services in a LDAP Directory.
 *
 * @author Misagh Moayyed
 * @author Marvin S. Addison
 * @since 4.0.0
 */
@Component("ldapServiceRegistryDao")
public final class LdapServiceRegistryDao implements ServiceRegistryDao {

    private final transient Logger logger = LoggerFactory.getLogger(this.getClass());

    @Nullable
    @Autowired(required = false)
    @Qualifier("ldapServiceRegistryConnectionFactory")
    private ConnectionFactory connectionFactory;

    @Nullable
    @Autowired(required = false)
    @Qualifier("ldapServiceRegistryMapper")
    private LdapRegisteredServiceMapper ldapServiceMapper = new DefaultLdapRegisteredServiceMapper();

    @Value("${svcreg.ldap.baseDn:}")
    private String baseDn;

    @NotNull
    private String searchFilter;

    @NotNull
    private String loadFilter;

    /**
     * Inits the dao with the search filter and load filters.
     */
    @PostConstruct
    public void init() {
        if (this.ldapServiceMapper != null) {
            this.searchFilter = '(' + this.ldapServiceMapper.getIdAttribute() + "={0})";
            logger.debug("Configured search filter to {}", this.searchFilter);
            this.loadFilter = "(objectClass=" + this.ldapServiceMapper.getObjectClass() + ')';
            logger.debug("Configured load filter to {}", this.loadFilter);
        }
    }

    @Override
    public RegisteredService save(final RegisteredService rs) {
        if (this.ldapServiceMapper != null) {
            if (rs.getId() != RegisteredService.INITIAL_IDENTIFIER_VALUE) {
                return update(rs);
            }

            try {
                final LdapEntry entry = this.ldapServiceMapper.mapFromRegisteredService(this.baseDn, rs);
                LdapUtils.executeAddOperation(this.connectionFactory, entry);
            } catch (final LdapException e) {
                logger.error(e.getMessage(), e);
            }
            return rs;
        }
        return null;
    }

    /**
     * Update the ldap entry with the given registered service.
     *
     * @param rs the rs
     * @return the registered service
     */
    private RegisteredService update(final RegisteredService rs) {
        String currentDn = null;

        if (ldapServiceMapper == null) {
            return null;
        }

        try {
            final Response<SearchResult> response = searchForServiceById(rs.getId());
            if (LdapUtils.containsResultEntry(response)) {
                currentDn = response.getResult().getEntry().getDn();
            }
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
        }

        if (StringUtils.isNotBlank(currentDn)) {
            logger.debug("Updating registered service at {}", currentDn);
            final LdapEntry entry = this.ldapServiceMapper.mapFromRegisteredService(this.baseDn, rs);
            LdapUtils.executeModifyOperation(currentDn, this.connectionFactory, entry);
        }

        return rs;
    }

    @Override
    public boolean delete(final RegisteredService registeredService) {
        try {
            final Response<SearchResult> response = searchForServiceById(registeredService.getId());
            if (LdapUtils.containsResultEntry(response)) {
                final LdapEntry entry = response.getResult().getEntry();
                return LdapUtils.executeDeleteOperation(this.connectionFactory, entry);
            }
        } catch (final LdapException e) {
            logger.error(e.getMessage(), e);
        }
        return false;
    }

    @Override
    public List<RegisteredService> load() {
        final List<RegisteredService> list = new LinkedList<>();
        if (ldapServiceMapper == null) {
            return list;
        }

        try {
            final Response<SearchResult> response = LdapUtils.executeSearchOperation(this.connectionFactory,
                    this.baseDn, new SearchFilter(this.loadFilter));
            if (LdapUtils.containsResultEntry(response)) {
                for (final LdapEntry entry : response.getResult().getEntries()) {
                    final RegisteredService svc = this.ldapServiceMapper.mapToRegisteredService(entry);
                    list.add(svc);
                }
            }
        } catch (final LdapException e) {
            logger.error(e.getMessage(), e);
        }
        return list;
    }

    @Override
    public RegisteredService findServiceById(final long id) {
        try {
            if (this.ldapServiceMapper != null) {
                final Response<SearchResult> response = searchForServiceById(id);
                if (LdapUtils.containsResultEntry(response)) {
                    return this.ldapServiceMapper.mapToRegisteredService(response.getResult().getEntry());
                }
            }
        } catch (final LdapException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * Search for service by id.
     *
     * @param id the id
     * @return the response
     * @throws LdapException the ldap exception
     */
    private Response<SearchResult> searchForServiceById(final long id)
            throws LdapException {
        final SearchFilter filter = new SearchFilter(this.searchFilter);
        filter.setParameter(0, id);
        return LdapUtils.executeSearchOperation(this.connectionFactory, this.baseDn, filter);
    }


    public void setConnectionFactory(@NotNull final ConnectionFactory factory) {
        this.connectionFactory = factory;
    }

    public void setLdapServiceMapper(final LdapRegisteredServiceMapper ldapServiceMapper) {
        this.ldapServiceMapper = ldapServiceMapper;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
