package application;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.util.Map;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker.State;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebEvent;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;

public class MainController
{
	volatile Map<String, String> currentParams = null;
	volatile String response;
	
	public static class Bridge{
		MainController controller;
		
		public Bridge(MainController controller) {
			super();
			this.controller = controller;
		}
		public void onSolved(String response){
			System.out.println("Captcha solved: \"" + response + "\"");
			controller.onSolved(response);
		}
		public void onTimeout(){
			System.out.println("Timeout!");
			controller.onTimeout();
		}
	}
	
	private class NotRecursiveURLStreamHandlerFactory implements URLStreamHandlerFactory{
		@Override
		public URLStreamHandler createURLStreamHandler(String protocol) {
            if ( "http".equals(protocol) || "https".equals(protocol)) {
                return new URLStreamHandler() {
                    protected URLConnection openConnection(URL u) throws IOException {
                    	String urlString = u.toString();
                    	String urlOriginal = currentParams != null ? currentParams.get("URL") : "";
                    	int minLen = Math.min(urlString.length(), urlOriginal.length());
                    	String baseString = urlString.substring(0,  minLen);
                    	String baseOriginal = urlOriginal.substring(0,  minLen);
                    	String tailString = urlString.substring(minLen).replace("/", "");
                    	String tailOriginal = urlOriginal.substring(minLen).replace("/", "");
                    	
                    	if(baseString.equals(baseOriginal) && tailString.equals(tailOriginal)){
/*                    		Map<String, String> map = new HashMap<String, String>();
                    		map.put("%SITEKEY%", "6LdPcQsTAAAAANvycrp6jiB7hht0P-2SD7hkwaTf");
                    		map.put("%TIMELIMIT%", "60000");
                    		map.put("%TEXT%", "Пожалуйста, покажите, что вы не робот");
*/
                    		return new ParametersConnection(u, getClass().getResource("template.html"), currentParams);
                    	}else{
                    		try {
								Class<?> c = Class.forName("sun.net.www.protocol." + protocol + ".Handler");
								URLStreamHandler handler = (URLStreamHandler)c.newInstance();
								c = handler.getClass();
								Method method = c.getDeclaredMethod("openConnection", new Class[]{URL.class});
								method.setAccessible(true);
	                    		return (URLConnection)method.invoke(handler, u);
							} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
								throw new RuntimeException(e);
							}
                    	}
                    }
                };
            }
      		return null;
		}
		
	}
	
    @FXML
    private WebView webView;
    @FXML
    private TextArea textArea;
    private Bridge bridge;

    @FXML
    private void initialize()
    {
        final WebEngine engine = webView.getEngine();
        engine.setUserAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_5) AppleWebKit/601.6.17 (KHTML, like Gecko) Version/9.1.1 Safari/601.6.17");
        
        // process page loading
        engine.getLoadWorker().stateProperty().addListener(
            new ChangeListener<State>() {
                @Override
                public void changed(ObservableValue<? extends State> ov, State oldState, State newState) {
                    if (newState == State.SUCCEEDED) {
                    		if(bridge == null) {
                    			//We need to hold the variable to prevent it being garbage collected!
                    			bridge = new Bridge(MainController.this);
                    		}
                    		
                            JSObject win = (JSObject) engine.executeScript("window");
                            win.setMember("_AnyBalanceRecaptcha", bridge);
                            
//                            engine.executeScript("if (!document.getElementById('FirebugLite')){E = document['createElement' + 'NS'] && document.documentElement.namespaceURI;E = E ? document['createElement' + 'NS'](E, 'script') : document['createElement']('script');E['setAttribute']('id', 'FirebugLite');E['setAttribute']('src', 'https://getfirebug.com/' + 'firebug-lite.js' + '#startOpened');E['setAttribute']('FirebugLite', '4');(document['getElementsByTagName']('head')[0] || document['getElementsByTagName']('body')[0]).appendChild(E);E = new Image;E['setAttribute']('src', 'https://getfirebug.com/' + '#startOpened');}"); 
                        }
                    }
                }
        );
        
        engine.setOnAlert(new EventHandler<WebEvent<String>>() {
            @Override public void handle(WebEvent<String> event) {
                log(event.getData());
            }
        });
        
        URL.setURLStreamHandlerFactory(new NotRecursiveURLStreamHandlerFactory());
        
        loadPlaceholder();
		
    }
    
    private void loadPlaceholder(){
        WebEngine engine = webView.getEngine();
//		String out = new Scanner(getClass().getResource("template.html").openStream(), "UTF-8").useDelimiter("\\A").next();
        engine.load(getClass().getResource("waiting.html").toExternalForm());
    }
    
    public void log(final String str){
    	Platform.runLater(new Runnable(){
			@Override
			public void run() {
		    	textArea.appendText(str);
			}
    	});
    }
    
    private void recaptcha(Map<String,String> params){
    	currentParams = params;
    	
    	WebEngine engine = webView.getEngine();
    	
    	if(!currentParams.containsKey("TIMELIMIT"))
    		currentParams.put("TIMELIMIT", "60000");
    	if(!currentParams.containsKey("TEXT"))
    		currentParams.put("TEXT", "");
    	if(currentParams.containsKey("USERAGENT"))
            engine.setUserAgent(currentParams.get("USERAGENT"));
    	
    	String url = params.get("URL");
    	
    	webView.getScene().getWindow().requestFocus();

        engine.load(url);
    }
    
    public void recaptchaAPI(final Map<String,String> params){
    	Platform.runLater(new Runnable(){
			@Override
			public void run() {
				recaptcha(params);
			}
    	});
    }
    
    private void onSolved(String response){
    	this.response = response;
    	this.currentParams = null;
    	loadPlaceholder();
    }
    
    private void onTimeout(){
    	this.response = "TIMEOUT";
    	this.currentParams = null;
    	loadPlaceholder();
    }
    
    public boolean isInProgress(){
    	return currentParams != null;
    }

    public String getResult(){
    	return response;
    }
    
}