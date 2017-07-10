package org.apereo.cas.authentication;

import org.apereo.cas.services.RegisteredService;

import java.util.Map;

/**
 * An encoder that defines how a CAS attribute
 * is to be encoded and signed in the CAS
 * validation response. The collection of
 * attributes should not be mangled with and
 * filtered. All attributes will be released.
 * It is up to the implementations
 * to decide which attribute merits encrypting.
 *
 * @author Misagh Moayyed
 * @since 4.1
 */
@FunctionalInterface
public interface ProtocolAttributeEncoder {

    /**
     * Encodes attributes that are ready to be released.
     * Typically, this method tries to ensure that the
     * PGT and the credential password are correctly encrypted
     * before they are released. Attributes should not be filtered
     * and removed and it is assumed that all will be returned
     * back to the service.
     * @param attributes The attribute collection that is ready to be released
     * @param service the requesting service for which attributes are to be encoded
     * @return collection of attributes after encryption ready for release.
     * @since 4.1
     */
    Map<String, Object> encodeAttributes(Map<String, Object> attributes, RegisteredService service);

}
