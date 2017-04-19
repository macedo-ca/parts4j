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

import java.util.function.Predicate;
import java.util.function.Supplier;

import parts4j.Part;
import parts4j.PartType;
import parts4j.PartsQuery;
import parts4j.PartsQuery.Criteria;
import parts4j.PartsQuery.CriteriaDef;

@SuppressWarnings("rawtypes")
public class PartsQueryFields {
	public enum CritType{hasErrors,healthNotOK,tenant,status,level,levelBelow,hasPartStats,id,name,namespace,version,licence}
	
	public static Criteria applyDef(PartsQueryFields.CritType type, Criteria cr, Object val){
		switch(type){
		case hasErrors: 	return cr.hasErrors();
		case hasPartStats: 	return cr.hasPartStats();
		case healthNotOK: 	return cr.healthNotOK();
		case tenant: 		return cr.tenant((String)val);
		case level: 		return cr.level((String)val);
		case levelBelow: 	return cr.levelBelow((String)val);
		case id: 			return cr.id((String)val);
		case name: 			return cr.name((String)val);
		case version: 		return cr.version((String)val);
		case status: 		return cr.status((String)val);
		case namespace: 	return cr.namespace((String)val);
		case licence: 		return cr.licence((String)val);
		} throw new RuntimeException("Invalid type "+type);
	}
	public interface CritFields{
		PartsQuery query();
		
		default public PartsQuery queryHasErrors(){
			return query().criteria().hasErrors().endCriteria();
		}
		default public PartsQuery queryHasPartStats(){
			return query().criteria().hasPartStats().endCriteria();
		}
		default public PartsQuery queryLevel(String level){
			return query().criteria().level(level).endCriteria();
		}
		default public PartsQuery queryNamespace(String namespace){
			return query().criteria().namespace(namespace).endCriteria();
		}
		default public PartsQuery queryName(String name){
			return query().criteria().name(name).endCriteria();
		}
		default public PartsQuery queryStatus(String name){
			return query().criteria().status(name).endCriteria();
		}
		default public PartsQuery queryId(String id){
			return query().criteria().id(id).endCriteria();
		}
		default public PartsQuery queryLevelBelow(String level){
			return query().criteria().levelBelow(level).endCriteria();
		}
		default public PartsQuery queryUnhealthy(){
			return query().criteria().healthNotOK().endCriteria();
		}
		default public PartsQuery queryLicence(String licence){
			return query().criteria().licence(licence).endCriteria();
		}
		default public PartsQuery queryTenant(String tenant){
			return query().criteria().tenant(tenant).endCriteria();
		}
		default public PartsQuery queryVersion(String version){
			return query().criteria().version(version).endCriteria();
		}
	}
	
	abstract public static class CriteriaFields{
		public Criteria namespace(final String inVal){
			return stringCriteria(PartsQueryFields.CritType.namespace,()->{return getType().getNamespace();},inVal, false);
		}
		public Criteria name(final String inVal){
			return stringCriteria(PartsQueryFields.CritType.name,()->{return getType().getName();},inVal, false);
		}
		public Criteria level(final String inVal){
			return stringCriteria(PartsQueryFields.CritType.level,()->{return getType().getLevel();},inVal, false);
		}
		public Criteria id(final String inVal){
			return stringCriteria(PartsQueryFields.CritType.id,()->{return getPart().getId();},inVal, true);
		}
		public Criteria status(final String inVal){
			return stringCriteria(PartsQueryFields.CritType.status,()->{return getPart().getStatus();},inVal, true);
		}
		public Criteria tenant(final String inVal){
			return stringCriteria(PartsQueryFields.CritType.tenant,()->{return getPart().getTenant();},inVal, true);
		}
		public Criteria licence(final String inVal){
			return stringCriteria(PartsQueryFields.CritType.licence,()->{return getPart().getTenant();},inVal, true);
		}
		public Criteria version(final String inVal){
			return stringCriteria(PartsQueryFields.CritType.version,()->{return getPart().getVersion();},inVal, true);
		}
		
		public Criteria hasPartStats(){
			parts=true;
			add(()->{return getPart().hasPartStats();},(v)->{return v!=null && ((Boolean)v);});
			def.add(PartsQueryFields.CritType.hasPartStats, null);
			return (Criteria)this;
		}
		public Criteria healthNotOK(){
			parts=true;
			add(()->{return getPart().isHealthy();},(v)->{
				return (v!=null && v.equals(new Boolean(false)));
			});
			def.add(PartsQueryFields.CritType.healthNotOK, null);
			return (Criteria)this;
		}
		public Criteria hasErrors(){
			parts=true;
			add(()->{return getPart().getErrors();},(v)->{return v!=null;});
			def.add(PartsQueryFields.CritType.hasErrors, null);
			return (Criteria)this;
		}
		
		public CriteriaDef def=new CriteriaDef();
		protected boolean types=false;
		protected boolean parts=false;
		
		abstract protected Criteria stringCriteria(PartsQueryFields.CritType cr, Supplier partVarSupplier, String value, boolean fromPart);
		abstract protected void add(Supplier from, Predicate eval);
		protected abstract Part getPart();
		protected abstract PartType getType();

	}

}
