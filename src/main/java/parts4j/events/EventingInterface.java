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
package parts4j.events;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import parts4j.managers.BaseManager;

@SuppressWarnings({"rawtypes",	"unchecked"})
public interface EventingInterface<R> {
	BaseManager mgr();
	
	///// EVENT LISTENING REGISTRATION
	default public R publishEvent(String context, String eventName, Object event){
		if(mgr().routing()!=null) mgr().routing().publishEvent(context,eventName,event);
		return (R)this;
	}
	default public R onEvent(String context, String event, Consumer callback){
		if(mgr().routing()!=null) mgr().routing().on(context, event, callback);
		return (R)this;
	}
	default public R onEvent(String context, String event, Supplier callback){
		if(mgr().routing()!=null) mgr().routing().on(context, event, callback);
		return (R)this;
	}
	default public R onEvent(String context, String event, BiConsumer<String, Object> callback){
		if(mgr().routing()!=null) mgr().routing().on(context, event,callback);
		return (R)this;
	}
	default public R onEvent(String context, Predicate<String> eventSelector, Supplier callback){
		if(mgr().routing()!=null) mgr().routing().on(context,eventSelector,callback);
		return (R)this;
	}
	default public R onEvent(String context, Predicate<String> eventSelector, Consumer callback){
		if(mgr().routing()!=null) mgr().routing().on(context, eventSelector,callback);
		return (R)this;
	}
	default public R onEvent(String context, Predicate<String> eventSelector, BiConsumer<String, Object> callback){
		if(mgr().routing()!=null) mgr().routing().on(context, eventSelector,callback);
		return (R)this;
	}
}
