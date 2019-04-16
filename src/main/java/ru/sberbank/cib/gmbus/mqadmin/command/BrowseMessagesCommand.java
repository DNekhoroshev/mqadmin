package ru.sberbank.cib.gmbus.mqadmin.command;

import ru.sberbank.cib.gmbus.mqadmin.MQAdmin;
import ru.sberbank.cib.gmbus.mqadmin.model.MQQueueAttributes;

public class BrowseMessagesCommand extends AbstractMqCommand {

	public BrowseMessagesCommand(MQAdmin mainApp) {
		super(mainApp);		
	}

	@Override
	public void execute(Object... objects) {
		if((objects!=null)&&(objects[0] instanceof MQQueueAttributes)){
			MQQueueAttributes queue = (MQQueueAttributes)objects[0];
			mainApp.showBrowseMessageDialog(queue.getNameString(),queue.getDepthLong());
		}
	}

}
