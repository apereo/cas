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
import org.jasig.cas.authentication.principal.SimplePrincipal;

import java.nio.ByteBuffer;

/**
 * Description of SimplePrincipalSerializer.
 *
 * @author Middleware Services
 * @version $Revision: $
 */
public class SimplePrincipalSerializer extends SimpleSerializer<SimplePrincipal> {
    private final Kryo kryo;

    private final AttributeMapSerializer attrSerializer;

    public SimplePrincipalSerializer(final Kryo kryo) {
        this.kryo = kryo;
        attrSerializer = new AttributeMapSerializer(kryo);
    }

    public void write(final ByteBuffer buffer, final SimplePrincipal principal) {
        kryo.writeObjectData(buffer, principal.getId());
        attrSerializer.write(buffer, principal.getAttributes());
    }

    public SimplePrincipal read(final ByteBuffer buffer) {
        return new SimplePrincipal(
                kryo.readObjectData(buffer, String.class),
                attrSerializer.read(buffer));
    }
}
