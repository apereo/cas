package org.apereo.cas.adaptors.ldap.services;

import org.apereo.cas.configuration.model.support.ldap.serviceregistry.LdapServiceRegistryProperties;
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
                svc.setId(System.currentTimeMillis());
            }
            final String newDn = getDnForRegisteredService(dn, svc);
            LOGGER.debug("Creating entry [{}]", newDn);
            
            final Collection<LdapAttribute> attrs = new ArrayList<>();
            attrs.add(new LdapAttribute(ldap.getIdAttribute(), String.valueOf(svc.getId())));

            final StringWriter writer = new StringWriter();
            this.jsonSerializer.to(writer, svc);
            attrs.add(new LdapAttribute(ldap.getServiceDefinitionAttribute(), writer.toString()));
            attrs.add(new LdapAttribute(LdapUtils.OBJECT_CLASS_ATTRIBUTE, "top", ldap.getObjectClass()));

            return new LdapEntry(newDn, attrs);
        } catch (final Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public RegisteredService mapToRegisteredService(final LdapEntry entry) {
        try {
            final String value = LdapUtils.getString(entry, ldap.getServiceDefinitionAttribute());
            if (StringUtils.hasText(value)) {
                return this.jsonSerializer.from(value);
            }

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


    public void setJsonSerializer(final StringSerializer<RegisteredService> jsonSerializer) {
        this.jsonSerializer = jsonSerializer;
    }

    @Override
    public String getDnForRegisteredService(final String parentDn, final RegisteredService svc) {
        return String.format("%s=%s,%s", ldap.getIdAttribute(), svc.getId(), parentDn);
    }
}
