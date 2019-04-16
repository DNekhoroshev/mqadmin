package ru.sberbank.cib.gmbus.mqadmin.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class MessageHeader {
	private StringProperty name;
	private StringProperty value;
	
	public MessageHeader(String name, String value) {
		super();
		this.name = new SimpleStringProperty(name);
		this.value = new SimpleStringProperty(value);
	}
	
	public StringProperty getNameProperty() {
		return name;
	}

	public StringProperty getValueProperty() {
		return value;
	}
	
	public String getName() {
		return name.get();
	}

	public String getValue() {
		return value.get();
	}
	
	@Override
	public String toString(){
		return name.get()+"="+value.get();
	}	
	
}
