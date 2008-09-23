/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.adaptors.x509.authentication.handler.support;

import java.security.Principal;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.cas.adaptors.x509.authentication.principal.X509CertificateCredentials;
import org.jasig.cas.authentication.handler.AuthenticationException;
import org.jasig.cas.authentication.handler.support.AbstractPreAndPostProcessingAuthenticationHandler;
import org.jasig.cas.authentication.principal.Credentials;
import org.inspektr.common.ioc.annotation.NotNull;

/**
 * Authentication Handler that accepts X509 Certificiates, determines their
 * validity and ensures that they were issued by a trusted issuer. (targeted at
 * X509v3) Optionally checks KeyUsage extension in the user certificate
 * (container should do that too). Note that this handler trusts the servlet
 * container to do some initial checks like path validation. Deployers can
 * supply an optional pattern to match subject dns against to further restrict
 * certificates in case they are not using their own issuer. It's also possible
 * to specify a maximum pathLength for the SUPPLIED certificates. (note that
 * this does not include a pathLength check for the root certificate)
 * [PathLength is 0 for the CA certficate that issues the end-user certificate]
 *
 * @author Scott Battaglia
 * @author Jan Van der Velpen
 * @version $Revision$ $Date$
 * @since 3.0.4
 */
public class X509CredentialsAuthenticationHandler extends AbstractPreAndPostProcessingAuthenticationHandler {

   /** Default setting to limit the number of intermediate certificates. */
   private static final int DEFAULT_MAXPATHLENGTH = 1;

   /** Default setting whether to allow unspecified number of intermediate certificates. */
   private static final boolean DEFAULT_MAXPATHLENGTH_ALLOW_UNSPECIFIED = false;

   /** Default setting to check keyUsage extension. */
   private static final boolean DEFAULT_CHECK_KEYUSAGE = false;

   /** Default setting to force require "KeyUsage" extension. */
   private static final boolean DEFAULT_REQUIRE_KEYUSAGE = false;

   /** Default subject pattern match. */
   private static final Pattern DEFAULT_SUBJECT_DN_PATTERN = Pattern.compile(".*");

   /** Instance of Logging. */
   private final Log log = LogFactory.getLog(getClass());

   /** The compiled pattern supplied by the deployer. */
   @NotNull
   private Pattern regExTrustedIssuerDnPattern;

   /**
    * Deployer supplied setting for maximum pathLength in a SUPPLIED
    * certificate.
    */
   private int maxPathLength = DEFAULT_MAXPATHLENGTH;

   /**
    * Deployer supplied setting to allow unlimited pathLength in a SUPPLIED
    * certificate.
    */
   private boolean maxPathLength_allowUnspecified = DEFAULT_MAXPATHLENGTH_ALLOW_UNSPECIFIED;


   /** Deployer supplied setting to check the KeyUsage extension. */
   private boolean checkKeyUsage = DEFAULT_CHECK_KEYUSAGE;

   /**
    * Deployer supplied setting to force require the correct KeyUsage
    * extension.
    */
   private boolean requireKeyUsage = DEFAULT_REQUIRE_KEYUSAGE;

   /** The compiled pattern for trusted DN's supplied by the deployer. */
   @NotNull
   private Pattern regExSubjectDnPattern = DEFAULT_SUBJECT_DN_PATTERN;

   protected final boolean doAuthentication(final Credentials credentials)
       throws AuthenticationException {

       final X509CertificateCredentials x509Credentials = (X509CertificateCredentials) credentials;
       final X509Certificate[] certificates = x509Credentials
           .getCertificates();

       /*
        * the certificate that was fully authenticated succesfully will be set
        * as the user credentials for CAS last certificate that can be set is
        * the end-user certificate
        */
       X509Certificate certificateCredentialsCandidate = null;
       // flag to check whether a trusted issuer is in the certificate chain
       boolean hasTrustedIssuerInChain = false;

       /*
        * reverse transversal of certificates (should be from root to end-user
        * cert)
        */
       for (int i = (certificates.length - 1); i >= 0; i--) {
           final X509Certificate certificate = certificates[i];
           try {
               final Principal issuerPrincipal = certificate.getIssuerDN();
               // flag that is set when this cert is an end user cert (no CA
               // cert)
               boolean isEndUserCertificate = false;

               if (log.isDebugEnabled()) {
                   log.debug("--examining cert["
                       + certificate.getSerialNumber().toString() + "] "
                       + certificate.getSubjectDN() + "\"" + " from issuer \""
                       + issuerPrincipal.getName() + "\"");
               }

               // check basic validity of the current certificate
               certificate.checkValidity();
               log.debug("certificate is valid");

               // initial check for trusted issuer in certificate chain
               // final check is done outside for loop
               if (isCertificateFromTrustedIssuer(issuerPrincipal)) {
                   hasTrustedIssuerInChain = true;
                   log.debug("certificate was issued by trusted issuer");
               }

               // getBasicConstraints returns pathLenContraint which is
               // >=0 when this is a CA cert and -1 when it's not
               int pathLength = certificate.getBasicConstraints();
               if (pathLength != -1) {
                   log.debug("this is a CA certificate");

                   // check pathLength when CA cert
                   //if unlimited/unspecified and unlimited/unspecified not allowed: warn+stop
                   if (pathLength == Integer.MAX_VALUE && this.maxPathLength_allowUnspecified != true) {
                       if (log.isWarnEnabled()) {
                           log.warn("authentication failed; cert pathLength not specified"
                                   + " and unlimited/unspecified not allowed by config [see maxPathLength_allow_unlimited]");
                       }
                       return false;
                   //else if more than allowed length but not unlimited/unspecified: warn+stop
                   } else if (pathLength > this.maxPathLength && pathLength < Integer.MAX_VALUE) {
                       if (log.isWarnEnabled()) {
                           log.warn("authentication failed; cert pathLength ["
                               + pathLength
                               + "] is more than allowed by config ["
                               + this.maxPathLength + "]");
                       }
                       return false;
                   }
               } else {
                   isEndUserCertificate = true;
                   log.debug("this is an end-user certificate");
               }

               /*
                * set this certificate as the user credentials if there is an
                * issuer in the cert (always so if valid cert) and this is an
                * end-user or CA certificate (so not a CA cert) and optional
                * KeyUsage check
                */
               if (issuerPrincipal != null
                   && isEndUserCertificate
                   && this.doesCertificateSubjectDnMatchPattern(certificate
                       .getSubjectDN())
                   && (!this.checkKeyUsage || (this.checkKeyUsage && this
                       .doesCertificateKeyUsageMatch(certificate)))) {

                   if (log.isDebugEnabled()) {
                       log.debug("cert["
                           + certificate.getSerialNumber().toString()
                           + "] ok, setting as credentials candidate");
                   }
                   certificateCredentialsCandidate = certificate;
               }
           } catch (final CertificateExpiredException e) {
               log.warn("authentication failed; certficiate expired ["
                   + certificate.toString() + "]");
               certificateCredentialsCandidate = null;
           } catch (final CertificateNotYetValidException e) {
               log.warn("authentication failed; certficate not yet valid ["
                   + certificate.toString() + "]");
               certificateCredentialsCandidate = null;
           }
       }

       // check whether one of the certificates in the chain was
       // from the trusted issuer; else => fail auth
       if (certificateCredentialsCandidate != null && hasTrustedIssuerInChain) {
           if (log.isInfoEnabled()) {
               log
                   .info("authentication OK; SSL client authentication data meets criteria for cert["
                       + certificateCredentialsCandidate.getSerialNumber()
                           .toString() + "]");
           }
           x509Credentials.setCertificate(certificateCredentialsCandidate);
           return true;
       }

       if (log.isInfoEnabled()) {
           if (!hasTrustedIssuerInChain) {
               log.info("client cert did not have trusted issuer pattern \""
                   + this.regExTrustedIssuerDnPattern.pattern()
                   + "\" in chain; authentication failed");
           } else {
               log
                   .info("authentication failed; SSL client authentication data doesn't meet criteria");
           }
       }
       return false;
   }

   public void setTrustedIssuerDnPattern(final String trustedIssuerDnPattern) {
       this.regExTrustedIssuerDnPattern = Pattern
       .compile(trustedIssuerDnPattern);
   }

   /**
    * @param maxPathLength The maxPathLength to set.
    */
   public void setMaxPathLength(int maxPathLength) {
       this.maxPathLength = maxPathLength;
   }

   /**
    * @param maxPathLength_allowUnspecified Allow CA certs to have unlimited intermediate certs (default=false).
    */
   public void setMaxPathLengthAllowUnspecified(boolean maxPathLength_allowUnspecified) {
       this.maxPathLength_allowUnspecified = maxPathLength_allowUnspecified;
   }

   /**
    * @param checkKeyUsage The checkKeyUsage to set.
    */
   public void setCheckKeyUsage(boolean checkKeyUsage) {
       this.checkKeyUsage = checkKeyUsage;
   }

   /**
    * @param requireKeyUsage The requireKeyUsage to set.
    */
   public void setRequireKeyUsage(boolean requireKeyUsage) {
       this.requireKeyUsage = requireKeyUsage;
   }

   public void setSubjectDnPattern(final String subjectDnPattern) {
       this.regExSubjectDnPattern = Pattern.compile(subjectDnPattern);
   }

   private boolean doesCertificateKeyUsageMatch(
       final X509Certificate certificate) {
       final String extensionOID = "2.5.29.15";
       final boolean keyUsage[] = certificate.getKeyUsage();
       /*
        * KeyUsage ::= BIT STRING { digitalSignature (0), nonRepudiation (1),
        * keyEncipherment (2), dataEncipherment (3), keyAgreement (4),
        * keyCertSign (5), cRLSign (6), encipherOnly (7), decipherOnly (8) }
        */

       if (keyUsage == null) {
           log.warn("isKeyUsageRequired?: " + this.requireKeyUsage
               + "; keyUsage not found.");
           return !this.requireKeyUsage;
       }

       log.debug("keyUsage extension found: examing...");

       if (!isExtensionMarkedCritical(certificate, extensionOID)
           && !this.requireKeyUsage) {
           log
               .debug("match ok; keyUsage extension not critical and not required so not checked");
           return true;
       }

       if (log.isDebugEnabled()) {
           log
               .debug("extension is marked critical in cert OR required by config"
                   + "[critical="
                   + isExtensionMarkedCritical(certificate, extensionOID)
                   + ";required=" + this.requireKeyUsage + "]");
       }

       // we need digitalSignature for SSL client auth
       if (keyUsage[0]) {
           log.debug("match ok; keyUsage extension OK");
           return true;
       }

       if (log.isWarnEnabled() && this.requireKeyUsage) {
           log.warn("match error; required/critical keyUsage extension fails"
               + "[critical="
               + isExtensionMarkedCritical(certificate, extensionOID)
               + ";required=" + this.requireKeyUsage + "]");
       }
       return false;
   }

   private boolean isExtensionMarkedCritical(
       final X509Certificate certificate, final String oid) {
       final Set<String> criticalOids = certificate.getCriticalExtensionOIDs();

       if (criticalOids == null || criticalOids.isEmpty()) {
           return false;
       }

       return criticalOids.contains(oid);
   }

   private boolean doesCertificateSubjectDnMatchPattern(
       final Principal principal) {
       return doesNameMatchPattern(principal, this.regExSubjectDnPattern);
   }

   private boolean isCertificateFromTrustedIssuer(final Principal principal) {
       return doesNameMatchPattern(principal, this.regExTrustedIssuerDnPattern);
   }

   private boolean doesNameMatchPattern(final Principal principal,
       final Pattern pattern) {
       final boolean result = pattern.matcher(principal.getName()).matches();

       if (log.isDebugEnabled()) {
           log.debug("Pattern Match: " + result + " [" + principal.getName()
               + "] against [" + pattern.pattern() + "].");
       }

       return result;
   }

   public boolean supports(final Credentials credentials) {
       return credentials != null
           && X509CertificateCredentials.class.isAssignableFrom(credentials
               .getClass());
   }
}
