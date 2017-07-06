package ui;

import java.net.URL;

import application.Main;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class UserWindow
{
	public static void createUserWindow(Stage s, String window, Class<?> c)
	{
		try
		{
			s.close();
			
			URL url = c.getResource("/server_gui/" + window + ".fxml");
			FXMLLoader fxmlLoader = new FXMLLoader(url);
			Parent root = (Parent) fxmlLoader.load();
			Stage stage = new Stage();
			stage.setTitle(window);
			Scene scene = new Scene(root);
			stage.setScene(scene);
			stage.setResizable(false);
			stage.show();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static void closeUserWindow(Class<?> c, Stage s)
	{
		s.close();
		Main.stack.pop(); // current window
		
		String name = Main.stack.peek();
		if(Main.stack.size()>1)
			name = Main.stack.pop();
		
		try
		{
			URL url = c.getResource("/server_gui/" + name + ".fxml");
			FXMLLoader fxmlLoader = new FXMLLoader(url);
			Parent root = (Parent) fxmlLoader.load();
			Stage stage = new Stage();
			stage.setTitle(name);
			Scene scene = new Scene(root);
			stage.setScene(scene);
			stage.setResizable(false);
			stage.show();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static void exitToMenu(Class<?> c, Stage s)
	{
		s.close();
		for(int i=1;i<Main.stack.size();i++){
			Main.stack.pop();
		}
		
		String name = Main.stack.peek();
		/*if(Main.stack.size()>1)
			name = Main.stack.pop();*/
		
		try
		{
			URL url = c.getResource("/server_gui/" + name + ".fxml");
			FXMLLoader fxmlLoader = new FXMLLoader(url);
			Parent root = (Parent) fxmlLoader.load();
			Stage stage = new Stage();
			stage.setTitle(name);
			Scene scene = new Scene(root);
			stage.setScene(scene);
			stage.setResizable(false);
			stage.show();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
