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

import java.util.function.Consumer;

public class Chain<L> {
	private L current=null;
	private Chain<L> next=null;
	
	public Chain(){}
	public Chain(L item){
		current=item;
	}
	public Chain<L> add(L obj){
		if(current==null){
			current=obj;
			return this;
		}
		if(current==obj) return this;
		if(next==null){
			next=new Chain<L>(obj);
		}else{
			next.add(obj);
		}
		return this;
	}
	public void forEach(Consumer<L> visitor){
		if(current!=null) visitor.accept(current);
		if(next!=null){
			next.forEach(visitor);
		}
	}
}
