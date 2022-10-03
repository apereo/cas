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
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * This is {@link RadiusClient}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class RadiusClient {
    protected RadiusClientTransport transport;

    protected static final LinkedHashMap<String, Class<?>> authenticators = new LinkedHashMap<>();

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

    public RadiusClient(InetAddress address, String secret, int authPort, int acctPort, int timeout) throws IOException {
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
            HashMap<String, PropertyDescriptor> elements = new HashMap<>();
            Class<?> clazz = auth.getClass();
            PropertyDescriptor[] props = null;
            try {
                props = Introspector.getBeanInfo(clazz).getPropertyDescriptors();
            } catch (Exception e) {
                RadiusLog.error("Could not instanciate authenticator " + protocolName, e);
                return auth;
            }
            for (int p = 0; p < props.length; p++) {
                PropertyDescriptor pd = props[p];
                Method m = pd.getWriteMethod();
                if (m != null) {
                    elements.put(pd.getName(), pd);
                }
            }
            for (int a = 0; a < args.length; a++) {
                int eq = args[a].indexOf("=");
                if (eq > 0) {
                    String name = args[a].substring(0, eq);
                    String value = args[a].substring(eq + 1);

                    PropertyDescriptor pd = elements.get(name);
                    Method m = pd.getWriteMethod();

                    if (m == null) {
                        RadiusLog.error("Authenticator " + protocolName + " does not have a writable attribute " + name);
                    } else {
                        Object valueObject = value;
                        Class<?> cType = pd.getPropertyType();
                        if (cType == Boolean.class) {
                            valueObject = new Boolean(value);
                        } else if (cType == Integer.class) {
                            valueObject = new Integer(value);
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

    public RadiusResponse sendReceive(RadiusRequest p, int retries) throws RadiusException {
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
    public RadiusResponse authenticate(AccessRequest p, RadiusAuthenticator auth, int retries)
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

    /**
     * @param acctPort The RADIUS accounting port
     */
    public void setAcctPort(int acctPort) {
        transport.setAcctPort(acctPort);
    }

    /**
     * @param authPort The RADIUS authentication port
     */
    public void setAuthPort(int authPort) {
        transport.setAuthPort(authPort);
    }

    /**
     * @param socketTimeout The socket timeout (in seconds)
     */
    public void setSocketTimeout(int socketTimeout) {
        transport.setSocketTimeout(socketTimeout);
    }

    /**
     * @return Returns the remote server IP Address
     */
    public InetAddress getRemoteInetAddress() {
        return transport.getRemoteInetAddress();
    }

    /**
     * @param remoteInetAddress The remote server IP Address
     */
    public void setRemoteInetAddress(InetAddress remoteInetAddress) {
        transport.setRemoteInetAddress(remoteInetAddress);
    }

    /**
     * @param sharedSecret The shared secret to set
     */
    public void setSharedSecret(String sharedSecret) {
        transport.setSharedSecret(sharedSecret);
    }
}
