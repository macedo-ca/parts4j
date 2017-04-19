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

import java.util.LinkedList;
import java.util.function.Predicate;
import java.util.function.Supplier;

import parts4j.Part;
import parts4j.PartsQuery.PartTypeVisitor;
import parts4j.PartsQuery.PartVisitor;
import parts4j.managers.BaseManager;

@SuppressWarnings({"rawtypes"})
public class PartType<T> extends BasePart<T,PartType<T>>{
	protected BaseManager specificMgr=null;
	
	public void setMgr(BaseManager specificMgr) {
		this.specificMgr = specificMgr;
	}
	protected String namespace=null;
	protected String name=null;
	protected String level=null;
	
	LinkedList<PartType<?>> subs=new LinkedList<>();
	LinkedList<Part> parts=new LinkedList<Part>();
	
	public PartType forEachSubType(PartTypeVisitor vis){
		for(PartType t : subs) vis.visit(t);
		return this;
	}
	public PartType forEachPart(PartVisitor vis){
		for(Part p : parts){
			if(p!=null) vis.visit(p);
		}
		return this;
	}
	public PartType forEachSubType(Predicate<PartType> selector, PartTypeVisitor vis){
		for(PartType t : subs) if(selector.test(t)) vis.visit(t);
		return this;
	}
	public PartType forEachPart(Predicate<Part> selector, PartVisitor vis){
		for(Part p : parts) if(selector.test(p)) vis.visit(p);
		return this;
	}
	
	public LinkedList<Part> listParts(){
		return parts;
	}
	public LinkedList<PartType<?>> listSubs(){
		return subs;
	}
	
	public BaseManager mgr(){
		return (specificMgr!=null) ? specificMgr : reg.mgrs.rootManager;
	}
	
	public String getNamespace() {
		return namespace;
	}
	protected void setNamespace(String namespace) {
		this.namespace = namespace;
	}
	public String getName() {
		return name;
	}
	protected void setName(String name) {
		this.name = name;
	}
	public String getLevel() {
		return level;
	}
	protected void setLevel(String level) {
		this.level = level;
	}
	public <S> PartType<S> addSub(PartType<S> sub){
		subs.add(sub);
		return sub;
	}
	public Part<T> register(T inst, Supplier idMethod){
		Part<T> p =new Part<T>(this,inst,idMethod);
		parts.add(p);
		return p;
	}
	public Part<T> register(T inst, String id){
		Part<T> p =new Part<T>(this,inst,id);
		parts.add(p);
		return p;
	}
	public Part<T> registerSingleton(T inst){
		Part<T> p =new Part<T>(this,inst,getName()).setSingleton(true);
		parts.add(p);
		return p;
	}
	public Part<T> registerSingleton(T inst, String id){
		Part<T> p =new Part<T>(this,inst,id).setSingleton(true);
		parts.add(p);
		return p;
	}
	public Part<T> registerSingleton(T inst, Supplier idMethod){
		Part<T> p =new Part<T>(this,inst,idMethod).setSingleton(true);
		parts.add(p);
		return p;
	}
	public PartType<T> action(String action, String method){
		if(mgr().routing()!=null) mgr().routing().action(this, action, method);
		return this;
	}
}