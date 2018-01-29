package org.apereo.cas.support.saml.web.idp.profile.sso.request;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.shibboleth.utilities.java.support.xml.ParserPool;
import org.apache.commons.lang3.tuple.Pair;
import org.apereo.inspektr.audit.annotation.Audit;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.messaging.decoder.servlet.BaseHttpServletRequestXMLMessageDecoder;
import org.opensaml.saml.common.SignableSAMLObject;

import javax.servlet.http.HttpServletRequest;

/**
 * This is {@link DefaultSSOSamlHttpRequestExtractor}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
@RequiredArgsConstructor
public class DefaultSSOSamlHttpRequestExtractor implements SSOSamlHttpRequestExtractor {
    /**
     * The Parser pool.
     */
    protected final ParserPool parserPool;

    @Audit(action = "SAML2_REQUEST",
        actionResolverName = "SAML2_REQUEST_ACTION_RESOLVER",
        resourceResolverName = "SAML2_REQUEST_RESOURCE_RESOLVER")
    @Override
    @SneakyThrows
    public Pair<? extends SignableSAMLObject, MessageContext> extract(final HttpServletRequest request,
                                                                      final BaseHttpServletRequestXMLMessageDecoder decoder,
                                                                      final Class<? extends SignableSAMLObject> clazz) {
        LOGGER.info("Received SAML profile request [{}]", request.getRequestURI());
        decoder.setHttpServletRequest(request);
        decoder.setParserPool(this.parserPool);
        decoder.initialize();
        decoder.decode();

        final MessageContext messageContext = decoder.getMessageContext();
        LOGGER.debug("Locating SAML object from message context...");
        @NonNull
        final SignableSAMLObject object = (SignableSAMLObject) messageContext.getMessage();
        if (!clazz.isAssignableFrom(object.getClass())) {
            throw new ClassCastException("SAML object [" + object.getClass().getName() + " type does not match " + clazz);
        }
        LOGGER.debug("Decoded SAML object [{}] from http request", object.getElementQName());
        return Pair.of(object, messageContext);
    }
}
