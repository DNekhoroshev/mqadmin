package ru.sberbank.cib.gmbus.mqadmin.util;

import java.util.HashMap;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import ru.sberbank.cib.gmbus.mqadmin.MQAdmin;
import ru.sberbank.cib.gmbus.mqadmin.command.ConnectCommand;
import ru.sberbank.cib.gmbus.mqadmin.command.MQAdmMenuCommand;
import ru.sberbank.cib.gmbus.mqadmin.command.PutMessageCommand;

public class ManagersMenuHandler implements EventHandler<ActionEvent> {

	private TreeView treeView;	
	private MQAdmin mainApp;	
	private HashMap<String,MQAdmMenuCommand> commands = new HashMap<>();
	
	public ManagersMenuHandler(TreeView treeView, MQAdmin mainApp) {
		super();
		this.treeView = treeView;
		this.mainApp = mainApp;
		
		commands.put("Connect", new ConnectCommand(mainApp));
		commands.put("Disconnect", new ConnectCommand(mainApp));
	}

	@Override
	public void handle(ActionEvent event) {
		TreeItem target = (TreeItem)treeView.getSelectionModel().getSelectedItem();	
		
		MenuItem menuItem = (MenuItem)event.getTarget();
		Object targetValue = target.getValue();
		
		String command = menuItem.getText();
		
		commands.get(command).execute(targetValue,command);	
		treeView.refresh();
	}

	public static void registerContextMenu(TreeView tree, MQAdmin mainApp){
				
		ContextMenu menu = new ContextMenu();
		MenuItem connect_item = new MenuItem("Connect");		
		MenuItem disconnect_item = new MenuItem("Disconnect");
    	
		connect_item.setOnAction(new ManagersMenuHandler(tree,mainApp));    	
		disconnect_item.setOnAction(new ManagersMenuHandler(tree,mainApp));
    	
		menu.getItems().add(connect_item);    	
		menu.getItems().add(disconnect_item);
    	tree.setContextMenu(menu);    	
	}
}
