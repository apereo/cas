package org.apereo.cas.aws;

import org.apereo.cas.configuration.model.support.aws.BaseAmazonWebServicesProperties;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

import java.net.InetAddress;

/**
 * This is {@link AmazonClientConfigurationBuilder}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Slf4j
public class AmazonClientConfigurationBuilder {

    /**
     * Build client configuration.
     *
     * @param props the props
     * @return the client configuration
     */
    @SneakyThrows
    public static ClientConfiguration buildClientConfiguration(final BaseAmazonWebServicesProperties props) {
        val cfg = new ClientConfiguration();
        cfg.setConnectionTimeout(props.getConnectionTimeout());
        cfg.setMaxConnections(props.getMaxConnections());
        cfg.setRequestTimeout(props.getRequestTimeout());
        cfg.setSocketTimeout(props.getSocketTimeout());
        cfg.setUseGzip(props.isUseGzip());
        cfg.setUseReaper(props.isUseReaper());
        cfg.setUseThrottleRetries(props.isUseThrottleRetries());
        cfg.setUseTcpKeepAlive(props.isUseTcpKeepAlive());
        cfg.setProtocol(Protocol.valueOf(props.getProtocol().toUpperCase()));
        cfg.setClientExecutionTimeout(props.getClientExecutionTimeout());
        if (props.getMaxErrorRetry() > 0) {
            cfg.setMaxErrorRetry(props.getMaxErrorRetry());
        }
        cfg.setProxyHost(props.getProxyHost());
        cfg.setProxyPassword(props.getProxyPassword());
        if (props.getProxyPort() > 0) {
            cfg.setProxyPort(props.getProxyPort());
        }
        cfg.setProxyUsername(props.getProxyUsername());
        cfg.setCacheResponseMetadata(props.isCacheResponseMetadata());

        if (StringUtils.isNotBlank(props.getLocalAddress())) {
            LOGGER.trace("Creating DynamoDb client local address [{}]", props.getLocalAddress());
            cfg.setLocalAddress(InetAddress.getByName(props.getLocalAddress()));
        }

        return cfg;
    }
}
