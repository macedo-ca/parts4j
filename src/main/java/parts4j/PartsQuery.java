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

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.function.Supplier;

import parts4j.PartRegistry.Managers;
import parts4j.PartRegistry.PartRegistryInst;
import parts4j.internal.Methods.ExecuteShortcuts;
import parts4j.internal.PartsQueryFields.CriteriaFields;
import parts4j.internal.PartsQueryIntf;
import parts4j.internal.Methods;
import parts4j.internal.PartsQueryFields;

@SuppressWarnings("rawtypes")
public class PartsQuery implements PartsQueryIntf.Query{
	public PartsQuery query(){
		return this;
	}
	
	public PartsQuery(PartRegistryInst inst){
		this(inst,inst.mgrs);
	}
	public PartsQuery(PartRegistryInst inst, Managers mgrs){
		this.inst=inst;
		levels=mgrs!=null ? mgrs.getLevels() : null;
	}
	PartRegistryInst inst=null;
	Criteria criteria=new Criteria();
	PartLevels levels=null;
	Collection<PartType> fromColl=null;
	Visitable fromVisitable=null;
	
	@Override
	public PartsQuery queryFrom(Collection<PartType> types){
		fromColl=types;
		return this;
	}
	@Override
	public PartsQuery queryFrom(Visitable fromVisitable){
		this.fromVisitable=fromVisitable;
		return this;
	}
	public PartsQuery setLevels(PartLevels levels) {
		this.levels = levels;
		return this;
	}
	
	public Criteria criteria(){
		return criteria;
	}
	
	public CriteriaDef compileCriteria(String criteriaStr) {
		if(criteriaStr==null) return null;
		String[] parts=criteriaStr.split("[,;\\n]");
		for(String p : parts){
			if(p.equals("unhealthy")){
				criteria.healthNotOK();
			}else{
				String lvl=levels.getValidLevel(p);
				if(lvl!=null){
					criteria.level(lvl);
				}else if(p.contains(".")){
					Class<?> clz=null;
					try{clz=Thread.currentThread().getContextClassLoader().loadClass(p.trim());}catch(Throwable t){}
					if(clz!=null){
						criteria.queryFor(clz);
					}else{
						criteria.namespace(p);
					}
				}else{
					// TODO other options
				}
			}
		}
		return criteria.def;
	}
	
	public interface PartTypeVisitor{
		void visit(PartType p);
	}
	public interface PartVisitor{
		void visit(Part p);
	}
	public interface Visitable{
	}
	public interface MapVisitable extends Visitable{
		Map<?,PartType> visitableMap();
	}
	public interface ListVisitable extends Visitable{
		Collection<PartType> visitableList();
	}
	
	public static void visitParts(Visitable from, PartVisitor vis){
		Collection<PartType> col = col0(from);
		for(PartType t : col) t.forEachPart(vis);
	}
	@SuppressWarnings("unchecked")
	public static void visitParts(Visitable from, Predicate<Part> selector, PartVisitor vis){
		Collection<PartType> col = col0(from);
		for(PartType t : col) t.forEachPart(selector,vis);
	}
	public static void visitTypes(Visitable from, PartTypeVisitor vis){
		Collection<PartType> col = col0(from);
		for(PartType t : col) vis.visit(t);
	}
	private static Collection<PartType> col0(Visitable from) {
		Collection<PartType> col=null;
		if(from instanceof MapVisitable){
			col = ((MapVisitable)from).visitableMap().values();
		}else{
			col =((ListVisitable)from).visitableList();
		}
		return col;
	}
	public static void visitTypes(Visitable from, Predicate<PartType> selector, PartTypeVisitor vis){
		Collection<PartType> col = col0(from);
		for(PartType t : col) if(selector.test(t)) vis.visit(t);
	}
	
	@SuppressWarnings("unchecked")
	public
	PartsQuery forEachPart0(PartVisitor vis){
		Predicate<Part> selector=criteria::eval;
		if(fromVisitable!=null && fromColl==null){
			fromColl=col0(fromVisitable);
		}
		if(fromColl!=null){
			for(PartType t : fromColl) t.forEachPart(selector,vis);
		}else{
			if(inst.mgrs.chain!=null) inst.mgrs.chain.visitPartsChain(selector,vis);
		}
		return this;
	}
	public PartsQuery forEachType0(PartTypeVisitor vis){
		Predicate<PartType> selector=criteria::eval;
		if(fromVisitable!=null && fromColl==null){
			fromColl=col0(fromVisitable);
		}
		if(fromColl!=null){
			for(PartType t : fromColl) if(selector.test(t)) vis.visit(t);
		}else{
			if(inst.mgrs.chain!=null) inst.mgrs.chain.visitTypesChain(criteria::eval,vis);
		}
		return this;
	}
	Integer levelIndex(String lvl){
		return inst.mgrs.getLevels().getLevelNum(lvl);
	}
	public class Criteria extends CriteriaFields implements PartsQueryIntf.Query{
		int criteriaCount=0;
		boolean all=true;
		CritItem first=null;
		boolean ignoreCase=true;
		
		public Criteria ignoreCase(){
			ignoreCase=true;
			return this;
		}
		public Criteria caseSensitive(){
			ignoreCase=false;
			return this;
		}
		
		@Override
		public PartsQuery query() {
			return PartsQuery.this;
		}
		public PartsQuery endCriteria(){
			return PartsQuery.this;
		}
		
		PartType type=null;
		Part part=null;
		protected PartType getType(){
			return type;
		}
		protected Part getPart(){
			return part;
		}
		public boolean eval(PartType type){
			if(all) return true;
			this.type=type;
			this.part=null;
			return first.eval();
		}
		public boolean eval(Part part){
			if(all) return true;
			this.type=part.getType();
			this.part=part;
			return first.eval();
		}
		
		protected void add(Supplier from, Predicate eval){
			all=false;
			CritItem i=new CritItem(from, eval);
			if(first==null){
				first=i;
			}else{
				first.add(i);
			}
			criteriaCount++;
		}
		protected Criteria stringCriteria(PartsQueryFields.CritType cr, Supplier partVarSupplier, String value, boolean fromPart){
			if(fromPart){
				parts=true;
			}else{
				types=true;
			}
			final CriteriaTest tst=new CriteriaTest(value,ignoreCase);
			add(partVarSupplier,(v)->{return tst.match(v);});
			def.add(cr, value);
			return this;
		}
		public Criteria levelBelow(final String inVal){
			types=true;
			final Integer levelIndex=levelIndex(inVal);
			if(levelIndex==null){
				throw new RuntimeException("'"+inVal+"' is not a valid level, the 'levelBelow' criteria requires reference to a valid level such as "
						+ Arrays.asList(inst.mgrs.getLevels().levels));
			}
			add(()->
				{ 		 // Determine value
					Integer lvlI=levelIndex(getType().getLevel());
					return lvlI;
				},(v)->{ // Test code
					return v!=null && ((Integer)v)>levelIndex;
			});
			def.add(PartsQueryFields.CritType.levelBelow, inVal);
			return this;
		}
	}
	public static class CriteriaTest{
		Comp c=null;
		String compareTo=null;
		boolean ignoreCase=true;
		CriteriaTest(String criteria, boolean ignoreCase){
			this.ignoreCase=ignoreCase;
			if(ignoreCase) criteria=criteria.toLowerCase();
			c = comparator(criteria);
			switch(c){
			case prefix: compareTo=criteria.substring(0, criteria.length()-1); return;
			case suffix: compareTo=criteria.substring(1); return;
			case contains: compareTo=criteria.substring(1, criteria.length()-1); return;
			default: compareTo=criteria;
			}
		}
		public boolean match(Object instanceValueObj){
			if(instanceValueObj==null) return false;
			String instanceValue = instanceValueObj instanceof String ? (String)instanceValueObj : instanceValueObj.toString();
			if(ignoreCase) instanceValue=instanceValue.toLowerCase();
			switch(c){
			case prefix: 	return instanceValue.startsWith(compareTo);
			case suffix: 	return instanceValue.endsWith(compareTo);
			case contains: 	return instanceValue.contains(compareTo);
			default: 		return instanceValue.equals(compareTo);
			}
		}
	}
	public static Comp comparator(String s){
		if(s.endsWith("*")){
			if(s.startsWith("*")){
				return Comp.contains;
			}
			return Comp.prefix;
		}else if(s.startsWith("*")){
			if(s.endsWith("*")){
				return Comp.contains;
			}
			return Comp.suffix;
		}else{
			return Comp.equals;
		}
	}
	enum Comp{equals,prefix,suffix,contains}
	public static class CritDef{
		Criteria apply(Criteria cr){
			return PartsQueryFields.applyDef(type,cr,val);
		}
		
		CritDef next=null;
		PartsQueryFields.CritType type=null;
		Object val=null;
		CritDef(PartsQueryFields.CritType ty, Object val){
			type=ty;
			this.val=val;
		}
		void add(CritDef newItem){
			if(next==null){
				next=newItem;
			}else{
				next.add(newItem);
			}
		}
	}
	public static class CriteriaDef{
		int criteriaCount=0;
		boolean all=true;
		CritDef first=null;
		public void apply(Criteria cr){
			apply0(cr,first);
		}
		private void apply0(Criteria cr, CritDef def){
			if(def!=null){
				def.apply(cr);
				if(def.next!=null){
					apply0(cr,def.next);
				}
			}
		}
		public void add(PartsQueryFields.CritType type, Object val){
			all=false;
			CritDef i=new CritDef(type, val);
			if(first==null){
				first=i;
			}else{
				first.add(i);
			}
			criteriaCount++;
		}
		public Predicate<Part> partSelector(PartRegistryInst inst) {
			return inst.newQuery().query(this).criteria::eval;
		}
		public Predicate<PartType> partTypeSelector(PartRegistryInst inst) {
			return inst.newQuery().query(this).criteria::eval;
		}
	}
	
	@SuppressWarnings({"unchecked"})
	private class CritItem{
		CritItem next=null;
		Supplier fr=null;
		Predicate ev=null;
		CritItem(Supplier from, Predicate eval){
			fr=from;
			ev=eval;
		}
		void add(CritItem newItem){
			if(next==null){
				next=newItem;
			}else{
				next.add(newItem);
			}
		}
		boolean eval(){
			try {
				Object o= fr.get();
				return ev.test(o) && (next==null || next.eval());
			} catch (Throwable e) {
				e.printStackTrace();
				return false;
			}
		}
	}
	
	//// PartResult interface
	public PartQueryResult forEachPartDo0() {
		return new PartQueryResult();
	}
	public class PartQueryResult implements ExecuteShortcuts<PartsQuery>{
		@Override
		public PartsQuery doAction(Methods.StandardMethod method) {
			forEachPart0((part)->{
				part.doAction(method);
			});
			return PartsQuery.this;
		}
		public int count(){
			AtomicInteger i=new AtomicInteger();
			forEachPart0((part)->{
				i.incrementAndGet();
			});
			return i.get();
		}
	}
}
