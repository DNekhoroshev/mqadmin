package ru.sberbank.cib.gmbus.mqadmin.util;

import java.io.File;
import java.io.FileFilter;

public class SentFileFilter implements FileFilter {
	
	@Override
	public boolean accept(File f) {
		if (f.isDirectory())
	            return true;

	    String name = f.getName();

	    if (name.endsWith(CommonConstants.SENT_MARKER))
	        return false;

	    return true;
	}
	
}
