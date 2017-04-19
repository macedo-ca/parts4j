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

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import parts4j.PartLifeCycle.CycleAction;
import parts4j.PartRegistry.PartRegistryInst;
import parts4j.events.EventingInterface;
import parts4j.internal.Methods.ConfigShortcuts;
import parts4j.internal.Methods;
import parts4j.internal.PartMethod;
import parts4j.managers.BaseManager;

@SuppressWarnings({"rawtypes","unchecked"})
public class BasePart<T,R extends BasePart<T,R>> implements EventingInterface<R>, ConfigShortcuts<T,R>{
	protected PartRegistryInst reg=null; 
	PartLifeCycle cycle=new PartLifeCycle();
	
	boolean noop=true;
	
	public PartLifeCycle lifecycle(){
		return cycle;
	}
	
	public PartEnv env(){
		return reg.env();
	}
	
	public R registerSubOf(Class otherClass){
		reg.register(otherClass).addSub(getType());
		return (R)this;
	}
	
	public R registerAction(String action, Supplier callback){
		Methods.StandardMethod m=Methods.methodOrNull(action);
		if(m!=null){
			((R)this).registerMethod(m, callback);
		}else{
			if(mgr().routing()!=null){
				if(this instanceof Part){
					mgr().routing().action((Part<T>)this, action, callback);
				}else{
					mgr().routing().action((PartType<T>)this, action, callback);
				}
			}
		}
		return (R)this;
	}
	public R registerAction(String action, Runnable callback){
		Methods.StandardMethod m=Methods.methodOrNull(action);
		if(m!=null){
			((R)this).registerMethod(m, callback);
		}else{
			if(mgr().routing()!=null){
				if(this instanceof Part){
					mgr().routing().action((Part<T>)this, action, callback);
				}else{
					mgr().routing().action((PartType<T>)this, action, callback);
				}
			}
		}
		return (R)this;
	}
	public R doAction(String name){
		return doAction(Methods.StandardMethod.valueOf(name));
	}
	public R doAction(Methods.StandardMethod method){
		PartMethod m=registerMethod(method);
		if(m.isSet()){
			CycleAction ac=m.asAction();
			if(ac!=null){
				lifecycle().moveTo(m, instance());
			}else{
				m.get(instance());
			}
		}
		return (R)this;
	}
	
	PartMethod[] methods=new PartMethod[Methods.StandardMethod.values().length];
	protected PartMethod registerMethod(Methods.StandardMethod m){
		return methods[m.ordinal()]!=null ? methods[m.ordinal()] : (methods[m.ordinal()]=new PartMethod(m));
	}
	public boolean hasMethod(Methods.StandardMethod m){
		PartMethod meth=((BasePart)this).registerMethod(m);
		if(meth.isSet()) return true;
		if(this instanceof Part){
			Part p =(Part)this;
			meth = p.tech.registerMethod(m);
			return meth.isSet();
		}
		return false;
	}
	public Object get(Methods.StandardMethod method){
		PartMethod m=registerMethod(method);
		if(m.isSet()) return m.get(instance());
		if(this instanceof Part){
			Part p =(Part)this;
			m = p.tech.registerMethod(method);
			return m.get(method);
		}
		return null;
	}
	@Override
	public R setMethodValue(Methods.StandardMethod method, String value){
		return registerMethod(method,value);
	}

	public boolean isNoop() {
		return noop;
	}
	R setNoop(boolean noop) {
		this.noop = noop;
		return (R)this;
	}
	public <U> U using(U u){
		return u;
	}
	public <U> U using(U u, String usingFor){
		return u;
	}
	public BaseManager mgr(){
		return null;
	}
	public T instance(){
		return null;
	}
	public PartType<T> getType(){
		return (PartType<T>)this;
	}
	public R registerMethod(Methods.StandardMethod method, String value){
		registerMethod(method).set(value);
		return (R)this;
	}
	public R registerMethod(Methods.StandardMethod method, Supplier callback){
		registerMethod(method).set(callback);
		return (R)this;
	}
	public R registerMethod(Methods.StandardMethod method, Runnable callback){
		registerMethod(method).set(callback);
		return (R)this;
	}
	
	//// Extension capability
	ConcurrentHashMap<Class, Object> metaData=new ConcurrentHashMap<Class, Object>();
	public <M> M getMetaData(Class<M> clz){
		return getMetaData(clz,true);
	}
	public <M> M getMetaData(Class<M> clz, boolean createIfNotPresent){
		M out=(M)metaData.get(clz);
		if(out!=null) return out;
		if(createIfNotPresent){
			synchronized(this){
				out=(M)metaData.get(clz);
				if(out==null){
					try {
						out = clz.newInstance();
						metaData.put(clz, out);
					} catch (Throwable e) {
					}
				}
			}
		}
		return (M)metaData.get(clz);
	}
	public <M> void setMetaData(Class<M> clz, M inst){
		metaData.put(clz, inst);
	}

}