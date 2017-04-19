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
package parts4j;

import java.util.HashMap;
import java.util.Map;

import parts4j.PartRegistry.PartRegistryInst;

public class PartLevels {
	static String[] DEFAULT_LEVELS={"system","subsystem","component","subcomponent","app","library","module","appconfig","utility","class"};
	static String TENANT_LEVELS="app,appconfig";
	PartRegistryInst inst;
	String[] levels;
	HashMap<String,Integer> levelByName=new HashMap<String,Integer> ();
	HashMap<String,Integer> tenantLevels=new HashMap<String,Integer>();
	
	public PartLevels(PartRegistryInst inst, Map<String,String> props){
		this(inst,props.containsKey(inst.getPrefix()+".levels")?props.get(inst.getPrefix()+".levels").split(","):DEFAULT_LEVELS);
		String s = props.get(inst.getPrefix()+".levels.map");
		if(s!=null){
			setMappings(s);
		}
		String tenLevl= props.getOrDefault(inst.getPrefix()+".levels.tenant",TENANT_LEVELS);
		if(tenLevl!=null){
			setTenantLevels(tenLevl);
		}
	}
	public PartLevels(PartRegistryInst inst, String[] levels){
		this.inst=inst;
		this.levels=levels;
		index();
	}
	public void setLevels(String[] levels){
		this.levels=levels;
		index();
		indexMaps();
	}
	private void index(){
		levelByName=new HashMap<String,Integer> ();
		for(int i=0;i<levels.length;i++){
			levels[i]=levels[i].toLowerCase();
			levelByName.put(levels[i], (i+1));
		}
	}
	public void setTenantLevels(String tenLevl) {
		tenantLevels=new HashMap<String,Integer>();
		String[] levAr=tenLevl.split(",");
		for(String l : levAr){
			l=l.trim().toLowerCase();
			Integer idx=levelByName.get(l);
			if(idx!=null){
				tenantLevels.put(l, idx);
			}
		}
	}
	public boolean isTenant(String name){
		return tenantLevels.containsKey(name);
	}
	public boolean isTenant(Integer idx){
		return tenantLevels.containsValue(idx);
	}
	public Integer getLevelNum(String name){
		Integer out=levelByName.get(name);
		if(out!=null) return out;
		// Try compatibility mappings
		return intmap.get(name);
	}
	public String getLevelName(int num){
		if(num>levels.length) return null;
		return levels[num-1];
	}
	public String getValidLevel(String name){
		if(name==null) return null;
		name=name.trim().toLowerCase();
		if(levelByName.containsKey(name)) return name;
		if(strmap.containsKey(name)) return strmap.get(name).trim().toLowerCase();
		return null;
	}
	
	// Mappings 
	HashMap<String,String> strmap=new HashMap<String,String>();
	HashMap<String,Integer> intmap=new HashMap<String,Integer>();
	public void setMappings(String s){
		strmap=new HashMap<String,String>();
		if(s==null){
			indexMaps();
			return;
		}
		String[] sp=s.toLowerCase().split(",");
		for(String pair : sp){
			if(pair!=null){
				String[] kp = pair.split("=");
				if(kp.length>1) strmap.put(kp[0].trim(), kp[1].trim());
			}
		}
		indexMaps();
	}
	private void indexMaps(){
		intmap=new HashMap<String,Integer>();
		for(Map.Entry<String, String> ent : strmap.entrySet()){
			Integer idx=levelByName.get(ent.getValue());
			if(idx!=null){
				intmap.put(ent.getKey(), idx);
			}
		}
	}
}