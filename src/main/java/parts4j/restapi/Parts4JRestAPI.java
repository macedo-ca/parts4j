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

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;
import org.json.simple.JSONArray;

import parts4j.Part;
import parts4j.PartRegistry;
import parts4j.PartRegistry.PartRegistryInst;
import parts4j.PartType;
import parts4j.internal.Methods.StandardMethod;
import parts4j.util.JSONBuilder;
import parts4j.util.JSONBuilder.JSONArrRoot;
import parts4j.util.JSONBuilder.JSONEntry;

public class Parts4JRestAPI extends RouteBuilder{
	String prefix="/parts4j";
	PartRegistryInst inst=PartRegistry.reg();
	
	@SuppressWarnings("unchecked")
	@Override
	public void configure() throws Exception {
		restConfiguration()
		.bindingMode(RestBindingMode.json);
		rest().consumes("application/json").produces("application/json")
			.get(prefix+"/env/")
				.route()
				.process((ex)->{
					ex.getIn().setBody(inst.env().setTo(JSONBuilder.object()).toJSONObject());
				})
				.endRest()
			.get(prefix+"/actions/")
				.route()
				.process((ex)->{
					JSONArray ar=new JSONArray();
					for(StandardMethod m : StandardMethod.values()){
						ar.add(m.name());
					}
					ex.getIn().setBody(ar);
				})
				.endRest()
			.get(prefix+"/types/")
				.route()
				.process((ex)->{
					JSONArrRoot ar=JSONBuilder.array();
					inst.forEachType((type)->{
						describeType(ar,type);
						ar.next();
					});
					ex.getIn().setBody(ar.toJSONArray());
				})
				.endRest()
			.get(prefix+"/parts/")
				.route()
				.process((ex)->{
					JSONArrRoot ar=JSONBuilder.array();
					inst.forEachPart((part)->{
						describePart(ar,part);
						ar.next();
					});
					ex.getIn().setBody(ar.toJSONArray());
				})
				.endRest()
			.get(prefix+"/types/{namespace}/{name}")
				.route()
				.process((ex)->{
					inst.queryNamespace(strHeader(ex,"namespace")).queryName(strHeader(ex,"name"))
						.forEachType((type)->{
							ex.getIn().setBody(describeType(JSONBuilder.object(), type).toJSONObject());;
						});
				})
				.endRest()
			.get(prefix+"/parts/{id}")
				.route()
				.process((ex)->{
					inst.queryId(strHeader(ex,"id"))
					.forEachPart((part)->{
						ex.getIn().setBody(describePart(JSONBuilder.object(), part).toJSONObject());;
					});
				})
				.endRest()
			.get(prefix+"/namespaces/")
				.route()
				.process((ex)->{
					JSONArray ar=new JSONArray();
					for(String ns : inst.namespaces()){
						ar.add(ns);
					}
					ex.getIn().setBody(ar);
				})
				.endRest()
			.get(prefix+"/namespaces/{namespace}")
				.route()
				.process((ex)->{
					inst.queryNamespace(strHeader(ex,"namespace"))
					.forEachPart((part)->{
						ex.getIn().setBody(describePart(JSONBuilder.object(), part).toJSONObject());;
					});
				})
				.endRest()
			.get(prefix+"/namespaces/{namespace}/{name}")
				.route()
				.process((ex)->{
					inst.queryNamespace(strHeader(ex,"namespace")).queryName(strHeader(ex,"name"))
					.forEachPart((part)->{
						ex.getIn().setBody(describePart(JSONBuilder.object(), part).toJSONObject());;
					});
				})
				.endRest()
			.get(prefix+"/levels/{level}")
				.route()
				.process((ex)->{
					JSONArrRoot ar=JSONBuilder.array();
					inst.queryLevel(strHeader(ex,"level"))
					.forEachPart((part)->{
						describePart(ar, part);
						ar.next();
					});
					ex.getIn().setBody(ar.toJSONArray());
				})
				.endRest()
			.get(prefix+"/levels/{level}/unhealthy")
				.route()
				.process((ex)->{
					JSONArrRoot ar=JSONBuilder.array();
					inst.queryLevel(strHeader(ex,"level")).queryUnhealthy()
					.forEachPart((part)->{
						describePart(ar, part);
						ar.next();
					});
					ex.getIn().setBody(ar.toJSONArray());
				})
				.endRest()
			.post(prefix+"/query")
				.type(Query.class)
				.route()
				.process((ex)->{
					Query q=(Query)ex.getIn().getBody();
					if(q==null) return;
					JSONArrRoot ar=JSONBuilder.array();
					q.apply(inst.newQuery())
						.forEachPart((part)->{
							describePart(ar, part);
							ar.next();
						});
					ex.getIn().setBody(ar.toJSONArray());;
				})
				.endRest()
			.post(prefix+"/query/do/{action}")
				.type(Query.class)
				.route()
				.process((ex)->{
					Query q=(Query)ex.getIn().getBody();
					if(q==null) return;
					JSONArrRoot ar= JSONBuilder.array();
					StandardMethod act=StandardMethod.valueOf(strHeader(ex, "action"));
					q.apply(inst.newQuery())
						.forEachPart((part)->{
							boolean found=part.hasMethod(act);
							if(found){
								describePart(ar, part);
								ar	.set("result", part.get(act))
									.next();
							}
						});
					ex.getIn().setBody(ar.toJSONArray());
				})
				.endRest()
			.post(prefix+"/query/health")
				.type(Query.class)
				.route()
				.process((ex)->{
					Query q=(Query)ex.getIn().getBody();
					if(q==null) return;
					JSONArrRoot ar= JSONBuilder.array();
					q.apply(inst.newQuery())
						.forEachPart((part)->{
							describePart(ar, part);
							ar	
								.set("isHealthy", part.isHealthy())
								.set("health", part.getHealth())
								.set("hasErrors", (part.getErrors()!=null))
								.next();
						});
					ex.getIn().setBody(ar.toJSONArray());
				})
				.endRest()
			.post(prefix+"/query/stats")
				.type(Query.class)
				.route()
				.process((ex)->{
					Query q=(Query)ex.getIn().getBody();
					if(q==null) return;
					JSONArrRoot ar= JSONBuilder.array();
					q.apply(inst.newQuery())
						.forEachPart((part)->{
							describePart(ar, part);
							ar	
								.set("stats", part.getStats())
								.set("partStats", part.partStatistics().toMap())
								.next();
						});
					ex.getIn().setBody(ar.toJSONArray());
				})
				.endRest()
			;
	}
	
	private JSONEntry describeType(JSONEntry out, PartType<?> type){
		return out.set("namespace", type.getNamespace(), "name", type.getName(), "level", type.getLevel());
	}
	private JSONEntry describePart(JSONEntry out, Part<?> part){
		String methods="";
		for(StandardMethod m : StandardMethod.values()){
			if(m!=StandardMethod.id && part.hasMethod(m)){
				if(methods.length()>0) methods+=",";
				methods+=m.name();
			}
		}
		return out
			.set("namespace", part.getType().getNamespace(), "name", part.getType().getName(), "level", part.getType().getLevel())
			.set("id", part.getId())
			.set("actions", methods);
	}
	
	private String strHeader(Exchange e, String head){
		Object o =e.getIn().getHeader(head);
		return o!=null ? o.toString() : null;
	}
	
	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public PartRegistryInst getInst() {
		return inst;
	}

	public void setInst(PartRegistryInst inst) {
		this.inst = inst;
	}


}