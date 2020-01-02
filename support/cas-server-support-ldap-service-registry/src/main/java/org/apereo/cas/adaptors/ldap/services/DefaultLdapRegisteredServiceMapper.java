package org.apereo.cas.adaptors.ldap.services;

import org.apereo.cas.configuration.model.support.ldap.serviceregistry.LdapServiceRegistryProperties;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.util.RegisteredServiceJsonSerializer;
import org.apereo.cas.util.LdapUtils;
import org.apereo.cas.util.serialization.StringSerializer;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;
import org.springframework.util.StringUtils;

import java.io.StringWriter;
import java.util.ArrayList;

/**
 * Default implementation of {@link LdapRegisteredServiceMapper} that is able
 * to map ldap entries to {@link RegisteredService} instances based on
 * certain attributes names. This implementation also respects the object class
 * attribute of LDAP entries via {@link LdapUtils#OBJECT_CLASS_ATTRIBUTE}.
 *
 * @author Misagh Moayyed
 * @since 4.1.0
 */
@Slf4j
public class DefaultLdapRegisteredServiceMapper implements LdapRegisteredServiceMapper {

    private final LdapServiceRegistryProperties ldap;

    private final StringSerializer<RegisteredService> jsonSerializer = new RegisteredServiceJsonSerializer();

    public DefaultLdapRegisteredServiceMapper(final LdapServiceRegistryProperties ldapProperties) {
        ldap = ldapProperties;
    }

    @Override
    @SneakyThrows
    public LdapEntry mapFromRegisteredService(final String dn, final RegisteredService svc) {

        if (svc.getId() == RegisteredService.INITIAL_IDENTIFIER_VALUE) {
            svc.setId(System.currentTimeMillis());
        }
        val newDn = getDnForRegisteredService(dn, svc);
        LOGGER.debug("Creating entry DN [{}]", newDn);

        val attrs = new ArrayList<LdapAttribute>(3);
        attrs.add(new LdapAttribute(ldap.getIdAttribute(), String.valueOf(svc.getId())));

        try (val writer = new StringWriter()) {
            this.jsonSerializer.to(writer, svc);
            attrs.add(new LdapAttribute(ldap.getServiceDefinitionAttribute(), writer.toString()));
            attrs.add(new LdapAttribute(LdapUtils.OBJECT_CLASS_ATTRIBUTE, "top", ldap.getObjectClass()));
        }
        LOGGER.debug("LDAP attributes assigned to the DN [{}] are [{}]", newDn, attrs);

        val entry = LdapEntry.builder().dn(newDn).attributes(attrs).build();
        LOGGER.debug("Created LDAP entry [{}]", entry);
        return entry;

    }

    @Override
    @SneakyThrows
    public RegisteredService mapToRegisteredService(final LdapEntry entry) {

        val value = LdapUtils.getString(entry, ldap.getServiceDefinitionAttribute());
        if (StringUtils.hasText(value)) {
            LOGGER.debug("Transforming LDAP entry [{}] into registered service definition", entry);
            return this.jsonSerializer.from(value);
        }
        LOGGER.warn("LDAP entry [{}] is not assigned the service definition attribute [{}] and will be ignored",
            entry, ldap.getServiceDefinitionAttribute());
        return null;
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
