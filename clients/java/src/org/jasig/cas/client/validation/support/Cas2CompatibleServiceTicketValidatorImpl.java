package org.jasig.cas.client.validation.support;

import java.io.StringReader;

import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.jasig.cas.client.receipt.CasReceipt;
import org.jasig.cas.client.receipt.CasResponseCasReceipt;
import org.jasig.cas.client.receipt.ErrorCasReceipt;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author Scott Battaglia
 * @version $Id$
 */
public class Cas2CompatibleServiceTicketValidatorImpl extends AbstractServiceTicketValidator {

    private static final String XML_CAS_AUTHENTICATION_FAILURE = "cas:authenticationFailure";

    private final HttpClient client = new HttpClient();

    /**
     * @see org.jasig.cas.client.validation.support.AbstractServiceTicketValidator#validateInternal(java.lang.String)
     */
    public CasReceipt validateInternal(String ticketId) {
        String response;
        GetMethod getMethod = new GetMethod(getCasValidateUrl().toString());
        getMethod.setQueryString(getNameValuePairs(ticketId));

        try {
            this.client.executeMethod(getMethod);
            response = getMethod.getResponseBodyAsString();

            return getCasReceipt(response);

        }
        catch (Exception e) {
            return new ErrorCasReceipt();
        }
    }

    protected NameValuePair[] getNameValuePairs(String ticketId) {
        NameValuePair pairs[] = new NameValuePair[4];

        pairs[0] = new NameValuePair("ticket", ticketId);
        pairs[1] = new NameValuePair("renew", Boolean.toString(this.isRenew()));
        pairs[2] = new NameValuePair("service", this.getService().toString());
        pairs[3] = new NameValuePair("pgtUrl", this.getProxyCallbackUrl().toString());
        return pairs;
    }

    protected CasReceipt getCasReceipt(String response) throws Exception {
        CasResponseCasReceipt casReceipt = new CasResponseCasReceipt();
        XMLReader xmlReader = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
        xmlReader.setFeature("http://xml.org/sax/features/namespaces", false);
        xmlReader.setContentHandler(new CasXmlCallbackHandler(casReceipt));
        xmlReader.parse(new InputSource(new StringReader(response)));

        return casReceipt;

    }

    // TODO implement callbackhandler
    protected class CasXmlCallbackHandler extends DefaultHandler {

        private CasReceipt casReceipt;

        protected CasXmlCallbackHandler(CasReceipt casReceipt) {
            this.casReceipt = casReceipt;

        }
    }
}
