package ru.sberbank.cib.gmbus.mqadmin.command;

import ru.sberbank.cib.gmbus.mqadmin.MQAdmin;

public abstract class AbstractMqCommand implements MQAdmMenuCommand {
	protected MQAdmin mainApp;

	public AbstractMqCommand(MQAdmin mainApp) {
		super();
		this.mainApp = mainApp;
	}	
}
