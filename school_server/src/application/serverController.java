package application;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.paint.Paint;
import server.SchoolServer;

/**
 * The Class serverController - Server Controller
 */
public class serverController
{
	public static String ServerPort;
	public static String SQLport;
	public static String SQLpassword;
	public static String SQLuser;
	public static String SQLip;
	SchoolServer server;

	/** The resources . */
	@FXML
	private ResourceBundle resources;

	/** The location . */
	@FXML
	private URL location;
	
    @FXML
    private Label LabelMyIP;
	
    @FXML
    private Label sqlPassLabel;

    @FXML
    private Label sqlIPlabel;

    @FXML
    private TextField sqlIPtxt;
	
    @FXML
    private TextField sqlPortTxt;

    @FXML
    private Label sqlUserLabel;

    @FXML
    private TextField sqlPasswordTxt;
	
    @FXML
    private TextField sqlUserTxt;

    @FXML
    private Label sqlPortLabel;

	/** The connect button . */
	@FXML
	private Button connectBtn;

	/** The port label. */
	@FXML
	private Label portLbl;

	/** The school Server Label . */
	@FXML
	private Label schoolServerLbl;

	/** The status label no.1. */
	@FXML
	private Label statusLbl1;

	/** The discon button . */
	@FXML
	private Button disconBtn;

	/** The status label no.2 . */
	@FXML
	private Label statusLbl2;

	/** The port text . */
	@FXML
	private TextField portTxt;

	/**
	 * connect.
	 *
	 * @param event - connect
	 */
	@FXML
	void connect(ActionEvent event)
	{
		ServerPort = portTxt.getText();
		SQLport = sqlPortTxt.getText();
		SQLpassword = sqlPasswordTxt.getText();
		SQLuser = sqlUserTxt.getText();
		SQLip = sqlIPtxt.getText();
		
		try
		{
			Integer.parseInt(ServerPort);
		}
		catch (NumberFormatException e)
		{
			new Alert(AlertType.ERROR, "Please enter 4-digit port number", ButtonType.OK).showAndWait();
			return;
		}
		if (ServerPort.length() != 4)
		{
			new Alert(AlertType.ERROR, "Please enter 4-digit port number", ButtonType.OK).showAndWait();
		}
		else
		{
			server = new SchoolServer(Integer.parseInt(ServerPort));
			statusLbl2.setTextFill(Paint.valueOf("green"));
			statusLbl2.setText("CONNECTED");
			connectBtn.setDisable(true);
			disconBtn.setDisable(false);
			try
			{
				server.listen();
			}
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * disconnect.
	 *
	 * @param event - enter disconnect
	 */
	@FXML
	void disconnect(ActionEvent event)
	{
		try
		{
			server.close();
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		statusLbl2.setTextFill(Paint.valueOf("red"));
		statusLbl2.setText("DISCONNECTED");
		connectBtn.setDisable(false);
		disconBtn.setDisable(true);
	}

	/**
	 * Initialize.
	 */
	@FXML
	void initialize()
	{
        assert sqlPassLabel != null : "fx:id=\"sqlPassLabel\" was not injected: check your FXML file 'server.fxml'.";
        assert sqlIPlabel != null : "fx:id=\"sqlIPlabel\" was not injected: check your FXML file 'server.fxml'.";
        assert sqlIPtxt != null : "fx:id=\"sqlIPtxt\" was not injected: check your FXML file 'server.fxml'.";
        assert sqlPortTxt != null : "fx:id=\"sqlPortTxt\" was not injected: check your FXML file 'server.fxml'.";
        assert sqlUserLabel != null : "fx:id=\"sqlUserLabel\" was not injected: check your FXML file 'server.fxml'.";
        assert portTxt != null : "fx:id=\"portTxt\" was not injected: check your FXML file 'server.fxml'.";
        assert sqlUserTxt != null : "fx:id=\"sqlUserTxt\" was not injected: check your FXML file 'server.fxml'.";
        assert sqlPortLabel != null : "fx:id=\"sqlPortLabel\" was not injected: check your FXML file 'server.fxml'.";
        assert sqlPasswordTxt != null : "fx:id=\"sqlPasswordTxt\" was not injected: check your FXML file 'server.fxml'.";
		assert connectBtn != null : "fx:id=\"connectBtn\" was not injected: check your FXML file 'server.fxml'.";
		assert portLbl != null : "fx:id=\"portLbl\" was not injected: check your FXML file 'server.fxml'.";
		assert schoolServerLbl != null : "fx:id=\"schoolServerLbl\" was not injected: check your FXML file 'server.fxml'.";
		assert statusLbl1 != null : "fx:id=\"statusLbl1\" was not injected: check your FXML file 'server.fxml'.";
		assert disconBtn != null : "fx:id=\"disconBtn\" was not injected: check your FXML file 'server.fxml'.";
		assert statusLbl2 != null : "fx:id=\"statusLbl2\" was not injected: check your FXML file 'server.fxml'.";
		assert portTxt != null : "fx:id=\"portTxt\" was not injected: check your FXML file 'server.fxml'.";

		portTxt.setText("5555");
		sqlIPtxt.setText("127.0.0.1");
		sqlPortTxt.setText("3306");
		sqlUserTxt.setText("root");
		sqlPasswordTxt.setText("admin");
		
		try
		{
			LabelMyIP.setText(LabelMyIP.getText() + "  " + InetAddress.getLocalHost().getHostAddress());
		}
		catch (UnknownHostException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
