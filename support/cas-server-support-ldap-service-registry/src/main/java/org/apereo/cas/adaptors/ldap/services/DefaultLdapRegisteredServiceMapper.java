package org.apereo.cas.adaptors.ldap.services;

import org.apereo.cas.configuration.model.support.ldap.serviceregistry.LdapServiceRegistryProperties;
import org.apereo.cas.services.AbstractRegisteredService;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.util.DefaultRegisteredServiceJsonSerializer;
import org.apereo.cas.util.LdapUtils;
import org.apereo.cas.util.serialization.StringSerializer;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Default implementation of {@link LdapRegisteredServiceMapper} that is able
 * to map ldap entries to {@link RegisteredService} instances based on
 * certain attributes names. This implementation also respects the object class
 * attribute of LDAP entries via {@link LdapUtils#OBJECT_CLASS_ATTRIBUTE}.
 *
 * @author Misagh Moayyed
 * @since 4.1.0
 */
public class DefaultLdapRegisteredServiceMapper implements LdapRegisteredServiceMapper {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultLdapRegisteredServiceMapper.class);
    private final LdapServiceRegistryProperties ldap;

    private StringSerializer<RegisteredService> jsonSerializer = new DefaultRegisteredServiceJsonSerializer();

    public DefaultLdapRegisteredServiceMapper(final LdapServiceRegistryProperties ldapProperties) {
        ldap = ldapProperties;
    }

    @Override
    public LdapEntry mapFromRegisteredService(final String dn, final RegisteredService svc) {
        try {
            if (svc.getId() == RegisteredService.INITIAL_IDENTIFIER_VALUE) {
                ((AbstractRegisteredService) svc).setId(System.currentTimeMillis());
            }
            final String newDn = getDnForRegisteredService(dn, svc);
            LOGGER.debug("Creating entry DN [{}]", newDn);

            final Collection<LdapAttribute> attrs = new ArrayList<>();
            attrs.add(new LdapAttribute(ldap.getIdAttribute(), String.valueOf(svc.getId())));

            try (StringWriter writer = new StringWriter()) {
                this.jsonSerializer.to(writer, svc);
                attrs.add(new LdapAttribute(ldap.getServiceDefinitionAttribute(), writer.toString()));
                attrs.add(new LdapAttribute(LdapUtils.OBJECT_CLASS_ATTRIBUTE, "top", ldap.getObjectClass()));
            }
            LOGGER.debug("LDAP attributes assigned to the DN [{}] are [{}]", newDn, attrs);

            final LdapEntry entry = new LdapEntry(newDn, attrs);
            LOGGER.debug("Created LDAP entry [{}]", entry);
            return entry;
        } catch (final Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public RegisteredService mapToRegisteredService(final LdapEntry entry) {
        try {
            final String value = LdapUtils.getString(entry, ldap.getServiceDefinitionAttribute());
            if (StringUtils.hasText(value)) {
                LOGGER.debug("Transforming LDAP entry [{}] into registered service definition", entry);
                return this.jsonSerializer.from(value);
            }
            LOGGER.warn("LDAP entry [{}] is not assigned the service definition attribute [{}] and will be ignored",
                entry, ldap.getServiceDefinitionAttribute());
            return null;
        } catch (final Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public String getObjectClass() {
        return ldap.getObjectClass();
    }

    @Override
    public String getIdAttribute() {
        return ldap.getIdAttribute();
    }

    @Override
    public String getDnForRegisteredService(final String parentDn, final RegisteredService svc) {
        return String.format("%s=%s,%s", ldap.getIdAttribute(), svc.getId(), parentDn);
    }
}
