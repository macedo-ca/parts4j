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

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.regex.Pattern;

import org.apache.commons.lang3.math.NumberUtils;

public class TimePeriod {
	public static enum Unit{YEARS,QUARTERS,MONTHS,WEEKS,DAYS,HOURS,TENS_MINUTES,MINUTES}
	
	public static Date BASE_DATE=new Date(1483246800000L); // 2017-01-01T00:00:00.000 base time
	public static class TimeSelection{
		public TimeSelection(TimePeriod period, String pointInTime){
			this.period=period;
			dateTime=toHistoricPointInTime(pointInTime);
			if(dateTime==null){
				throw new RuntimeException("Could not determine point in time for '"+pointInTime+"'");
			}
		}
		public TimeSelection(TimePeriod period, Date pointInTime){
			this.period=period;
			dateTime=pointInTime;
		}
		public TimePeriod period=null;
		public Date dateTime=null;
	}
	
	public static void main(String[] ar){
//		for(Integer i : parts("20161212",4,2,2)){
//			System.out.println(i);
//		}
		Date d=new Date();
//		System.out.println(new TimePeriod("PT30M").format(d, 1));
//		System.out.println(new TimePeriod("PT30M").level());
//		System.out.println(new TimePeriod("PT30M").toOracleTO_CHAR_PARAM("tmFld"));
//		System.out.println(new TimePeriod("3M").format(d));
//		System.out.println(new TimePeriod("3M").level());
//		System.out.println(new TimePeriod("2W").level());
//		System.out.println(new TimePeriod("2W").toOracleTO_CHAR_PARAM("tmFld"));
//		System.out.println(new TimePeriod("3M").toOracleTO_CHAR_PARAM("tmFld"));
		System.out.println(new TimePeriod("Q").format(d,1));
	}
	
	
	private static SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMdd");
	private static SimpleDateFormat sdf1=new SimpleDateFormat("yyyy-MM-dd");
	private static SimpleDateFormat sdf2=new SimpleDateFormat("yyyyMMddHHmm");
	private static SimpleDateFormat sdf3=new SimpleDateFormat("yyyy-MM-dd'T'HH-mm");
	private static Pattern Y=Pattern.compile("2[0-9]{3}");
	private static Pattern YM=Pattern.compile("2[0-9]{5}");
	public static Date toHistoricPointInTime(String s){
		Date d=null;
		if(s!=null && s.length()==0) return new Date();
		if(s==null) return new Date();
		if(s.startsWith("20")){
			try{d=sdf.parse(s);}catch(Throwable t){}
			if(d==null) try{d=sdf1.parse(s);}catch(Throwable t){}
			if(d==null) try{d=sdf2.parse(s);}catch(Throwable t){}
			if(d==null) try{d=sdf3.parse(s);}catch(Throwable t){}
			if(d==null && Y.matcher(s).matches()){
				int year=new Integer(s.substring(0, 4));
				int month=new Integer(s.substring(5));
				d= new GregorianCalendar(year, month, 1).getTime();
			}
			if(d==null && YM.matcher(s).matches()){
				int year=new Integer(s.substring(0, 4));
				d= new GregorianCalendar(year, 1, 1).getTime();
			}
			if(d!=null) return d;
		}
		try {
			TimePeriod seg=new TimePeriod(s);
			return seg.subtractFrom(new Date(), 1);
		} catch (Throwable e) {
		}
		return null;
	}
	
	public static Integer[] parts(String s, int ... parts){
		int idx=0;
		LinkedList<Integer> out=new LinkedList<Integer>();
		for(int part : parts){
			if((idx+part)>s.length()){
				out.add(0);
			}else{
				String partStr = s.substring(idx,idx+part);
				out.add(new Integer(partStr));
				idx=idx+part;
			}
		}
		return out.toArray(new Integer[out.size()]);
	}
	
	public String[] getTimePartNames(){
		switch(level()){
		case YEARS: return new String[]{"Year"};
		case QUARTERS: return new String[]{"Year","Quarter"};
		case MONTHS: return new String[]{"Year","Month"};
		case WEEKS: return new String[]{"Year","Week"};
		case DAYS: return new String[]{"Year","Month","Day"};
		case HOURS: return new String[]{"Year","Month","Day","Hour"};
		default: return new String[]{"Year","Month","Day","Hour","Minute"};
		}
	}
	public Integer[] getTimePartValues(String v){
		switch(level()){
		case YEARS: return parts(v,4);
		case QUARTERS: return parts(v,4,1);
		case MONTHS: return parts(v,4,2);
		case WEEKS: return parts(v,4,3);
		case DAYS: return parts(v,4,2,2);
		case HOURS: return parts(v,4,2,2,2);
		case TENS_MINUTES: return parts(v,4,2,2,2,1);
		default: return parts(v,4,2,2,2,2);
		}
	}
	
	Duration d=null;
	Period p=null;
	SimpleDateFormat format=null;
	DateTimeFormatter advFormat=null;
	Unit level=null;
	public Unit getLevel() {
		return level;
	}


	boolean only10minChunks=false;
	
	public boolean isOnly10minChunks() {
		return only10minChunks;
	}
	public void setOnly10minChunks(boolean only10minChunks) {
		this.only10minChunks = only10minChunks;
	}
	public TimePeriod(String s){
		s=s.toUpperCase();
		if(s.equals("T")) s="PT10M";
		if(s.equals("N")) s="PT1M";
		if(s.equals("H")) s="PT1H";
		if(s.equals("Q")) s="3M";
		if(s.equals("2Q")) s="6M";
		if(s.contains("N") && !s.startsWith("PT")){
			s="PT"+s.replace('N', 'M');
		}else if(s.contains("H") && !s.startsWith("PT")){
			s="PT"+s;
		}
		if(s.length()==1 && !NumberUtils.isNumber(s)) s="1"+s;
		if(s.charAt(0)!='P') s="P"+s;
		try {
			p=Period.parse(s);
		} catch (Exception e) {
			d=Duration.parse(s);
		}
		level=level();
		determineDateFormat();
	}
	public Unit level(){
		if(p!=null){
			if(p.getDays()!=0){
				if((p.getDays() % 7) == 0) return Unit.WEEKS;
				return Unit.DAYS;
			}else if(p.getMonths()!=0){
				if((p.getMonths() % 3) == 0) return Unit.QUARTERS;
				return Unit.MONTHS;
			}else if(p.getYears()!=0){
				return Unit.YEARS;
			}
		}else{
			if(d.toMinutes()!=0L && ((d.toMinutes() % 60) !=0)){
				if(d.toMinutes()!=0L && ((d.toMinutes() % 10)==0)){
					return Unit.TENS_MINUTES;
				}
				return Unit.MINUTES;
			}else{
				return Unit.HOURS;
			}
		}
		return null;
	}
	
	public String format(Date d){
		return advFormat!=null ? advFormat.format(LocalDateTime.parse(d.toInstant().toString().substring(0,19))) : 
			(level==Unit.TENS_MINUTES ? format.format(d).substring(0,11) : format.format(d));
	}
	public String format(int stepsBack){
		Date historical=subtractFrom(new Date(), stepsBack);
		return format(historical);
	}
	public String format(Date d, int stepsBack){
		Date historical=subtractFrom(d, stepsBack);
		return format(historical);
	}
	public boolean isRoundFuture() {
		return roundFuture;
	}

	public TimePeriod setRoundFuture(boolean roundFuture) {
		this.roundFuture = roundFuture;
		return this;
	}
	boolean roundFuture=true;
	
	/**
	 * 0 is next, and 1 is the one after
	 * @return
	 */
	public Date future(int offsetZeroIsCurrentOrPrevious){
		return future(new Date(),offsetZeroIsCurrentOrPrevious);
	}
	/**
	 * 0 is next, and 1 is the one after
	 * @return
	 */
	public Date future(Date date,int offsetZeroIsCurrentOrPrevious){
		return future(date, offsetZeroIsCurrentOrPrevious,roundFuture);
	}
	/**
	 * 0 is next, and 1 is the one after
	 * @return
	 */
	public Date future(Date date,int offsetZeroIsCurrentOrPrevious, boolean round){
		if(round){
			date = roundBack(date);
		}
		return subtractFrom(date, -1 * (offsetZeroIsCurrentOrPrevious+1));
	}
	public Date next(){
		return nextFrom(new Date());
	}
	public Date nextFrom(Date date){
		return nextFrom(date, roundFuture);
	}
	public Date nextFrom(Date date, boolean round){
		if(round){
			date = roundBack(date);
		}
		return subtractFrom(date, -1);
	}
	public Date roundBack(Date d){
		GregorianCalendar g=new GregorianCalendar();
		g.setTime(d);
		g.set(GregorianCalendar.SECOND,0); 
		g.set(GregorianCalendar.MILLISECOND,0);
		int m =g.get(GregorianCalendar.MONTH);
		switch(level){
		case YEARS:
			g.set(GregorianCalendar.DAY_OF_YEAR,1);break;
		case QUARTERS: 
		case MONTHS:
			g.set(GregorianCalendar.MONTH, m - ( m % p.getMonths() ));
			g.set(GregorianCalendar.HOUR_OF_DAY,0);
			g.set(GregorianCalendar.MINUTE,0);
			g.set(GregorianCalendar.DAY_OF_MONTH,1);
			break;
		case WEEKS:
		case DAYS:
			g.set(GregorianCalendar.HOUR_OF_DAY,0);
			g.set(GregorianCalendar.MINUTE,0);
			break;
		case HOURS:
			g.set(GregorianCalendar.MINUTE,0);
		case TENS_MINUTES:
			g.set(GregorianCalendar.MINUTE,g.get(GregorianCalendar.MINUTE)-(g.get(GregorianCalendar.MINUTE)%10));
		default: break;
		}
		return g.getTime();
	}
	
	static long years=365 * 24 * 60 * 60 * 1000L;
	static long quarters=years/4;
	static long months=years/12;
	static long days=24 * 60 * 60 * 1000L;
	static long hours=60 * 60 * 1000L;
	static long minutes=60 * 1000L;
	
	public int number(Date d){
		GregorianCalendar c=new GregorianCalendar();
		c.setTime(d);
		int years = (c.get(GregorianCalendar.YEAR)-2017);
		if((c.get(GregorianCalendar.YEAR))>2037) throw new RuntimeException("The number mechanism is only valid until 2037");
		int days = (years * 366) + c.get(GregorianCalendar.DAY_OF_YEAR);
		int hours = (days * 24) + c.get(GregorianCalendar.HOUR_OF_DAY);
		if(c.getTimeZone().inDaylightTime(d)){ // Work-around that works until 2038
			hours=hours-1;
		}
		long interval = d.getTime()-BASE_DATE.getTime();
		switch(level){
		case YEARS:
			return years;
		case WEEKS:
		case MONTHS:
		case DAYS:
			return days;
		case HOURS:
			return hours;
		case TENS_MINUTES:
			return (int)(interval / (minutes*10L));
		default: 
			return (int)(interval / minutes);
		}
	}
	
	public Date subtractFrom(Date date, int stepsBack){
		GregorianCalendar g=new GregorianCalendar();
		g.setTime(date);
		switch(level){
		case WEEKS:
		case DAYS:
			 g.add(GregorianCalendar.DAY_OF_YEAR, -(stepsBack * p.getDays()));break;
		case QUARTERS: 
		case MONTHS:
			g.add(GregorianCalendar.MONTH, -(stepsBack * p.getMonths()));break;
		case YEARS:
			g.add(GregorianCalendar.YEAR, -(stepsBack * p.getYears()));break;
		default:
			g.add(GregorianCalendar.SECOND, -(int)(stepsBack * d.getSeconds()));
		}
		return g.getTime();
	}
	
	private void determineDateFormat(){
		switch(level){
		case WEEKS:
			format=new SimpleDateFormat("yyyy'0'ww");return;
		case DAYS:
			format=new SimpleDateFormat("yyyyMMdd");return;
		case QUARTERS:
			advFormat=DateTimeFormatter.ofPattern("yyyyQ");return;
		case MONTHS:
			format=new SimpleDateFormat("yyyyMM");return;
		case YEARS:
			format=new SimpleDateFormat("yyyy");return;
		case TENS_MINUTES:
			format=new SimpleDateFormat("yyyyMMddHHm");return;
		case MINUTES:
			format=new SimpleDateFormat("yyyyMMddHHmm");return;
		case HOURS:
			format=new SimpleDateFormat("yyyyMMddHH");return;
		default:
		}
	}
	
	public String toString(){
		return p!=null ? p.toString() : d.toString();
	}

}