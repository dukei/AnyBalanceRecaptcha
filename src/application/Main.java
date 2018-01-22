package application;
	
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Date;

import com.sun.net.httpserver.HttpServer;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import server.ReCaptchaHandler;
import server.ResultHandler;
import server.RootHandler;


public class Main extends Application {
	HttpServer server;
	MainController controller;
	
	@Override
	public void start(Stage primaryStage) {
		try {
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(getClass().getResource("Main.fxml"));
	        Parent root = loader.load();
	        
	        Scene scene = new Scene(root, 800, 700);
	        primaryStage.setScene(scene);
	        primaryStage.show();
	        
	        controller = (MainController) loader.getController();
	        redirectOutput(controller);

	        startServer();
		} catch(Exception e) {
			e.printStackTrace();
			stopServer();
		}
	}
	
	public static void main(String[] args) {
		launch(args);
	}
	
	private void startServer(){
		Thread serverThread = new Thread(new Runnable(){
			@Override
			public void run() {
				int port = 1500;
				try {
					server = HttpServer.create(new InetSocketAddress(port), 0);
					System.out.println(new Date() + ": server started at http://127.0.0.1:" + port);
					server.createContext("/", new RootHandler(controller));
					server.createContext("/recaptcha", new ReCaptchaHandler(controller));
					server.createContext("/result", new ResultHandler(controller));
/*					server.createContext("/echoHeader", new EchoHeaderHandler());
					server.createContext("/echoGet", new EchoGetHandler());
					server.createContext("/echoPost", new EchoPostHandler()); */
					server.setExecutor(null);
					server.start();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		serverThread.start();
	}
	
	private void stopServer(){
		if(server != null)
			server.stop(5);
	}

	@Override
	public void stop() throws Exception {
		stopServer();
		super.stop();
	}
	
	private void redirectOutput(MainController controller){
		System.setOut(new PrintStreamCapturer(controller, System.out));
		System.setErr(new PrintStreamCapturer(controller, System.err, "[ERROR] "));
	}
	
	
}
