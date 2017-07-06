package application;
	
import java.util.Stack;

import javafx.application.Application;
import javafx.stage.Stage;
import ui.UserWindow;

/**
 * The Class Main extends Application  - this class is the main of server
 */
public class Main extends Application {
	public static Stack<String> stack;
	@Override
	public void start(Stage primaryStage) {
		stack = new Stack<>();
		stack.push("server");
		UserWindow.createUserWindow(primaryStage, "server", getClass());
	}
	
	/**
	 * The main
	 * @param arg - argument
	 */
	public static void main(String[] args) {
		launch(args);
	}
}
