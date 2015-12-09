/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jasig.cas.authentication.support;

import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapException;
import org.ldaptive.auth.FormatDnResolver;
import org.jasig.cas.web.flow.AuthUtils;


public final class WavityFormatDnResolver extends FormatDnResolver {

    /** Default constructor. */
    public WavityFormatDnResolver() {}


    /**
     * Creates a new format DN resolver.
     *
     * @param  format  formatter string
     */
    public WavityFormatDnResolver(final String format) {
      super(format);
    }


    /**
     * Creates a new format DN resolver with the supplied format and arguments.
     *
     * @param  format  to set formatter string
     * @param  args  to set formatter arguments
     */
    public WavityFormatDnResolver(final String format, final Object[] args) {
      super(format,args);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String resolve(String encodedUser) throws LdapException {
        String dn = null;
        Object[] formatArgs = getFormatArgs();
        String formatString = getFormat();
        boolean escapeUser = getEscapeUser();

//        String[] values = encodedUser.split(":");
//        if(values.length!=2)
//        {
//            throw new LdapException("Invalid value for the user");
//        }

        String user = encodedUser;
        String tenantId =  AuthUtils.getTenantId();

        if (user != null && !"".equals(user)) {
          final String escapedUser = escapeUser ? LdapAttribute.escapeValue(user)
                                                : user;
          logger.debug("Formatting DN for {} with {}", escapedUser,tenantId, formatString);
          if (formatArgs != null && formatArgs.length > 0) {
            final Object[] args = new Object[formatArgs.length + 1];
            args[0] = escapedUser;
            System.arraycopy(formatArgs, 0, args, 1, formatArgs.length);
            dn = String.format(formatString, args);
          } else {
            dn = String.format(formatString, escapedUser,tenantId);
          }
        } else {
          logger.debug("User input was empty or null");
        }
        return dn;
    }
}
