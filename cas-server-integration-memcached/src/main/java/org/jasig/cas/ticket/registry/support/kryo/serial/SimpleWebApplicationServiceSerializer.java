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
import org.jasig.cas.authentication.principal.SimpleWebApplicationServiceImpl;
import org.jasig.cas.util.HttpClient;

import java.nio.ByteBuffer;

/**
 * Description of SimpleWebApplicationServiceSerializer.
 *
 * @author Middleware Services
 * @version $Revision: $
 */
public class SimpleWebApplicationServiceSerializer extends SimpleSerializer<SimpleWebApplicationServiceImpl> {
    protected final Kryo kryo;

    public SimpleWebApplicationServiceSerializer(final Kryo kryo) {
        this.kryo = kryo;
    }

    public void write(final ByteBuffer buffer, final SimpleWebApplicationServiceImpl service) {
        kryo.writeObjectData(buffer, service.getId());
    }

    public SimpleWebApplicationServiceImpl read(final ByteBuffer buffer) {
        return new SimpleWebApplicationServiceImpl(kryo.readObjectData(buffer, String.class), new HttpClient());
    }
}
