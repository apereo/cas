package org.jasig.cas.adaptors.ldap.services;

import org.jasig.cas.services.AbstractRegisteredService;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.util.JsonSerializer;
import org.jasig.cas.util.LdapUtils;
import org.jasig.cas.util.services.RegisteredServiceJsonSerializer;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Default implementation of {@link LdapRegisteredServiceMapper} that is able
 * to map ldap entries to {@link RegisteredService} instances based on
 * certain attributes names. This implementation also respects the object class
 * attribute of LDAP entries via {@link LdapUtils#OBJECTCLASS_ATTRIBUTE}.
 * @author Misagh Moayyed
 * @since 4.1.0
 */
@RefreshScope
@Component("ldapServiceRegistryMapper")
public class DefaultLdapRegisteredServiceMapper implements LdapRegisteredServiceMapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultLdapRegisteredServiceMapper.class);

    
    private JsonSerializer<RegisteredService> jsonSerializer = new RegisteredServiceJsonSerializer();

    
    @Value("${ldap.svc.reg.map.objclass:casRegisteredService}")
    private String objectClass = "casRegisteredService";

    
    @Value("${ldap.svc.reg.map.attr.id:uid}")
    private String idAttribute = "uid";

    
    @Value("${ldap.svc.reg.map.attr.svc:description}")
    private String serviceDefinitionAttribute = "description";

    @Override
    public LdapEntry mapFromRegisteredService(final String dn, final RegisteredService svc) {
        try {
            if (svc.getId() == RegisteredService.INITIAL_IDENTIFIER_VALUE) {
                ((AbstractRegisteredService) svc).setId(System.nanoTime());
            }
            final String newDn = getDnForRegisteredService(dn, svc);
            LOGGER.debug("Creating entry {}", newDn);

            final Collection<LdapAttribute> attrs = new ArrayList<>();
            attrs.add(new LdapAttribute(this.idAttribute, String.valueOf(svc.getId())));

            final StringWriter writer = new StringWriter();
            this.jsonSerializer.toJson(writer, svc);
            attrs.add(new LdapAttribute(this.serviceDefinitionAttribute, writer.toString()));
            attrs.add(new LdapAttribute(LdapUtils.OBJECTCLASS_ATTRIBUTE, "top", this.objectClass));

            return new LdapEntry(newDn, attrs);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public RegisteredService mapToRegisteredService(final LdapEntry entry) {
        try {
            final String value = LdapUtils.getString(entry, this.serviceDefinitionAttribute);
            if (StringUtils.hasText(value)) {
                final RegisteredService service = this.jsonSerializer.fromJson(value);
                return service;
            }

            return null;
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getObjectClass() {
        return this.objectClass;
    }

    public void setObjectClass(final String objectClass) {
        this.objectClass = objectClass;
    }

    @Override
    public String getIdAttribute() {
        return this.idAttribute;
    }

    public void setIdAttribute(final String idAttribute) {
        this.idAttribute = idAttribute;
    }

    public String getServiceDefinitionAttribute() {
        return serviceDefinitionAttribute;
    }

    public void setServiceDefinitionAttribute(final String serviceDefinitionAttribute) {
        this.serviceDefinitionAttribute = serviceDefinitionAttribute;
    }

    public void setJsonSerializer(final JsonSerializer<RegisteredService> jsonSerializer) {
        this.jsonSerializer = jsonSerializer;
    }

    @Override
    public String getDnForRegisteredService(final String parentDn, final RegisteredService svc) {
        return String.format("%s=%s,%s", this.idAttribute, svc.getId(), parentDn);
    }
    
}
