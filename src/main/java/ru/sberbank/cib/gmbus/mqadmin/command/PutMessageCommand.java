package ru.sberbank.cib.gmbus.mqadmin.command;

import ru.sberbank.cib.gmbus.mqadmin.MQAdmin;
import ru.sberbank.cib.gmbus.mqadmin.model.MQQueueAttributes;

public class PutMessageCommand extends AbstractMqCommand {

	public PutMessageCommand(MQAdmin mainApp) {
		super(mainApp);
	}

	@Override
	public void execute(Object... objects) {		
		if(objects!=null){
			if(objects[0] instanceof MQQueueAttributes){
				MQQueueAttributes queue = (MQQueueAttributes)objects[0];
				mainApp.showPutMessageDialog(queue.getNameString(),null);
			}else{
				mainApp.showWarning("This object type is not supported for putting messages!");
			}
		}
	}

}
