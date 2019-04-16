package ru.sberbank.cib.gmbus.mqadmin.concurrent;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jms.JMSException;

import org.supercsv.io.CsvMapReader;
import org.supercsv.prefs.CsvPreference;

import com.google.gson.Gson;
import com.ibm.mq.MQException;

import javafx.concurrent.Task;
import ru.sberbank.cib.gmbus.mqadmin.MQAdmin;
import ru.sberbank.cib.gmbus.mqadmin.connect.MQHelper;
import ru.sberbank.cib.gmbus.mqadmin.util.CommonConstants;
import ru.sberbank.cib.gmbus.mqadmin.util.SentFileFilter;

public class ImportMessageTask extends Task<Integer> {

	public enum SourceFormat {CSV,PLAIN}
	
	private MQAdmin mainApp;
	private String queue;	
	private File inputSource;
	private SourceFormat format;
	
	int importedMsgs  = 0;
    long importedBytes = 0;
	
	public ImportMessageTask(MQAdmin mainApp,String queue,File sourceFile,SourceFormat format) {		
		this.mainApp = mainApp;
		this.queue = queue;		
		this.inputSource = sourceFile;
		this.format = format;
	}
	
	@Override
	protected Integer call() throws Exception {
				
		if(inputSource.isDirectory()){					
			List<File> plainDirContent = getFiles(inputSource);			
			int fileCount = 0;
			for(File file : plainDirContent){				
				if(format==SourceFormat.CSV)
					importFromCSV(file);
				else
					importFromPlain(file);
				fileCount++;
				updateMessage(String.format("%s :: processed %d files of %d", queue, fileCount,plainDirContent.size()));
			}
		}else{			
			if(format==SourceFormat.CSV)
				importFromCSV(inputSource);
			else
				importFromPlain(inputSource);
		}
		return importedMsgs;
	}

	private void importFromCSV(File input) throws IOException, JMSException, InterruptedException, MQException {

		Reader source = new InputStreamReader(new FileInputStream(input), CommonConstants.STD_CHARSET);
		CsvMapReader reader = new CsvMapReader(source, CsvPreference.STANDARD_PREFERENCE);

		String[] headers = reader.getHeader(true);
		Map<String, String> row;

		while ((row = reader.read(headers)) != null) {			
			
			Map<String, String> msgHeaders = extractMessageHeaders(row.get(CommonConstants.CSV_HEADER_HEADER));
			String msgText = row.get(CommonConstants.CSV_BODY_HEADER);
						
			MQHelper.send(mainApp.getCurrentSession().getMqManager(), queue, CommonConstants.DEFAULT_PERSISTENCE, msgHeaders, msgText);
			
			importedMsgs++;
			importedBytes += msgText.getBytes().length;
			updateMessage(String.format("%s :: imported %d messages", queue, importedMsgs));			
		}
		source.close();		
		commit(input);
	}
	
	private void importFromPlain(File input) throws IOException, MQException {		
				
		byte[] bodyElement = Files.readAllBytes(input.toPath());		
		String msgText = new String(bodyElement);		
		MQHelper.send(mainApp.getCurrentSession().getMqManager(), queue, CommonConstants.DEFAULT_PERSISTENCE, null, msgText);
		importedMsgs++;
		importedBytes += bodyElement.length;
		commit(input);
	}
	
	private List<File> getFiles(File dir){
		List<File> result = new ArrayList<File>();
		FileFilter filter = new SentFileFilter();			
		
		if(dir.isDirectory()){			
		    for (File file : dir.listFiles(filter)) {
		        if (file.isFile())
		            result.add(file);
		        else
		        	result.addAll(getFiles(file));
		        	
		    }		    
		}
		
		return result;
	}	
	
	@SuppressWarnings("unchecked")
    private static Map<String, String> extractMessageHeaders(String json) {
        return new Gson().fromJson(json, HashMap.class);
    }
	
	private void commit(File file){		
		File commited = new File(file.getAbsolutePath()+CommonConstants.SENT_MARKER);
		file.renameTo(commited);
	}

}
