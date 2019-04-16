package ru.sberbank.cib.gmbus.mqadmin;

import java.util.Hashtable;

import com.ibm.mq.MQMessage;
import com.ibm.mq.constants.MQConstants;
import com.ibm.mq.pcf.CMQC;
import com.ibm.mq.pcf.CMQCFC;
import com.ibm.mq.pcf.MQCFH;
import com.ibm.mq.pcf.MQCFIN;
import com.ibm.mq.pcf.MQCFSL;
import com.ibm.mq.pcf.MQCFST;
import com.ibm.mq.pcf.PCFAgent;
import com.ibm.mq.pcf.PCFParameter;

import ru.sberbank.cib.gmbus.mqadmin.connect.MQHelper;

public class Test {

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		com.ibm.mq.MQQueueManager qmRequest = null;
        com.ibm.mq.MQQueue qRequest = null;

        com.ibm.mq.MQMessage reqMsg = new com.ibm.mq.MQMessage();
        reqMsg.writeString(new String("first MQ SSL Message test"));
        reqMsg.messageId = "1".getBytes();
        reqMsg.correlationId = "2".getBytes(); 

        System.setProperty("javax.net.ssl.trustStore", "C:\\tmp\\SSL\\new_bus\\trustcib.jks");
        System.setProperty("javax.net.ssl.trustStorePassword", "password");
        System.setProperty("javax.net.ssl.keyStore", "C:\\tmp\\SSL\\new_bus\\client.jks"); 
        System.setProperty("javax.net.ssl.keyStorePassword", "password"); 
        Hashtable<String,Object> propMap = new Hashtable<String,Object>();

        propMap.put(MQConstants.HOST_NAME_PROPERTY, "gmbus-mq-dev-01");
        propMap.put(MQConstants.PORT_PROPERTY,  1414);
        propMap.put(MQConstants.CHANNEL_PROPERTY, "CIB.SVRCONN");
        propMap.put(MQConstants.SSL_CIPHER_SUITE_PROPERTY,  "TLS_RSA_WITH_AES_128_CBC_SHA256");

        String qmNameRequest = "GMSB_SEGMENT1_INOUT_DEV1";
        
        qmRequest = new com.ibm.mq.MQQueueManager(qmNameRequest,propMap);
        
        //MQHelper.getQueueStatus(qmRequest, "ALIASQ");        
        
        /*String qNameRequest = "ABC";                

        int reqQueueOpt = MQConstants.MQOO_OUTPUT | MQConstants.MQOO_SET_IDENTITY_CONTEXT;
        qRequest = qmRequest.accessQueue(qNameRequest, reqQueueOpt);
        reqMsg.messageType = MQConstants.MQMT_DATAGRAM;
        com.ibm.mq.MQPutMessageOptions reqMsgOpt = new com.ibm.mq.MQPutMessageOptions();
        reqMsgOpt.options = MQConstants.MQPMO_SET_IDENTITY_CONTEXT;

        qRequest.put(reqMsg, reqMsgOpt);
        qRequest.close();*/
        
        
	}

}
