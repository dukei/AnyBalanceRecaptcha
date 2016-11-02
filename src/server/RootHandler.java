package server;

import java.io.IOException;
import java.io.OutputStream;

import application.MainController;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class RootHandler implements HttpHandler {
	private MainController controller;

    public RootHandler(MainController controller) {
		super();
	}

	@Override

    public void handle(HttpExchange he) throws IOException {
            String response = "AnyBalance Recaptcha API";
            he.sendResponseHeaders(200, response.length());
            OutputStream os = he.getResponseBody();
            os.write(response.getBytes());
            os.close();
            
            System.out.println("-> /");
            System.out.println("<- " + he.getResponseCode() + " " + response);
    }
}