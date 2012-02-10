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
import org.jasig.cas.authentication.principal.GoogleAccountsService;
import org.jasig.cas.ticket.registry.support.kryo.FieldHelper;

import java.lang.reflect.Constructor;
import java.nio.ByteBuffer;
import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * Description of SamlServiceSerializer.
 *
 * @author Middleware Services
 * @version $Revision: $
 */
public class GoogleAccountsServiceSerializer extends AbstractWebApplicationServiceSerializer<GoogleAccountsService> {

    private static final Constructor CONSTRUCTOR;

    private PrivateKey privateKey;
    
    private PublicKey publicKey;
    
    private String alternateUsername;
            
    static {
        try {
            CONSTRUCTOR = GoogleAccountsService.class.getDeclaredConstructor(
                    String.class,
                    String.class,
                    String.class,
                    String.class,
                    String.class,
                    PrivateKey.class,
                    PublicKey.class,
                    String.class);
            CONSTRUCTOR.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("Expected constructor signature not found.", e);
        }
    }

    public GoogleAccountsServiceSerializer(
            final Kryo kryo,
            final FieldHelper helper,
            final PublicKey publicKey,
            final PrivateKey privateKey,
            final String alternateUsername) {

        super(kryo, helper);
        this.publicKey = publicKey;
        this.privateKey = privateKey;
        this.alternateUsername = alternateUsername;
    }

    public void write(final ByteBuffer buffer, final GoogleAccountsService service) {
        super.write(buffer, service);
        kryo.writeObject(buffer, fieldHelper.getFieldValue(service, "requestId"));
        kryo.writeObject(buffer, fieldHelper.getFieldValue(service, "relayState"));
    }

    protected GoogleAccountsService createService(
            final ByteBuffer buffer,
            final String id,
            final String originalUrl,
            final String artifactId) {

        final String requestId = kryo.readObject(buffer, String.class);
        final String relayState = kryo.readObject(buffer, String.class);
        try {
            final GoogleAccountsService service = (GoogleAccountsService) CONSTRUCTOR.newInstance(
                    id,
                    originalUrl,
                    artifactId,
                    relayState,
                    requestId,
                    privateKey,
                    publicKey,
                    alternateUsername);
            return service;
        } catch (Exception e) {
            throw new IllegalStateException("Error creating SamlService", e);
        }
    }
}
