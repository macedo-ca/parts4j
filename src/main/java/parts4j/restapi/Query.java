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
package parts4j.restapi;

import parts4j.PartsQuery;
import parts4j.PartsQuery.Criteria;

public class Query {
	String id=null;
	String level=null;
	String levelBelow=null;
	String name=null;
	String namespace=null;
	String tenant=null;
	String version=null;
	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}
	String[] flags=null;
	
	public PartsQuery apply(PartsQuery query){
		Criteria c=query.criteria();
		if(id!=null) c.id(id);
		if(level!=null) c.level(level);
		if(levelBelow!=null) c.levelBelow(levelBelow);
		if(name!=null) c.name(name);
		if(namespace!=null) c.namespace(namespace);
		if(flags!=null) for(String flag : flags){
			if(flag!=null){
				flag=flag.toLowerCase().trim();
				if(flag.equals("unhealthy")){
					c.healthNotOK();
				}else if(flag.equals("haspartstats")){
					c.hasPartStats();
				}else if(flag.equals("haserrors")){
					c.hasErrors();
				}
			}
		}
		return query;
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getLevel() {
		return level;
	}
	public void setLevel(String level) {
		this.level = level;
	}
	public String getLevelBelow() {
		return levelBelow;
	}
	public void setLevelBelow(String levelBelow) {
		this.levelBelow = levelBelow;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getNamespace() {
		return namespace;
	}
	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}
	public String getTenant() {
		return tenant;
	}
	public void setTenant(String tenant) {
		this.tenant = tenant;
	}
	public String[] getFlags() {
		return flags;
	}
	public void setFlags(String[] flags) {
		this.flags = flags;
	}
}