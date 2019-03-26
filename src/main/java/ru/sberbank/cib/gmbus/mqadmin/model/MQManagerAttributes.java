package ru.sberbank.cib.gmbus.mqadmin.model;

public class MQManagerAttributes {

	private int transportType;
	private String hostName;
	private int port;
	private String qmName;
	private String channel;
	
	private String keyStorePath;
	private String trustStorePath;
	private String keyStorePassword;
	private String trustStorePassword;
	private String cipherSuite;
	private boolean connected;	
	
	public MQManagerAttributes(int transportType, String hostName, int port, String qmName, String channel) {
		super();
		this.transportType = transportType;
		this.hostName = hostName;
		this.port = port;
		this.qmName = qmName;
		this.channel = channel;
	}
	
	public MQManagerAttributes(int transportType, String hostName, int port, String qmName, String channel,
			String keyStorePath, String trustStorePath, String keyStorePassword, String trustStorePassword,
			String cipherSuite) {
		super();
		this.transportType = transportType;
		this.hostName = hostName;
		this.port = port;
		this.qmName = qmName;
		this.channel = channel;
		this.keyStorePath = keyStorePath;
		this.trustStorePath = trustStorePath;
		this.keyStorePassword = keyStorePassword;
		this.trustStorePassword = trustStorePassword;
		this.cipherSuite = cipherSuite;	
	}

	/**
	 * @return the keyStorePath
	 */
	public String getKeyStorePath() {
		return keyStorePath;
	}



	/**
	 * @param keyStorePath the keyStorePath to set
	 */
	public void setKeyStorePath(String keyStorePath) {
		this.keyStorePath = keyStorePath;
	}



	/**
	 * @return the trustStorePath
	 */
	public String getTrustStorePath() {
		return trustStorePath;
	}



	/**
	 * @param trustStorePath the trustStorePath to set
	 */
	public void setTrustStorePath(String trustStorePath) {
		this.trustStorePath = trustStorePath;
	}



	/**
	 * @return the keyStorePassword
	 */
	public String getKeyStorePassword() {
		return keyStorePassword;
	}



	/**
	 * @param keyStorePassword the keyStorePassword to set
	 */
	public void setKeyStorePassword(String keyStorePassword) {
		this.keyStorePassword = keyStorePassword;
	}



	/**
	 * @return the trustStorePassword
	 */
	public String getTrustStorePassword() {
		return trustStorePassword;
	}



	/**
	 * @param trustStorePassword the trustStorePassword to set
	 */
	public void setTrustStorePassword(String trustStorePassword) {
		this.trustStorePassword = trustStorePassword;
	}



	/**
	 * @return the cipherSuite
	 */
	public String getCipherSuite() {
		return cipherSuite;
	}



	/**
	 * @param cipherSuite the cipherSuite to set
	 */
	public void setCipherSuite(String cipherSuite) {
		this.cipherSuite = cipherSuite;
	}



	/**
	 * @return the transportType
	 */
	public int getTransportType() {
		return transportType;
	}

	/**
	 * @return the hostName
	 */
	public String getHostName() {
		return hostName;
	}

	/**
	 * @return the port
	 */
	public int getPort() {
		return port;
	}

	/**
	 * @return the qmName
	 */
	public String getQmName() {
		return qmName;
	}

	/**
	 * @return the channel
	 */
	public String getChannel() {
		return channel;
	}	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((channel == null) ? 0 : channel.hashCode());
		result = prime * result + ((hostName == null) ? 0 : hostName.hashCode());
		result = prime * result + port;
		result = prime * result + ((qmName == null) ? 0 : qmName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MQManagerAttributes other = (MQManagerAttributes) obj;
		if (channel == null) {
			if (other.channel != null)
				return false;
		} else if (!channel.equals(other.channel))
			return false;
		if (hostName == null) {
			if (other.hostName != null)
				return false;
		} else if (!hostName.equals(other.hostName))
			return false;
		if (port != other.port)
			return false;
		if (qmName == null) {
			if (other.qmName != null)
				return false;
		} else if (!qmName.equals(other.qmName))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return String.format("%s on '%s(%d)'", qmName,hostName,port);
	}

	public boolean isConnected() {
		return connected;
	}

	public void setConnected(boolean connected) {
		this.connected = connected;
	}	
	
}
