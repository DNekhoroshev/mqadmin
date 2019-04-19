package ru.sberbank.cib.gmbus.mqadmin.view;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import com.ibm.mq.MQException;
import com.ibm.mq.MQGetMessageOptions;
import com.ibm.mq.MQMessage;
import com.ibm.mq.MQQueue;
import com.ibm.mq.constants.MQConstants;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import ru.sberbank.cib.gmbus.mqadmin.MQAdmin;
import ru.sberbank.cib.gmbus.mqadmin.connect.MQHelper;
import ru.sberbank.cib.gmbus.mqadmin.model.MessageHeader;
import ru.sberbank.cib.gmbus.mqadmin.util.CommonConstants;
import ru.sberbank.cib.gmbus.mqadmin.util.PropertiesMenuHandler;

public class MQBrowseMessageController {

	@FXML
	private Label queueNameLabel;

	@FXML
	private CheckBox includeHeadersCheck;

	@FXML
	private CheckBox binaryFomatCheck;

	@FXML
	private Button closeButton;

	@FXML
	private TextArea messageTextArea;

	@FXML
	private TableView<MessageHeader> messagePropertiesTable;

	@FXML
	private TableColumn<MessageHeader, String> messageHeaderColumn;

	@FXML
	private TableColumn<MessageHeader, String> messageValueColumn;

	@FXML
	private TextField selector;

	private MQAdmin mainApp;
	private Stage dialogStage;

	private MQQueue queue;

	private long curr_position;

	private long queueDepth;

	private String queueName;

	private ObservableList<MessageHeader> properties = FXCollections.observableArrayList();

	private boolean thereAreMessages = true;
	
	private final int TRUNCATE_BIG_DATA = 4096;	

	private void refreshProperties(Object o) throws MQException {
		properties.clear();		
		properties.addAll(MQHelper.readProperties((MQMessage) o));
	}

	public String getQueueName() {
		return queueName;
	}

	public void setQueueName(String queueName) {
		this.queueName = queueName;
		queueNameLabel.setText(queueName);
	}

	/**
	 * Инициализация класса-контроллера. Этот метод вызывается автоматически
	 * после того, как fxml-файл будет загружен.
	 */
	@FXML
	private void initialize() {
		messagePropertiesTable.setItems(properties);
		messageHeaderColumn.setCellValueFactory(cellData -> cellData.getValue().getNameProperty());
		messageValueColumn.setCellValueFactory(cellData -> cellData.getValue().getValueProperty());
		messagePropertiesTable.getSelectionModel().setCellSelectionEnabled(true);
		messagePropertiesTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		
		curr_position = 0;
		
		PropertiesMenuHandler.registerContextMenu(messagePropertiesTable);

	}

	public void initialize(String queueName, long queueDepth, MQAdmin mainApp) {
		this.queueName = queueName;
		this.queueDepth = queueDepth;
		this.mainApp = mainApp;

		try {			
			this.queue = MQHelper.getQueue(mainApp.getCurrentSession().getMqManager(), queueName, true);		
		} catch (Exception e) {
			mainApp.showException(e);
		}
		refreshHeader();
	}

	/**
	 * Вызывается главным приложением, которое даёт на себя ссылку.
	 * 
	 * @param mainApp
	 */
	public void setMainApp(MQAdmin mainApp) {
		this.mainApp = mainApp;
	}

	@FXML
	private void handleSelectorChanged() {
		try {			
			this.queue = MQHelper.getQueue(mainApp.getCurrentSession().getMqManager(), queueName, true);
		} catch (Exception e) {
			mainApp.showException(e);
		}
		messageTextArea.clear();
		refreshHeader();
	}

	@FXML
	public void handleClose() {
		Stage stage = (Stage) closeButton.getScene().getWindow();
		try {
			queue.close();			
		} catch (MQException e) {
			mainApp.showException(e);
		}
		stage.close();
	}

	@FXML
	private void handleNext() throws MQException {
		
		MQMessage theMessage    = new MQMessage();
		MQGetMessageOptions gmo = new MQGetMessageOptions();
		gmo.options = MQConstants.MQGMO_WAIT | MQConstants.MQGMO_BROWSE_NEXT;
		gmo.matchOptions=MQConstants.MQMO_NONE;
		gmo.waitInterval=1000;
		
		try{
			queue.get(theMessage,gmo);
			messageTextArea.setText(theMessage.readString(theMessage.getMessageLength()));
			refreshProperties(theMessage);
			curr_position++;
			refreshHeader();
		}catch(MQException e){			
			if(e.reasonCode == e.MQRC_NO_MSG_AVAILABLE) {
	        	mainApp.showWarning("No more messages available!");
	        	thereAreMessages=false;
	        	return;
	        }
	        mainApp.showException(e);	        
	    }catch(Exception e){	    	
	    	mainApp.showException(e);
	    	queue.close();
		}	
		
	}

	@FXML
	private void handleFirst() throws MQException {
		initialize();
		
		MQMessage theMessage    = new MQMessage();
		MQGetMessageOptions gmo = new MQGetMessageOptions();
		gmo.options=MQConstants.MQGMO_WAIT | MQConstants.MQGMO_BROWSE_FIRST;
		gmo.matchOptions=MQConstants.MQMO_NONE;
		gmo.waitInterval=1000;
		
		try{
			queue.get(theMessage,gmo);
			messageTextArea.setText(theMessage.readString(theMessage.getMessageLength()));
			refreshProperties(theMessage);
			curr_position++;
			refreshHeader();
		}catch(MQException e){	        
			if(e.reasonCode == MQConstants.MQRC_NO_MSG_AVAILABLE) {
	        	mainApp.showWarning("No more messages available!");
	        	thereAreMessages=false;	        	
	        	return;
	        }
	        mainApp.showException(e);	        
	    }catch(Exception e){
	    	queue.close();
	    	mainApp.showException(e);
		}	
		refreshHeader();
	}

	@FXML
	private void handleLast() throws MQException, IOException {
		
		MQMessage theMessage    = new MQMessage();
		MQGetMessageOptions gmo = new MQGetMessageOptions();
		
		gmo.matchOptions=MQConstants.MQMO_NONE;
		gmo.waitInterval=100;
		
		if(curr_position==0){
			gmo.options=MQConstants.MQGMO_WAIT | MQConstants.MQGMO_BROWSE_FIRST;			
		}else{
			gmo.options = MQConstants.MQGMO_WAIT | MQConstants.MQGMO_BROWSE_NEXT;			
		}

		String lastMsgText = "";
		while(thereAreMessages){
			try{				
				queue.get(theMessage,gmo); 
				gmo.options = MQConstants.MQGMO_WAIT | MQConstants.MQGMO_BROWSE_NEXT;
				curr_position++;
				lastMsgText = theMessage.readString(theMessage.getMessageLength());
			}catch(MQException e){		        
				if(e.reasonCode == MQConstants.MQRC_NO_MSG_AVAILABLE) {		        	
		        	thereAreMessages=false;	        	
		        	break;
		        }
		        mainApp.showException(e);	        
		    }catch(Exception e){
		    	queue.close();
		    	mainApp.showException(e);
		    	return;
			}	
		}
		messageTextArea.setText(lastMsgText);		
		refreshProperties(theMessage);
		refreshHeader();				
	}

	@FXML
	private void handleSave() {		
		try {
			FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle("Save queue to text file");
			fileChooser.getExtensionFilters().addAll(new ExtensionFilter("Text Files", "*.txt"),
					new ExtensionFilter("All Files", "*.*"));
			File selectedFile = fileChooser.showSaveDialog(dialogStage);
			if ((selectedFile != null)) {

				initialize(this.queueName, this.queueDepth, this.mainApp);
				initialize();
				
				MQMessage theMessage    = new MQMessage();
				MQGetMessageOptions gmo = new MQGetMessageOptions();
				gmo.options=MQConstants.MQGMO_WAIT | MQConstants.MQGMO_BROWSE_FIRST;
				gmo.matchOptions=MQConstants.MQMO_NONE;
				gmo.waitInterval=1000;

				FileWriter fileWriter = new FileWriter(selectedFile);
				int msg_count = 0;
				boolean thereAreMessagesInQ=true;
				while (thereAreMessagesInQ) {
					try{
						queue.get(theMessage,gmo); 
						String msgText = theMessage.readString(theMessage.getMessageLength());
						if (includeHeadersCheck.isSelected()) {
							fileWriter.write(CommonConstants.PROPERTIES_DELIMITER);
							List<MessageHeader> curr_props = MQHelper.readProperties(theMessage);
							for (MessageHeader prop : curr_props)
								fileWriter.write(prop.toString() + "\n");
							fileWriter.write(CommonConstants.BODY_DELIMITER);
						}
						fileWriter.write(msgText + "\n");
						fileWriter.write(CommonConstants.MESSAGE_DELIMITER);
						msg_count++;
						gmo.options = MQConstants.MQGMO_WAIT | MQConstants.MQGMO_BROWSE_NEXT;
					}catch(MQException e){

				        if(e.reasonCode == MQConstants.MQRC_NO_MSG_AVAILABLE) {
				            System.out.println("no more message available or retrived");
				        }

				        thereAreMessages=false;
				    } catch (IOException e) {
				        mainApp.showException(e);
				    }				
					
				}
				fileWriter.close();	
				queue.close();
				mainApp.showInfo(msg_count + " messages saved successfully!");
			}
		} catch (Exception e) {
			mainApp.showException(e);
		}	
	}

	private void refreshHeader() {
		String queueDepthString = String.valueOf(queueDepth);
		if (curr_position > queueDepth) {
			queueDepthString = "Unknown";
			queueDepth = 0;
		}
		this.queueNameLabel.setText(queueName + "[" + String.valueOf(curr_position) + "/" + queueDepthString + "]");
	}
	
}
