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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import parts4j.util.JSONBuilder.JSONEntry;

public class PartEnv {
	static SimpleDateFormat df=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
	public static String formatDate(Date d){
		return d!=null ? df.format(d) : null;
	}
	String startTime=null;
	String env=null;
	String platform=null;
	String cluster=null;
	String hostname=null;
	String application=null;
	String context=null;
	String tags=null;
	
	String majorVersion=null;
	String minorVersion=null;
	String build=null;
	String memory=null;
	String disk=null;
	String processors=null;
	
	HashMap<String,String> extFields=new HashMap<String,String>();
	
	HashMap<String,Map<String,String>> envSources=new HashMap<String,Map<String,String>>();
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public JSONEntry setTo(JSONEntry out){
		out.set((HashMap)extFields);
		out.set("env", env);
		out.set("platform", platform);
		out.set("cluster", cluster);
		out.set("hostname", hostname);
		out.set("application", application);
		out.set("context", context);
		out.set("majorVersion", majorVersion);
		out.set("minorVersion", minorVersion);
		out.set("build", build);
		out.set("version",majorVersion+"."+minorVersion+"."+build);
		out.set("memory", memory);
		out.set("disk", disk);
		out.set("processors", processors);
		out.set("startTime", startTime);
		return out;
	}
	public Map<String,String> toMap(){
		HashMap<String,String> out=new HashMap<String,String>();
		out.putAll(extFields);
		out.put("env", env);
		out.put("platform", platform);
		out.put("cluster", cluster);
		out.put("hostname", hostname);
		out.put("application", application);
		out.put("context", context);
		out.put("majorVersion", majorVersion);
		out.put("minorVersion", minorVersion);
		out.put("build", build);
		out.put("version",majorVersion+"."+minorVersion+"."+build);
		out.put("memory", memory);
		out.put("disk", disk);
		out.put("processors", processors);
		out.put("startTime", startTime);
		return out;
	}
	
	public void setVersion(String v){
		if(v==null) return;
		if(v.indexOf('.')>-1){
			String[] vp=v.split("\\.");
			setMajorVersion(vp[0]);
			setMinorVersion(vp[1]);
			if(vp.length>2) setBuild(vp[2]);
		}else{
			setMajorVersion(v);
		}
	}
	
	public HashMap<String, Map<String, String>> getEnvSources() {
		return envSources;
	}
	public void setEnvSources(HashMap<String, Map<String, String>> envSources) {
		this.envSources = envSources;
	}
	public String getContext() {
		return context;
	}
	public void setContext(String context) {
		this.context = context;
	}
	public String getEnv() {
		return env;
	}
	public void setEnv(String env) {
		this.env = env;
	}
	public String getPlatform() {
		return platform;
	}
	public void setPlatform(String platform) {
		this.platform = platform;
	}
	public String getCluster() {
		return cluster;
	}
	public void setCluster(String cluster) {
		this.cluster = cluster;
	}
	public String getHostname() {
		return hostname;
	}
	public void setHostname(String node) {
		this.hostname = node;
	}
	public String getApplication() {
		return application;
	}
	public void setApplication(String application) {
		this.application = application;
	}
	public String getMajorVersion() {
		return majorVersion;
	}
	public void setMajorVersion(String majorVersion) {
		this.majorVersion = majorVersion;
	}
	public String getMinorVersion() {
		return minorVersion;
	}
	public void setMinorVersion(String minorVersion) {
		this.minorVersion = minorVersion;
	}
	public String getBuild() {
		return build;
	}
	public void setBuild(String buildVersion) {
		this.build = buildVersion;
	}

	public String getTags() {
		return tags;
	}
	public void setTags(String tags) {
		this.tags = tags;
	}	
	public HashMap<String, String> getExtFields() {
		return extFields;
	}
	public void setExtFields(HashMap<String, String> extFields) {
		this.extFields = extFields;
	}
	public String getMemory() {
		return memory;
	}
	public void setMemory(String memory) {
		this.memory = memory;
	}
	public String getDisk() {
		return disk;
	}
	public void setDisk(String disk) {
		this.disk = disk;
	}
	public String getProcessors() {
		return processors;
	}
	public void setProcessors(String processors) {
		this.processors = processors;
	}
	public String getStartTime() {
		return startTime;
	}
	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

}
