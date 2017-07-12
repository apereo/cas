package org.apereo.cas.authentication.principal;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;

/**
 * This is {@link ResponseBuilderLocator} which attempts to locate {@link ResponseBuilder}
 * objects registered in the application context. This is an abstraction that is separated
 * from the actual service and response API to remove issues with serialization and such.
 * Services are no longer responsible for producing a response and their response builder
 * will need to be located via this class. This frees up the service API quite a bit to
 * inject all sorts of components into builders to accommodate for various use cases
 * and not have to worry about whether a given field in a builder is serialization friendly.
 *
 * @author Misagh Moayyed
 * @param <T> the type parameter
 * @since 5.1.0
 */
@FunctionalInterface
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
public interface ResponseBuilderLocator<T extends WebApplicationService> extends Serializable {

    /**
     * Locate response builder appropriate for the given service.
     *
     * @param service the service
     * @return the response builder
     */
    ResponseBuilder<T> locate(T service);
}
