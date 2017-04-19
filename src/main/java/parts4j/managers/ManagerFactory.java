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

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import parts4j.PartEnv;
import parts4j.PartLevels;
import parts4j.PartRegistry.Managers;
import parts4j.PartRegistry.PartRegistryInst;
import parts4j.internal.HealthyTester;
import parts4j.internal.PartTypeByClass;
import parts4j.PartType;

public class ManagerFactory {
	private static Logger log =LoggerFactory.getLogger(ManagerFactory.class);
	private static Map<String,String> defaultSettings=null;
	static Date startTime=new Date();
	
	static PartEnv env=new PartEnv();
	public static PartEnv env(){
		return env;
	}
	
	static Map<String,String> getDefaultSettings(){
		return defaultSettings;
	}
	static void setDefaultSettings(Map<String,String> settings){
		defaultSettings=settings;
	}
	
	@SuppressWarnings("unchecked")
	public static Managers createFromConfig(PartRegistryInst inst, Properties props){
		@SuppressWarnings("rawtypes")
		HashMap m = new LinkedHashMap<String,String>();
		m.putAll(props);
		return createFromConfig(inst, m);
	}
	public static Managers createFromConfig(PartRegistryInst inst, Map<String,String> props){
		Managers out=new Managers(inst, props);
		out.setLevels(new PartLevels(inst,props));
		out.setHealthy(new HealthyTester(inst, props));
		out.setEnv(env);
		
		ClassLoader loader=Thread.currentThread().getContextClassLoader();
		String prefix=inst.getPrefix();
		Map<String,BaseManager> managers=new HashMap<String,BaseManager>();
		Map<String,HashMap<String,String>> managerSettings=new HashMap<String,HashMap<String,String>>();
		
		for(String k : props.keySet()){
			String val=props.get(k);
			if(k.startsWith(prefix+ ".manager")){
				String param = k.substring((prefix+ ".manager").length()+1);
				if(param.indexOf('.')==-1){
					try {
						Object o = loader.loadClass(val).newInstance();
						if(o instanceof BaseManager){
							BaseManager m=(BaseManager)o;
							if(out.getRootManager()==null) out.setRootManager(m);
							managers.put(param, m.setInst(inst));
						}else{
							log.error("Manager "+k+" is class '"+val+"' which is not a sub-class of '"+BaseManager.class.getName()+"'");
						}
					} catch (Throwable e) {
						log.error("Manager "+k+" could not be created",e);
					}
				}else{
					String mgrName=param.substring(0,param.indexOf('.'));
					HashMap<String,String> set=managerSettings.get(mgrName);
					if(set==null) managerSettings.put(mgrName, set=new HashMap<String,String>());
					set.put(param.substring(mgrName.length()+1), val);
				}
			}
		}
		if(managers.size()==0){
			out.setRootManager(BaseManager.newDefaultManager().setInst(inst));
			return out;
		}else{
			for(Map.Entry<String, BaseManager> i : managers.entrySet()){
				i.getValue().setMgrID(i.getKey());
				Map<String,String> setss=managerSettings.get(i.getKey());
				i.getValue().configure(out,setss!=null?setss:new HashMap<String,String>());
			}
		}
		String rootManager = props.get(prefix+".rootManager");
		if(rootManager!=null){
			BaseManager m = managers.get(rootManager);
			if(m==null){
				log.error("No such rootManager:" +rootManager);
				m=BaseManager.newDefaultManager().setInst(inst);
			}
			out.setRootManager(m);
		}
		out.loadManagers(managers);
		
		return out;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <T> PartType<T> getOrCreatePartType(Class<T> clz, String levelIfProvided, PartRegistryInst inst, BaseManager mgr){
		PartType t = mgr.types.get(clz);
		if(t!=null) return (PartType<T>)t;
		log.info("Using " + mgr.getMgrID()+" ("+mgr.getClass().getName()+") for '"+clz.getName()+"'");
		synchronized(mgr){
			t = mgr.types.get(clz);
			if(t==null){
				t=new PartTypeByClass<T>(inst,clz,levelIfProvided);
				t.setMgr(mgr);
				mgr.types.put(clz,t=mgr.onCreate((PartTypeByClass<T>)t));
			}
			return mgr.types.get(clz);
		}
	}
	
	public static <M> M configuredManagerForClass(Class<?> clz, Map<String,M> lookup, Map<String,String> props, String prefix, M defaultValue){
		String spl = clz.getName();
		while(spl!=null){
			String mgrSetting = props.get(prefix+".part."+spl);
			if(mgrSetting!=null){
				M m= lookup.get(mgrSetting);
				if(m!=null){
					return m;
				}
			}
			int idx=spl.lastIndexOf('$');
			if(idx==-1) idx=spl.lastIndexOf('.');
			if(idx>-1){
				spl=spl.substring(0,idx);
			}else{
				spl=null;
			}
		}
		return defaultValue;
	}
	
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static Managers createDefaultManagers(PartRegistryInst inst) {
		Properties prop=new Properties();
		if(defaultSettings!=null && defaultSettings.containsKey("parts4j.prefix")){
			inst.setPrefix(defaultSettings.get("parts4j.prefix"));
		}
		if(defaultSettings==null) defaultSettings= new LinkedHashMap<String,String>();
		String setting = defaultSettings.containsKey("parts4j") ? defaultSettings.get("parts4j") : System.getenv("parts4j");
		if(setting==null) setting = System.getProperty("parts4j");
		if(setting==null) setting = "resource:parts4j.properties";
		if(setting.startsWith("resource:")){
			setting = setting.substring("resource:".length());
			try {
				prop.load(Thread.currentThread().getContextClassLoader().getResourceAsStream(setting));
				log.info("Loaded props4j settings from class-path resource '" +setting+"'");
			} catch (Throwable e) {
				log.info("No '" +setting+"' resource found on class-path resource");
			}
			setting=null;
		}else if(setting.startsWith("file:")){
			setting = setting.substring("file:".length());
			try {
				prop.load(new FileReader(new File(setting)));
				log.info("Loaded props4j settings from file '" +setting + "'");
			} catch (IOException e) {
			}
			setting=null;
		}else if(setting.startsWith("custom:")){
			setting = setting.substring("custom:".length());
		}
		if(prop.containsKey("parts4j")) setting=prop.getProperty("parts4j");
		if(prop.size()>0){
			LinkedHashMap<String, String> out=new LinkedHashMap<>();
			((HashMap)out).putAll(prop);
			out.putAll(defaultSettings);
			defaultSettings.putAll(out);
		}
		if(setting!=null){
			SettingsProvider prov=null;
			try{
				String clzName=null;
				int idx=setting.indexOf('/');
				if(idx>-1){
					clzName=setting.substring(0, idx);
					setting = setting.substring(idx+1);
				}else{
					clzName=setting;
					setting=null;
				}
				prov=(SettingsProvider)Thread.currentThread().getContextClassLoader().loadClass(clzName).newInstance();
				Map<String,String> out=prov.provideSettings(env,defaultSettings,setting);
				if(out!=null) defaultSettings.putAll(out);
			}catch(Throwable t){
				log.error("Could not create custom settings provider: "+setting,t);
			}
		}
		EnvControl.initialize(defaultSettings, env);
		return ManagerFactory.createFromConfig(inst, defaultSettings);
	}
	public interface SettingsProvider{
		Map<String,String> provideSettings(PartEnv env, Map<String,String> current, String source);
	}
}