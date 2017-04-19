package parts4j.managers;

import java.io.File;
import java.net.InetAddress;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import parts4j.PartEnv;

public class EnvControl {
	private static Logger log=LoggerFactory.getLogger(EnvControl.class);
	
	public static void initialize(Map<String,String> defaultSettings, PartEnv env){
		String envControl = defaultSettings.get("parts4j.env.control");
		if(envControl==null || envControl.length()==0) envControl=EnvControl.class.getName();
		try{
			EnvControl envCtrl=(EnvControl)Thread.currentThread().getContextClassLoader().loadClass(envControl).newInstance();
			envCtrl.evaluateEnvironment(defaultSettings,env);
		}catch(Throwable t){
			log.error("Issue creating custom EnvControl: ",t);
		}
	}
	
	public PartEnv evaluateEnvironment(Map<String,String> defaultSettings, PartEnv env){
		Map<String,String> s=defaultSettings;
		env.setPlatform("jee");
		env.setApplication(getDefaultApplication());
		env.setStartTime(PartEnv.formatDate(ManagerFactory.startTime));
		if(env.getEnvSources().containsKey("ServletContext")){
			Map<String,String> sc=env.getEnvSources().get("ServletContext");
			env.setPlatform(sc.get("serverInfo"));
			env.setApplication(sc.get("servletContextName"));
			env.setContext("/"+sc.get("servletContextName"));
			if(sc.containsKey("startTime")) env.setStartTime(sc.get("startTime"));
		}
		env.setEnv			(get(fieldName("env",s),			"local",s));
		env.setApplication	(get(fieldName("application",s),	env.getApplication(),s));
		env.setPlatform		(get(fieldName("platform",s),		env.getPlatform(),s));
		env.setCluster		(get(fieldName("cluster",s),		"default",s));
		env.setTags			(get(fieldName("tags",s),			"",s));
		env.setContext		(get(fieldName("context",s),		env.getContext(),s));
		env.setMajorVersion	(get(fieldName("majorVersion",s),	"0",s));
		env.setMinorVersion	(get(fieldName("minorVersion",s),	"1",s));
		env.setBuild		(get(fieldName("build",s),	"0",s));
		String ver=	get(fieldName("version",s),null,s);
		if(ver!=null && ver.trim().length()>0){
			env.setVersion(ver);
		}
		env.setHostname		(determineHostname());
		additionalEnvLoading(s,env);
		
		env.setDisk(""+(int)((new File(".").getFreeSpace()/1024/1024))+"mb");
		env.setMemory(""+(int)((Runtime.getRuntime().maxMemory()/1024/1024))+"mb");
		env.setProcessors(""+Runtime.getRuntime().availableProcessors());
		log.info("Determined environment "+env.toMap());
		return env;
	}
	public PartEnv additionalEnvLoading(Map<String,String> defaultSettings, PartEnv env){
		// noop - for extensions to use
		return env;
	}
	
	public String getDefaultApplication(){
		return "unknown";
	}
	public static String fieldName(String field, Map<String,String> defaultSettings){
		String altFieldName=defaultSettings.get("parts4j.env.fieldname."+field);
		return altFieldName!=null && altFieldName.trim().length()>0 ? altFieldName : field;
	}
	public static String get(String var, String defaultValue, Map<String,String> defaultSettings){
		try {
			String out=System.getenv(var);
			if(out==null) out=System.getProperty(var);
			if(out==null) out=defaultSettings.get("parts4j.env."+var);
			if(out==null) out=defaultValue;
			return out;
		} catch (Throwable e) {
			log.error("Error evaluating var '"+var+"'",e);
			return defaultValue;
		}
	}
	
	public String determineHostname(){
		try {
		    String result = InetAddress.getLocalHost().getHostName();
		    if (result!=null && result.trim().length()>0) return result;
		} catch (Throwable e) {
		}
		String host = null;
		host = System.getenv("HOSTNAME");
		if (host != null && host.trim().length()>0) return host;
		host=System.getenv("COMPUTERNAME");
		if (host != null && host.trim().length()>0) return host;
		return "localhost";
	}
}
