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

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;

import parts4j.PartStats.PartsStats;
import parts4j.PartsQuery.PartTypeVisitor;
import parts4j.PartsQuery.PartVisitor;
import parts4j.internal.Chain;
import parts4j.internal.HealthyTester;
import parts4j.internal.PartsQueryIntf;
import parts4j.managers.BaseManager;
import parts4j.managers.ManagerFactory;

@SuppressWarnings({"rawtypes","unchecked"})
public class PartRegistry {
	
	private static PartRegistryInst registry=new PartRegistryInst();
	public static PartRegistryInst getRegistry(){
		return registry;
	}
	public static PartRegistryInst reg(){
		return registry;
	}
	
	public static PartRegistryInst autoStart(boolean shouldAutoStart) {
		return autoStart(shouldAutoStart);
	}
	public static <T> PartType<T> register(PartType parent,Class<T> sub) {
		return registry.register(parent,sub);
	}
	public static <T> PartType<T> register(Class<T> clz) {
		return registry.register(clz);
	}
	
	public static <T> Part<T> registerSingleton(T inst) {
		return registry.registerSingleton(inst);
	}
	public static <T> Part<T> registerSingleton(T inst, Supplier idSupplier) {
		return registry.registerSingleton(inst,idSupplier);
	}
	public static <T> Part<T> registerSingleton(T inst, Supplier idSupplier, String level) {
		return registry.registerSingleton(inst,idSupplier,level);
	}
	public static <T> Part<T> registerSingleton(T inst, String id, String level) {
		return registry.registerSingleton(inst,id,level);
	}
	public static <T> PartType<T> register(Class<T> clz, String level) {
		return registry.register(clz,level);
	}
	public static <T> PartType<T> register(Class parent,Class<T> sub) {
		return registry.register(parent,sub);
	}
	public static <T> PartType<T> register(PartType parent,Class<T> sub, String level) {
		return registry.register(parent,sub,level);
	}
	public static <T> PartType<T> register(Class parent,Class<T> sub, String level) {
		return registry.register(parent,sub,level);
	}
	public static <T> Part<T> registerPart(T inst, Supplier idSupplier) {
		return registry.registerPart(inst,idSupplier);
	}
	public static <T> Part<T> registerPart(T inst, Supplier idSupplier, String level) {
		return registry.registerPart(inst,idSupplier,level);
	}
	public static <T> Part<T> registerPart(T inst, String id, String level) {
		return registry.registerPart(inst,id,level);
	}
	
	public static PartsQuery newQuery(){
		return registry.newQuery();
	}
	public static PartsQuery newQuery(Collection<PartType> from){
		return registry.newQuery(from);
	}
	public static PartsQuery newQueryFor(Class from){
		return registry.newQueryFor(from);
	}
	
	public static class PartRegistryInst implements PartsQueryIntf.CreateAndQuery{
		boolean autoStart=true;
		String prefix="parts4j";
		PartsStats stats=new PartsStats(this);
		Managers mgrs=ManagerFactory.createDefaultManagers(this);
		
		public PartEnv env() {
			return mgrs.env;
		}
		
		public boolean isHealthy(Object o){
			return mgrs.healthy.isHealthy(o);
		}
		
		public PartRegistryInst autoStart(boolean shouldAutoStart){
			autoStart=shouldAutoStart;
			return this;
		}
		
		public String getPrefix() {
			return prefix;
		}
		public void setPrefix(String prefix) {
			this.prefix = prefix;
		}		
		public <T> PartType<T> register(PartType parent,Class<T> sub) {
			return parent.addSub(mgrs.getOrCreatePartType(sub, null));
		}
		public <T> PartType<T> register(Class parent,Class<T> sub) {
			return register(parent).addSub(mgrs.getOrCreatePartType(sub, null));
		}
		public <T> PartType<T> register(PartType parent,Class<T> sub, String level) {
			return parent.addSub(mgrs.getOrCreatePartType(sub, level));
		}
		public <T> PartType<T> register(Class parent,Class<T> sub, String level) {
			return register(parent).addSub(mgrs.getOrCreatePartType(sub, level));
		}
		public <T> PartType<T> register(Class<T> clz) {
			return mgrs.getOrCreatePartType(clz,null);
		}
		public <T> PartType<T> register(Class<T> clz, String level) {
			return mgrs.getOrCreatePartType(clz,level);
		}
		
		public <T> Part<T> registerPart(T inst, Supplier idSupplier) {
			return register((Class<T>)inst.getClass()).register(inst,idSupplier);
		}
		public <T> Part<T> registerPart(T inst, Supplier idSupplier, String level) {
			return register((Class<T>)inst.getClass(),level).register(inst,idSupplier);
		}
		public <T> Part<T> registerPart(T inst, String id, String level){
			return register((Class<T>)inst.getClass(),level).register(inst,id);
		}
		
		public <T> Part<T> registerSingleton(T inst) {
			return register((Class<T>)inst.getClass()).registerSingleton(inst);
		}
		public <T> Part<T> registerSingleton(T inst, Supplier idSupplier) {
			return register((Class<T>)inst.getClass()).registerSingleton(inst,idSupplier);
		}
		public <T> Part<T> registerSingleton(T inst, Supplier idSupplier, String level) {
			return register((Class<T>)inst.getClass(),level).registerSingleton(inst,idSupplier);
		}
		public <T> Part<T> registerSingleton(T inst, String id, String level) {
			return register((Class<T>)inst.getClass(),level).registerSingleton(inst,id);
		}
		public HashSet<String> namespaces(){
			HashSet<String> out=new HashSet<String>();
			if(mgrs.chain!=null) mgrs.chain.visitTypesChain(type->out.add(type.getNamespace()));
			return out;
		}
		public PartsQuery newQuery(){
			return new PartsQuery(this);
		}
		@Override
		public PartsQuery query() {
			return new PartsQuery(this);
		}
		
		/// Internal methods
		<T> Part<T> onCreate(Part<T> inst){
			return mgrs.onCreatePart(inst);
		}
	}
	public static class Managers{
		PartRegistryInst inst=null;
		Map<String,String> props=null;
		BaseManager rootManager=null;
		Map<String,BaseManager> managersLookup=null;
		PartLevels levels=null;
		HealthyTester healthy=null;
		PartEnv env=new PartEnv();
		
		public PartEnv getEnv() {
			return env;
		}
		public void setEnv(PartEnv env) {
			this.env = env;
		}

		public PartsQuery newVisit(){
			return new PartsQuery(inst).setLevels(levels);
		}
		
		public HealthyTester getHealthy() {
			return healthy;
		}
		public void setHealthy(HealthyTester healthy) {
			this.healthy = healthy;
		}
		public PartLevels getLevels() {
			return levels;
		}
		public void setLevels(PartLevels levels) {
			this.levels = levels;
		}
		public Map<String, String> getProps() {
			return props;
		}
		public void setProps(Map<String, String> props) {
			this.props = props;
		}
		public BaseManager getRootManager() {
			return rootManager;
		}
		public void setRootManager(BaseManager rootManager) {
			this.rootManager = rootManager;
		}
		public Map<String, BaseManager> getManagersLookup() {
			return managersLookup;
		}
		public void loadManagers(Map<String, BaseManager> managersLookup) {
			this.managersLookup = managersLookup;
			chain=new MgrChain(rootManager);
			if(managersLookup!=null && managersLookup.size()>0) for(BaseManager m : managersLookup.values()){
				chain.add(m);
			}
		}
		public Managers(PartRegistryInst inst){
			this.inst=inst;
		}
		public Managers(PartRegistryInst inst,Map<String,String> props){
			this.inst=inst;
			this.props=props;
		}
		
		public <T> PartType<T> getOrCreatePartType(Class<T> clz, String levelIfProvided){
			BaseManager mgr = ManagerFactory.configuredManagerForClass(clz, managersLookup, props, inst.getPrefix(), rootManager);
			return ManagerFactory.getOrCreatePartType(clz, levelIfProvided, inst, mgr);
		}
		public <T> Part<T> onCreatePart(Part<T> c){
			return c.getType().mgr().onCreate(c);
		}
		
		MgrChain chain=null;
		static class MgrChain extends Chain<BaseManager>{
			MgrChain(BaseManager mgr){
				super(mgr);
			}
			void visitPartsChain(PartVisitor vis){
				forEach((mgr)->{PartsQuery.visitParts(mgr,vis);});
			}
			void visitTypesChain(PartTypeVisitor vis){
				forEach((mgr)->{PartsQuery.visitTypes(mgr,vis);});
			}
			void visitPartsChain(Predicate<Part> selector, PartVisitor vis){
				forEach((mgr)->{PartsQuery.visitParts(mgr,selector,vis);});
			}
			void visitTypesChain(Predicate<PartType> selector, PartTypeVisitor vis){
				forEach((mgr)->{PartsQuery.visitTypes(mgr,selector,vis);});
			}
		}
	}
}