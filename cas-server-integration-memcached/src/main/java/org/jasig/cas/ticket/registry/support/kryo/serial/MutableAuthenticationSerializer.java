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
import org.jasig.cas.authentication.MutableAuthentication;
import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.ticket.registry.support.kryo.FieldHelper;

import java.util.Date;
import java.util.Map;

/**
 * Description of MutableAuthenticationSerializer.
 *
 * @author Middleware Services
 * @version $Revision: $
 */
public class MutableAuthenticationSerializer extends AbstractAuthenticationSerializer<MutableAuthentication> {

    private final FieldHelper fieldHelper;

    public MutableAuthenticationSerializer(final Kryo kryo, final FieldHelper helper) {
        super(kryo);
        this.fieldHelper = helper;
    }
    
    protected MutableAuthentication createAuthentication(
            final Date authDate, final Principal principal, final Map<String, Object> attributes) {
        final MutableAuthentication auth = new MutableAuthentication(principal, authDate);
        fieldHelper.setFieldValue(auth, "attributes", attributes);
        return auth;
    }
}
