package ru.sberbank.cib.gmbus.mqadmin.connect;

import java.io.FileInputStream;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import com.ibm.mq.MQException;
import com.ibm.mq.MQMessage;
import com.ibm.mq.MQQueue;
import com.ibm.mq.MQQueueManager;
import com.ibm.mq.constants.MQConstants;
import com.ibm.mq.jms.MQConnectionFactory;
import com.ibm.mq.pcf.CMQC;
import com.ibm.mq.pcf.CMQCFC;
import com.ibm.mq.pcf.MQCFH;
import com.ibm.mq.pcf.MQCFIN;
import com.ibm.mq.pcf.MQCFSL;
import com.ibm.mq.pcf.MQCFST;
import com.ibm.mq.pcf.PCFAgent;
import com.ibm.mq.pcf.PCFParameter;

import ru.sberbank.cib.gmbus.mqadmin.exception.MQAdminException;
import ru.sberbank.cib.gmbus.mqadmin.model.MQManagerAttributes;

public class MQHelper {
	
	private static Map<String,String> cipherSuiteMap = new HashMap<>();
	
	static {
		cipherSuiteMap.put("SSL_RSA_WITH_AES_128_CBC_SHA256", "TLS_RSA_WITH_AES_128_CBC_SHA256");
	}
    
    public static MQQueueManager getMQConnection (MQManagerAttributes qmgr) throws Exception{          

        System.setProperty("javax.net.ssl.trustStore", qmgr.getTrustStorePath());
        System.setProperty("javax.net.ssl.trustStorePassword", qmgr.getTrustStorePassword());
        System.setProperty("javax.net.ssl.keyStore", qmgr.getKeyStorePath()); 
        System.setProperty("javax.net.ssl.keyStorePassword", qmgr.getKeyStorePassword()); 
        Hashtable<String,Object> propMap = new Hashtable<String,Object>();

        propMap.put(MQConstants.HOST_NAME_PROPERTY, qmgr.getHostName());
        propMap.put(MQConstants.PORT_PROPERTY,  qmgr.getPort());
        propMap.put(MQConstants.CHANNEL_PROPERTY, qmgr.getChannel());
        propMap.put(MQConstants.SSL_CIPHER_SUITE_PROPERTY,  cipherSuiteMap.get(qmgr.getCipherSuite()));       
        
        return new com.ibm.mq.MQQueueManager(qmgr.getQmName(),propMap);
    }
    
    public static List<String> getQueueNames(MQQueueManager qmgr) throws Exception{
    	
    	List<String> result = new ArrayList<>();
    	
    	PCFAgent agent = new PCFAgent(qmgr);    	
    	PCFParameter [] parameters =  
			{
				new MQCFST (CMQC.MQCA_Q_NAME, "*"), 
				new MQCFIN (CMQC.MQIA_Q_TYPE, CMQC.MQQT_ALL)
			};    	
    	    	
    	MQMessage [] responses = agent.send (CMQCFC.MQCMD_INQUIRE_Q_NAMES, parameters);		
    	MQCFH cfh = new MQCFH (responses [0]);    	
    	
    	if (cfh.reason == 0){		
			MQCFSL cfsl = new MQCFSL (responses [0]);

			for (int i = 0; i < cfsl.strings.length; i++){
				result.add(cfsl.strings [i]);
			}
		}
		else{
			throw new MQAdminException(cfh.reason,responses [0]);
		}
    	
    	return result;
    	
    }
	
    public static int getQueueDepth(MQQueueManager qmgr, String qName) throws MQException{    	
    	int openOptions =  CMQC.MQOO_FAIL_IF_QUIESCING + CMQC.MQOO_INPUT_SHARED + CMQC.MQOO_INQUIRE;
    	MQQueue destQueue = qmgr.accessQueue(qName,   openOptions);   
    	int currDepth = destQueue.getCurrentDepth(); 
    	destQueue.close();    	
    	return currDepth;
    }
    
    public static MQConnectionFactory getMQJMSConnectionFactory(MQManagerAttributes qmgr) throws Exception{
    	MQConnectionFactory factory = new MQConnectionFactory();
		
    	factory.setTransportType(qmgr.getTransportType());
		factory.setHostName(qmgr.getHostName());		
		factory.setPort(qmgr.getPort());
		factory.setQueueManager(qmgr.getQmName());
		factory.setChannel(qmgr.getChannel());	
		
		SSLSocketFactory sslFactory = getSSLSocketFactory(qmgr);
		if(sslFactory!=null){		
			factory.setSSLSocketFactory(sslFactory);
			factory.setSSLCipherSuite(cipherSuiteMap.get(qmgr.getCipherSuite()));
		}
		
		return factory;
    }
	
    
    private static SSLSocketFactory getSSLSocketFactory(MQManagerAttributes qmgr) throws Exception{
		
		if((qmgr.getKeyStorePath()==null) || (qmgr.getTrustStorePath()==null) || (qmgr.getKeyStorePassword()==null) || (qmgr.getTrustStorePassword()==null))
			return null;
		
		KeyStore keyStore = KeyStore.getInstance("JKS");
		FileInputStream keyStoreLocation=new FileInputStream(qmgr.getKeyStorePath());
		FileInputStream trustStoreLocation=new FileInputStream(qmgr.getTrustStorePath());
		String keyStorePassword = qmgr.getKeyStorePassword();
		String trustStorePassword = qmgr.getTrustStorePassword();		
				
		keyStore.load(keyStoreLocation, keyStorePassword.toCharArray());        
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore, keyStorePassword.toCharArray());
        
        KeyStore trustStore = KeyStore.getInstance("JKS");
		trustStore.load(trustStoreLocation, trustStorePassword.toCharArray());        
		TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(trustStore);        
        
        SSLContext sslContext = SSLContext.getInstance("SSL");
        sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);
        return sslContext.getSocketFactory(); 	
	}
}