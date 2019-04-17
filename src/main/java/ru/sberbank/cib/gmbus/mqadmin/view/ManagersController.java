package ru.sberbank.cib.gmbus.mqadmin.view;

import java.util.List;

import com.ibm.mq.MQQueueManager;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.stage.Stage;
import javafx.util.Callback;
import ru.sberbank.cib.gmbus.mqadmin.MQAdmin;
import ru.sberbank.cib.gmbus.mqadmin.MQAdmin.MQSession;
import ru.sberbank.cib.gmbus.mqadmin.connect.MQHelper;
import ru.sberbank.cib.gmbus.mqadmin.model.MQManagerAttributes;
import ru.sberbank.cib.gmbus.mqadmin.model.MQQueueAttributes;
import ru.sberbank.cib.gmbus.mqadmin.model.ObservableProperty;
import ru.sberbank.cib.gmbus.mqadmin.util.QueueTableMenuHandler;

public class ManagersController {
	
	private MQAdmin mainApp;
    private Stage dialogStage;   
    
    @FXML
	private TabPane mainTab;
    
    @FXML
	private Tab mTab;
	
	@FXML
	private Tab qTab;
	
	@FXML
	private Tab tTab;
	
	@FXML
	private Tab sTab;    
    
    @FXML
    private TreeView<String> qmgrsTree;
    private TreeItem qmgrsRoot = new TreeItem<String>("All queue managers");
    
    // Queue managers table
    @FXML
    private TableView<ObservableProperty> qmgrPropTable;

    @FXML
    private TableColumn<ObservableProperty,String> qmgrPropColumn;
    
    @FXML
    private TableColumn<ObservableProperty,String> qmgrPropValueColumn;
    
    // Queue table
    @FXML
    private TableView<MQQueueAttributes> queueTable;        
    
    @FXML
    private TableColumn<MQQueueAttributes, String> queueNameColumn;
    
    @FXML
    private TableColumn<MQQueueAttributes, String> queueTypeColumn;
    
    @FXML
    private TableColumn<MQQueueAttributes, Number> queueDepthColumn;
    
    @FXML
    private TableColumn<MQQueueAttributes, Number> queueReadersColumn;
    
    @FXML
    private TableColumn<MQQueueAttributes, Number> queueWritersColumn;    
    
    @FXML
    private TextField queueFilterField;
    
    
    @FXML
    private Button showHiddenButton;
    
    @FXML
    private Label currentQmgrLabelQ;
    
    @FXML
    private Label currentQmgrLabelT;
    
    private boolean showHidden = false;    
    
    public void setMainApp(MQAdmin mainApp) {
        this.mainApp = mainApp;
        
        QueueTableMenuHandler.registerContextMenu(queueTable, mainApp);
        
        qmgrsTree.setRoot(qmgrsRoot);
        for(MQManagerAttributes qmgr : mainApp.getMqManagers()){
        	qmgrsRoot.getChildren().add(new TreeItem<MQManagerAttributes>(qmgr));
        }
    }
    
    /**
     * Инициализация класса-контроллера. Этот метод вызывается автоматически
     * после того, как fxml-файл будет загружен.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
	@FXML
    private void initialize() {
    	
    	queueTable.setRowFactory(getQueuesRowFactory());
    	
    	qmgrsTree.getSelectionModel().selectedItemProperty().addListener(
    			(observable, oldValue, newValue) -> showQueueManagerDetails(newValue));   	
    	
    	qmgrsTree.setCellFactory(tv -> new TreeCell() {    	   
			@Override
			protected void updateItem(Object item, boolean empty) {
				super.updateItem(item, empty);
				if(item!=null)
					setText(item.toString());
				if (item instanceof MQManagerAttributes) {		
					MQManagerAttributes mqm = (MQManagerAttributes)item;					
					if(mqm.isConnected()){						
						setStyle("-fx-font-weight: bold; -fx-text-fill: green;");						
					}else{
						setStyle("-fx-font-weight: normal; -fx-text-fill: black;");
					}
				}
			}

    	});    	
    	
    	qmgrPropColumn.setCellValueFactory(cellData -> cellData.getValue().getNameProperty());    	
    	qmgrPropValueColumn.setCellValueFactory(cellData -> {
    			if(cellData.getValue().isHidden()&&!showHidden)
    				return new SimpleStringProperty("<hidden>");
    			else
    				return cellData.getValue().getValueProperty();
    		});    	
    	
    	queueNameColumn.setCellValueFactory(cellData -> cellData.getValue().getName());
    	queueTypeColumn.setCellValueFactory(cellData -> cellData.getValue().getType());
    	queueDepthColumn.setCellValueFactory(cellData -> cellData.getValue().getDepth());
    	queueReadersColumn.setCellValueFactory(cellData -> cellData.getValue().getReaderCount());
    	queueWritersColumn.setCellValueFactory(cellData -> cellData.getValue().getWriterCount());
    }       
    
    private void showQueueManagerDetails(Object qmgr){    	
    	
    	if(qmgr==null)
    		return;
    	
    	Object valueObject = ((TreeItem)qmgr).getValue();
    	
    	if(valueObject!=null){    		
    		if(valueObject instanceof MQManagerAttributes){
    			MQManagerAttributes currentQM = (MQManagerAttributes)valueObject;
    			showQMInfo(currentQM); 
    			
    			ObservableList<MQQueueAttributes> qList = FXCollections.observableArrayList(mainApp.getCurrentQueueList(currentQM,null));
    			queueTable.setItems(qList);
    		}
    	}  	
    }
    
    private void showQMInfo(MQManagerAttributes qmgr){
    	
    	ObservableList<ObservableProperty> qmgrPropList = FXCollections.observableArrayList();
    	qmgrPropList.add(new ObservableProperty("Name", qmgr.getQmName()));
    	qmgrPropList.add(new ObservableProperty("Host", qmgr.getHostName()));
    	qmgrPropList.add(new ObservableProperty("Port", qmgr.getPort()));
    	qmgrPropList.add(new ObservableProperty("Channel", qmgr.getChannel()));
    	if(qmgr.getCipherSuite()!=null){
    		qmgrPropList.add(new ObservableProperty("CipherSuite", qmgr.getCipherSuite()));
    		qmgrPropList.add(new ObservableProperty("Personal store", qmgr.getKeyStorePath()));
    		qmgrPropList.add(new ObservableProperty("Personal store password", qmgr.getKeyStorePassword(),true));
    		qmgrPropList.add(new ObservableProperty("Trusted store", qmgr.getTrustStorePath()));
    		qmgrPropList.add(new ObservableProperty("Trusted store password", qmgr.getTrustStorePassword(),true));
    	}
    	qmgrPropList.add(new ObservableProperty("Connected", String.valueOf(qmgr.isConnected())));
    	qmgrPropTable.setItems(qmgrPropList);
    	if(qmgr.isConnected()){
    		currentQmgrLabelQ.setText(qmgr.toString());
			currentQmgrLabelT.setText(qmgr.toString());
    	}else{
    		currentQmgrLabelQ.setText("<Not connected>");
			currentQmgrLabelT.setText("<Not connected>");
    	}    	
    }
    
    @FXML
    public void handleTabSelect(){    	
    	if(mainApp==null){    		
    		if(queueTable!=null)
    			queueTable.setItems(MQAdmin.EMPTY_QUEUE_LIST);
    		return;			
    	}
		
    	TreeItem<MQManagerAttributes> selectedManager = (TreeItem)qmgrsTree.getSelectionModel().getSelectedItem();
    	
    	try {
    		if(mTab.isSelected()){    			
    			mainApp.setCurrentSession(null);
    		}else if(qTab.isSelected()){    			
    			mainApp.setCurrentSession(mainApp.getConnCache().get(selectedManager.getValue()));
    		}else if(tTab.isSelected()){
    			mainApp.setCurrentSession(mainApp.getConnCache().get(selectedManager.getValue()));
    		}else if(sTab.isSelected()){
    			mainApp.setCurrentSession(mainApp.getConnCache().get(selectedManager.getValue()));
    		} 
    	}catch(Exception e){
    		mainApp.showException(e);
    	}
    }
    
    @FXML
    public void handleConnect(){
    	TreeItem<MQManagerAttributes> selectedManager = (TreeItem)qmgrsTree.getSelectionModel().getSelectedItem();
    	
    	if(selectedManager.getValue().isConnected()){
    		return;
    	}
    	
    	try {
			mainApp.getConnCache().put(
					selectedManager.getValue(),
					MQHelper.getMQSession(selectedManager.getValue())
					);
			selectedManager.getValue().setConnected(true);		
			showQMInfo(selectedManager.getValue());			
			refreshQueues(selectedManager.getValue());			
			qmgrsTree.refresh();
		} catch (Exception e) {
			mainApp.showException(e);
		}
    }  
    
    @FXML
    public void handleDisconnect(){
    	TreeItem<MQManagerAttributes> selectedManager = (TreeItem)qmgrsTree.getSelectionModel().getSelectedItem();
    	
    	if(!selectedManager.getValue().isConnected()){
    		return;
    	}
    	
    	try {			
			mainApp.getConnCache().get(selectedManager.getValue()).getMqManager().close();
			mainApp.getConnCache().remove(selectedManager.getValue());
			selectedManager.getValue().setConnected(false);
			showQMInfo(selectedManager.getValue());
			refreshQueues(selectedManager.getValue());
			qmgrsTree.refresh();
		} catch (Exception e) {
			mainApp.showException(e);
		}    	
    }
        
    @FXML
    public void handleShowHidden(){
    	showHidden = !showHidden;
    	if(showHidden){
    		showHiddenButton.setText("Hide hidden");
    	}else{
    		showHiddenButton.setText("Show hidden");
    	}
    	qmgrPropTable.refresh();
    }
    
    @FXML
    private void handleQueueFilterChanged(){
    	String filterString = queueFilterField.getText();
    	    	
    	queueTable.getItems().clear(); 
    	
		try {
			TreeItem<MQManagerAttributes> selectedManager = (TreeItem)qmgrsTree.getSelectionModel().getSelectedItem();			

			ObservableList<MQQueueAttributes> qList = FXCollections.observableArrayList(mainApp.getCurrentQueueList(selectedManager.getValue(),filterString));
			queueTable.setItems(qList);			
			
		} catch (Exception e) {
				mainApp.showException(e);
		}
    }    
    
    @FXML
    private void handleClose(){   	
    	System.exit(0);
    }
    
    @FXML
    private void handleRefresh(){    	
    	TreeItem<MQManagerAttributes> selectedManager = (TreeItem)qmgrsTree.getSelectionModel().getSelectedItem();
    	refreshQueues(selectedManager.getValue());
    }
    
    private void refreshQueues(MQManagerAttributes qmAttrs) {
		
    	MQSession session = mainApp.getCurrentQMSession(qmAttrs);
    	if(session==null){
    		queueTable.setItems(mainApp.EMPTY_QUEUE_LIST);
    		return;
    	}    	
		
    	mainApp.getCurrentQueueList(qmAttrs,null).clear();
    	
    	try {
			List<String> queueNames = MQHelper.getQueueNames(session);			
			for (String queueName : queueNames) {				
				MQQueueAttributes queueAttrs = MQHelper.getQueueStatus(session, queueName);				
				mainApp.getCurrentQueueList(qmAttrs,null).add(queueAttrs);
			}
		} catch (Exception e) {
			mainApp.showException(e);
		}
		
		ObservableList<MQQueueAttributes> qList = FXCollections.observableArrayList(mainApp.getCurrentQueueList(qmAttrs,queueFilterField.getText()));
		queueTable.setItems(qList);		
	}   
    
    private Callback<TableView<MQQueueAttributes>, TableRow<MQQueueAttributes>> getQueuesRowFactory(){
    	return new Callback<TableView<MQQueueAttributes>, TableRow<MQQueueAttributes>>() {
            @Override
            public TableRow<MQQueueAttributes> call(TableView<MQQueueAttributes> tableView) {
                final TableRow<MQQueueAttributes> row = new TableRow<MQQueueAttributes>() {
                    @Override
                    protected void updateItem(MQQueueAttributes q, boolean empty){
                        super.updateItem(q, empty);                                              
                        if((q!=null)&&(q.isUpdated())) {                          	
                          	setStyle("-fx-background-color:lightyellow");                       
                        }else{
                        	setStyle("-fx-background-color:lightgreen");
                        }
                    }
                };
                return row;
            }
    	};
    }
    
}
