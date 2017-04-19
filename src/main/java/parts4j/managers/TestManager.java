/******************************************************************************
 *  Copyright (c) 2017 Johan Macedo
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *    Johan Macedo
 *****************************************************************************/
package parts4j.managers;

import java.util.Map;

import parts4j.PartRegistry.Managers;
import parts4j.managers.BaseManager.ScheduledManager;

public class TestManager extends ScheduledManager{
	@Override
	public void configure(Managers mgrs, Map<String, String> config) {
		for(String k : config.keySet()){
			System.out.println(k+"="+config.get(k));
		}
	}
	@Override
	public void report() {
		System.out.println("report()");
	}
}
