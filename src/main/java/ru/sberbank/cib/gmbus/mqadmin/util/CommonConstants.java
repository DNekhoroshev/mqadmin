package ru.sberbank.cib.gmbus.mqadmin.util;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public interface CommonConstants {
	public static final String MESSAGE_DELIMITER = "-------------------------------------------------------------------\n";
    public static final String PROPERTIES_DELIMITER = "PROPERTIES:";
    public static final String BODY_DELIMITER = "BODY:";
    public static final boolean DEFAULT_PERSISTENCE = true;
    public static final boolean EXCLUDE_JMS_HEADERS = true;
    public static final String MESSAGE_FILE_PREFIX = "message_";
    public static final String MESSAGE_FILE_EXT = ".qmsg";
    public static final Charset STD_CHARSET = StandardCharsets.UTF_8;
    
    /**
     * csv header name for the fixml message value.
     */
    public static final String CSV_BODY_HEADER = "Body";

    /**
     * csv header name for the fixml headers value.
     */
    public static final String CSV_HEADER_HEADER = "Header";
    
    public static final String SENT_MARKER = ".sent";
}
