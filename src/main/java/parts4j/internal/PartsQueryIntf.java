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

import java.util.Collection;

import parts4j.PartType;
import parts4j.PartsQuery;
import parts4j.PartsQuery.PartQueryResult;
import parts4j.PartsQuery.Visitable;
import parts4j.internal.PartsQueryFields.CritFields;

@SuppressWarnings("rawtypes")
public class PartsQueryIntf{
	public interface Query extends CritFields{
		PartsQuery query();
		default public PartsQuery queryFrom(Collection<PartType> types){
			return query().queryFrom(types);
		}
		default public PartsQuery queryFrom(Visitable fromVisitable){
			return query().queryFrom(fromVisitable);
		}
		default public PartsQuery queryFor(Class<?> clz){
			return query().criteria().namespace(clz.getPackage().getName()).name(clz.getSimpleName()).endCriteria();
		}
		default public PartsQuery query(PartsQuery.CriteriaDef def){
			PartsQuery q=query();
			if(def!=null) def.apply(q.criteria());
			return q;
		}
		
		default public PartsQuery forEachPart(PartsQuery.PartVisitor vis){
			return query().forEachPart0(vis);
		}
		default public PartsQuery forEachType(PartsQuery.PartTypeVisitor vis){
			return query().forEachType0(vis);
		}
		default public PartsQuery forEachPart(PartsQuery.Visitable from, PartsQuery.PartVisitor vis){
			return query().queryFrom(from).forEachPart0(vis);
		}
		default public PartsQuery forEachType(PartsQuery.Visitable from, PartsQuery.PartTypeVisitor vis){
			return query().queryFrom(from).forEachType0(vis);
		}
		default public PartQueryResult forEachPart(){
			return query().forEachPartDo0();
		}
	}

	public interface CreateAndQuery extends Query{
		PartsQuery newQuery();
		default PartsQuery newQueryFor(Class forClass){
			return newQuery().queryFor(forClass);
		}
		default PartsQuery newQuery(Collection<PartType> from){
			return newQuery().queryFrom(from);
		}
		default PartsQuery newQuery(PartsQuery.CriteriaDef def){
			return newQuery().query(def);
		}
		default PartsQuery newQuery(Collection<PartType> from, PartsQuery.CriteriaDef def){
			return newQuery().query(def);
		}
		default public PartsQuery.CriteriaDef compileQuery(String query){
			return newQuery().compileCriteria(query);
		}
	}
}