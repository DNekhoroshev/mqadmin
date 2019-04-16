package ru.sberbank.cib.gmbus.mqadmin.util;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

public class PropertiesMenuHandler implements EventHandler<ActionEvent> {

	private TableView tableView;

	public PropertiesMenuHandler(TableView tv){
		this.tableView = tv;
	}
	
	@Override
	public void handle(ActionEvent event) {
		
		if(tableView==null)
			return;
		
		ObservableList<TablePosition> posList = tableView.getSelectionModel().getSelectedCells();
		int old_r = -1;
		StringBuilder clipboardString = new StringBuilder();		
		for (TablePosition p : posList) {
			int r = p.getRow();
			int c = p.getColumn();
			TableColumn nameColumn = (TableColumn) tableView.getColumns().get(0);
			TableColumn valueColumn = (TableColumn) tableView.getColumns().get(1);
						
			Object nameCell = nameColumn.getCellData(r);
			Object valueCell = valueColumn.getCellData(r);
					
			if (nameCell == null)
				nameCell = "";
			if (valueCell == null)
				valueCell = "";
			
			if (old_r == r)
				continue;
			else if (old_r != -1)
				clipboardString.append('\n');
			
			try{
				Long.parseLong(valueCell.toString());
			}catch(NumberFormatException e){
				valueCell = "'"+valueCell+"'";
			}
			
			clipboardString.append(nameCell+" = "+valueCell);
			old_r = r;
		}
		final ClipboardContent content = new ClipboardContent();
		content.putString(clipboardString.toString());
		Clipboard.getSystemClipboard().setContent(content);
	}

	public static void registerContextMenu(TableView table){
		
		table.getSelectionModel().setCellSelectionEnabled(true);
    	table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		
		ContextMenu menu = new ContextMenu();
    	MenuItem item = new MenuItem("Copy");
    	item.setOnAction(new PropertiesMenuHandler(table));
    	menu.getItems().add(item);
    	table.setContextMenu(menu);
	}
}
