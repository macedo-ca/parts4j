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

import parts4j.Part;
import parts4j.PartType;
import parts4j.PartsQuery;

@SuppressWarnings({"rawtypes","unchecked"})
public class RollupHelp {
	BaseManager mgr;
	public RollupHelp(BaseManager mgr){
		this.mgr=mgr;
	}
	
	public String level=null;
	public static Boolean getRollupHealth(Part<?> part) {
		HealthyStat st=part.getMetaData(HealthyStat.class,false);
		//System.out.println(part.getId() +" had roll-up health "+st);
		return st!=null ? st.perfect() : null;
	}
	public void rollup(){
		PartsQuery.visitTypes(mgr, (type)->{
			if(type.getLevel().equals(level)){
				type.forEachPart((part)->{
					// Only singleton roll-up supported
					if(part.isSingleton()){
						HealthyStat calc=new HealthyStat();
						healthPercentage(calc, type);
						part.setMetaData(HealthyStat.class, calc);
					}
				});
			}
		});
	}
	public static class HealthyStat{
		int healthy=0;
		int total=0;
		public boolean perfect(){
			return healthy==total;
		}
		public double percentage(){
			return total>0 ? (double)(100.0*healthy/total) : 100.0;
		}
	}
	public void healthPercentage(HealthyStat calc, PartType t){
		t.forEachPart((part)->{
			Boolean h=part.isHealthy();
			if(h!=null && h){
				calc.healthy++;
			}else{
				// Nothing for now
			}
			if(h!=null) calc.total++;
		});
		t.forEachSubType((type)->{
			//System.out.println(t.getName()+" has sub "+type.getName());
			healthPercentage(calc,type);
		});
	}
	public String getLevel() {
		return level;
	}
	public void setLevel(String level) {
		this.level = level;
	}
	
}
