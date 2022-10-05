package net.jradius.client;

import net.jradius.client.auth.CHAPAuthenticator;
import net.jradius.client.auth.EAPMD5Authenticator;
import net.jradius.client.auth.EAPMSCHAPv2Authenticator;
import net.jradius.client.auth.MSCHAPv1Authenticator;
import net.jradius.client.auth.MSCHAPv2Authenticator;
import net.jradius.client.auth.PAPAuthenticator;
import net.jradius.client.auth.RadiusAuthenticator;
import net.jradius.exception.RadiusException;
import net.jradius.log.RadiusLog;
import net.jradius.packet.AccessChallenge;
import net.jradius.packet.AccessRequest;
import net.jradius.packet.RadiusRequest;
import net.jradius.packet.RadiusResponse;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.net.InetAddress;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This is {@link RadiusClient}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class RadiusClient {
    protected RadiusClientTransport transport;

    protected static final Map<String, Class<?>> authenticators = new LinkedHashMap<>();

    static {
        registerAuthenticator("pap", PAPAuthenticator.class);
        registerAuthenticator("chap", CHAPAuthenticator.class);
        registerAuthenticator("mschapv1", MSCHAPv1Authenticator.class);
        registerAuthenticator("mschapv2", MSCHAPv2Authenticator.class);
        registerAuthenticator("mschap", MSCHAPv2Authenticator.class);
        registerAuthenticator("eap-md5", EAPMD5Authenticator.class);
        registerAuthenticator("eap-mschapv2", EAPMSCHAPv2Authenticator.class);
    }

    public RadiusClient(final RadiusClientTransport transport) {
        this.transport = transport;
        this.transport.setRadiusClient(this);
    }

    public RadiusClient(final InetAddress address, final String secret, final int authPort,
                        final int acctPort, final int timeout) throws IOException {
        this.transport = new UDPClientTransport();
        this.transport.setRadiusClient(this);
        setRemoteInetAddress(address);
        setSharedSecret(secret);
        setAuthPort(authPort);
        setAcctPort(acctPort);
        setSocketTimeout(timeout);
    }

    public void close() {
        if (transport != null) {
            transport.close();
        }
    }

    /**
     * Registration of supported RadiusAuthenticator protocols
     *
     * @param name The authentication protocol name
     * @param c    The RadiusAuthenticator class that implements the protocol
     */
    public static void registerAuthenticator(String name, Class<?> c) {
        authenticators.put(name, c);
    }

    public static RadiusAuthenticator getAuthProtocol(String protocolName) {
        RadiusAuthenticator auth = null;
        String[] args = null;
        int i;

        if ((i = protocolName.indexOf(':')) > 0) {
            if (i < protocolName.length()) {
                args = protocolName.substring(i + 1).split(":");
            }
            protocolName = protocolName.substring(0, i);
        }

        protocolName = protocolName.toLowerCase();

        Class<?> c = authenticators.get(protocolName);

        if (c == null) {
            return null;
        }

        try {
            auth = (RadiusAuthenticator) c.getConstructor().newInstance();
        } catch (Exception e) {
            RadiusLog.error("Invalid auth protocol", e);
            return null;
        }

        if (args != null) {
            var elements = new HashMap<String, PropertyDescriptor>();
            var clazz = auth.getClass();
            PropertyDescriptor[] props = null;
            try {
                props = Introspector.getBeanInfo(clazz).getPropertyDescriptors();
            } catch (final Exception e) {
                RadiusLog.error("Could not instanciate authenticator " + protocolName, e);
                return auth;
            }
            for (int p = 0; p < props.length; p++) {
                var pd = props[p];
                var m = pd.getWriteMethod();
                if (m != null) {
                    elements.put(pd.getName(), pd);
                }
            }
            for (int a = 0; a < args.length; a++) {
                int eq = args[a].indexOf('=');
                if (eq > 0) {
                    var name = args[a].substring(0, eq);
                    var value = args[a].substring(eq + 1);

                    var pd = elements.get(name);
                    var m = pd.getWriteMethod();

                    if (m == null) {
                        RadiusLog.error("Authenticator " + protocolName + " does not have a writable attribute " + name);
                    } else {
                        Object valueObject = value;
                        var cType = pd.getPropertyType();
                        if (cType == Boolean.class) {
                            valueObject = Boolean.valueOf(value);
                        } else if (cType == Integer.class) {
                            valueObject = Integer.valueOf(value);
                        }
                        try {
                            m.invoke(auth, new Object[]{valueObject});
                        } catch (Exception e) {
                            RadiusLog.error("Error setting attribute " + name + " for authenticator " + protocolName, e);
                        }
                    }
                }
            }
        }
        return auth;
    }

    public RadiusResponse sendReceive(final RadiusRequest p, final int retries) throws RadiusException {
        return transport.sendReceive(p, retries);
    }

    /**
     * Authenicates using the specified method. For all methods, it is assumed
     * that the user's password can be found in the User-Password attribute. All
     * authentiation requests automatically contain the Message-Authenticator attribute.
     *
     * @param p       RadiusPacket to be send (should be AccessRequest)
     * @param auth    The RadiusAuthenticator instance (if null, PAPAuthenticator is used)
     * @param retries Number of times to retry (without response)
     * @return Returns the reply RadiusPacket
     */
    public RadiusResponse authenticate(final AccessRequest p, RadiusAuthenticator auth, final int retries)
        throws RadiusException, NoSuchAlgorithmException {
        if (auth == null) {
            auth = new PAPAuthenticator();
        }

        auth.setupRequest(this, p);
        auth.processRequest(p);

        while (true) {
            RadiusResponse reply = transport.sendReceive(p, retries);

            if (reply instanceof AccessChallenge) {
                auth.processChallenge(p, reply);
            } else {
                return reply;
            }
        }
    }

    public int getAcctPort() {
        return this.transport.getAcctPort();
    }

    public void setAcctPort(int acctPort) {
        this.transport.setAcctPort(acctPort);
    }

    public int getAuthPort() {
        return this.transport.getAuthPort();
    }

    public void setAuthPort(int authPort) {
        this.transport.setAuthPort(authPort);
    }

    public int getSocketTimeout() {
        return this.transport.getSocketTimeout();
    }

    public void setSocketTimeout(int socketTimeout) {
        this.transport.setSocketTimeout(socketTimeout);
    }

    public InetAddress getRemoteInetAddress() {
        return this.transport.getRemoteInetAddress();
    }

    public void setRemoteInetAddress(InetAddress remoteInetAddress) {
        this.transport.setRemoteInetAddress(remoteInetAddress);
    }

    public InetAddress getLocalInetAddress() {
        return this.transport.getLocalInetAddress();
    }

    public void setLocalInetAddress(InetAddress localInetAddress) {
    }

    public String getSharedSecret() {
        return this.transport.getSharedSecret();
    }

    public void setSharedSecret(String sharedSecret) {
        this.transport.setSharedSecret(sharedSecret);
    }

}
