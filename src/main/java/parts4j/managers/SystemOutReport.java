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

import parts4j.PartRegistry.PartRegistryInst;
import parts4j.managers.BaseManager.ScheduledManager;

public class SystemOutReport extends ScheduledManager{
	static SystemOutReport singleton=new SystemOutReport();
	public static SystemOutReport getSingleton(){
		return singleton;
	}
	
	public SystemOutReport(){}
	public SystemOutReport(PartRegistryInst inst, long frequencyInSeconds){
		setInst(inst);
		setFrequency(frequencyInSeconds);
	}
//	
//	ConcurrentHashMap<String,LinkedList<PartTypeByClass>> partsByNamespace=new ConcurrentHashMap<String,LinkedList<PartTypeByClass>>();
//	@Override
//	protected <T> PartTypeByClass<T> onCreate(PartTypeByClass<T> c){
//		c=super.onCreate(c);
//		String namespace=c.getNamespace();
//		LinkedList<PartTypeByClass> list=partsByNamespace.get(namespace);
//		if(list==null){
//			LinkedList<PartTypeByClass> existing = partsByNamespace.putIfAbsent(namespace, list=new LinkedList<PartTypeByClass>());
//			if(existing!=null) list=existing;
//		}
//		list.add(c);
//		return c;
//	}
	
	public void report(){
		inst.newQuery(types.values()).query(criteria).forEachPart((part)->{
			String namespace=part.getType().getNamespace();
			Boolean healthy=part.isHealthy();
			if(healthy!=null){
				output(namespace+"/"+part.getType().getName()+ "#"+part.getId()+" ("+part.lifecycle() + ") HEALTH:"+ (healthy?"OK":"NotOK"));
			}else{
				output(namespace+"/"+part.getType().getName()+ "#"+part.getId()+" ("+part.lifecycle() + ")");
			}
		});
	}
	void output(String line){
		System.out.println(line);
	}
}