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

import java.util.HashMap;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import parts4j.BasePart;
import parts4j.PartLifeCycle.CycleAction;
import parts4j.managers.BaseManager;

public class Methods {
	private static Logger log=LoggerFactory.getLogger(Methods.class);
	public enum StandardMethod implements MethodToLifeCycleAction{
		id,
		health,
		stats,
		errors,
		tenant,
		version,
		status,
		licence, 
		create,
		install,
		deploy,
		start,
		stop,
		suspend,
		resume,
		destroy}

	public static String DESCRIPTION(StandardMethod m){switch(m){
		case id: 		return "the unique ID of the part";
		case health: 	return "the current health of the part";
		case stats: 	return "statistics for the part";
		case errors: 	return "significant errors for the part";
		case tenant: 	return "the tenant identifier or details for the part";
		case version: 	return "the part version";
		case status: 	return "the current status of the part";
		case licence: 	return "the part licence";
		case create: 	return "creates new instance";
		case install: 	return "performs initial install of the part";
		case deploy: 	return "deploys the part";
		case start: 	return "starts the part";
		case stop: 		return "stops the part";
		case suspend: 	return "temporarily suspends the part";
		case resume: 	return "resumes a suspended part";
		case destroy: 	return "destroys (un-recoverable) a part";
		default : return null;
	}};
	
	public interface MethodGetters<T> {
		Object get(Methods.StandardMethod method);
		default public Object getId() {
			return get(Methods.StandardMethod.id);
		}
		default public Object getStatus() {
			return get(Methods.StandardMethod.status);
		}
		default public Object getVersion() {
			return get(Methods.StandardMethod.version);
		}
		default public Object getErrors() {
			return get(Methods.StandardMethod.errors);
		}
		default public Object getLicence() {
			return get(Methods.StandardMethod.licence);
		}
		default public Object getHealth() {
			return get(Methods.StandardMethod.health);
		}
		default public Object getTenant() {
			return get(Methods.StandardMethod.health);
		}
	}

	public interface ExecuteShortcuts<R> {
		R doAction(Methods.StandardMethod method);
		default public R doCreate(){
			return doAction(Methods.StandardMethod.create);
		}
		default public R doStart(){
			return doAction(Methods.StandardMethod.start);
		}
		default public R doStop(){
			return doAction(Methods.StandardMethod.stop);
		}
		default public R doSuspend(){
			return doAction(Methods.StandardMethod.suspend);
		}
		default public R doResume(){
			return doAction(Methods.StandardMethod.resume);
		}
		default public R doDeploy(){
			return doAction(Methods.StandardMethod.deploy);
		}
		default public R doInstall(){
			return doAction(Methods.StandardMethod.install);
		}
		default public R doDestroy(){
			return doAction(Methods.StandardMethod.destroy);
		}
	}
	
	/**
	 * Adds action method hooks to Part instances, this interface is implemented by Part<T> class
	 * @param <T>
	 */
	@SuppressWarnings({"unchecked","rawtypes"})
	public interface ConfigShortcuts<T,R extends BasePart<T, R>> extends ExecuteShortcuts<R>{
		BaseManager mgr();
		R setMethodValue(Methods.StandardMethod method, String value);
		
		default public R setId(String val) {
			return setMethodValue(Methods.StandardMethod.id,val);
		}
		default public R setStatus(String val) {
			return setMethodValue(Methods.StandardMethod.status,val);
		}
		default public R setVersion(String val) {
			return setMethodValue(Methods.StandardMethod.version,val);
		}
		default public R setErrors(String val) {
			return setMethodValue(Methods.StandardMethod.errors,val);
		}
		default public R setLicence(String val) {
			return setMethodValue(Methods.StandardMethod.licence,val);
		}
		default public R setHealth(String val) {
			return setMethodValue(Methods.StandardMethod.health,val);
		}
		default public R setTenant(String val) {
			return setMethodValue(Methods.StandardMethod.tenant,val);
		}
		default public R setStats(String val) {
			return setMethodValue(Methods.StandardMethod.stats,val);
		}
		
		default public R registerCreate(Supplier method){
			((R)this).registerMethod(Methods.StandardMethod.create,method);
			return (R)this;
		}
		default public R registerId(Supplier method){
			((R)this).registerMethod(Methods.StandardMethod.id,method);
			return (R)this;
		}
		default public R registerLicence(Supplier method){
			((R)this).registerMethod(Methods.StandardMethod.licence,method);
			return (R)this;
		}
		default public R registerStatus(Supplier method){
			((R)this).registerMethod(Methods.StandardMethod.status,method);
			return (R)this;
		}
		default public R registerVersion(Supplier method){
			((R)this).registerMethod(Methods.StandardMethod.version,method);
			return (R)this;
		}
		default public R registerHealth(Supplier method){
			((R)this).registerMethod(Methods.StandardMethod.health,method);
			return (R)this;
		}
		default public R registerStats(Supplier method){
			((R)this).registerMethod(Methods.StandardMethod.stats,method);
			return (R)this;
		}
		default public R registerErrors(Supplier method){
			((R)this).registerMethod(Methods.StandardMethod.errors,method);
			return (R)this;
		}
		default public R registerTenant(Supplier method){
			((R)this).registerMethod(Methods.StandardMethod.tenant,method);
			return (R)this;
		}
		
		default public R registerStart(Supplier callback){
			return ((R)this).registerMethod(Methods.StandardMethod.start, callback);
		}
		default public R registerStop(Supplier callback){
			return ((R)this).registerMethod(Methods.StandardMethod.stop, callback);
		}
		default public R registerSuspend(Supplier callback){
			return ((R)this).registerMethod(Methods.StandardMethod.suspend, callback);
		}
		default public R registerResume(Supplier callback){
			return ((R)this).registerMethod(Methods.StandardMethod.resume, callback);
		}
		default public R registerDeploy(Supplier callback){
			return ((R)this).registerMethod(Methods.StandardMethod.deploy, callback);
		}
		default public R registerInstall(Supplier callback){
			return ((R)this).registerMethod(Methods.StandardMethod.install, callback);
		}
		default public R registerDestroy(Supplier callback){
			return ((R)this).registerMethod(Methods.StandardMethod.destroy, callback);
		}
		
		default public R registerCreate(Runnable callback){
			return ((R)this).registerMethod(Methods.StandardMethod.create, callback);
		}
		default public R registerStart(Runnable callback){
			return ((R)this).registerMethod(Methods.StandardMethod.start, callback);
		}
		default public R registerStop(Runnable callback){
			return ((R)this).registerMethod(Methods.StandardMethod.stop, callback);
		}
		default public R registerSuspend(Runnable callback){
			return ((R)this).registerMethod(Methods.StandardMethod.suspend, callback);
		}
		default public R registerResume(Runnable callback){
			return ((R)this).registerMethod(Methods.StandardMethod.resume, callback);
		}
		default public R registerDeploy(Runnable callback){
			return ((R)this).registerMethod(Methods.StandardMethod.deploy, callback);
		}
		default public R registerInstall(Runnable callback){
			return ((R)this).registerMethod(Methods.StandardMethod.install, callback);
		}
		default public R registerDestroy(Runnable callback){
			return ((R)this).registerMethod(Methods.StandardMethod.destroy, callback);
		}
		
		default public R registerActions(String action, Supplier callback, String action2, Supplier callback2){
			((R)this).registerAction(action,callback);
			((R)this).registerAction(action2, callback2);
			return (R)this;
		} 
		default public R registerActions(String action, Supplier callback, String action2, Supplier callback2, String action3, Supplier callback3){
			((R)this).registerAction(action,callback);
			((R)this).registerAction(action2, callback2);
			((R)this).registerAction(action3,callback3);
			return (R)this;
		}
		default public R registerActions(String action, Supplier callback, String action2, Supplier callback2, String action3, Supplier callback3, String action4, Supplier callback4){
			((R)this).registerAction(action,callback);
			((R)this).registerAction(action2, callback2);
			((R)this).registerAction(action3,callback3);
			((R)this).registerAction(action4,callback4);
			return (R)this;
		}
		default public R registerActions(String action, Supplier callback, String action2, Supplier callback2, String action3, Supplier callback3, String action4, Supplier callback4, String action5, Supplier callback5){
			((R)this).registerAction(action,callback);
			((R)this).registerAction(action2, callback2);
			((R)this).registerAction(action3,callback3);
			((R)this).registerAction(action4,callback4);
			((R)this).registerAction(action5,callback5);
			return (R)this;
		}
		
		default public R registerMethods(String commaList, Supplier ... sups){
			Methods.registerMethods((R)this,commaList,sups);
			return (R)this;
		}
		default public R registerMethods(String commaList, Runnable... sups){
			Methods.registerMethods((R)this,commaList,sups);
			return (R)this;
		}
	}
	

	public interface MethodToLifeCycleAction{
		default CycleAction action(){
			try {
				return CycleAction.valueOf(((StandardMethod)this).name());
			} catch (Throwable e) {
				return null;
			}
		}
	}
	
	static HashMap<String,Methods.StandardMethod> methNames=new HashMap<String,Methods.StandardMethod>();
	static{
		for(Methods.StandardMethod m : Methods.StandardMethod.values()) methNames.put(m.name(),m);
	}
	public static StandardMethod methodOrNull(String name){
		return methNames.get(name!=null?name.toLowerCase():null);
	}
	public static boolean isMethod(String meth){
		return methNames.containsKey(meth!=null?meth.toLowerCase():"");
	}
	
	@SuppressWarnings("rawtypes")
	private static void registerMethods(BasePart part, String commaList, Supplier[] sups){
		String[] sp=commaList.split(",");
		if(sp.length==sups.length){
			for(int i=0;i<sp.length;i++){
				try {
					part.registerMethod(StandardMethod.valueOf(sp[i]),sups[i]);
				} catch (Throwable e) {
					log.error("Error register method: "+sp[i],e);
				}
			}
		}else{
			log.error("Comma-seperated list of methods '"+commaList+"' does not match lenght of methods "+sups.length);
		}
	}
	@SuppressWarnings("rawtypes")
	private static void registerMethods(BasePart part, String commaList, Runnable[] sups){
		String[] sp=commaList.split(",");
		if(sp.length==sups.length){
			for(int i=0;i<sp.length;i++){
				try {
					part.registerMethod(StandardMethod.valueOf(sp[i]),sups[i]);
				} catch (Throwable e) {
					log.error("Error register method: "+sp[i],e);
				}
			}
		}else{
			log.error("Comma-seperated list of methods '"+commaList+"' does not match lenght of methods "+sups.length);
		}
	}
}
