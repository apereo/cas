package org.apereo.cas.adaptors.x509.authentication.principal;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.CoreAuthenticationUtils;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.resolvers.PrincipalResolutionContext;
import org.apereo.cas.configuration.model.core.authentication.PrincipalAttributesCoreProperties;
import org.apereo.cas.util.CollectionUtils;

import lombok.val;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.core.io.ClassPathResource;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

/**
 * Unit test for {@link X509SubjectPrincipalResolver}.
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
@Tag("X509")
public class X509SubjectPrincipalResolverTests {

    /**
     * Gets the unit test parameters.
     *
     * @return Test parameter data.
     */
    public static Stream<Arguments> getTestParameters() throws IOException {
        return Stream.of(
            /* Test case #1
             * Use CN for principal ID
             */
            arguments(
                new ClassPathResource("x509-ctop-resolver-hizzy.crt").getFile().getCanonicalPath(),
                "$CN",
                "Hizzogarthington I.S. Pleakinsense"
            ),

            /* Test case #2
             * Use email address for principal ID
             */
            arguments(
                new ClassPathResource("x509-ctop-resolver-hizzy.crt").getFile().getCanonicalPath(),
                "$EMAILADDRESS",
                "hizzy@vt.edu"
            ),

            /* Test case #2
             * Use combination of ou and cn for principal ID
             */
            arguments(
                new ClassPathResource("x509-ctop-resolver-hizzy.crt").getFile().getCanonicalPath(),
                "$OU $CN",
                "Middleware Hizzogarthington I.S. Pleakinsense"
            ),

            /* Test case #3
             * Use combination of serial number and cn for principal ID
             */
            arguments(
                new ClassPathResource("x509-ctop-resolver-gazzo.crt").getFile().getCanonicalPath(),
                "$CN:$SERIALNUMBER",
                "Gazzaloddi P. Wishwashington:271828183"
            ),

            /* Test case #4
             * Build principal ID from multivalued attributes
             */
            arguments(
                new ClassPathResource("x509-ctop-resolver-jacky.crt").getFile().getCanonicalPath(),
                "$UID@$DC.$DC",
                "jacky@vt.edu"
            )
        );
    }

    @ParameterizedTest
    @MethodSource("getTestParameters")
    public void verifyResolvePrincipalInternal(final String certPath,
                                               final String descriptor,
                                               final String expectedResult) throws Exception {

        val context = PrincipalResolutionContext.builder()
            .attributeMerger(CoreAuthenticationUtils.getAttributeMerger(PrincipalAttributesCoreProperties.MergingStrategyTypes.REPLACE))
            .attributeRepository(CoreAuthenticationTestUtils.getAttributeRepository())
            .principalFactory(PrincipalFactoryUtils.newPrincipalFactory())
            .returnNullIfNoAttributes(false)
            .principalNameTransformer(formUserId -> formUserId)
            .useCurrentPrincipalId(false)
            .resolveAttributes(true)
            .activeAttributeRepositoryIdentifiers(CollectionUtils.wrapSet(IPersonAttributeDao.WILDCARD))
            .build();

        val resolver = new X509SubjectPrincipalResolver(context);
        resolver.setPrincipalDescriptor(descriptor);
        resolver.setX509AttributeExtractor(new DefaultX509AttributeExtractor());
        val certificate = (X509Certificate) CertificateFactory.getInstance("X509").generateCertificate(
            new FileInputStream(certPath));

        assertEquals(expectedResult, resolver.resolvePrincipalInternal(certificate));
    }

}
