package ru.sberbank.cib.gmbus.mqadmin.util;

import java.util.HashMap;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableView;
import ru.sberbank.cib.gmbus.mqadmin.MQAdmin;
import ru.sberbank.cib.gmbus.mqadmin.command.BrowseMessagesCommand;
import ru.sberbank.cib.gmbus.mqadmin.command.ExportMessagesCommand;
import ru.sberbank.cib.gmbus.mqadmin.command.ImportMessagesCommand;
import ru.sberbank.cib.gmbus.mqadmin.command.MQAdmMenuCommand;
import ru.sberbank.cib.gmbus.mqadmin.command.PurgeQueueCommand;
import ru.sberbank.cib.gmbus.mqadmin.command.PutMessageCommand;
import ru.sberbank.cib.gmbus.mqadmin.command.RepairFromBackoutCommand;
import ru.sberbank.cib.gmbus.mqadmin.model.MQQueueAttributes;

public class QueueTableMenuHandler implements EventHandler<ActionEvent> {

	private TableView tableView;	
	private MQAdmin mainApp;
	private HashMap<String,MQAdmMenuCommand> commands = new HashMap<String,MQAdmMenuCommand>();
	
	public QueueTableMenuHandler(TableView tableView, MQAdmin mainApp) {	
		this.tableView = tableView;
		this.mainApp = mainApp;
		
		commands.put("Put message", new PutMessageCommand(mainApp));
		commands.put("Browse messages", new BrowseMessagesCommand(mainApp));
		commands.put("Repair from backout", new RepairFromBackoutCommand(mainApp));
		commands.put("Export messages", new ExportMessagesCommand(mainApp));
		commands.put("Import messages (CSV)", new ImportMessagesCommand(mainApp));
		commands.put("Import messages (PLAIN)", new ImportMessagesCommand(mainApp));
		commands.put("Purge", new PurgeQueueCommand(mainApp));
	}

	@Override
	public void handle(ActionEvent evt) {
					
		if(tableView==null)
			return;
		
		MenuItem item = (MenuItem)evt.getTarget();
				
		MQQueueAttributes queue = (MQQueueAttributes)tableView.getSelectionModel().getSelectedItem();	
		String command = item.getText();
		
		commands.get(item.getText()).execute(queue,command);		
		
	}
	
	public static void registerContextMenu(TableView table, MQAdmin mainApp){
		
		table.getSelectionModel().setCellSelectionEnabled(false);    	
		
		ContextMenu menu = new ContextMenu();
		MenuItem pm_item = new MenuItem("Put message");
		MenuItem br_item = new MenuItem("Browse messages");
		MenuItem repair_item = new MenuItem("Repair from backout");
		MenuItem export_item = new MenuItem("Export messages");
		MenuItem import_csv_item = new MenuItem("Import messages (CSV)");
		MenuItem import_txt_item = new MenuItem("Import messages (PLAIN)");
		MenuItem purge_item = new MenuItem("Purge");
    	pm_item.setOnAction(new QueueTableMenuHandler(table,mainApp));
    	br_item.setOnAction(new QueueTableMenuHandler(table,mainApp));
    	repair_item.setOnAction(new QueueTableMenuHandler(table,mainApp));
    	export_item.setOnAction(new QueueTableMenuHandler(table,mainApp));
    	import_csv_item.setOnAction(new QueueTableMenuHandler(table,mainApp));
    	import_txt_item.setOnAction(new QueueTableMenuHandler(table,mainApp));
    	purge_item.setOnAction(new QueueTableMenuHandler(table,mainApp));
    	menu.getItems().add(pm_item);
    	menu.getItems().add(br_item);
    	menu.getItems().add(repair_item);
    	menu.getItems().add(export_item);
    	menu.getItems().add(import_csv_item);
    	menu.getItems().add(import_txt_item);
    	menu.getItems().add(purge_item);
    	table.setContextMenu(menu);    	
	}
	
}
