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

import parts4j.PartRegistry.PartRegistryInst;
import parts4j.PartType;

public class PartTypeByClass<T> extends PartType<T>{
	Class<T> clz=null;
	Method healthMethod=null;
	Method statsMethod=null;
	Method errorMethod=null;
	
	Class<T> clz(){
		return clz;
	}
	
	public PartTypeByClass(PartRegistryInst reg, Class<T> clz, String level){
		this.reg=reg;
		this.clz=clz;
		this.level=level;
		setNamespace(clz.getPackage().getName());
		setName(clz.getSimpleName());
	}
	public PartType<T> health(String method){
		registerMethod(Methods.StandardMethod.health).set(clz(),method);
		return this;
	}
	public PartType<T> stats(String method){
		registerMethod(Methods.StandardMethod.stats).set(clz(),method);
		return this;
	}
	public PartType<T> errors(String method){
		registerMethod(Methods.StandardMethod.errors).set(clz(),method);
		return this;
	}
}