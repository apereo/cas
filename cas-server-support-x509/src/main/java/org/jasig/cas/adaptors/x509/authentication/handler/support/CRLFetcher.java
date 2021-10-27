package org.jasig.cas.adaptors.x509.authentication.handler.support;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.security.cert.X509CRL;
import java.util.Set;

/**
 * Defines operations needed to a fetch a CRL.
 * @author Misagh Moayyed
 * @since 4.1
 */
public interface CRLFetcher {
    /**
     * Fetches a collection of crls from the specified resources
     * and returns a map of CRLs each tracked by its url.
     * @param crls resources to retrieve
     * @return map of crl entries and their urls
     * @throws Exception the exception thrown if resources cant be fetched
     */
    Set<X509CRL> fetch(@NotNull @Size(min=1)  Set<? extends Object> crls) throws Exception;

    /**
     * Fetches a single of crl from the specified resource
     * and returns it.
     * @param crl resources to retrieve
     * @return the CRL entry
     * @throws Exception the exception thrown if resources cant be fetched
     */
    X509CRL fetch(@NotNull Object crl) throws Exception;
}
