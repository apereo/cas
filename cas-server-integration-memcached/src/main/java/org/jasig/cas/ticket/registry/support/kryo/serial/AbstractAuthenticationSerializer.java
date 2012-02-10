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
import com.esotericsoftware.kryo.serialize.SimpleSerializer;
import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.principal.Principal;

import java.nio.ByteBuffer;
import java.util.Date;
import java.util.Map;

/**
 * Description of AbstractAuthenticationSerializer.
 *
 * @author Middleware Services
 * @version $Revision: $
 */
public abstract class AbstractAuthenticationSerializer<T extends Authentication> extends SimpleSerializer<T> {

    private final Kryo kryo;

    private final AttributeMapSerializer attrSerializer;

    public AbstractAuthenticationSerializer(final Kryo kryo) {
        this.kryo = kryo;
        attrSerializer = new AttributeMapSerializer(kryo);
    }

    public void write(final ByteBuffer buffer, final T auth) {
        buffer.putLong(auth.getAuthenticatedDate().getTime());
        kryo.writeClassAndObject(buffer, auth.getPrincipal());
        attrSerializer.write(buffer, auth.getAttributes());
    }

    public T read(final ByteBuffer buffer) {
        return createAuthentication(
                new Date(buffer.getLong()),
                (Principal) kryo.readClassAndObject(buffer),
                attrSerializer.read(buffer));
    }

    protected abstract T createAuthentication(
            final Date authDate, final Principal p, final Map<String, Object> attributes);
}
