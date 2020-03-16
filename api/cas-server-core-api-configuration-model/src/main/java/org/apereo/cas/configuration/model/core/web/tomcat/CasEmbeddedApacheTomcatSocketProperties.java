package org.apereo.cas.configuration.model.core.web.tomcat;

import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * This is {@link CasEmbeddedApacheTomcatSocketProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@RequiresModule(name = "cas-server-webapp-tomcat")
@Getter
@Setter
@Accessors(chain = true)
public class CasEmbeddedApacheTomcatSocketProperties implements Serializable {
    private static final long serialVersionUID = 3280755966422957481L;

    /**
     * Each connection that is opened up in Tomcat get associated with a read ByteBuffer.
     * This attribute controls the size of this buffer. By default this read buffer is sized
     * at 8192 bytes. For lower concurrency, you can increase this to buffer more data. For an
     * extreme amount of keep alive connections, decrease this number or increase your heap size.
     */
    private int appReadBufSize;

    /**
     * Each connection that is opened up in Tomcat get associated with a write ByteBuffer.
     * This attribute controls the size of this buffer. By default this write buffer is sized at
     * 8192 bytes. For low concurrency you can increase this to buffer more response data. For an
     * extreme amount of keep alive connections, decrease this number or increase your heap size.
     * The default value here is pretty low, you should up it if you are not dealing
     * with tens of thousands concurrent connections.
     */
    private int appWriteBufSize;

    /**
     * The NIO connector uses a class called NioChannel that holds elements linked to a socket.
     * To reduce garbage collection, the NIO connector caches these channel objects. This value
     * specifies the size of this cache. The default value is 500, and represents that
     * the cache will hold 500 NioChannel objects. Other values are -1 for unlimited cache and 0 for no cache.
     */
    private int bufferPool;

    /**
     * An int expressing the relative importance of a short connection time.
     * Performance preferences are described by three integers whose values indicate the relative
     * importance of short connection time, low latency, and high bandwidth. The absolute values of the
     * integers are irrelevant; in order to choose a protocol the values are simply compared, with larger values indicating
     * stronger preferences. Negative values disable the setting.
     * If the application prefers short connection time over both low latency and high bandwidth,
     * for example, then it could invoke this method with the values (1, 0, 0). If the application
     * prefers high bandwidth above low latency, and low latency above short connection time,
     * then it could invoke this method with the values (0, 1, 2).
     */
    private int performanceConnectionTime = -1;

    /**
     * An int expressing the relative importance of low latency.
     * Performance preferences are described by three integers whose values indicate the relative
     * importance of short connection time, low latency, and high bandwidth. The absolute values of the
     * integers are irrelevant; in order to choose a protocol the values are simply compared, with larger values indicating
     * stronger preferences. Negative values disable the setting.
     * If the application prefers short connection time over both low latency and high bandwidth,
     * for example, then it could invoke this method with the values (1, 0, 0). If the application
     * prefers high bandwidth above low latency, and low latency above short connection time,
     * then it could invoke this method with the values (0, 1, 2).
     */
    private int performanceLatency = -1;

    /**
     * An int expressing the relative importance of high bandwidth.
     * Performance preferences are described by three integers whose values indicate the relative
     * importance of short connection time, low latency, and high bandwidth. The absolute values of the
     * integers are irrelevant; in order to choose a protocol the values are simply compared, with larger values indicating
     * stronger preferences. Negative values disable the setting.
     * If the application prefers short connection time over both low latency and high bandwidth,
     * for example, then it could invoke this method with the values (1, 0, 0). If the application
     * prefers high bandwidth above low latency, and low latency above short connection time,
     * then it could invoke this method with the values (0, 1, 2).
     */
    private int performanceBandwidth = -1;

}
