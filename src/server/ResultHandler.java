package server;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import application.MainController;

public class ResultHandler implements HttpHandler {
	private MainController controller;

    public ResultHandler(MainController controller) {
		super();
		this.controller = controller;
	}
	

	@Override
	public void handle(HttpExchange he) throws IOException {
		String response = "";
		if(!he.getRequestMethod().equals("GET")){
            response = "Invalid method";
            he.sendResponseHeaders(405, response.length());
		}else{
			if(controller.isInProgress()){
				response = "IN_PROGRESS";
			}else{
				response = controller.getResult();
				if(response == null)
					response = "NOT_STARTED";
			}
            he.sendResponseHeaders(200, response.length());
		}

		OutputStream os = he.getResponseBody();
        os.write(response.getBytes());
        os.close();
        
        System.out.println(new Date() + " -> /result");
        System.out.println(new Date() + " <- " + he.getResponseCode() + " " + response);
	}
	
}
