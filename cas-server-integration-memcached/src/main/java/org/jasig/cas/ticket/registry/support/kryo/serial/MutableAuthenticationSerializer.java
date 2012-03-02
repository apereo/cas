/*
 * Copyright 2012 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.jasig.org/cas/license.
 */
package org.jasig.cas.ticket.registry.support.kryo.serial;

import com.esotericsoftware.kryo.Kryo;
import org.jasig.cas.authentication.MutableAuthentication;
import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.ticket.registry.support.kryo.FieldHelper;

import java.util.Date;
import java.util.Map;

/**
 * Serializer for {@link MutableAuthentication} class.
 *
 * @author Marvin S. Addison
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
