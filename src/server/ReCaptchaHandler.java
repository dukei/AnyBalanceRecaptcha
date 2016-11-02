package server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import application.MainController;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class ReCaptchaHandler implements HttpHandler {
	private MainController controller;

    public ReCaptchaHandler(MainController controller) {
		super();
		this.controller = controller;
	}
	

	@Override
	public void handle(HttpExchange he) throws IOException {
		String response = "";
		Map<String, String> params = new HashMap<String, String>();

		if(!he.getRequestMethod().equals("POST")){
            response = "Invalid method";
            he.sendResponseHeaders(405, response.length());
		}else{
			params = getPostParams(he);
			if(!params.containsKey("SITEKEY")){
	            response = "SITEKEY is required parameter";
	            he.sendResponseHeaders(400, response.length());
			}else if(!params.containsKey("URL")){
	            response = "URL is required parameter";
	            he.sendResponseHeaders(400, response.length());
			}else if(controller.isInProgress()){
	            response = "IN_PROGRESS";
	            he.sendResponseHeaders(401, response.length());
			}else{
	            response = "OK";
	            controller.recaptchaAPI(params);
	            he.sendResponseHeaders(200, response.length());
			}
		}

		OutputStream os = he.getResponseBody();
        os.write(response.getBytes());
        os.close();

        System.out.println("-> /recaptcha?" + params);
        System.out.println("<- " + he.getResponseCode() + " " + response);
	}
	
	private Map<String,String> getPostParams(HttpExchange exchange) throws IOException{
		// determine encoding
		Headers reqHeaders = exchange.getRequestHeaders();
		String contentType = reqHeaders.getFirst("Content-Type");
		String encoding = "utf-8";
/*		if (contentType != null) {
		    Map<String,String> parms = ValueParser.parse(contentType);
		    if (parms.containsKey("charset")) {
		        encoding = parms.get("charset");
		    }
		}*/
		// read the query string from the request body
		String qry;
		InputStream in = exchange.getRequestBody();
		try {
		    ByteArrayOutputStream out = new ByteArrayOutputStream();
		    byte buf[] = new byte[4096];
		    for (int n = in.read(buf); n > 0; n = in.read(buf)) {
		        out.write(buf, 0, n);
		    }
		    qry = new String(out.toByteArray(), encoding);
		} finally {
		    in.close();
		}
		// parse the query
		Map<String,List<String>> parms = new HashMap<String,List<String>>();
		String defs[] = qry.split("[&]");
		for (String def: defs) {
		    int ix = def.indexOf('=');
		    String name;
		    String value;
		    if (ix < 0) {
		        name = URLDecoder.decode(def, encoding);
		        value = "";
		    } else {
		        name = URLDecoder.decode(def.substring(0, ix), encoding);
		        value = URLDecoder.decode(def.substring(ix+1), encoding);
		    }
		    List<String> list = parms.get(name);
		    if (list == null) {
		        list = new ArrayList<String>();
		        parms.put(name, list);
		    }
		    list.add(value);
		}
		
		Map<String,String> params = new HashMap<String, String>();
		for(Map.Entry<String, List<String>> entry : parms.entrySet()){
			params.put(entry.getKey(), entry.getValue().get(0));
		}
		
		return params;
	}

}
