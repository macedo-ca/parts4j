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

import java.io.Closeable;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.javasimon.Manager;
import org.javasimon.Simon;
import org.javasimon.SimonManager;
import org.javasimon.Split;
import org.javasimon.Stopwatch;

import parts4j.PartRegistry.PartRegistryInst;

public class PartStats {
	static String DELIM=Manager.HIERARCHY_DELIMITER;
	public static class PartsStats{
		public PartsStats(PartRegistryInst inst){
			this.inst=inst;
		}
		PartRegistryInst inst=null;
		Manager simon=SimonManager.manager();
	}
	
	boolean hasStats=false;
	public boolean hasStats(){
		return hasStats;
	}
	String partID=null;
	PartsStats stats=null;
	String prefix=null;
	@SuppressWarnings("rawtypes")
	PartStats(PartsStats stats, Part part){
		this.prefix=part.getType().getName();
		this.stats=stats;
		Object o=part.getId();
		if(o!=null) partID=o.toString();
	}
	
	@SuppressWarnings("resource")
	public Counter increaseCounter(String name){
		if(stats.simon==null) return new Counter();
		hasStats=true;
		org.javasimon.Counter c = stats.simon.getCounter(prefix+DELIM+name);
		return new Counter(c).increase();
	}
	public Counter getCounterInst(String name){
		if(stats.simon==null) return new Counter();
		hasStats=true;
		org.javasimon.Counter c = stats.simon.getCounter(prefix+DELIM+name);
		org.javasimon.CounterSample samp = c.sample();
		return new Counter(c,samp);
	}
	public Counter getCounter(String name){
		if(stats.simon==null) return new Counter();
		hasStats=true;
		org.javasimon.Counter c = stats.simon.getCounter(prefix+DELIM+name);
		return new Counter(c);
	}
	public class Counter implements Closeable{
		public Counter setUnits(String units){
			if(c!=null) c.setAttribute("units", units);
			return this;
		}
		org.javasimon.CounterSample samp=null;
		org.javasimon.Counter c=null;
		Counter(){}
		Counter(org.javasimon.Counter c,org.javasimon.CounterSample samp){
			this.samp=samp;
			this.c=c;
		}
		Counter(org.javasimon.Counter c){
			this.c=c;
		}
		public Counter increase(long v){
			if(c!=null) c.increase(v);
			return this;
		}
		public Counter increase(){
			if(c!=null) c.increase();
			return this;
		}
		public Counter decrease(){
			if(c!=null) c.decrease();
			return this;
		}
		@Override
		public void close() throws IOException {
			decrease();
			c=null;
		}
	}
	
	public Timer startTimer(String timer){
		if(stats.simon==null) return new Timer();
		hasStats=true;
		return new Timer(stats.simon.getStopwatch(prefix+DELIM+timer).start());
	}
	public class Timer implements Closeable{
		Split sp=null;
		Timer(){}
		Timer(Split sp){
			this.sp=sp;
		}
		public void stop(){
			if(sp!=null) sp.stop();
		}
		@Override
		public void close() {
			stop();
		}
	}
	
	public String print() {
		String out="";
		if(stats.simon==null) return out;
		Simon parent=stats.simon.getSimon(prefix);
		for(Simon s : parent.getChildren()){
			String units=(s.getAttribute("units")!=null?s.getAttribute("units").toString():"count");
			if(s instanceof Stopwatch){
				out+=s.getName() +" " + ((Stopwatch)s).getCounter()+" "+units+", " + (((Stopwatch)s).getMean()/1000000.0) + " ms avg, " + (((Stopwatch)s).getMin()/1000000.0) + " ms min, " + (((Stopwatch)s).getMax()/1000000.0) + " ms max";
			}else if(s instanceof org.javasimon.Counter){
				out+=s.getName() +" " + ((org.javasimon.Counter)s).getCounter() + " "+units;
			}
			out+="\n";
		}
		return out;
	}
	public String toString(){
		return print();
	}
		
	public Map<String,Object> toMap() {
		return toMap(false);
	}
	public Map<String,Object> toMap(boolean withPrefixInKey) {
		if(stats.simon==null) return null;
		Simon parent=stats.simon.getSimon(prefix);
		LinkedHashMap<String, Object> out=new LinkedHashMap<>();
		if(parent==null) return out;
		for(Simon s : parent.getChildren()){
			LinkedHashMap<String, Object> item=new LinkedHashMap<>();
			String units=(s.getAttribute("units")!=null?s.getAttribute("units").toString():"count");
			if(s instanceof Stopwatch){
				item.put("count", ((Stopwatch)s).getCounter());
				item.put("units", units);
				item.put("mean", (((Stopwatch)s).getMean()/1000000.0));
				item.put("min", (((Stopwatch)s).getMin()/1000000.0));
				item.put("max", (((Stopwatch)s).getMax()/1000000.0));
			}else if(s instanceof org.javasimon.Counter){
				item.put("count", ((org.javasimon.Counter)s).getCounter());
				item.put("units", units);
			}
			out.put(withPrefixInKey ? s.getName() : s.getName().substring(prefix.length()+1), item);
		}
		return out;
	}
}
