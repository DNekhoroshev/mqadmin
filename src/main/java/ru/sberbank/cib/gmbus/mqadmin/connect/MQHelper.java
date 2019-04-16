package ru.sberbank.cib.gmbus.mqadmin.connect;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import org.supercsv.io.CsvMapWriter;
import org.supercsv.io.ICsvMapWriter;
import org.supercsv.prefs.CsvPreference;

import com.google.gson.Gson;
import com.ibm.mq.MQAsyncStatus;
import com.ibm.mq.MQException;
import com.ibm.mq.MQGetMessageOptions;
import com.ibm.mq.MQMessage;
import com.ibm.mq.MQPutMessageOptions;
import com.ibm.mq.MQQueue;
import com.ibm.mq.MQQueueManager;
import com.ibm.mq.constants.MQConstants;
import com.ibm.mq.jms.MQConnectionFactory;
import com.ibm.mq.pcf.CMQC;
import com.ibm.mq.pcf.MQCFH;
import com.ibm.mq.pcf.MQCFIN;
import com.ibm.mq.pcf.MQCFSL;
import com.ibm.mq.pcf.MQCFST;
import com.ibm.mq.pcf.PCFAgent;
import com.ibm.mq.pcf.PCFMessage;
import com.ibm.mq.pcf.PCFMessageAgent;
import com.ibm.mq.pcf.PCFParameter;

import ru.sberbank.cib.gmbus.mqadmin.MQAdmin;
import ru.sberbank.cib.gmbus.mqadmin.MQAdmin.MQSession;
import ru.sberbank.cib.gmbus.mqadmin.concurrent.ImportMessageTask;
import ru.sberbank.cib.gmbus.mqadmin.concurrent.ImportMessageTask.SourceFormat;
import ru.sberbank.cib.gmbus.mqadmin.exception.MQAdminException;
import ru.sberbank.cib.gmbus.mqadmin.model.MQManagerAttributes;
import ru.sberbank.cib.gmbus.mqadmin.model.MQQueueAttributes;
import ru.sberbank.cib.gmbus.mqadmin.model.MessageHeader;
import ru.sberbank.cib.gmbus.mqadmin.util.CommonConstants;

public class MQHelper {
	
	private static Map<String,String> cipherSuiteMap = new HashMap<>();
	private static Map<Integer,String> queueTypeMap = new HashMap<>();
	
	/**
     * csv header name for the fixml message value.
     */
    private static final String BODY = "Body";

    /**
     * csv header name for the fixml headers value.
     */
    private static final String HEADER = "Header";
	
	static {
		cipherSuiteMap.put("SSL_RSA_WITH_AES_128_CBC_SHA256", "TLS_RSA_WITH_AES_128_CBC_SHA256");
		queueTypeMap.put(MQConstants.MQQT_LOCAL, "Local");
		queueTypeMap.put(MQConstants.MQQT_REMOTE, "Remote");
		queueTypeMap.put(MQConstants.MQQT_ALIAS, "Alias");
		queueTypeMap.put(MQConstants.MQQT_MODEL, "Model");
	}
    
    public static MQSession getMQSession (MQManagerAttributes qmgr) throws Exception{          

        System.setProperty("javax.net.ssl.trustStore", qmgr.getTrustStorePath());
        System.setProperty("javax.net.ssl.trustStorePassword", qmgr.getTrustStorePassword());
        System.setProperty("javax.net.ssl.keyStore", qmgr.getKeyStorePath()); 
        System.setProperty("javax.net.ssl.keyStorePassword", qmgr.getKeyStorePassword()); 
        Hashtable<String,Object> propMap = new Hashtable<String,Object>();

        propMap.put(MQConstants.HOST_NAME_PROPERTY, qmgr.getHostName());
        propMap.put(MQConstants.PORT_PROPERTY,  qmgr.getPort());
        propMap.put(MQConstants.CHANNEL_PROPERTY, qmgr.getChannel());
        propMap.put(MQConstants.SSL_CIPHER_SUITE_PROPERTY,  cipherSuiteMap.get(qmgr.getCipherSuite()));
        propMap.put(MQConstants.CONNECT_OPTIONS_PROPERTY, MQConstants.MQCNO_HANDLE_SHARE_NO_BLOCK);
        
        MQQueueManager qm =  new com.ibm.mq.MQQueueManager(qmgr.getQmName(),propMap);
        PCFMessageAgent agent = new PCFMessageAgent(qm);
        return new MQAdmin.MQSession(qm, agent);
    }   
	
    public static MQAsyncStatus send(MQQueueManager qmgr,String queueName,boolean persistent, Map<String,String> headers, String text) throws MQException, IOException{
    	int openOptions = MQConstants.MQOO_OUTPUT | MQConstants.MQOO_INPUT_AS_Q_DEF;
    	MQQueue queue = qmgr.accessQueue(queueName, openOptions);
    	MQPutMessageOptions pmo = new MQPutMessageOptions(); // default
    	pmo.options = MQConstants.MQPMO_ASYNC_RESPONSE;

        // create message
        MQMessage message = new MQMessage();
        message.format = MQConstants.MQFMT_STRING;
		if (headers != null) {
			for (Entry<String, String> header : headers.entrySet()) {
				message.setStringProperty(header.getKey(), header.getValue());
			}
		}
        message.writeString(text); 
        queue.put(message, pmo);
        queue.close();

        return qmgr.getAsyncStatus();
    	
    }
    
    public static MQAsyncStatus send(MQQueueManager qmgr,String queueName,boolean persistent, Map<String,String> headers, byte[] buffer) throws MQException, IOException{
    	int openOptions = MQConstants.MQOO_OUTPUT | MQConstants.MQOO_INPUT_AS_Q_DEF;
    	MQQueue queue = qmgr.accessQueue(queueName, openOptions);
    	MQPutMessageOptions pmo = new MQPutMessageOptions(); // default
    	pmo.options = MQConstants.MQPMO_ASYNC_RESPONSE;

        // create message
        MQMessage message = new MQMessage();
        message.format = MQConstants.MQFMT_NONE;
		if (headers != null) {
			for (Entry<String, String> header : headers.entrySet()) {
				message.setStringProperty(header.getKey(), header.getValue());
			}
		}
        message.write(buffer);
        queue.put(message, pmo);
        queue.close();

        return qmgr.getAsyncStatus();
    	
    }
    
    public static MQQueue getQueue(MQQueueManager qmgr, String qName, boolean browseMode) throws Exception{    	
    	
    	int openOptions = MQConstants.MQOO_FAIL_IF_QUIESCING | MQConstants.MQOO_INPUT_SHARED | (browseMode?MQConstants.MQOO_BROWSE:MQConstants.MQOO_OUTPUT);
    	
    	return qmgr.accessQueue(qName, openOptions);    	
    }
    
	public static List<String> getQueueNames(MQSession mqSession) throws Exception {

		com.ibm.mq.jms.MQQueueConnectionFactory g;
		List<String> result = new ArrayList<>();

		PCFAgent agent = mqSession.getMqPCFAgent();
		PCFParameter[] parameters = { new MQCFST(MQConstants.MQCA_Q_NAME, "*"),
				new MQCFIN(MQConstants.MQIA_Q_TYPE, MQConstants.MQQT_ALL) };

		MQMessage[] responses = agent.send(MQConstants.MQCMD_INQUIRE_Q_NAMES, parameters);
		MQCFH cfh = new MQCFH(responses[0]);

		if (cfh.reason == 0) {
			MQCFSL cfsl = new MQCFSL(responses[0]);

			for (int i = 0; i < cfsl.strings.length; i++) {
				result.add(cfsl.strings[i]);
			}
		} else {
			throw new MQAdminException(cfh.reason, responses[0]);
		}

		return result;

	}
    
    /*public static List<String> getQueueNames(MQSession mqSession) throws Exception{
    	
    	com.ibm.mq.jms.MQQueueConnectionFactory g;
    	List<String> result = new ArrayList<>();
    	
    	PCFMessageAgent agent = mqSession.getMqPCFAgent();    	
    	PCFMessage request = new PCFMessage(MQConstants.MQCMD_INQUIRE_Q_NAMES);
    	request.addParameter(MQConstants.MQCA_Q_NAME, "*"); 
    	request.addParameter(MQConstants.MQIA_Q_TYPE, MQConstants.MQQT_ALL);
    	
    	PCFMessage[] responses = agent.send(request);   	   	    	
    	
    	if ((responses[0]).getCompCode() == MQConstants.MQCC_OK) {		
    		System.out.println(responses[0].getStringParameterValue(MQConstants.MQCA_Q_NAME));			
		}
		else{
			throw new MQAdminException(responses [0].getReason(),null);
		}
    	
    	return result;
    	
    }   */
    
    public static MQQueueAttributes getQueueStatus(MQSession mqSession, String qName) throws Exception{
    	PCFMessageAgent agent = null;
        PCFMessage   request = null;
        PCFMessage[] responses = null;
        
        try{
        	agent = mqSession.getMqPCFAgent();     	
        	            
        	request = new PCFMessage(MQConstants.MQCMD_INQUIRE_Q);            
            request.addParameter(MQConstants.MQCA_Q_NAME, qName);            
            request.addParameter(MQConstants.MQIACF_Q_ATTRS,
                    new int [] { MQConstants.MQCA_Q_NAME,
                    			 MQConstants.MQIA_CURRENT_Q_DEPTH,
                    			 MQConstants.MQIA_OPEN_INPUT_COUNT,
                    			 MQConstants.MQIA_OPEN_OUTPUT_COUNT,
                    			 MQConstants.MQIA_Q_TYPE                                                                  
                               });
            responses = agent.send(request);
            
            String name = "";
            int depth = 0;
            int iprocs = 0;
            int oprocs = 0;
            
			if (((responses[0]).getCompCode() == MQConstants.MQCC_OK)
					&& ((responses[0]).getParameterValue(MQConstants.MQCA_Q_NAME) != null)) {
				name = responses[0].getStringParameterValue(MQConstants.MQCA_Q_NAME);
				if (name != null)
					name = name.trim();

				int type = responses[0].getIntParameterValue(MQConstants.MQIA_Q_TYPE);
				
				if(type==CMQC.MQQT_LOCAL){
					depth = responses[0].getIntParameterValue(MQConstants.MQIA_CURRENT_Q_DEPTH);
					iprocs = responses[0].getIntParameterValue(MQConstants.MQIA_OPEN_INPUT_COUNT);
					oprocs = responses[0].getIntParameterValue(MQConstants.MQIA_OPEN_OUTPUT_COUNT);
				}			
				
				return new MQQueueAttributes(name, queueTypeMap.get(type), depth, iprocs, oprocs);
			}            
        }catch(MQException e){        
        	e.printStackTrace();
        	throw new MQAdminException(e);
        }        
        return null;
    }
    
    public static void importMessages(MQAdmin mainApp,String queueName,File sourceFile,SourceFormat format) throws Exception{		
    	
    	ImportMessageTask imt = new ImportMessageTask(mainApp, queueName, sourceFile, format);
		
		mainApp.statusBar.textProperty().bind(imt.messageProperty());
        
		imt.setOnSucceeded(event -> {
            mainApp.statusBar.textProperty().unbind();
            mainApp.showInfo(String.format("Import into %s. Imported %d messages.", queueName, event.getSource().getValue()));            
        });
		
        imt.setOnFailed(event -> {
            mainApp.statusBar.textProperty().unbind();
            mainApp.showException(event.getSource().getException());
        });
        
		Thread t = new Thread(imt);			
		t.start();
		
    	
	}
    
    public static long exportMessages(MQAdmin mainApp,String queueName,String path,boolean includeHeaders) throws Exception{		
		 				 
		 Path currFilePath = Paths.get(path+getExportFileName(queueName));					
		 
		 ICsvMapWriter csvWriter = new CsvMapWriter(
				 Files.newBufferedWriter(currFilePath, CommonConstants.STD_CHARSET),
				 CsvPreference.STANDARD_PREFERENCE);
		 
		 String[] CSV_HEADER = null;
		 
		 if(includeHeaders)
			 CSV_HEADER = new String[] {BODY, HEADER};					 
		 else
			 CSV_HEADER = new String[] {BODY};
		 
		 csvWriter.writeHeader(CSV_HEADER);
		 
		 long msg_count=0;
		 
		 MQQueue queue = getQueue(mainApp.getCurrentSession().getMqManager(), queueName, true);
		 MQMessage theMessage    = new MQMessage();
		 MQGetMessageOptions gmo = new MQGetMessageOptions();
			
		 gmo.matchOptions=MQConstants.MQMO_NONE;
		 gmo.waitInterval=100;
			
		 gmo.options=MQConstants.MQGMO_WAIT | MQConstants.MQGMO_BROWSE_FIRST;			
		
		 boolean thereAreMessages = true;
		 String msgText = "";
		 
		 while (thereAreMessages) {
				try {		
					queue.get(theMessage, gmo);
					gmo.options = MQConstants.MQGMO_WAIT | MQConstants.MQGMO_BROWSE_NEXT;
					msgText = theMessage.readString(theMessage.getMessageLength());
					Map<String, Object> csvEntry = new HashMap<>();
					csvEntry.put("Body", new String(msgText));
					if (includeHeaders) {
						List<MessageHeader> curr_props = readProperties(theMessage);
						csvEntry.put("Header", marshalToJSON(curr_props));
					}
					csvWriter.write(csvEntry, CSV_HEADER);
					msg_count++;
				} catch (MQException e) {
					if (e.reasonCode == MQConstants.MQRC_NO_MSG_AVAILABLE) {
						thereAreMessages = false;
						break;
					}
					mainApp.showException(e);
				} catch (Exception e) {
					mainApp.showException(e);
					return -1;
				} finally {
					csvWriter.flush();
				}
			}
		 
			queue.close();
			csvWriter.close();
			return msg_count;
}
    
    
    public static int getQueueDepth(MQQueueManager qmgr, String qName) throws MQException{    	
    	int openOptions =  MQConstants.MQOO_FAIL_IF_QUIESCING + MQConstants.MQOO_INPUT_SHARED + MQConstants.MQOO_INQUIRE;
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
    
    public static List<MessageHeader> readProperties(MQMessage msg) throws MQException {

		List<MessageHeader> result = new ArrayList<MessageHeader>();
		

		Enumeration p_names = msg.getPropertyNames("%");
		while (p_names.hasMoreElements()) {
			String name = (String) p_names.nextElement();
			result.add(new MessageHeader(name, msg.getObjectProperty(name).toString()));				
		}		
			
		return result;
	}
    
    private static String marshalToJSON(List<MessageHeader> headers) {

        Map<String, Object> acceptedHeaders = new HashMap<>();

        for (MessageHeader header : headers) {           
           acceptedHeaders.put(header.getName(), header.getValue());
        }        

        return new Gson().toJson(acceptedHeaders);
    }    
    
    @SuppressWarnings("unchecked")
    private static Map<String, String> extractMessageHeaders(String json) {
        return new Gson().fromJson(json, HashMap.class);
    }
    
    private static String getExportFileName(String queueName){
		DateFormat df = new SimpleDateFormat("MM_dd_yyyy_HH_mm_ss");
		Date today = Calendar.getInstance().getTime();	
		String reportDate = df.format(today);
		return String.format("/%s_%s.csv", queueName,reportDate); 
	}
}
