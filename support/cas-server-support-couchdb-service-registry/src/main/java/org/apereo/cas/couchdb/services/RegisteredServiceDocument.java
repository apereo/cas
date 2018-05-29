package org.apereo.cas.couchdb.services;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apereo.cas.services.RegisteredService;
import org.ektorp.support.CouchDbDocument;

/**
 * This is {@link RegisteredServiceDocument}. Wraps a {@link RegisteredService} for use with CouchDB.
 *
 * @author Timur Duehr
 * @since 5.3.0
 */
@Getter
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
@NoArgsConstructor
public class RegisteredServiceDocument extends CouchDbDocument {
    private RegisteredService service;

    public RegisteredServiceDocument(final RegisteredService service) {
        this.setId(String.valueOf(service.getId()));
        this.service = service;
    }
}
