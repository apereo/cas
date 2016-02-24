/**
 * Copyright (c) 2015, Wavity Inc. and/or its affiliates. All rights reserved.
 */
package org.jasig.cas;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wavity.plan.api.deployment.DeploymentPlan;
import com.wavity.plan.api.node.NodeInfo;
import com.wavity.plan.api.util.WavityHome;

/**
 * This class is used for initializing Wavity environment for broker API
 * 
 * @author davidlee
 *
 */
public final class WavityEnvInitializer {
	
	/**
	 * logger
	 */
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	/**
	 * Zoo Keeper host
	 */
	private final static String zkHost = "localhost";
	
	/**
	 * Zoo Keeper port
	 */
	private final static int zkPort = 2181;
	
	/**
	 * Broker host
	 */
	private final static String brokerHost = "localhost";
	
	/**
	 * Broker port
	 */
	private final static int brokerPort = 9092;
	
	/**
	 * indicates if zookeeper is available or not
	 */
	private static boolean isZkAvailable = false;
	
	/**
	 * indicates if broker is available or not
	 */
	private static boolean isBrokerAvailable = false;
	
	/**
	 * Constructor
	 */
	public WavityEnvInitializer() {
	}
	
	/**
	 * Checks if server available or not
	 * 
	 * @param host the string of server host
	 * @param port the number of server port
	 * @return boolean
	 */
	private final static boolean isServerAvailable(String host, int port) {
		Socket socket = null;
		try {
			socket = new Socket(host, port);
			return true;
		} catch (Throwable t) {
			return false;
		} finally {
			if (socket != null) {
				try {
					socket.close();
				} catch (IOException e) {
				}
			}
		}
	}
	
	/**
	 * Checks if the environment for broker is available or not
	 * @return boolean indicating if the broker environment is available or not
	 */
	public final static boolean isBrokerEnvAvailable() {
		WavityEnvInitializer.isZkAvailable = WavityEnvInitializer.isServerAvailable(WavityEnvInitializer.zkHost, WavityEnvInitializer.zkPort);
		WavityEnvInitializer.isBrokerAvailable = WavityEnvInitializer.isServerAvailable(WavityEnvInitializer.brokerHost, WavityEnvInitializer.brokerPort);
		
		if (!WavityEnvInitializer.isZkAvailable || !WavityEnvInitializer.isBrokerAvailable) {
			return false;
		}
		return true;
	}
	
	/**
	 * Sets up the Wavity environment
	 * 
	 * @throws Exception
	 */
	public final void setUp() throws Exception {
		WavityEnvInitializer.isZkAvailable = WavityEnvInitializer.isServerAvailable(WavityEnvInitializer.zkHost, WavityEnvInitializer.zkPort);
		WavityEnvInitializer.isBrokerAvailable = WavityEnvInitializer.isServerAvailable(WavityEnvInitializer.brokerHost, WavityEnvInitializer.brokerPort);
		
		if (!WavityEnvInitializer.isZkAvailable || !WavityEnvInitializer.isBrokerAvailable) {
			log.info(" *** Failed to find zookeepr and broker *** ");
			return;
		}
		
		File home = new File(WavityEnvInitializer.getWavityHome());
		File dFile = new File(home, "zdata.data");
		File pFile = new File(home, NodeInfo.ZNODE_PROPERTIES);
		System.setProperty(WavityHome.SYS_WAVITY_HOME, home.getAbsolutePath());
		Properties properties = new Properties();
		properties.setProperty(NodeInfo.ZNODE_ID, "DEV_APP_USW1_001_OTS_1");
		properties.setProperty(NodeInfo.ZNODE_CONNECT, WavityEnvInitializer.getZkConnect()); // zookeeper server list
		properties.setProperty(NodeInfo.ZNODE_BROKER, WavityEnvInitializer.getBrokerConnect()); // kafka server list
		properties.setProperty(NodeInfo.ZNODE_DATA, dFile.getAbsolutePath());
		properties.setProperty(NodeInfo.ZNODE_SERVICE, "oneteam");
		properties.setProperty(NodeInfo.ZNODE_DEPLOY, "development");
		FileOutputStream out = new FileOutputStream(pFile);
		properties.store(out, "znode zookeeper client properties");
		out.close();
		
		InputStream inStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(DeploymentPlan.WAVITY_DEPLOYMENT_PLAN_JSON);
		if(inStream != null) {
			File plan = new File(home, DeploymentPlan.WAVITY_DEPLOYMENT_PLAN_JSON);
			FileOutputStream outStream = new FileOutputStream(plan);
			byte buf[] = new byte[8192];
			int nRead = 0;
			while(true) {
				nRead = inStream.read(buf, 0, buf.length);
				if(nRead < 0) {
					break;
				} else if(nRead > 0) {
					outStream.write(buf, 0, nRead);
				}
			}
			inStream.close();
			outStream.close();
		}
	}
	
	/**
	 * Gets Wavity home
	 * 
	 * @return the string of Wavity home directory
	 */
	private final static String getWavityHome() {
		StringBuilder sb = new StringBuilder();
		sb.append(System.getProperty(WavityEnvironmentContextListener.CONFIG_DIR));
		return sb.toString();
	}
	
	/**
	 * Gets Zoo Keeper connect information
	 * 
	 * @return the string of Zoo Keeper connection information
	 */
	private final static String getZkConnect() {
		StringBuilder sb = new StringBuilder();
		sb.append(WavityEnvInitializer.zkHost);
		sb.append(":");
		sb.append(WavityEnvInitializer.zkPort);
		return sb.toString();
	}
	
	/**
	 * Gets the broker connection information
	 * 
	 * @return the string of broker connection information
	 */
	private final static String getBrokerConnect() {
		StringBuilder sb = new StringBuilder();
		sb.append(WavityEnvInitializer.brokerHost);
		sb.append(":");
		sb.append(WavityEnvInitializer.brokerPort);
		return sb.toString();
	}
}
