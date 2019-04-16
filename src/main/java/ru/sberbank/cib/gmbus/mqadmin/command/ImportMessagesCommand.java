package ru.sberbank.cib.gmbus.mqadmin.command;

import java.io.File;

import javafx.stage.DirectoryChooser;
import ru.sberbank.cib.gmbus.mqadmin.MQAdmin;
import ru.sberbank.cib.gmbus.mqadmin.concurrent.ImportMessageTask.SourceFormat;
import ru.sberbank.cib.gmbus.mqadmin.connect.MQHelper;
import ru.sberbank.cib.gmbus.mqadmin.model.MQQueueAttributes;

public class ImportMessagesCommand extends AbstractMqCommand {

	private DirectoryChooser chooser = new DirectoryChooser();
	
	public ImportMessagesCommand(MQAdmin mainApp) {
		super(mainApp);		
	}

	@Override
	public void execute(Object... objects) {
		if((objects!=null)&&(objects[0] instanceof MQQueueAttributes)){
			MQQueueAttributes queue = (MQQueueAttributes)objects[0];
			String command = (String)objects[1];
			SourceFormat format = null;
			String title = "";
			
			if("Import messages (CSV)".equals(command)){
				format = SourceFormat.CSV;
				title = "Import messages from csv files";
			}else if("Import messages (PLAIN)".equals(command)){
				format = SourceFormat.PLAIN;
				title = "Import messages from plain files";
			}else
				return;
						
			
			chooser.setTitle(title); 
			File selectedDir = chooser.showDialog(null);			
			 if ((selectedDir != null)){				
				 chooser.setInitialDirectory(selectedDir);
				 try {				
					 MQHelper.importMessages(mainApp,queue.getNameString(),selectedDir,format);										
				} catch (Exception e) {
					mainApp.showException(e);
				}				 
			 }	
		}
	}

}
