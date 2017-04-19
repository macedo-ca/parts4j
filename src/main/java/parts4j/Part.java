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

import java.util.function.Supplier;

import parts4j.PartStats.Timer;
import parts4j.internal.Methods;
import parts4j.internal.Methods.MethodGetters;
import parts4j.managers.BaseManager;
import parts4j.managers.RollupHelp;

@SuppressWarnings("rawtypes")
public class Part<T> extends BasePart<T,Part<T>> implements MethodGetters<T>{
	PartType<T> tech=null;
	T inst=null;
	boolean singleton=false;
	PartStats stat=null;
	
	public Timer startTimer(String name){
		return partStatistics().startTimer(name);
	}
	public PartStats partStatistics(){
		if(stat==null) stat=new PartStats(reg.stats, this);
		return stat;
	}
	public boolean hasPartStats(){
		return (stat!=null && stat.hasStats());
	}
	public Object getStats() {
		if(hasMethod(Methods.StandardMethod.stats)) return get(Methods.StandardMethod.stats);
		if(hasPartStats()){
			return partStatistics().toMap();
		}
		return null;
	}
	
	public PartType<T> getType(){
		return tech;
	}
	@Override
	public T instance() {
		return inst;
	}
	public Boolean isHealthy(){
		Boolean otherThingsOK = RollupHelp.getRollupHealth(this);
		if(otherThingsOK!=null && !otherThingsOK) return false;
		
		if(hasMethod(Methods.StandardMethod.health)){
			Object o=get(Methods.StandardMethod.health);
			return (otherThingsOK==null || otherThingsOK) && reg.mgrs.getHealthy().isHealthy(o);
		}else if(hasMethod(Methods.StandardMethod.errors)){
			return (otherThingsOK==null || otherThingsOK) && mgr().isHealthyConsideringErrors(this);
		}else{
			return otherThingsOK;
		}
	}
	
	public boolean isSingleton() {
		return singleton;
	}
	Part(PartType<T> tech,T inst, String id){
		this.tech=tech;
		this.inst=inst;
		registerMethod(Methods.StandardMethod.id).set(id);
		initBasedOn(tech);
	}
	Part(PartType<T> tech,T inst){
		this.tech=tech;
		this.inst=inst;
		initBasedOn(tech);
	}
	Part(PartType<T> tech, T inst, Supplier method){
		this.tech=tech;
		this.inst=inst;
		if(method!=null) registerMethod(Methods.StandardMethod.id).set(method);
		initBasedOn(tech);
	}
	void initBasedOn(PartType<T> tech){
		this.reg=tech.reg;
		reg.onCreate(this);
	}
	public BaseManager mgr(){
		return (tech.specificMgr!=null) ? tech.specificMgr : reg.mgrs.rootManager;
	}
	Part<T> setSingleton(boolean isSingleton){
		singleton=isSingleton;
		return this;
	}
}