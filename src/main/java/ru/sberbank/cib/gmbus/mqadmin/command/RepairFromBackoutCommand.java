package ru.sberbank.cib.gmbus.mqadmin.command;

import ru.sberbank.cib.gmbus.mqadmin.MQAdmin;
import ru.sberbank.cib.gmbus.mqadmin.connect.MQHelper;
import ru.sberbank.cib.gmbus.mqadmin.model.MQQueueAttributes;

public class RepairFromBackoutCommand extends AbstractMqCommand {

	public RepairFromBackoutCommand(MQAdmin mainApp) {
		super(mainApp);
	}

	@Override
	public void execute(Object... objects) {		
		if(objects!=null){
			if(objects[0] instanceof MQQueueAttributes){
				MQQueueAttributes queue = (MQQueueAttributes)objects[0];
				if(queue.getNameString().endsWith(".BACK")){					
					try {
						MQHelper.moveMessages(mainApp.getCurrentSession().getMqManager(), queue.getNameString(), queue.getNameString().replace(".BACK", ""));
					} catch (Exception e) {
						mainApp.showException(e);
					}
				}else{
					mainApp.showWarning("This is not backout queue!");
				}
			}else{
				mainApp.showWarning("This object type is not supported for putting messages!");
			}
		}
	}

}
