package ru.sberbank.cib.gmbus.mqadmin.command;

import ru.sberbank.cib.gmbus.mqadmin.MQAdmin;
import ru.sberbank.cib.gmbus.mqadmin.connect.MQHelper;
import ru.sberbank.cib.gmbus.mqadmin.model.MQManagerAttributes;

public class ConnectCommand extends AbstractMqCommand {

	public ConnectCommand(MQAdmin mainApp) {
		super(mainApp);
	}

	@Override
	public void execute(Object... objects) {		
		if(objects!=null){
			if(objects[0] instanceof MQManagerAttributes){
				MQManagerAttributes qmgr = (MQManagerAttributes)objects[0];
				String action = (String)objects[1];
				if("Connect".equals(action)){
					if(qmgr.isConnected()){
			    		return;
			    	}
			    	
			    	try {
						mainApp.getConnCache().put(
								qmgr,
								MQHelper.getMQSession(qmgr)
								);
						qmgr.setConnected(true);
						mainApp.refreshQueues(qmgr);
					} catch (Exception e) {
						mainApp.showException(e);
					}
				}else if("Disconnect".equals(action)){
					if(!qmgr.isConnected()){
			    		return;
			    	}
			    	
			    	try {			
						mainApp.getConnCache().get(qmgr).getMqManager().close();
						mainApp.getConnCache().remove(qmgr);
						qmgr.setConnected(false);
						mainApp.refreshQueues(qmgr);
					} catch (Exception e) {
						mainApp.showException(e);
					}    
				}else{
					mainApp.showWarning("Unknown action!");
				}
			}else{
				mainApp.showWarning("This object type is not supported for connect/disconnect!");
			}
		}
	}

}
