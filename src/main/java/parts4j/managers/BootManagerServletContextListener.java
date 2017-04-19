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

import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import parts4j.PartEnv;

public class BootManagerServletContextListener implements ServletContextListener{
	@Override
	public void contextInitialized(ServletContextEvent sce) {
		Date startTime=new Date();
		Map<String,String> p=new java.util.LinkedHashMap<>();
		Enumeration<String> enr=sce.getServletContext().getAttributeNames();
		
		ServletContext ctx=sce.getServletContext();
		HashMap<String,String> servletContext=new HashMap<String,String>();
		
		servletContext.put("startTime", PartEnv.formatDate(startTime));
//		servletContext.put("contextPath", ctx.getContextPath());
		servletContext.put("majorVersion",""+ctx.getMajorVersion());
		servletContext.put("minorVersion",""+ctx.getMinorVersion());
//		servletContext.put("effectiveMajorVersion",""+ctx.getEffectiveMajorVersion());
//		servletContext.put("effectiveMinorVersion",""+ctx.getEffectiveMinorVersion());
		servletContext.put("serverInfo",""+ctx.getServerInfo());
		servletContext.put("servletContextName",""+ctx.getServletContextName());
//		servletContext.put("virtualServerName",""+ctx.getVirtualServerName());
		
		while(enr.hasMoreElements()){
			String k=enr.nextElement();
			Object v=ctx.getAttribute(k);
			p.put(k, v!=null?v.toString():"");
		}
		enr=sce.getServletContext().getInitParameterNames();
		while(enr.hasMoreElements()){
			String k=enr.nextElement();
			Object v=ctx.getInitParameter(k);
			p.put(k, v!=null?v.toString():"");
		}
		
		ManagerFactory.env().getEnvSources().put("ServletContext", servletContext);
		BootManager.setParams(p);
		BootManager.start();
	}
	
	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		BootManager.stop();
	}
}