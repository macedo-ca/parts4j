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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import parts4j.Part;
import parts4j.PartRegistry.Managers;
import parts4j.PartRegistry.PartRegistryInst;
import parts4j.PartType;
import parts4j.PartsQuery.CriteriaDef;
import parts4j.PartsQuery.MapVisitable;
import parts4j.internal.Methods;
import parts4j.internal.PartTypeByClass;

@SuppressWarnings("rawtypes")
public class BaseManager implements MapVisitable{
	protected PartRegistryInst inst=null;
	protected ConcurrentHashMap<Class, PartType> types=new ConcurrentHashMap<Class, PartType> ();
	
	static BaseManager factory=null;
	public static BaseManager newDefaultManager(){
		return factory!=null ? factory.newDefault() : new BaseManager();
	}
	protected BaseManager newDefault(){
		return new BaseManager();
	}
	
	RollupHelp rollup=null;
	CriteriaDef criteria=null;
	
	String mgrID="default";
	
	public String getMgrID() {
		return mgrID;
	}
	public void setMgrID(String mgrID) {
		this.mgrID = mgrID;
	}

	@Override
	public Map<?, PartType> visitableMap() {
		return types;
	}
	
	Routing routing=null;
	public Routing routing(){
		return routing;
	}
	public abstract class Routing{
		public abstract Routing publishEvent(String context, String eventName, Object event);
		public abstract Routing on(String context, String event, Consumer callback);
		public abstract Routing on(String context, String event, Supplier callback);
		public abstract Routing on(String context, String event, BiConsumer<String, Object> callback);
		public abstract Routing on(String context, Predicate<String> eventSelector, Supplier callback);
		public abstract Routing on(String context, Predicate<String> eventSelector, Consumer callback);
		public abstract Routing on(String context, Predicate<String> eventSelector, BiConsumer<String, Object> callback);
		public abstract Routing action(Part part, String action, Supplier callback);
		public abstract Routing action(PartType partType, String action, String method);
		public abstract Routing action(PartType partType, String action, Supplier callback);
		public abstract Routing action(Part part, String action, Runnable callback);
		public abstract Routing action(PartType partType, String action, Runnable callback);
	}
	public void allTasks(){
		if(rollup!=null){
			rollup.rollup();
		}
		report();
	}
	
	public void report(){
		// noop
	}
	
	protected BaseManager setInst(PartRegistryInst inst){
		this.inst=inst;
		return this;
	}
	public void configure(Managers managers, Map<String,String> config){
		String healthrollup=config.get("healthrollup");
		if(healthrollup!=null){
			rollup=new RollupHelp(this);
			rollup.setLevel(healthrollup);
		}
		String criteriaStr=config.get("criteria");
		if(criteriaStr!=null){
			criteria = managers.newVisit().compileCriteria(criteriaStr);
		}
		// TODO do filter
	}
	
	protected <T> PartTypeByClass<T> onCreate(PartTypeByClass<T> c){
		// noop
		return c;
	}
	public <T> Part<T> onCreate(Part<T> c){
		// noop
		return c;
	}
	public boolean isHealthyConsideringErrors(Part r){
		return (!r.hasMethod(Methods.StandardMethod.errors)) || r.getErrors()==null;
	}
	
	public static class ScheduledManager extends BaseManager{
		static ScheduledExecutorService exec=Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder().setDaemon(true).build());
		ScheduledFuture<?> future=null;
		public ScheduledManager setFrequency(long frequencyInSeconds){
			if(future!=null){
				try {
					future.cancel(true);
				} catch (Exception e) {
				}
			}
			future = exec.scheduleAtFixedRate(()->allTasks(), frequencyInSeconds, frequencyInSeconds, TimeUnit.MILLISECONDS);
			return this;
		}
		@Override
		public void configure(Managers mgrs, Map<String, String> config) {
			super.configure(mgrs,config);
			setFrequency(getLong(config.get("frequency"),60000L));
		}
	}

	protected static Integer getInt(String str, Integer defaultValue){
		if(str==null) return defaultValue;
		try {
			return new Integer(str);
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}
	protected static Long getLong(String str, Long defaultValue){
		if(str==null) return defaultValue;
		try {
			return new Long(str);
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}
	
}
