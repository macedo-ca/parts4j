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
package parts4j.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.macedo.stores4j.TextStores;
import ca.macedo.stores4j.TextStores.TextStore;
import ca.macedo.stores4j.TextStores.TextStore.TextRef;

/**
 * Reads files, resources, URLs or other sources.
 * <br>
 * Typical use is:
 * <pre>
 * Readers.fromXYZ(..).toZYX()
 * </pre>
 * By default, the Readers will NOT throw an exception but return null. Error behaviour can be set through:
 * <li>onErrNull - return null (default)
 * <li>onErrJSON - return default JSON object/array
 * <li>onErrException - throw exception
 */
public class Readers {
	private static Logger log=LoggerFactory.getLogger(Readers.class);
	public static Charset UTF_8=Charset.forName("UTF-8");
	
	JSONParser p=new JSONParser();
	
	boolean errReturnNull=true;
	boolean errLog=false;
	ErrorHandler errHandler=null;
	Object defaultValue=null;
	
	String file=null;
	String resource=null;
	String logicalName=null;
	String systemEnvVar=null;
	
	TextRef storeRef=null;
	
	public interface ErrorHandler {
		public boolean handle(Object context, String message, Throwable t);
	}
	
	/**
	 * Read text-store file
	 * @param textStore
	 * @param itemID
	 */
	public static Readers fromTextStore(TextStore store, String itemID){
		Readers r=new Readers();
		r.storeRef=store.item(itemID);
		return r;
	}
	/**
	 * Read file from one of the provided text-stores, using the one first found
	 * @param itemID
	 * @param stores
	 */
	public static Readers fromTextStores(String itemID, TextStore ...stores){
		Readers r=new Readers();
		r.storeRef=TextStores.findIn(itemID, stores);
		return r;
	}
	
	/**
	 * Read file-system file
	 * @param file
	 */
	public static Readers fromFile(String file){
		Readers r=new Readers();
		r.logicalName=r.file=file;
		return r;
	}
	/**
	 * Reading class-path resource
	 * @param resource
	 * @return
	 */
	public static Readers fromResource(String resource){
		Readers r=new Readers();
		r.logicalName=r.resource=resource;
		return r;
	}
	/**
	 * Reading System environment variable
	 * @param resource
	 * @return
	 */
	public static Readers fromSystemEnv(String variable){
		Readers r=new Readers();
		r.logicalName=r.systemEnvVar=variable;
		return r;
	}
	/**
	 * Set error handling to return null
	 * @return
	 */
	public Readers onErrNull(){
		errReturnNull=true;
		return this;
	}
	/**
	 * Set error handling
	 * @return
	 */
	public Readers onErr(ErrorHandler errHandler){
		this.errHandler=errHandler;
		return this;
	}
	/**
	 * Set error handling to return specified json object/array
	 * @return
	 */
	public Readers onErrJSON(String json){
		try {
			defaultValue=p.parse(json);
		} catch (Throwable e) { handleErr(e,"Could not parse JSON object/array from "+logicalName); }
		return this;
	}
	/**
	 * Set error handling to throw exception
	 * @return
	 */
	public Readers onErrException(){
		errReturnNull=false;
		return this;
	}
	/**
	 * Set error handling to log the exception
	 * @return
	 */
	public Readers logError(){
		errLog=true; return this;
	}
	private void handleErr(Throwable e,String msg) {
		if(errHandler!=null && errHandler.handle(this,msg,e)) return;
		if(errLog) log.warn(msg,e);
		if(errReturnNull) return;
		throw new RuntimeException(msg,e);
	}
	
	/**
	 * Returns a string reader to read the data from the source
	 * @return
	 */
	public BufferedReader toReader(){
		try {
			return toReader0();
		} catch (Throwable e) {
			handleErr(e,"Could not read from "+logicalName); return null; 
		}
	}
	private BufferedReader toReader0() throws IOException, URISyntaxException{
		return 
			systemEnvVar!=null? new BufferedReader(new StringReader(System.getenv(systemEnvVar))) : 
			file!=null ? Files.newBufferedReader( Paths.get(file), UTF_8 ) :
			storeRef!=null ? new BufferedReader(new StringReader(storeRef.getContent())) :
			Files.newBufferedReader( Paths.get(Thread.currentThread().getContextClassLoader().getResource(resource).toURI()),UTF_8);
	}
	/**
	 * Parse and return JSONObject (i.e. JSON that starts with {)
	 * @return the parsed JSON object
	 */
	public JSONObject toJSONObject(){
		try {
			return (JSONObject)(p.parse(toReader0()));
		} catch (Throwable e) {
			if(defaultValue!=null) return (JSONObject)defaultValue;
			handleErr(e,"Could not parse JSONObject from "+logicalName); return null; 
		}
	}
	/**
	 * Parse and return JSONArray (i.e. JSON that starts with [)
	 * @return the parsed JSON array
	 */
	public JSONArray toJSONArray(){
		try {
			return (JSONArray)(p.parse(toReader0()));
		} catch (Throwable e) {
			if(defaultValue!=null) return (JSONArray)defaultValue;
			handleErr(e,"Could not parse JSONArray from "+logicalName); return null; 
		}
	}
}
