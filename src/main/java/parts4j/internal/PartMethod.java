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

import java.lang.reflect.Method;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import parts4j.PartLifeCycle.CycleAction;

@SuppressWarnings("rawtypes")
public class PartMethod {
	enum Source{mthd,supp,run,value}
	static final Logger log = LoggerFactory.getLogger(PartMethod.class);
	static Object[] NULL_ARG=new Object[0];
	
	public CycleAction asAction(){
		return method.action();
	}
	
	public PartMethod(Methods.StandardMethod m){
		method=m;
	}
	Source src=null;
	Supplier supplier=null;
	Runnable runnable=null;
	Method clzMethod=null;
	String value=null;
	
	Methods.StandardMethod method=null;
	
	public boolean isSet(){
		return src!=null;
	}
	
	public void set(String value){
		if(value==null) return;
		src=Source.value;
		this.value=value;
	}
	public void set(Supplier s){
		if(s==null) return;
		src=Source.supp;
		supplier=s;
	}
	public void set(Runnable s){
		if(s==null) return;
		src=Source.run;
		runnable=s;
	}
	public void set(Method m){
		if(m==null) return;
		src=Source.mthd;
		clzMethod=m;
	}
	@SuppressWarnings("unchecked")
	public void set(Class clz, String method){
		if(method==null) return;
		try {
			set(clz.getMethod(method, new Class[0]));
		} catch (Throwable e) {
			log.error("No such method " + method +" for "+clz,e);
		}
	}
	public Object get(Object actualObjectInstance){
		try {
			switch(src){
			case value: return value;
			case mthd:	return clzMethod.invoke(actualObjectInstance, NULL_ARG);
			case supp:	return supplier.get();
			case run: 	runnable.run(); return null;
			}
			return null;
		} catch (Throwable e) {
			return null;
		}
	}
}
