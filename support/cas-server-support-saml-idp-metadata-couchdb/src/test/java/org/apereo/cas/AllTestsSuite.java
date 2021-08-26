
package org.apereo.cas;

import org.apereo.cas.support.saml.CouchDbSamlIdPMetadataDocumentTests;
import org.apereo.cas.support.saml.CouchDbSamlMetadataDocumentTests;
import org.apereo.cas.support.saml.idp.metadata.CouchDbSamlIdPMetadataGeneratorTests;
import org.apereo.cas.support.saml.metadata.resolver.CouchDbSamlRegisteredServiceMetadataResolverTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * This is {@link AllTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0-RC3
 */
@SelectClasses({
    CouchDbSamlMetadataDocumentTests.class,
    CouchDbSamlIdPMetadataDocumentTests.class,
    CouchDbSamlIdPMetadataGeneratorTests.class,
    CouchDbSamlRegisteredServiceMetadataResolverTests.class
})
@Suite
public class AllTestsSuite {
}
