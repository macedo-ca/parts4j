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

import parts4j.internal.PartMethod;

import static parts4j.PartLifeCycle.CycleStatus.*;
public class PartLifeCycle {
	public enum CycleStatus{
		none,unknown,creating
		,installing,installed
		,deploying,deployed
		,registering,registered
		,starting
		,active
		,restarting
		,disabling,disabled
		,enabling
		,suspending,suspended
		,resuming
		,failed,failedtostart,crashed,outofmemory,outofresources
		,stopping,stopped
		,destroying,destroyed
		,deregistering
		,undeploying
	}
	public enum CycleAction implements ActionStatuses{
		create
		,install
		,deploy
		,register
		,start
		,restart
		,disable
		,enable
		,suspend
		,resume
		,stop{
			@Override public CycleStatus before() { return stopping; }
			@Override public CycleStatus after() { return stopped; }
		}
		,destroy
		,deregister
		,undeploy
	}
	
	public String toString(){
		return status.toString();
	}
	
	public PartLifeCycle(){
	}
	public PartLifeCycle(String status){
		setStatus(status);
	}
	public PartLifeCycle(CycleStatus status){
		this.status=status;
	}
	
	CycleStatus status=CycleStatus.none;
	public String status(){
		return status.toString();
	}
	public PartLifeCycle setStatus(String status){
		this.status=CycleStatus.valueOf(status);
		return this;
	}
	public PartLifeCycle setStatus(CycleStatus status){
		this.status=status;
		return this;
	}
	
	public <T> Object moveTo(PartMethod method, T inst){
		return moveTo(method, inst,Object.class);
	}
	@SuppressWarnings("unchecked")
	public <R,T> R moveTo(PartMethod method, T inst, Class<R> resultType){
		CycleAction act=method.asAction();
		if(act==null) return null;
		status=act.before();
		try{
			R out=(R)method.get(inst);
			status=act.after();
			return out;
		}catch(Throwable t){
			if(t instanceof RuntimeException) throw (RuntimeException)t;
			throw new RuntimeException(t);
		}
	}
	public <R> R moveTo(CycleAction newStatus, Supplier<R> supplier){
		CycleStatus errorStatus=null;
		if(newStatus==CycleAction.start){
			errorStatus=failedtostart;
		}
		return moveTo(newStatus,supplier,errorStatus);
	}
	public <R> R moveTo(CycleAction newStatus, Supplier<R> supplier, CycleStatus errorStatus){
		status=newStatus.before();
		try{
			R out=supplier.get();
			status=newStatus.after();
			return out;
		}catch(Throwable t){
			if(errorStatus!=null) status=errorStatus;
			if(t instanceof RuntimeException) throw (RuntimeException)t;
			throw new RuntimeException(t);
		}
	}
	
	interface ActionStatuses{
		default CycleAction self(){
			return (CycleAction)this;
		}
		default CycleStatus before(){
			String ac=self().toString();
			if(ac.endsWith("e")) ac=ac.substring(0, ac.length()-1);
			return CycleStatus.valueOf(ac+"ing");
		}
		default CycleStatus after(){
			String ac=self().toString();
			if(ac.endsWith("e")) ac=ac.substring(0, ac.length()-1);
			return CycleStatus.valueOf(ac+"ed");
		}
	}
}
