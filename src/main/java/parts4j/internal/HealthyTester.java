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
package parts4j.internal;

import java.util.HashSet;
import java.util.Map;

import parts4j.PartRegistry.PartRegistryInst;

public class HealthyTester {
	public static String DEFAULT_HEALTHY="200,ok,true,yes,null,good,green";
	HashSet<String> strings=new HashSet<String>();
	boolean nullIsHealthy=false;
	
//	public HealthyTester(PartRegistryInst inst, String ... healthy){
//		reset();
//		strings.addAll(Arrays.asList(healthy));
//	}
	
	public boolean isHealthy(Object o){
		if(o==null){
			return nullIsHealthy;
		}
		return strings.contains(o.toString().toLowerCase());
	}
	
	public HealthyTester(PartRegistryInst inst, Map<String,String> settings){
		String k = inst.getPrefix() +".healthy";
		String v = settings.get(k);
		if(v!=null){
			setStrings(v);
		}else{
			setStrings(DEFAULT_HEALTHY);
		}
	}
	private void reset(){
		strings=new HashSet<String>();
		nullIsHealthy=false;
	}
	private void setStrings(String stringConcat){
		reset();
		String[] hlt=stringConcat.toLowerCase().split(",");
		for(String s : hlt){
			if(hlt.equals("null")){
				nullIsHealthy=true;
			}else{
				strings.add(s);
			}
		}
	}
	
	public boolean isNullIsHealthy() {
		return nullIsHealthy;
	}

	public void setNullIsHealthy(boolean nullIsHealthy) {
		this.nullIsHealthy = nullIsHealthy;
	}
	
}
