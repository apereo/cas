package org.apereo.cas.adaptors.x509.authentication;

import org.springframework.core.io.Resource;

import javax.validation.constraints.Size;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.security.cert.CRLException;
import java.security.cert.CertificateException;
import java.security.cert.X509CRL;
import java.util.Collection;

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
     * @throws IOException the exception thrown if resources cant be fetched
     * @throws CRLException the exception thrown if resources cant be fetched
     */
    Collection<X509CRL> fetch(@Size(min=1) Collection<Resource> crls) throws IOException, CRLException;

    /**
     * Fetches a single of crl from the specified resource
     * and returns it.
     * @param crl resources to retrieve
     * @return the CRL entry
     * @throws IOException the exception thrown if resources cant be fetched
     * @throws CRLException the exception thrown if resources cant be fetched
     * @throws CertificateException the exception thrown if resources cant be fetched
     */
    X509CRL fetch(String crl) throws IOException, CRLException, CertificateException;

    /**
     * Fetches a single of crl from the specified resource
     * and returns it.
     * @param crl resources to retrieve
     * @return the CRL entry
     * @throws IOException the exception thrown if resources cant be fetched
     * @throws CRLException the exception thrown if resources cant be fetched
     * @throws CertificateException the exception thrown if resources cant be fetched
     */
    X509CRL fetch(URI crl) throws IOException, CRLException, CertificateException;

    /**
     * Fetches a single of crl from the specified resource
     * and returns it.
     * @param crl resources to retrieve
     * @return the CRL entry
     * @throws IOException the exception thrown if resources cant be fetched
     * @throws CRLException the exception thrown if resources cant be fetched
     * @throws CertificateException the exception thrown if resources cant be fetched
     */
    X509CRL fetch(URL crl) throws IOException, CRLException, CertificateException;

    /**
     * Fetches a single of crl from the specified resource
     * and returns it.
     * @param crl resources to retrieve
     * @return the CRL entry
     * @throws IOException the exception thrown if resources cant be fetched
     * @throws CRLException the exception thrown if resources cant be fetched
     * @throws CertificateException the exception thrown if resources cant be fetched
     */
    X509CRL fetch(Resource crl) throws IOException, CRLException, CertificateException;
}
