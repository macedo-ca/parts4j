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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class LogReport extends SystemOutReport{
	private static Logger log = LoggerFactory.getLogger(LogReport.class);
	
	static LogReport singleton=new LogReport();
	public static LogReport getSingleton(){
		return singleton;
	}
	
	@Override
	void output(String line) {
		log.info(line);
	}
}