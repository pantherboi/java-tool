package com.pantherboi.tool;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class LogFilter {
    protected SimpleDateFormat timestampFormat;
    protected String logLineStartWith;
    protected long skipLineCnt = 0;
    protected long maxReadLineCnt = Long.MAX_VALUE;
    protected File src;
    protected String srcEncoding;
    protected boolean printFirstLogLineOnly;
    protected OutputStream outputStream;
    protected List<Filter> includeFilters = new ArrayList<Filter>();
    protected List<Filter> excludeFilters = new ArrayList<Filter>();

    public LogFilter timestampFormat(SimpleDateFormat timestampFormat){
        this.timestampFormat = timestampFormat;
        return this;
    }
    public LogFilter logLineStartWith(String logLineStartWith){
        this.logLineStartWith = logLineStartWith;
        return this;
    }
    public LogFilter printFirstLogLineOnly(boolean printFirstLogLineOnly){
    	this.printFirstLogLineOnly = printFirstLogLineOnly;
    	return this;
    }
    public LogFilter skipLineCnt(long skipLineCnt){
        this.skipLineCnt = skipLineCnt;
        return this;
    }
    public LogFilter maxReadLineCnt(long maxReadLineCnt){
        this.maxReadLineCnt = maxReadLineCnt;
        return this;
    }
    public LogFilter srcEncoding(String srcEncoding){
    	this.srcEncoding = srcEncoding;
    	return this;
    }
    public LogFilter src(File src){
        this.src = src;
        return this;
    }
    
    public LogFilter addIncludeFilters(Filter filter){
        this.includeFilters.add(filter);
        return this;
    }
    public LogFilter addexcludeFilters(Filter filter){
        this.excludeFilters.add(filter);
        return this;
    }
    public LogFilter init(){
        if(src != null && !src.exists())  throw new RuntimeException("src file doesn't exists:" + src.getAbsolutePath());
        if(outputStream == null) throw new RuntimeException("outputStream is null");
        return this;
    }

    public LogFilter outputStream(OutputStream outputStream){
        this.outputStream = outputStream;
        return this;
    }
    public static class Filter {
        public static final int TYPE_LINE_CONTAIN = 1;
        public static final int TYPE_LINE_PATTERN = 2;
        public static final int TYPE_STACKTRACE_CONTAIN = 3;
        protected final int type;
        protected String filterValue;
        
        public Filter(int type,String filterValue){
            this.type = type;
            this.filterValue = filterValue;
        }
        public boolean isMatch(String line,boolean isNew){
            if(type == TYPE_LINE_CONTAIN && isNew ){
            	if(line.contains(filterValue)) return true; 
            }
            else if(type == TYPE_LINE_PATTERN && isNew ){
            	if(line.matches(filterValue)) return true;             	
            }
            else if(type == TYPE_STACKTRACE_CONTAIN  && !isNew){            	
            	if(line.contains(filterValue)) return true; 
            }
            
            return false;
        }
    }

    public void read() throws Exception {
        BufferedReader br =null;
		String line=null;
        long lineNo = 0;
        long readLineCnt = 0;
		try {
            br = new BufferedReader( srcEncoding != null ? new InputStreamReader(new FileInputStream(src),srcEncoding): new InputStreamReader(new FileInputStream(src)));

            for(; (line=br.readLine())!=null; ) {
				lineNo++;
                if (lineNo < skipLineCnt ) continue;
                readLineCnt++;
                if(readLineCnt > maxReadLineCnt) break;
//                System.out.println(readLineCnt +":" + line);
                processFilter(lineNo,line);
                
			}
		}catch(Throwable e){
			throw new Exception("error at lineNo["+lineNo+"]["+line+"]",e);
		}finally {
			try{ br.close(); }catch(Exception e){}
		}
		System.out.println("total read cnt : " + readLineCnt);
    }


    private boolean isIncludeCurrentStackTrace = false;
    private List<String> currentStackTrace = new ArrayList<String>();
 

    private void processFilter(long lineNo, String line) throws IOException {
        boolean isNew = checkIsNewLog(line);
        if(isNew){
        	writeCurrentStacktrace();
        	resetCurrentStackTrace();
        	if(checkIncludeFilter(isNew,line))
        		isIncludeCurrentStackTrace = true;
        	
        }else {
        	if(checkIncludeFilter(isNew,line)) 
        		isIncludeCurrentStackTrace = true;        	
        }
        
       	addCurrentStackeTrace(line);
    }
    
    private void resetCurrentStackTrace() {
    	currentStackTrace = new ArrayList<String>();
    	isIncludeCurrentStackTrace = false;
	}
	private void addCurrentStackeTrace(String line) {
        currentStackTrace.add(line);
    }
    
    private void writeCurrentStacktrace() throws IOException {
    	if(!isIncludeCurrentStackTrace ) return;
    	
        for(int i = 0 ; i < currentStackTrace.size() ; i++) {
        	String line = currentStackTrace.get(i);
        	if(printFirstLogLineOnly && i > 0 ) break;	
        	outputStream.write((line+  System.lineSeparator()).getBytes());
        }
        
    }

    private boolean checkIncludeFilter(boolean isNew, String line) {
        for(Filter filter: includeFilters){
            boolean isInclude = filter.isMatch(line,isNew);
            if(isInclude ) return true;
        }
        return false;
    }

    private boolean checkIsNewLog(String line) {
        return line.startsWith(logLineStartWith);
    }
}
    
