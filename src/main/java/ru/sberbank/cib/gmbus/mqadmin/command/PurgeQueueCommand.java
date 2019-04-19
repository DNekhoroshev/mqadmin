package ru.sberbank.cib.gmbus.mqadmin.command;

import ru.sberbank.cib.gmbus.mqadmin.MQAdmin;
import ru.sberbank.cib.gmbus.mqadmin.connect.MQHelper;
import ru.sberbank.cib.gmbus.mqadmin.model.MQQueueAttributes;

public class PurgeQueueCommand extends AbstractMqCommand {

	public PurgeQueueCommand(MQAdmin mainApp) {
		super(mainApp);
	}

	@Override
	public void execute(Object... objects) {		
		if(objects!=null){
			if(objects[0] instanceof MQQueueAttributes){
				MQQueueAttributes queue = (MQQueueAttributes)objects[0];
				try {
					queue = MQHelper.getQueueStatus(mainApp.getCurrentSession(), queue.getNameString());
					if(queue.getReaderCountLong()==0&&queue.getWriterCountLong()==0){
						MQHelper.purgeQueue(mainApp.getCurrentSession(), queue.getNameString(),true);
					}else{
						MQHelper.purgeQueue(mainApp.getCurrentSession(), queue.getNameString(),false);
					}
					mainApp.showInfo(String.format("Purge queue %s", queue.getNameString()));
				} catch (Exception e) {
					mainApp.showException(e);
				}
			}else{
				mainApp.showWarning("This object type is not supported for putting messages!");
			}
		}
	}

}
