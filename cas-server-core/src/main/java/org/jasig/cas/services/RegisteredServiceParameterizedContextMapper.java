/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.services;

import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.simple.AbstractParameterizedContextMapper;
import org.springframework.ldap.core.simple.ParameterizedContextMapper;

/**
 * Implementation of {@link ParameterizedContextMapper} that maps a row to a RegisteredService.
 * 
 * @author Scott Battaglia
 * @version $Revision: 1.1 $ $Date: 2005/08/19 18:27:17 $
 * @since 3.1.2
 *
 */
public final class RegisteredServiceParameterizedContextMapper extends AbstractParameterizedContextMapper<RegisteredService> {

    private boolean getBooleanAttribute(final DirContextOperations context, final String attributeName, final boolean defaultValue) {
        final String value = context.getStringAttribute("attributeName");
        return value == null ? defaultValue : Boolean.parseBoolean(value);
    }
    
    protected RegisteredService doMapFromContext(final DirContextOperations context) {
        final RegisteredServiceImpl rs = new RegisteredServiceImpl();
        
        rs.setAllowedAttributes(context.getStringAttributes("berkeleyEduCasServiceAllowedAttributes"));
        rs.setAllowedToProxy(getBooleanAttribute(context, "berkeleyEduCasServiceAllowedToProxy", true));
        rs.setAnonymousAccess(getBooleanAttribute(context, "berkeleyEduCasServiceAnnonymousAccess", false));
        rs.setDescription(context.getStringAttribute("berkeleyEduCasServiceDescription"));
        rs.setEnabled(getBooleanAttribute(context, "berkeleyEduCasServiceEnabled", true));
        rs.setId(Long.parseLong(context.getStringAttribute("uid")));
        rs.setName(context.getStringAttribute("cn"));
        rs.setServiceId(context.getStringAttribute("berkeleyEduCasServiceUrl"));
        rs.setSsoEnabled(getBooleanAttribute(context, "berkeleyEduCasServiceSsoEnabled", true));
        rs.setTheme(context.getStringAttribute("berkeleyEduCasServiceTheme"));

        return rs;
    }
}
