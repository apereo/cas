/*
  $Id: $

  Copyright (C) 2012 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: $
  Updated: $Date: $
*/
package org.jasig.cas.ticket.registry.support.kryo.serial;

import com.esotericsoftware.kryo.Kryo;
import org.jasig.cas.authentication.ImmutableAuthentication;
import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.ticket.registry.support.kryo.FieldHelper;

import java.util.Date;
import java.util.Map;

/**
 * Description of ImmutableAuthenticationSerializer.
 *
 * @author Middleware Services
 * @version $Revision: $
 */
public class ImmutableAuthenticationSerializer extends AbstractAuthenticationSerializer<ImmutableAuthentication> {

    private final FieldHelper fieldHelper;

    public ImmutableAuthenticationSerializer(final Kryo kryo, final FieldHelper helper) {
        super(kryo);
        this.fieldHelper = helper;
    }

    protected ImmutableAuthentication createAuthentication(
            final Date authDate, final Principal principal, final Map<String, Object> attributes) {
        final ImmutableAuthentication auth = new ImmutableAuthentication(principal, attributes);
        fieldHelper.setFieldValue(auth, "authenticatedDate", authDate);
        return auth;
    }
}
