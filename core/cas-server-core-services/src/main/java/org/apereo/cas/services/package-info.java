/**
 * <p>This package is contains classes related to the restriction of CAS
 * usage to a particular set of services. This is accomplished via a
 * combination of registries and interceptors.</p>
 * <p>The ServiceRegistry, with its default implementation of
 * DefaultServiceRegistry contains the list of RegisteredServices allowed
 * to access CAS. This list is periodically refreshed via the
 * ServiceRegistryReloader.</p>
 * <p>CAS itself is protected by a group of interceptors found in the
 * subpackage advice.</p>
 * @since 3.0
 */
package org.apereo.cas.services;

