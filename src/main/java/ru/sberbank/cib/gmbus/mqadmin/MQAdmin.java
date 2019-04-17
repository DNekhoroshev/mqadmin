package ru.sberbank.cib.gmbus.mqadmin;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

import org.controlsfx.control.StatusBar;

import com.ibm.mq.MQQueueManager;
import com.ibm.mq.pcf.PCFMessageAgent;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import ru.sberbank.cib.gmbus.mqadmin.model.MQManagerAttributes;
import ru.sberbank.cib.gmbus.mqadmin.model.MQQueueAttributes;
import ru.sberbank.cib.gmbus.mqadmin.view.MQBrowseMessageController;
import ru.sberbank.cib.gmbus.mqadmin.view.MQPutMessageController;
import ru.sberbank.cib.gmbus.mqadmin.view.ManagersController;
import ru.sberbank.cib.gmbus.mqadmin.view.RootLayoutController;

public class MQAdmin extends Application {

	public StatusBar statusBar;
	private Stage primaryStage,broweMessagesStage,putMessasgeStage;
	
	public static class MQSession {
		private MQQueueManager mqManager;
		private PCFMessageAgent mqPCFAgent;		
			
		public MQSession(MQQueueManager mqManager, PCFMessageAgent mqPCFAgent) {
			super();
			this.mqManager = mqManager;
			this.mqPCFAgent = mqPCFAgent;
		}
		
		public MQQueueManager getMqManager() {
			return mqManager;
		}
		public PCFMessageAgent getMqPCFAgent() {
			return mqPCFAgent;
		}	
		
	}
	
	private MQSession currentSession;
	
	private List<MQManagerAttributes> mqManagers = new ArrayList<>();
	private Map<MQManagerAttributes,MQSession> connCache = new HashMap<>();
	
	private Map<MQManagerAttributes,List<MQQueueAttributes>> cachedQueueList = new HashMap<>();
	
	public static final ObservableList<MQQueueAttributes> EMPTY_QUEUE_LIST = FXCollections.observableArrayList();
	
	public Stage getPrimaryStage() {
		return primaryStage;
	}

	public void setPrimaryStage(Stage primaryStage) {
		this.primaryStage = primaryStage;
	}

	private BorderPane rootLayout;  
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		this.primaryStage = primaryStage;
        this.primaryStage.setTitle("IBM MQ administration tool");
        
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent t) {
                Platform.exit();
                System.exit(0);
            }
        });   
        
        initRootLayout();                
	}

	public static void main(String[] args) {
		launch(args);
	}
	
	/**
     * Инициализирует корневой макет.
     */
    public void initRootLayout() {
    	try {
            // Загружаем корневой макет из fxml файла.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MQAdmin.class.getResource("view/RootLayout.fxml"));
            rootLayout = (BorderPane) loader.load();

            // Отображаем сцену, содержащую корневой макет.
            Scene scene = new Scene(rootLayout);
            this.primaryStage.setScene(scene);
            // Даём контроллеру доступ к главному прилодению.
            RootLayoutController controller = loader.getController();
            controller.setMainApp(this);                    
            statusBar = new StatusBar();            
            rootLayout.setBottom(statusBar);
            
            getPrimaryStage().show();
        } catch (IOException e) {
        	showException(e);
        }
    }	

    public void showManagers(){
    	try {            
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MQAdmin.class.getResource("view/Managers.fxml"));
            TabPane managersOverview = (TabPane) loader.load();

            // Помещаем сведения об адресатах в центр корневого макета.
            rootLayout.setCenter(managersOverview);
            
            // Даём контроллеру доступ к главному приложению.
            ((ManagersController)loader.getController()).setMainApp(this);
                        
        } catch (IOException e) {
        	showException(e);
        }
    }
    
    public void showBrowseMessageDialog(String queueName, Long queueDepth){    	 	
    	try {            
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MQAdmin.class.getResource("view/BrowseMessagesView.fxml"));
            BorderPane personOverview = (BorderPane) loader.load();

            // Даём контроллеру доступ к главному приложению.
            MQBrowseMessageController controller = loader.getController();
            controller.initialize(queueName,queueDepth,this);            
            
            //Отображаем сцену, содержащую корневой макет.
            broweMessagesStage = new Stage();
            broweMessagesStage.setTitle("Browse messages");
            broweMessagesStage.initModality(Modality.APPLICATION_MODAL);
            
            Scene scene = new Scene(personOverview);
            broweMessagesStage.setScene(scene);
            broweMessagesStage.show();            
        } catch (IOException e) {
            showException(e);
        } 
    }
    
    public void showPutMessageDialog(String queueName,Properties props){    		
    	try {            
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MQAdmin.class.getResource("view/PutMessageView.fxml"));
            BorderPane personOverview = (BorderPane) loader.load();

            // Даём контроллеру доступ к главному приложению.
            MQPutMessageController controller = loader.getController();
            controller.setMainApp(this);  
            controller.setInitialProps(props);
            controller.setQueueName(queueName);
            
            //Отображаем сцену, содержащую корневой макет.
            putMessasgeStage = new Stage();
            putMessasgeStage.setTitle("Put message");
            putMessasgeStage.initModality(Modality.APPLICATION_MODAL);
            
            Scene scene = new Scene(personOverview);
            putMessasgeStage.setScene(scene);
            putMessasgeStage.show();            
        } catch (IOException e) {
        	showException(e);
        }
    }
    
    public void showException(Throwable t){
    	Alert alert = new Alert(AlertType.ERROR);
    	alert.setTitle("Exception Dialog");
    	alert.setHeaderText("Look, an Exception Dialog");
    	alert.setContentText("There was an error in application");

    	// Create expandable Exception.
    	StringWriter sw = new StringWriter();
    	PrintWriter pw = new PrintWriter(sw);
    	t.printStackTrace(pw);
    	String exceptionText = sw.toString();

    	Label label = new Label("The exception stacktrace was:");

    	TextArea textArea = new TextArea(exceptionText);
    	textArea.setEditable(false);
    	textArea.setWrapText(true);

    	textArea.setMaxWidth(Double.MAX_VALUE);
    	textArea.setMaxHeight(Double.MAX_VALUE);
    	GridPane.setVgrow(textArea, Priority.ALWAYS);
    	GridPane.setHgrow(textArea, Priority.ALWAYS);

    	GridPane expContent = new GridPane();
    	expContent.setMaxWidth(Double.MAX_VALUE);
    	expContent.add(label, 0, 0);
    	expContent.add(textArea, 0, 1);

    	// Set expandable Exception into the dialog pane.
    	alert.getDialogPane().setExpandableContent(expContent);

    	alert.showAndWait();
    }
    
    public void showInfo(String text){
    	Alert alert = new Alert(AlertType.INFORMATION);
    	alert.setTitle("Information Dialog");
    	alert.setHeaderText(null);
    	alert.setContentText(text);

    	alert.showAndWait();
    }
    
    public void showWarning(String text){
    	Alert alert = new Alert(AlertType.WARNING);
    	alert.setTitle("Warning Dialog");
    	alert.setHeaderText(null);
    	alert.setContentText(text);

    	alert.showAndWait();
    }
    
    public String showInputDialog(String title,String header,String legend){
    	TextInputDialog dialog = new TextInputDialog();
    	dialog.setTitle(title);    	
    	dialog.setHeaderText(header);
    	dialog.setContentText(legend);

    	// Traditional way to get the response value.
    	Optional<String> result = dialog.showAndWait();
    	if (result.isPresent()){
    	    return result.get();
    	}
    	return null;    	
    }
    
    public boolean showConfirmation(String header,String content){
    	Alert alert = new Alert(AlertType.CONFIRMATION);
    	alert.setTitle("Confirmation Dialog");
    	alert.setHeaderText(header);
    	alert.setContentText(content);

    	Optional<ButtonType> result = alert.showAndWait();
    	if (result.get() == ButtonType.OK){
    	    return true;
    	} else {
    	    return false;
    	}   	
    }   
    
    public List<MQManagerAttributes> getMqManagers() {
		return mqManagers;
	}

	public Map<MQManagerAttributes,MQSession> getConnCache() {
		return connCache;
	}

	public MQSession getCurrentQMSession(MQManagerAttributes qm){
		return connCache.get(qm);
	}

	public List<MQQueueAttributes> getCurrentQueueList(MQManagerAttributes qm,String filterString) {		
		if(cachedQueueList.get(qm)==null){
			cachedQueueList.put(qm, new ArrayList<>());
		}
		if(filterString==null||filterString.isEmpty()){
			return cachedQueueList.get(qm);
		}else{
			return cachedQueueList.get(qm).stream().filter(q->q.getNameString().toLowerCase().contains(filterString.toLowerCase())).collect(Collectors.toList());	
		}
	}

	public void setCurrentQueueList(MQManagerAttributes qm, List<MQQueueAttributes> currentQueueList) {
		cachedQueueList.put(qm, currentQueueList);
	}

	public MQSession getCurrentSession() {
		return currentSession;
	}

	public void setCurrentSession(MQSession currentSession) {
		this.currentSession = currentSession;
	}
    
}
