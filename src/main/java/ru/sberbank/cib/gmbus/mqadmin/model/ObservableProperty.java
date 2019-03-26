package ru.sberbank.cib.gmbus.mqadmin.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class ObservableProperty {
	private StringProperty name;
	private StringProperty value;
	private boolean hidden;
	
	public ObservableProperty(String name, String value) {
		super();
		this.name = new SimpleStringProperty(name);
		this.value = new SimpleStringProperty(value);
		this.hidden = false;
	}
	
	public ObservableProperty(String name, int value) {
		super();
		this.name = new SimpleStringProperty(name);
		this.value = new SimpleStringProperty(String.valueOf(value));
		this.hidden = false;
	}
	
	public ObservableProperty(String name, String value, boolean hidden) {
		super();
		this.name = new SimpleStringProperty(name);
		this.value = new SimpleStringProperty(value);
		this.hidden = hidden;
	}
	
	public ObservableProperty(String name, int value, boolean hidden) {
		super();
		this.name = new SimpleStringProperty(name);
		this.value = new SimpleStringProperty(String.valueOf(value));
		this.hidden = hidden;
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
	
	public boolean isHidden() {
		return hidden;
	}
	
	@Override
	public String toString(){
		return name.get()+"="+value.get();
	}		
	
}
