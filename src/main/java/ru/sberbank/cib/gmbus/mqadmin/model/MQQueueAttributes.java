package ru.sberbank.cib.gmbus.mqadmin.model;

import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class MQQueueAttributes {
	private final StringProperty name;
	private final StringProperty type;
	private final LongProperty depth;
	private final LongProperty readerCount;
	private final LongProperty writerCount;
	private boolean updated = false;
	
	public MQQueueAttributes(String name, String type, int depth, int readerCount, int writerCount) {
		super();
		this.name = new SimpleStringProperty(name);
		this.type = new SimpleStringProperty(type);
		this.depth = new SimpleLongProperty(depth);
		this.readerCount = new SimpleLongProperty(readerCount);
		this.writerCount = new SimpleLongProperty(writerCount);
	}

	public StringProperty getName() {
		return name;
	}

	public StringProperty getType() {
		return type;
	}

	public LongProperty getDepth() {
		return depth;
	}

	public LongProperty getReaderCount() {
		return readerCount;
	}

	public LongProperty getWriterCount() {
		return writerCount;
	}
	
	public String getNameString() {		
		return name.getValue();
	}

	public String getTypeString() {
		return type.getValue();
	}

	public Long getDepthLong() {
		return depth.longValue();
	}

	public Long getReaderCountLong() {
		return readerCount.longValue();
	}

	public Long getWriterCountLong() {
		return writerCount.longValue();
	}

	public boolean isUpdated() {
		return updated;
	}

	public void setUpdated(boolean updated) {
		this.updated = updated;
	}
	
}
