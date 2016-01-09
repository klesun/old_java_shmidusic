package org.klesun_model;

import org.shmidusic.stuff.graphics.Settings;

import java.awt.*;

public interface IComponent
{
	IModel getModel();
	IComponent getModelParent();
	IComponent getFocusedChild();
	AbstractHandler getHandler();

	default Component getFirstAwtParent() {
		IComponent context = this;
		while (!(context instanceof Component) && context != null) {
			context = context.getModelParent();
		}
		return (Component)context;
	}

	default int dx() {
		return Settings.inst().getStepWidth();
	}
	default int dy() {
		return Settings.inst().getStepHeight();
	}
}
