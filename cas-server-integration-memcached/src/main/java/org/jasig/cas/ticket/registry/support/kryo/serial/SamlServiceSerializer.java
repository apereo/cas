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
import org.jasig.cas.authentication.principal.SamlService;
import org.jasig.cas.ticket.registry.support.kryo.FieldHelper;
import org.jasig.cas.util.HttpClient;

import java.lang.reflect.Constructor;
import java.nio.ByteBuffer;

/**
 * Description of SamlServiceSerializer.
 *
 * @author Middleware Services
 * @version $Revision: $
 */
public class SamlServiceSerializer extends AbstractWebApplicationServiceSerializer<SamlService> {
    
    private static final Constructor CONSTRUCTOR;
    
    static {
        try {
            CONSTRUCTOR = SamlService.class.getDeclaredConstructor(
                    String.class, String.class, String.class, HttpClient.class, String.class);
            CONSTRUCTOR.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("Expected constructor signature not found.", e);
        }
    }

    public SamlServiceSerializer(final Kryo kryo, final FieldHelper helper) {
        super(kryo, helper);
    }

    public void write(final ByteBuffer buffer, final SamlService service) {
        super.write(buffer, service);
        kryo.writeObject(buffer, service.getRequestID());
    }

    protected SamlService createService(
            final ByteBuffer buffer,
            final String id,
            final String originalUrl,
            final String artifactId) {

        final String requestId = kryo.readObject(buffer, String.class);
        try {
            return (SamlService) CONSTRUCTOR.newInstance(id, originalUrl, artifactId, new HttpClient(), requestId);
        } catch (Exception e) {
            throw new IllegalStateException("Error creating SamlService", e);
        }
    }
}
