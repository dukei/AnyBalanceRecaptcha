package application;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.security.Permission;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import javax.management.RuntimeErrorException;

public class ParametersConnection extends URLConnection {
	private final URLConnection delegate;
	private final Map<String, String> params;
	private InputStream inputStream;
	
	protected ParametersConnection(URL originalURL, URL url, Map<String, String> params) {
		super(originalURL);
		try {
			delegate = url.openConnection();
			this.params = params; 
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void connect() throws IOException {
		delegate.connect();
	}

	public void setConnectTimeout(int timeout) {
		delegate.setConnectTimeout(timeout);
	}

	public int getConnectTimeout() {
		return delegate.getConnectTimeout();
	}

	public void setReadTimeout(int timeout) {
		delegate.setReadTimeout(timeout);
	}

	public int getReadTimeout() {
		return delegate.getReadTimeout();
	}

	public int getContentLength() {
		return delegate.getContentLength();
	}

	public long getContentLengthLong() {
		return delegate.getContentLengthLong();
	}

	public String getContentType() {
		return delegate.getContentType();
	}

	public String getContentEncoding() {
		return delegate.getContentEncoding();
	}

	public long getExpiration() {
		return delegate.getExpiration();
	}

	public long getDate() {
		return delegate.getDate();
	}

	public long getLastModified() {
		return delegate.getLastModified();
	}

	public String getHeaderField(String name) {
		return delegate.getHeaderField(name);
	}

	public Map<String, List<String>> getHeaderFields() {
		return delegate.getHeaderFields();
	}

	public int getHeaderFieldInt(String name, int Default) {
		return delegate.getHeaderFieldInt(name, Default);
	}

	public long getHeaderFieldLong(String name, long Default) {
		return delegate.getHeaderFieldLong(name, Default);
	}

	public long getHeaderFieldDate(String name, long Default) {
		return delegate.getHeaderFieldDate(name, Default);
	}

	public String getHeaderFieldKey(int n) {
		return delegate.getHeaderFieldKey(n);
	}

	public String getHeaderField(int n) {
		return delegate.getHeaderField(n);
	}

	public Object getContent() throws IOException {
		return delegate.getContent();
	}

	public Object getContent(Class[] classes) throws IOException {
		return delegate.getContent(classes);
	}

	public Permission getPermission() throws IOException {
		return delegate.getPermission();
	}

	public InputStream getInputStream() throws IOException {
		if(inputStream != null)
			return inputStream;
		
		String content;
		try(InputStream is = delegate.getInputStream()){
			content = new Scanner(is, "UTF-8").useDelimiter("\\A").next();
		}
		
		for(Map.Entry<String, String> entry: params.entrySet()){
			content = content.replace('%' + entry.getKey() + '%', entry.getValue());
		}
		
		InputStream is = new ByteArrayInputStream(content.getBytes("utf-8"));
		return this.inputStream = is;
	}

	public OutputStream getOutputStream() throws IOException {
		return delegate.getOutputStream();
	}

	public void setDoInput(boolean doinput) {
		delegate.setDoInput(doinput);
	}

	public boolean getDoInput() {
		return delegate.getDoInput();
	}

	public void setDoOutput(boolean dooutput) {
		delegate.setDoOutput(dooutput);
	}

	public boolean getDoOutput() {
		return delegate.getDoOutput();
	}

	public void setAllowUserInteraction(boolean allowuserinteraction) {
		delegate.setAllowUserInteraction(allowuserinteraction);
	}

	public boolean getAllowUserInteraction() {
		return delegate.getAllowUserInteraction();
	}

	public void setUseCaches(boolean usecaches) {
		delegate.setUseCaches(usecaches);
	}

	public boolean getUseCaches() {
		return delegate.getUseCaches();
	}

	public void setIfModifiedSince(long ifmodifiedsince) {
		delegate.setIfModifiedSince(ifmodifiedsince);
	}

	public long getIfModifiedSince() {
		return delegate.getIfModifiedSince();
	}

	public boolean getDefaultUseCaches() {
		return delegate.getDefaultUseCaches();
	}

	public void setDefaultUseCaches(boolean defaultusecaches) {
		delegate.setDefaultUseCaches(defaultusecaches);
	}

	public void setRequestProperty(String key, String value) {
		delegate.setRequestProperty(key, value);
	}

	public void addRequestProperty(String key, String value) {
		delegate.addRequestProperty(key, value);
	}

	public String getRequestProperty(String key) {
		return delegate.getRequestProperty(key);
	}

	public Map<String, List<String>> getRequestProperties() {
		return delegate.getRequestProperties();
	}

}
