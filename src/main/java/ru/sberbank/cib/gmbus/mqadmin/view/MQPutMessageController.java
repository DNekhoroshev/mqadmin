package ru.sberbank.cib.gmbus.mqadmin.view;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import javax.jms.JMSException;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputDialog;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import ru.sberbank.cib.gmbus.mqadmin.MQAdmin;
import ru.sberbank.cib.gmbus.mqadmin.connect.MQHelper;
import ru.sberbank.cib.gmbus.mqadmin.model.MessageHeader;

public class MQPutMessageController {
	
	@FXML
	private Button closeButton;
	
	@FXML
	private Label queueNameLabel;
	
	@FXML
	private TextArea messageText; 
	
	@FXML
	private CheckBox persistence;
	
	@FXML
	private TableView<MessageHeader> headersTable;
	
	@FXML
	private TableColumn<MessageHeader,String> headerNameColumn;
	
	@FXML
	private TableColumn<MessageHeader,String> headerValueColumn;
	
	private String queueName;
		
	// Ссылка на главное приложение.
    private MQAdmin mainApp;
	
    private Stage dialogStage;
    
    private ObservableList<MessageHeader> headers = FXCollections.observableArrayList();
        
	private byte[] byteBuffer;
    
	private FileChooser bin_chooser = new FileChooser();
	private FileChooser txt_chooser = new FileChooser();
	
    /**
     * Вызывается главным приложением, которое даёт на себя ссылку.
     * 
     * @param mainApp
     */
    public void setMainApp(MQAdmin mainApp) {
        this.mainApp = mainApp;        
    }
    
    /**
     * Инициализация класса-контроллера. Этот метод вызывается автоматически
     * после того, как fxml-файл будет загружен.
     */
    @FXML
    private void initialize() {
        // Инициализация таблицы заголовков.
        headersTable.setItems(headers);
    	headerNameColumn.setCellValueFactory(cellData -> cellData.getValue().getNameProperty());
        headerValueColumn.setCellValueFactory(cellData -> cellData.getValue().getValueProperty());   
    }
    
    
    @FXML
    private void handleClose(){    	
    	Stage stage = (Stage) closeButton.getScene().getWindow();        
        stage.close();
    }

	@FXML
	private void handlePutMessage(){
		if((queueName==null)||(queueName.isEmpty()))
			return;
		
		String text = messageText.getText();		
		
		if((text==null)||(text.isEmpty()))
			return;
		
		boolean persist = persistence.isSelected();
		
		Map<String,String> snd_headers = new HashMap<String,String>();
		for(MessageHeader h : headers){			
			snd_headers.put(h.getName(), h.getValue());
		}		
		
		try {
			if(byteBuffer==null)
				MQHelper.send(mainApp.getCurrentSession().getMqManager(),queueName, persist, snd_headers, text);
			else
				MQHelper.send(mainApp.getCurrentSession().getMqManager(),queueName, persist, snd_headers, byteBuffer);
			mainApp.showInfo("Message sent!");			
		} catch (Exception e) {
			mainApp.showException(e);
		} finally{
			messageText.clear();
			headers.clear();
			byteBuffer = null;
		}
	}
    
	@FXML
	private void handleClearHeaders(){
		headers.clear();		
	}
	
	@FXML
	private void handleAddHeaderButton(){
		TextInputDialog dialog = new TextInputDialog("");
		dialog.setTitle("Input header");
		dialog.setHeaderText("Input header value in form <header name> = <value>");
		dialog.setContentText("Header:");

		// Traditional way to get the response value.
		Optional<String> result = dialog.showAndWait();
		
		// The Java 8 way to get the response value (with lambda expression).
		//result.ifPresent(header -> System.out.println("Header added: " + header));
		
		MessageHeader h = createHeader(result.get());
		
		if(h!=null)
			result.ifPresent(header -> headers.add(h));
	}
	
   	@FXML
	private void handleOpenTextFile(){
		
	    txt_chooser.setTitle("Open File");
	    File file = txt_chooser.showOpenDialog(new Stage());
	    if(file==null)
	    	return;
	    
	    txt_chooser.setInitialDirectory(file.getParentFile());
	    
	    String text = "";
	    try {
			text = new String(Files.readAllBytes(file.toPath()));
		} catch (IOException e) {			
			e.printStackTrace();
		}
	    messageText.setText(text);
	}
	
   	@FXML
	private void handleOpenBinaryFile(){  			 		 		
   		
   		bin_chooser.setTitle("Open File");
	    File file = bin_chooser.showOpenDialog(new Stage());
	    if(file==null)
	    	return;
	    
	    bin_chooser.setInitialDirectory(file.getParentFile());
	    
	    String text = "";
	    try {
			text = new String(Files.readAllBytes(file.toPath()));
			byteBuffer = Files.readAllBytes(file.toPath());
		} catch (IOException e) {			
			e.printStackTrace();
		}
	    messageText.setText(text);
	}
   	
   	
	public String getQueueName() {
		return queueName;
	}

	public void setQueueName(String queueName) {
		this.queueName = queueName;
		queueNameLabel.setText(queueName);
	}

	public Stage getDialogStage() {
		return dialogStage;
	}

	public void setDialogStage(Stage dialogStage) {
		this.dialogStage = dialogStage;
	}
    
	private MessageHeader createHeader(String parseHeader){
		String[] a_header = parseHeader.split("=");
		if(a_header.length!=2){
			return null;
		}
		return new MessageHeader(a_header[0], a_header[1]);		
	}
	
	public void setInitialProps(Properties initialProps) {
		if (initialProps != null) {
			for (String key : initialProps.stringPropertyNames()) {
				headers.add(new MessageHeader(key, initialProps.getProperty(key)));
			}
		}
	}	
		
}
