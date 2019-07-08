package org.apereo.cas.util.jmx;

import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;

/**
 * This is {@link ExportableManagedResource}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@FunctionalInterface
@ManagedResource
public interface ExportableManagedResource {

    @ManagedOperation
    Object exportManagedResource();
}                
