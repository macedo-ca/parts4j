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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import ca.macedo.stores4j.TextStores.TextStore;
import parts4j.Part;
import parts4j.PartRegistry;
import parts4j.PartStats.Timer;

public class ScheduledJobs {
	private static SimpleDateFormat df=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
	public static int IDLE=100, DONE=200, IN_PROGRESS_START=201, IN_PROGRESS_TOP=300, FAILED_NOT_FOUND=404, TRY_LATER=503, RESOURCE_UNAVAILABLE=507, FAILED_TIMEOUT=599, FAILED_UNKNOWN=500;
	public boolean isRetryAbleIssue(int code){
		return (code==FAILED_NOT_FOUND ||code==TRY_LATER ||code==RESOURCE_UNAVAILABLE);
	}
	
	Part<ScheduledJobs> part=PartRegistry.registerSingleton(this).registerStart(this::start).registerStop(this::stop);
	ConcurrentHashMap<String,Job> jobs=new ConcurrentHashMap<String,Job>();
	String seperator="_";
	TextStore persistence=null;
	ScheduledExecutorService service=null;
	ExecutorService exec=null;
	
	public ScheduledJobs(TextStore persistence){
		this.persistence=persistence;
	}
	public void start(){
		service=Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder().setDaemon(true).build());
		exec=Executors.newCachedThreadPool();
	}
	public void stop(){
		service.shutdown();
	}
	public void register(TimePeriod period, String id, Callback jobStart){
		Job j = null;
		Job old = j!=null ? j : jobs.putIfAbsent(id, j = new Job());
		if(old!=null) j =old;
		j.period=period;
		j.id=id;
		j.jobStart=jobStart;
		j.scheduleNextExecution();
		part.partStatistics().getCounter("jobs").increase();
	}
	public void deregister(String id){
		jobs.remove(id);
		part.partStatistics().getCounter("jobs").decrease();
	}
	@FunctionalInterface
	public interface Callback{
		Integer executeJob(String jobID, Date jobTimer, Consumer<Integer> statusUpdates);
	}
	
	@SuppressWarnings("unused")
	private class Job{
		Part<Job> jobpart=PartRegistry.registerPart(this,this::id)
				.registerStart(this::scheduleNextExecution)
				.registerStop(this::stop)
				.registerHealth(this::status)
				.registerDestroy(this::stop);
		
		String id=null;
		TimePeriod period=null;
		Callback jobStart=null;
		@SuppressWarnings("rawtypes")
		ScheduledFuture scheduled=null;
		Future<Integer> running=null;
		
		int status=IDLE;
		int lastRunOutcome=IDLE;
		Date nextJobRun=null;
		String nextRunID=null;
		
		public String id(){
			return id;
		}
		public int status(){
			return lastRunOutcome;
		}
		
		public void stop(){
			if(running!=null) running.cancel(true);
			if(scheduled!=null) scheduled.cancel(true);
			jobs.remove(id);
		}
		
		synchronized void scheduleNextExecution(){
			if(scheduled!=null) return;
			if(service==null) return;
			Date nextTime=period.next();
			nextRunID = id + seperator + period.format(nextTime) + seperator + period.number(nextTime);
			nextJobRun=nextTime;
			scheduled=service.schedule(this::initiateScheduledExecutionOfJob, nextTime.getTime()-System.currentTimeMillis(), TimeUnit.MILLISECONDS);
		}
		synchronized void initiateScheduledExecutionOfJob(){
			running = exec.submit(this::run);
			scheduled=null;
			scheduleNextExecution();
		}
		Integer run(){
			Integer out=null;
			String runIDtoLock=nextRunID;
			if(createLock(runIDtoLock)){
				try(Timer t = part.startTimer("run")){
					setLastRun(id,nextJobRun);
					setStatus( out = jobStart.executeJob(id, nextJobRun, this::setStatus));
				}finally{
					removeLock(runIDtoLock);
				}
			}
			status=IDLE;
			return out;
		}
		public void setStatus(Integer status){
			if(status!=null){
				lastRunOutcome=this.status=status;
				ScheduledJobs.this.setStatus(id, status);
			}
		}
	}

	///// Persistence
	public Integer getStatus(String id){
		String out=persistence.item(id+"_status").getContent();
		return (out!=null && out.length()>0) ? new Integer(out) : null;
	}
	public Date getLastRun(String id){
		try {
			String out=persistence.item(id+"_ran").getContent();
			return (out!=null && out.length()>0) ? df.parse(out) : null;
		} catch (ParseException e) {
			return null;
		}
	}
	private void setStatus(String id, Integer i){
		persistence.item(id+"_status").setContent(""+i);
	}
	private void setLastRun(String id, Date d){
		persistence.item(id+"_ran").setContent(""+df.format(d));
	}
	private boolean createLock(String runID){
		return persistence.item(runID+"_running").createWithContent("true");
	}
	private void removeLock(String runID){
		persistence.item(runID+"_running").delete(null);
	}
}
