package org.apereo.cas.configuration.model.core.web.tomcat;

import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This is {@link CasEmbeddedApacheTomcatAjpProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RequiresModule(name = "cas-server-webapp-tomcat")
@Getter
@Setter
@Accessors(chain = true)
public class CasEmbeddedApacheTomcatAjpProperties implements Serializable {

    private static final long serialVersionUID = -32143821503580896L;

    /**
     * Sets the protocol to handle incoming traffic.
     */
    private String protocol = "AJP/1.3";

    /**
     * The TCP port number on which this Connector will create a server socket and await incoming connections.
     * Your operating system will allow only one server application to listen to a
     * particular port number on a particular IP address. If the special value of 0 (zero) is used,
     * then Tomcat will select a free port at random to use for this connector.
     * This is typically only useful in embedded and testing applications.
     */
    private int port = 8009;

    /**
     * Set this attribute to true if you wish to have calls to request.isSecure() to return true for requests received
     * by this Connector (you would want this on an SSL Connector). The default value is false.
     */
    private boolean secure;

    /**
     * Set the secret that must be included with every request.
     */
    private String secret;

    /**
     * A boolean value which can be used to enable or disable
     * the TRACE HTTP method. If not specified, this attribute is set to false.
     */
    private boolean allowTrace;

    /**
     * Set this attribute to the name of the protocol you wish to have returned by calls to request.getScheme(). For example,
     * you would set this attribute to "https" for an SSL Connector.
     */
    private String scheme = "http";

    /**
     * Enable AJP support in CAS for the embedded Apache Tomcat container.
     */
    private boolean enabled;

    /**
     * The default timeout for asynchronous requests in milliseconds. If not specified, this attribute is set to 10000 (10 seconds).
     */
    private String asyncTimeout = "PT5S";

    /**
     * Set to true if you want calls to request.getRemoteHost() to perform DNS lookups in order to return the actual host name of the remote client.
     * Set to false to skip the DNS lookup and return the IP address in String form instead (thereby improving performance).
     * By default, DNS lookups are disabled.
     */
    private boolean enableLookups;

    /**
     * The maximum size in bytes of the POST which will be handled by the container
     * FORM URL parameter parsing. The feature can be disabled by setting this attribute to a value
     * less than or equal to 0. If not specified, this attribute is set to 2097152 (2 megabytes).
     */
    private int maxPostSize = 20971520;

    /**
     * If this Connector is being used in a proxy configuration, configure this attribute
     * to specify the server port to be returned for calls to request.getServerPort().
     */
    private int proxyPort = -1;

    /**
     * If this Connector is supporting non-SSL requests, and a request is received
     * for which a matching &lt;security-constraint&gt; requires SSL transport,
     * Catalina will automatically redirect the request to the port number specified here.
     */
    private int redirectPort = -1;

    /**
     * Additional attributes to be set on the AJP connector in form of key-value pairs.
     * Examples include:
     * <ul>
     * <li>{@code tomcatAuthentication}: If set to true, the authentication will be done in Tomcat.
     * Otherwise, the authenticated principal will be propagated from the native webserver
     * and used for authorization in Tomcat.
     * Note that this principal will have no roles associated with it. The default value is true.</li>
     * <li>{@code maxThreads}: The maximum number of request processing threads to be created
     * by this Connector, which therefore determines the maximum number of simultaneous
     * requests that can be handled. If not specified, this attribute is set to 200.
     * If an executor is associated with this connector, this attribute is
     * ignored as the connector will execute tasks using the executor rather than an internal thread pool.</li>
     * <li>{@code keepAliveTimeout}: The number of milliseconds this Connector
     * will wait for another AJP request before closing the connection.
     * The default value is to use the value that has been set for the connectionTimeout attribute.</li>
     * <li>{@code maxCookieCount}: The maximum number of cookies that are permitted for a request.
     * A value of less than zero means no limit. If not specified, a default value of 200 will be used.</li>
     * <li>{@code bufferSize}: The size of the output buffer to use. If less than or equal to zero,
     * then output buffering is disabled. The default value is -1 (i.e. buffering disabled)</li>
     * <li>{@code clientCertProvider}: When client certificate information is presented in a
     * form other than instances of java.security.cert.X509Certificate it needs to be converted
     * before it can be used and this property controls which JSSE provider is used to perform
     * the conversion. For example it is used with the AJP connectors,
     * the HTTP APR connector and with the org.apache.catalina.valves.SSLValve.If not specified,
     * the default provider will be used.</li>
     * <li>{@code connectionTimeout}: The number of milliseconds this Connector
     * will wait, after accepting a connection,
     * for the request URI line to be presented. The default value is infinite (i.e. no timeout).</li>
     * <li>{@code address}: For servers with more than one IP address,
     * this attribute specifies which address will be used for listening on
     * the specified port. By default, this port will be used on all IP addresses associated with the server.
     * A value of 127.0.0.1 indicates that the Connector will only listen on the loopback interface.</li>
     * </ul>
     * <p>
     * See the Apache Tomcat documentation for a full list.
     */
    private Map<String, String> attributes = new LinkedHashMap<>(0);
}
