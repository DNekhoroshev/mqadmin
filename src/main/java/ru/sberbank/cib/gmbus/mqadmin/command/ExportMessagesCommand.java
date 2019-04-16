package ru.sberbank.cib.gmbus.mqadmin.command;

import java.io.File;

import javafx.stage.DirectoryChooser;
import ru.sberbank.cib.gmbus.mqadmin.MQAdmin;
import ru.sberbank.cib.gmbus.mqadmin.connect.MQHelper;
import ru.sberbank.cib.gmbus.mqadmin.model.MQQueueAttributes;

public class ExportMessagesCommand extends AbstractMqCommand {

	private DirectoryChooser chooser = new DirectoryChooser();
	
	public ExportMessagesCommand(MQAdmin mainApp) {
		super(mainApp);		
	}

	@Override
	public void execute(Object... objects) {
		if((objects!=null)&&(objects[0] instanceof MQQueueAttributes)){
			MQQueueAttributes queue = (MQQueueAttributes)objects[0];
			
			chooser.setTitle("Save queue to binary files");
			
			File selectedDir = chooser.showDialog(null);			
									
			if ((selectedDir != null)){
				String selectedDirPath = selectedDir.getAbsolutePath();
				chooser.setInitialDirectory(selectedDir);
				try{
					long exported = MQHelper.exportMessages(mainApp, queue.getNameString(), selectedDirPath, true);
					mainApp.showInfo(String.format("%d messages exported successfully!", exported));
				}catch(Exception e){
					mainApp.showException(e);
				}
			}	
		}
	}

}
