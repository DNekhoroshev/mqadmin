package ru.sberbank.cib.gmbus.mqadmin.view;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStream;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.stage.FileChooser;
import ru.sberbank.cib.gmbus.mqadmin.MQAdmin;
import ru.sberbank.cib.gmbus.mqadmin.model.MQManagerAttributes;

public class RootLayoutController {
	
	// Ссылка на главное приложение.
    private MQAdmin mainApp;
	private Properties globalProps = new Properties();
    private final String tmpProps = System.getProperty("java.io.tmpdir")+"mqadmin.properties";
	
    @FXML
    private Menu menuRecent;
    
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
    	
    	File tmpPropsFile = new File(tmpProps);
    	if(tmpPropsFile.exists()){
    		try {
				globalProps.load(new FileReader(tmpPropsFile));
				if(globalProps.containsKey("QMGRS.1")){
					File qmgrsFile = new File(globalProps.getProperty("QMGRS.1"));
					if(qmgrsFile.exists()){
						menuRecent.getItems().clear();
						MenuItem mi = new MenuItem(qmgrsFile.getName());						
								mi.setOnAction(
										event-> openEnvironmentFile(qmgrsFile) 
								);						
						menuRecent.getItems().add(mi);						
					}
				}
			} catch (Exception e) {
				mainApp.showException(e);
			} 
    	}    	
    	
    }   
    
    @FXML
	private void handleMenuAbout(){
		mainApp.showInfo("This is a simple MQ command app v.1.0.0\n (c) Nekhoroshev Dmitry <Dmitry_Nekhoroshev@sberbank-cib.ru>,\n2019.02");
	}
    
    @FXML
	private void handleMenuClose(){		
		System.exit(0);
	}
	
	private void openEnvironmentFile(File qmConfigurationFile){
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document qmConfig = builder.parse(qmConfigurationFile);
			qmConfig.getDocumentElement().normalize();
			NodeList qmgrList = qmConfig.getElementsByTagName("QueueManagerHandle");				         
	        for (int qmgrIndex = 0; qmgrIndex < qmgrList.getLength(); qmgrIndex++) {
	        	 Node qmgrInfoNode = qmgrList.item(qmgrIndex);
	        	 if (qmgrInfoNode.getNodeType() == Node.ELEMENT_NODE) {
	        		 Element qmgrInfoElement = (Element) qmgrInfoNode;
	        		 MQManagerAttributes qmgr = createMQManager(qmgrInfoElement);
	        		 mainApp.getMqManagers().add(qmgr);
	        	 }
	        }
	        mainApp.showManagers();
		} catch (Exception e) {
			mainApp.showException(e);
		}
	}
    
    @FXML
	private void handleMenuOpenEnv(){
		FileChooser fileChooser = new FileChooser();

        // Задаём фильтр расширений
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
                "XML files (*.xml)", "*.xml");
        fileChooser.getExtensionFilters().add(extFilter);

        // Показываем диалог загрузки файла
        File qmConfigurationFile = fileChooser.showOpenDialog(mainApp.getPrimaryStage());
        
        if(qmConfigurationFile==null)
        	return;
        
        globalProps.put("QMGRS.1", qmConfigurationFile.getAbsolutePath());
                
        saveProperties();
        
        openEnvironmentFile(qmConfigurationFile);
        
	}
	
	private MQManagerAttributes createMQManager(Element qmgrInfoElement){
		//int connType = Integer.parseInt(qmgrInfoElement.getAttribute("connnectionType"));
		int connType = 1;
		String[] connAttributes = qmgrInfoElement.getAttribute("connName").replace(")", "").split("\\(");		
		String host = connAttributes[0];
		int port = Integer.parseInt(connAttributes[1]);
		String qmgrName = qmgrInfoElement.getAttribute("name");
		String channel = qmgrInfoElement.getAttribute("channel");
		if((qmgrInfoElement.getAttribute("sslCipherSuite")==null)||(qmgrInfoElement.getAttribute("sslCipherSuite").isEmpty())){
			return new MQManagerAttributes(connType, host, port, qmgrName, channel);
		}else{
			String sslCipherSuite   = qmgrInfoElement.getAttribute("sslCipherSuite");
			String sslPersonalStore = qmgrInfoElement.getAttribute("sslPersonalStore");
			String sslPersonalStorePw = qmgrInfoElement.getAttribute("sslPersonalStorePw");
			String sslTrustedStore = qmgrInfoElement.getAttribute("sslTrustedStore");
			String sslTrustedStorePw = qmgrInfoElement.getAttribute("sslTrustedStorePw");
			return new MQManagerAttributes(connType, host, port, qmgrName, channel, sslPersonalStore, sslTrustedStore, sslPersonalStorePw, sslTrustedStorePw, sslCipherSuite);
		}		
	}
	
	private void saveProperties(){
		try {
			OutputStream output = new FileOutputStream(tmpProps);
			globalProps.store(output, null);
		} catch (Exception e) {
			mainApp.showException(e);			
		}
		
	}
	
}
