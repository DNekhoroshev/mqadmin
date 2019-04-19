package ru.sberbank.cib.gmbus.mqadmin.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class MQTopicAttributes {
	private StringProperty topicName;
	private StringProperty topicString;
	
	public MQTopicAttributes(String topicName, String topicString) {
		super();
		this.topicName = new SimpleStringProperty(topicName);
		this.topicString = new SimpleStringProperty(topicString);
	}

	public StringProperty getTopicName() {
		return topicName;
	}

	public StringProperty getTopicString() {
		return topicString;
	}
	
	public String getTopicNameString() {
		return topicName.getValue();
	}

	public String getTopicStringString() {
		return topicString.getValue();
	}

	@Override
	public String toString() {
		return "MQTopicAttributes [topicName=" + topicName + ", topicString=" + topicString + "]";
	}
	
}
