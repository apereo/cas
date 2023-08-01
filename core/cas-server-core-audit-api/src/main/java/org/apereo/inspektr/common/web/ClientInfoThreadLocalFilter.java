package org.apereo.inspektr.common.web;

import lombok.val;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * Creates a ClientInfo object and passes it to the {@link ClientInfoHolder}
 * <p>
 * If one provides an alternative IP Address Header (i.e. init-param "alternativeIpAddressHeader"), the client
 * IP address will be read from that instead.
 *
 * @author Scott Battaglia
 * @since 1.0
 */
public class ClientInfoThreadLocalFilter implements Filter {

    /**
     * Header name for alternative client IP address.
     */
    public static final String CONST_IP_ADDRESS_HEADER = "alternativeIpAddressHeader";

    /**
     * Header name for alternative server address.
     */
    public static final String CONST_SERVER_IP_ADDRESS_HEADER = "alternateServerAddrHeaderName";


    /**
     * Header name for alternative server host address.
     */
    public static final String CONST_USE_SERVER_HOST_ADDRESS = "useServerHostAddress";

    private String alternateLocalAddrHeaderName;

    private boolean useServerHostAddress;

    private String alternateServerAddrHeaderName;

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response,
                         final FilterChain filterChain) throws IOException, ServletException {
        try {
            val clientInfo = ClientInfo.from((HttpServletRequest) request,
                this.alternateServerAddrHeaderName,
                this.alternateLocalAddrHeaderName,
                this.useServerHostAddress);
            ClientInfoHolder.setClientInfo(clientInfo);
            filterChain.doFilter(request, response);
        } finally {
            ClientInfoHolder.clear();
        }
    }

    @Override
    public void init(final FilterConfig filterConfig) {
        this.alternateLocalAddrHeaderName = filterConfig.getInitParameter(CONST_IP_ADDRESS_HEADER);
        this.alternateServerAddrHeaderName = filterConfig.getInitParameter(CONST_SERVER_IP_ADDRESS_HEADER);
        var useServerHostAddr = filterConfig.getInitParameter(CONST_USE_SERVER_HOST_ADDRESS);
        if (useServerHostAddr != null && !useServerHostAddr.isEmpty()) {
            this.useServerHostAddress = Boolean.parseBoolean(useServerHostAddr);
        }
    }
}
