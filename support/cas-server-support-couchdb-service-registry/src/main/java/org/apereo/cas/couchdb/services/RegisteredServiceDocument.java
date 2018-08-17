package org.apereo.cas.couchdb.services;

import org.apereo.cas.services.RegisteredService;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.ektorp.support.CouchDbDocument;

/**
 * This is {@link RegisteredServiceDocument}. Wraps a {@link RegisteredService} for use with CouchDB.
 *
 * @author Timur Duehr
 * @since 5.3.0
 */
@Getter
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@NoArgsConstructor
public class RegisteredServiceDocument extends CouchDbDocument {
    private static final long serialVersionUID = -6787906520673248670L;
    private RegisteredService service;

    public RegisteredServiceDocument(final RegisteredService service) {
        this.setId(String.valueOf(service.getId()));
        this.service = service;
    }
}
