package ru.sberbank.cib.gmbus.mqadmin;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.jms.Connection;

import org.controlsfx.control.StatusBar;

import com.ibm.mq.MQQueueManager;

import javafx.application.Application;
import javafx.application.Platform;
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
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import ru.sberbank.cib.gmbus.mqadmin.model.MQManagerAttributes;
import ru.sberbank.cib.gmbus.mqadmin.view.ManagersController;
import ru.sberbank.cib.gmbus.mqadmin.view.RootLayoutController;

public class MQAdmin extends Application {

	private StatusBar statusBar;
	private Stage primaryStage;
	
	private MQManagerAttributes currentQM;
	private List<MQManagerAttributes> mqManagers = new ArrayList<>();
	private Map<MQManagerAttributes,MQQueueManager> connCache = new HashMap<>();
	
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

	public Map<MQManagerAttributes,MQQueueManager> getConnCache() {
		return connCache;
	}

	public MQManagerAttributes getCurrentQM() {
		return currentQM;
	}

	public void setCurrentQM(MQManagerAttributes currentQM) {
		this.currentQM = currentQM;
	}	
    
}
