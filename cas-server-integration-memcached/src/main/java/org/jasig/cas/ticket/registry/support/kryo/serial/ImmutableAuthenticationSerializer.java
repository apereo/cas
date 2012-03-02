/*
 * Copyright 2012 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.jasig.org/cas/license.
 */
package org.jasig.cas.ticket.registry.support.kryo.serial;

import com.esotericsoftware.kryo.Kryo;
import org.jasig.cas.authentication.ImmutableAuthentication;
import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.ticket.registry.support.kryo.FieldHelper;

import java.util.Date;
import java.util.Map;

/**
 * Serializer for {@link ImmutableAuthentication}.
 *
 * @author Marvin S. Addison
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
