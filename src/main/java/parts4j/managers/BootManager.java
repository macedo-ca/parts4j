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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.LinkedList;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import parts4j.PartRegistry;
import parts4j.PartType;
import parts4j.PartRegistry.Managers;
import parts4j.PartRegistry.PartRegistryInst;
import parts4j.PartsQuery.CriteriaDef;
import parts4j.internal.Methods.StandardMethod;

public class BootManager extends BaseManager{
	private static Logger log=LoggerFactory.getLogger(BootManager.class);
	public static BootManager rootManager=null;
	static Map<String, String> params=null;
	static PartRegistryInst instance=null;
	/**
	 * Invoked by other BootManager classes before calling main(String[])
	 * @param p
	 */
	public static void setParams(Map<String, String> p) {
		params=p;
	}
	public static void start() {
		start0();
		System.err.println("BootManager started");
	}
	private static void start0() {
		ManagerFactory.setDefaultSettings(params);
		factory=rootManager=new BootManager();
		instance=PartRegistry.getRegistry();
		if(params==null) params=ManagerFactory.getDefaultSettings();
		boot();
	}
	public static void boot(){
		String pfx=instance.getPrefix();
		if(params.containsKey(pfx+".load")){
			load(params.get(pfx+".load"));
		}
		if(params.containsKey(pfx+".start")){
			start(params.get(pfx+".start"));
		}
	}
	static LinkedList<Object> created=new LinkedList<Object>();
	public static void start(String param){
		String[] parts=param.split("[,;\\n]");
		for(String s : parts){
			if(s.contains(".")){
				Class<?> c=loadClass("start class",s);
				if(c!=null){
					String info="Loaded "+c +" for creation";
				    Field[] fields = c.getDeclaredFields();
				    if(fields!=null) for(Field f : fields){
				    	if(Modifier.isStatic(f.getModifiers()) && f.getType().equals(PartType.class)){
				    		info+=" with "+(Modifier.isPrivate(f.getModifiers())?"private ":"")+"static PartType field '"+f.getName()+"'";
				    	}
				    }
				    log.info(info);
				}
			}
			CriteriaDef def=instance.newQuery().compileCriteria(s.trim());
			if(instance.newQuery(def).forEachPart().count()==0){
			    instance.newQuery(def).forEachType(type ->{
					if(type.hasMethod(StandardMethod.create)){
						type.doCreate();
					}
				});
				if(instance.newQuery(def).forEachPart().count()==0){
					Class<?> lclclz=loadClass("start class",s.trim());
					try {
						created.add(lclclz.newInstance());
					}catch(Throwable t){
						log.info("Error creating new singleton instance of class '"+s+"' to start ",t);
					}
				}
//				def=instance.newQuery().compileCriteria(s.trim());
			}
			instance.newQuery(def).forEachPart(part ->{
				try {
					log.info("Starting " +s);
					part.doStart();
					log.info("Started " +s);
				} catch (Throwable e) {
					log.info("Failed starting" +s,e);
				}
			});
		}
	}
	public static void load(String classes){
		String[] parts=classes.split("[,;\\n]");
		String ctx="load command";
		for(String s : parts){
			loadClass(ctx, s);
		}
	}
	private static Class<?> loadClass(String ctx, String s) {
		try{
			s=s.trim();
			return Thread.currentThread().getContextClassLoader().loadClass(s);
		}catch(ClassNotFoundException t){
			log.info("Invalid class '"+s+"' in "+ctx,t);
		}catch(Throwable t){
			log.info("Error loading class '"+s+"' in "+ctx,t);
		}
		return null;
	}
	public static void stop(){
		System.err.println("BootManager stopped");
	}
	
	@Override
	protected BaseManager newDefault() {
		return this;
	}
	@Override
	public void configure(Managers managers, Map<String, String> config) {
		super.configure(managers, config);
		
	}
	
	public static void main(String[] ar){
		start0();
		System.err.println("BootManager started - type 'quit' to shutdown");
		String in=null;
		try {
			while((in=new BufferedReader(new InputStreamReader(System.in)).readLine())!=null){
				if(in!=null && in.trim().equalsIgnoreCase("quit")){
					stop();
					return;
				}
			}
		} catch (Exception e) {
			return;
		}
	}
}
