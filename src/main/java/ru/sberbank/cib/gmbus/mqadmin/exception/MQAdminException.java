package ru.sberbank.cib.gmbus.mqadmin.exception;

import com.ibm.mq.MQException;
import com.ibm.mq.MQMessage;
import com.ibm.mq.pcf.MQCFH;
import com.ibm.mq.pcf.PCFMessage;
import com.ibm.mq.pcf.PCFParameter;

public class MQAdminException extends Exception {
	private int rc;
	private String description;	
	
	public MQAdminException(int rc, MQMessage response) {		
		this.rc = rc;
		StringBuilder desc = new StringBuilder();
		MQCFH cfh;
		try {
			cfh = new MQCFH (response);
			for (int i = 0; i < cfh.parameterCount; i++)
			{
				desc.append(PCFParameter.nextParameter (response));
			}
		} catch (Exception ex) {
			desc.append("Unknown error");
		}		
		description = desc.toString();
	}		
	
	public MQAdminException(MQException e) {		
		super(e);
		this.rc = e.getReason();
		StringBuilder desc = new StringBuilder();
		description = desc.append("CC: ").append(e.completionCode).append(", RC: ").append(e.reasonCode).toString();		 
	}

	@Override
	public String toString() {
		return "MQAdminException [rc=" + rc + ", description=" + description + "]";
	}

	public int getRc() {
		return rc;
	}

	public void setRc(int rc) {
		this.rc = rc;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}	
}
