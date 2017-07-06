package server;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.UUID;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import application.FilePart;
import application.serverController;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import ocsf.*;

/**
 * The Class schoolServer - this class is school Server
 */
public class SchoolServer extends AbstractServer
{
	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("YYYY-MM-dd HH:mm:ss");
	private static final File ASSIGNMENTS_DIR = new File("assignments");
	private static final File SOLUTIONS_DIR = new File("solutions");

	String IP = serverController.SQLip;
	String User = serverController.SQLuser;
	String Password = serverController.SQLpassword;
	String Port = serverController.SQLport;

	//final public static int DEFAULT_PORT = 5556;
	ArrayList<String> arr;

	public SchoolServer(int port)
	{
		super(port);

		// Create the directory if it doesn't exist.
		if (!ASSIGNMENTS_DIR.exists())
		{
			ASSIGNMENTS_DIR.mkdir();
		}
		if (!SOLUTIONS_DIR.exists())
		{
			SOLUTIONS_DIR.mkdir();
		}
	}

	/**
	 * add assignment
	 * @param msg - message
	 * @param client
	 */
	private void addAssignment(ArrayList<?> msg, ConnectionToClient client)
	{
		System.out.println("Adding Teacher Assignment");
		LocalDateTime dueDate = (LocalDateTime) msg.get(1);
		String courseID = (String) msg.get(2);
		String originalFileName = (String) msg.get(3);
		byte[] fileContents = (byte[]) msg.get(4);

		int dotIndex = originalFileName.lastIndexOf('.');
		String extension = originalFileName.substring(dotIndex);

		String assignmentFileName = originalFileName;

		File output = new File(ASSIGNMENTS_DIR, assignmentFileName);

		try
		{
			Files.write(output.toPath(), fileContents);
		}
		catch (IOException e)
		{
			e.printStackTrace();

			return;
		}

		String formattedDate = dueDate.format(FORMATTER);

		System.out.println("FILENAME IS " + assignmentFileName);

		executeInsert("INSERT INTO assignment (assignmentName, dueDate, farmat, courseID) VALUES (?, ?, ?, ?)", assignmentFileName, dueDate.format(FORMATTER), extension.substring(1), courseID);

		executeInsert("INSERT INTO assignment_in_course (courseID, assignmentName) VALUES (?, ?)", courseID, assignmentFileName);

		System.out.println("OK!");
	}

	/**
	 * download assignment
	 * @param msg - message
	 * @param client
	 */
	private void downloadAssignment(ArrayList<?> msg, ConnectionToClient client) throws IOException
	{
		try
		{
			String filter = "";
			String fileName = "check.docx";
			File output = new File(ASSIGNMENTS_DIR, fileName);
			FileInputStream fis = null;
			fis = new FileInputStream(output);
			byte fileContents[] = new byte[(int) output.length()];
			fis.read(fileContents);

			// Creates file chooser.
			FileChooser chooser = new FileChooser();
			chooser.setTitle("Save File");
			// sets file extension filter . This extension stored in the
			// database.
			FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(filter + " File (*." + filter + ")", "*." + filter + "");
			// Adding the filter.
			chooser.getExtensionFilters().add(extFilter);
			// Starts the saving stage.
			File dest = chooser.showSaveDialog(new Stage());
			// If the user canceled.
			if (dest != null)
			{
				FileOutputStream fout;
				fout = new FileOutputStream(dest);
				// Converting the array of bytes into the file.
				BufferedOutputStream bout = new BufferedOutputStream(fout);
				bout.write(fileContents);
				bout.flush();
				bout.close();
				fout.close();
			}
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

	}

	/**
	 * add solution 
	 * @param msg - message
	 * @param client
	 */
	private void addSolution(ArrayList<?> msg, ConnectionToClient client)
	{
		System.out.println("Adding solution");
		String assignmentName = (String) msg.get(1);
		String courseid = (String) msg.get(2);
		String originalFileName = (String) msg.get(3);
		String PupilID = (String) msg.get(4);
		byte[] contents = (byte[]) msg.get(5);

		int dotIndex = originalFileName.lastIndexOf('.');
		String extension = originalFileName.substring(dotIndex);

		String solutionFileName = UUID.randomUUID().toString() + extension;

		File output = new File(SOLUTIONS_DIR, solutionFileName);

		try
		{
			System.out.println("Writing file");
			Files.write(output.toPath(), contents);
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return;
		}

		System.out.println("Wrote to " + output.getAbsolutePath());
		System.out.println("Running INSERT");

		executeInsert("INSERT INTO pupil_assignment (pupilAssignmentName, subbmisionDate, assignmentName, PupilID) VALUES (?, NOW(), ?, ?)", output.getPath(), assignmentName, PupilID);

		System.out.println("OK!");
	}

	private void download(ConnectionToClient conn, FilePart filePart)
	{

	}

	private void upload(ConnectionToClient conn, FilePart filePart)
	{
		File f;
		String respond = "";
		try
		{
			File dir1 = new File("files");
			if (!dir1.exists())
			{
				dir1.mkdir();
			}

			File dir2 = new File("files/" + filePart.userId);
			if (!dir2.exists())
			{
				dir2.mkdir();
			}

			f = new File("files/" + filePart.userId + "/" + filePart.fileName + ".part" + filePart.order);
		}
		catch (Exception e)
		{
			try
			{
				conn.sendToClient("upload failed because folders cann't be created in the server.");
				return;
			}
			catch (IOException e1)
			{
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			return;
		}

		try (FileOutputStream fos = new FileOutputStream(f, true)) // safe block, true for append to end of file!!
		{
			fos.write(filePart.buffer, 0, filePart.length);
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			f.delete();
			respond = "File upload failed because there was problem with the file saving!";
			try
			{
				conn.sendToClient(respond);
				return;
			}
			catch (IOException e1)
			{
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}

		if (isGetAllFiles(("files/" + filePart.userId), filePart.fileName, filePart.totalLength)) // if last time: update db, response server
		{
			if (!combineFiles(("files/" + filePart.userId), filePart.fileName, filePart.totalLength, 10240))
			{
				try
				{
					conn.sendToClient("Upload failed, combine files can't be completed!");
				}
				catch (IOException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return;
			}
			boolean ans;
			if (filePart.uploaderType.equals("pupil"))
			{
				ans = executeInsert("INSERT INTO pupil_assignment (pupilAssignmentName, assignmentName, PupilID, courseId) VALUES (?, ?, ?, ?)", "files/" + filePart.userId + "/" + filePart.fileName, filePart.assignmentName, filePart.userId, filePart.courseId);
			}
			else if (filePart.uploaderType.equals("pupil check"))
			{
				ans = executeInsert("INSERT INTO pupil_assignment (checkedAssignmentName, assignmentName, PupilID, courseId) VALUES (?, ?, ?, ?)", "files/" + filePart.userId + "/" + filePart.fileName, filePart.assignmentName, filePart.userId, filePart.courseId);
			}
			else
			{
				ans = executeInsert("INSERT INTO assignment (assignmentName, dueDate, courseID) VALUES (?, ?, ?)", "files/" + filePart.userId + "/" + filePart.fileName, filePart.date, filePart.courseId);
			}

			if (!ans)
			{
				if (filePart.uploaderType.equals("pupil"))
				{
					ArrayList<String> map = new ArrayList<>();
					map.add("pupil_assignment");
					map.add("pupilAssignmentName");
					map.add("files/" + filePart.userId + "/" + filePart.fileName);
					map.add("conditions");
					map.add("assignmentName");
					map.add(filePart.assignmentName);
					map.add("PupilID");
					map.add(filePart.userId);
					map.add("courseId");
					map.add(filePart.courseId);
					update(map);
				}
				else if (filePart.uploaderType.equals("pupil check"))
				{
					ArrayList<String> map = new ArrayList<>();
					map.add("pupil_assignment");
					map.add("checkedAssignmentName");
					map.add("files/" + filePart.userId + "/" + filePart.fileName);
					map.add("conditions");
					map.add("assignmentName");
					map.add(filePart.assignmentName);
					map.add("PupilID");
					map.add(filePart.userId);
					map.add("courseId");
					map.add(filePart.courseId);
					update(map);
				}
			}
			respond = "File upload succeeded!";

			try
			{
				conn.sendToClient(respond);
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		else
		{
			respond = "continue";

			try
			{
				conn.sendToClient(respond);
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}

	private boolean combineFiles(String dir, String fileName, long totalLength, int size)
	{
		byte[] buffer = new byte[size];
		File directory = new File(dir);
		File newFile = new File(dir + "/" + fileName);
		FileFilter ff = new FileFilter()
		{
			@Override
			public boolean accept(File f)
			{
				return f.isFile() && f.getName().substring(0, f.getName().length() - 1).contains(fileName + ".part");
			}
		};

		if (newFile.exists())
			newFile.delete();
		try (FileOutputStream fos = new FileOutputStream(newFile, true))
		{
			File[] files = directory.listFiles(ff);
			Arrays.sort(files, new Comparator<File>()
			{
				@Override
				public int compare(File f1, File f2)
				{
					if (f1 == null || f2 == null)
						return -1;

					String str1 = f1.getName();
					str1 = str1.substring(str1.indexOf(".part") + 5);
					int num1 = Integer.parseInt(str1);

					String str2 = f2.getName();
					str2 = str2.substring(str2.indexOf(".part") + 5);
					int num2 = Integer.parseInt(str2);

					if (num1 > num2)
						return 1;
					else if (num1 == num2)
						return 0;
					return -1;
				}

			});
			for (File fi : files)
			{
				try (FileInputStream fis = new FileInputStream(fi))
				{
					int read = fis.read(buffer);
					fos.write(buffer, 0, read);
				}
				catch (Exception e1)
				{
					e1.printStackTrace();
					fos.close();
					return false;
				}
				fi.delete();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return false;
		}
		return true;
	}

	private boolean isGetAllFiles(String dir, String fileName, long totalLength)
	{
		try
		{
			File directory = new File(dir);
			FileFilter ff = new FileFilter()
			{
				@Override
				public boolean accept(File f)
				{
					return f.isFile() && f.getName().contains(fileName + ".part");
				}
			};

			long currentLength = 0;
			for (File f : directory.listFiles(ff))
			{
				currentLength += f.length();
			}

			return (currentLength == totalLength);
		}
		catch (Exception e)
		{
		}
		return false;
	}

	/**
	 * handle message from client 
	 * @param msg - message
	 * @param client
	 */
	public void handleMessageFromClient(Object msg, ConnectionToClient client)
	{
		/************************************************ Checks *************************************************/
		System.out.println("Request received from " + client);
		Object response = null;
		boolean isJson = (msg instanceof String) && ((String) msg).startsWith("JSON: ");
		if (!isJson && (!(msg instanceof ArrayList<?>) || ((ArrayList<?>) msg).size() < 3))
		{
			try
			{
				client.sendToClient(null);
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			return;
		}

		ArrayList<?> rawMessage;
		Gson gson = new Gson();
		HashMap<String, String> map = new HashMap<>();
		FilePart filePart = null;

		if (isJson)
		{
			String action = "";

			try
			{
				filePart = (FilePart) gson.fromJson(((String) msg).substring(6), new TypeToken<FilePart>()
				{
				}.getType()); // convert json to mappings automatically!!} catch (Exception e)
			}
			catch (Exception e)
			{
			}

			try
			{
				map = gson.fromJson(((String) msg).substring(6), new TypeToken<HashMap<String, String>>()
				{
				}.getType()); // convert json to mappings automatically!!			
			}
			catch (Exception e)
			{
			}

			if (((String) msg).contains("send file"))
			{
				action = "send file";
			}
			else
			{
				action = (map.getOrDefault("action", "none")).toLowerCase(); // get action
				map.remove("action"); // we don't need it anymore
			}

			try
			{
				if (action.contains("receive file header")) // if the action is 'receive-file'
				{
					ArrayList<String> mapFile = new ArrayList<>();
					mapFile.add("pupil_assignment");
					mapFile.add("courseId");
					mapFile.add(filePart.courseId);
					mapFile.add("PupilID");
					mapFile.add(filePart.userId);
					mapFile.add("assignmentName");
					mapFile.add(filePart.assignmentName);
					ArrayList<String> res = (ArrayList<String>) select(mapFile);
					String[] cols = res.get(0).split(";");
					HashMap<String, String> resMap = new HashMap<>();
					for (String col : cols)
					{
						String[] field = col.split("=");
						resMap.put(field[0], field[1]);
					}
					FilePart copy = new FilePart();

					try
					{
						try
						{
							File f;
							if (filePart.uploaderType.equals("self"))
								f = new File(resMap.get("pupilAssignmentName"));
							else if (filePart.uploaderType.equals("self"))
								f = new File(resMap.get("checkedAssignmentName"));
							else // task
								f = new File(resMap.get("assignmentName"));
							copy.totalLength = f.length();
							copy.fileName = resMap.get(filePart.uploaderType.equals("self") ? "pupilAssignmentName" : "checkedAssignmentName");
							copy.fileName = copy.fileName.substring(copy.fileName.lastIndexOf("/") + 1);
							copy.assignmentName = filePart.assignmentName;
							copy.courseId = filePart.courseId;
							copy.userId = filePart.userId;
						}
						catch (Exception e)
						{
						}
						String respond = gson.toJson(copy);
						client.sendToClient(respond);
					}
					catch (IOException e)
					{
						String respond = "File get header failed!";
						// TODO Auto-generated catch block
						e.printStackTrace();
						try
						{
							client.sendToClient(respond);
						}
						catch (IOException e1)
						{
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}
				}
				else if (action.contains("receive file")) // if the action is 'receive-file'
				{
					String respond = "";
					File f;
					String res = "File get failed!";
					FilePart copy = new FilePart();

					try
					{
						f = new File("files/" + filePart.userId + "/" + filePart.fileName);
						try (FileInputStream fis = new FileInputStream(f)) // safe block, true for append to end of file!!
						{
							copy.buffer = new byte[10240];
							copy.order = filePart.order;
							copy.assignmentName = filePart.assignmentName;
							copy.courseId = filePart.courseId;
							copy.userId = filePart.userId;
							copy.fileName = filePart.fileName;
							fis.skip((copy.order - 1) * 10240);
							int read = fis.read(copy.buffer, 0, 10240);
							copy.length = read;
							copy.totalLength = f.length();
							respond = res = "Read file(" + copy.order + ") succeeded";
						}
						catch (IOException e)
						{
							// TODO Auto-generated catch block
							e.printStackTrace();
							respond = "File get failed!";
							try
							{
								client.sendToClient(respond);
							}
							catch (IOException e1)
							{
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
						}
					}
					catch (Exception e)
					{
						respond = "File get failed!";
						try
						{
							client.sendToClient(respond);
						}
						catch (IOException e1)
						{
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}

					try
					{
						respond = gson.toJson(copy);
						client.sendToClient(respond);
					}
					catch (IOException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				else if (action.equals("send file"))
				{
					upload(client, filePart);
				}

				return;
			}
			catch (RuntimeException e)
			{
				System.out.println("Something went wrong");
				e.printStackTrace(System.out);
			}
			return;
		}

		rawMessage = (ArrayList<?>) msg;
		if (rawMessage.get(0).equals("add assignment solution"))
		{
			rawMessage.remove(0);
			try
			{
				FilePart fp = new FilePart();
				fp.assignmentName = (String) rawMessage.remove(0);
				fp.buffer = (byte[]) rawMessage.remove(0);
				fp.courseId = (String) rawMessage.remove(0);
				fp.date = (String) rawMessage.remove(0);
				fp.fileName = (String) rawMessage.remove(0);
				fp.length = (Integer) rawMessage.remove(0);
				fp.order = (Integer) rawMessage.remove(0);
				fp.totalLength = (Long) rawMessage.remove(0);
				fp.uploaderType = (String) rawMessage.remove(0);
				fp.userId = (String) rawMessage.remove(0);

				upload(client, fp);
				return;

			}
			catch (RuntimeException e)
			{
				System.out.println("Something went wrong");
				e.printStackTrace(System.out);
			}
			return;
		}

		/*
		 * if (rawMessage.get(0).equals("teacher update assignment solution")) { rawMessage.remove(0); try { FilePart fp = new FilePart(); fp.assignmentName = (String) rawMessage.remove(0); fp.buffer = (byte[]) rawMessage.remove(0); fp.courseId = (String) rawMessage.remove(0); fp.date = (String) rawMessage.remove(0); fp.fileName = (String) rawMessage.remove(0); fp.length = (Integer) rawMessage.remove(0); fp.order = (Integer) rawMessage.remove(0); fp.totalLength= (Long) rawMessage.remove(0); fp.uploaderType = (String) rawMessage.remove(0); fp.userId = (String) rawMessage.remove(0); fp.comments = (String) rawMessage.remove(0); fp.grade = (Double) rawMessage.remove(0);
		 * 
		 * upload(client, fp); return;
		 * 
		 * } catch (RuntimeException e) { System.out.println("Something went wrong"); e.printStackTrace(System.out); } return; }
		 */

		if (rawMessage.get(0).equals("get assignment"))
		{
			try
			{
				downloadAssignment(rawMessage, client);
			}
			catch (RuntimeException e)
			{
				System.out.println("Something went wrong");
				e.printStackTrace(System.out);
			}
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println("query handler");

		/************************************************
		 * Query handler
		 ******************************************/
		// msg is array list and has at least 2 strings
		arr = (ArrayList<String>) msg;
		String clientId = arr.remove(0);
		String query = arr.remove(0);

		if (query.equals("select"))
		{
			response = select(arr);
		}
		else if (query.equals("update"))
		{
			response = update(arr);
		}
		else if (query.equals("insert"))
		{
			response = insert(arr);
		}
		else if (query.equals("delete"))
		{
			response = delete(arr);
		}
		else if (query.equals("select field"))
		{
			response = selectField(arr);
		}
		else if (query.equals("histogram 1"))
		{
			response = histogram1(arr);
		}
		else if (query.equals("histogram 2"))
		{
			response = histogram2(arr);
		}
		else if (query.equals("histogram 3"))
		{
			response = histogram3(arr);
		}

		System.out.println("sending back response " + response);

		/************************************************
		 * Send to Client
		 ******************************************/
		try
		{
			if (response != null)
				((ArrayList<String>) response).add(0, clientId);
			System.out.println("sending now");
			client.sendToClient(response);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * server started 
	 */
	protected void serverStarted()
	{
		System.out.println("Server listening for connections on port " + getPort());
	}

	/**
	 * server stopped 
	 */
	protected void serverStopped()
	{
		System.out.println("Server has stopped listening for connections.");
	}

	/**
	 * execute insert 
	 * @param sql
	 * @param arguments
	 */
	private boolean executeInsert(String sql, String... arguments)
	{
		try
		{
			Class.forName("com.mysql.jdbc.Driver").newInstance();
		}
		catch (Exception ex)
		{
			System.out.println("Error - connection to DB");
			return false;
		}

		try
		{
			Connection conn = DriverManager.getConnection("jdbc:mysql://" + IP + "/school", User, Password);
			PreparedStatement stmt = conn.prepareStatement(sql);
			for (int i = 0; i < arguments.length; i++)
			{
				stmt.setString(i + 1, arguments[i]);
			}

			System.out.println("Executing INSERT");
			stmt.executeUpdate();
		}
		catch (SQLException e)
		{
			e.printStackTrace(System.out);
			System.out.println("Insert failed! " + e);
			return false;
		}
		return true;
	}

	/**
	 * select field 
	 * @param arr
	 */
	protected Object selectField(ArrayList<String> arr)
	{
		Statement stmt;
		String sql = "";
		ArrayList<String> answer = new ArrayList<>();
		try
		{
			Class.forName("com.mysql.jdbc.Driver").newInstance();
		}
		catch (Exception ex)
		{
			System.out.println("Error - connection to DB");
		}
		try
		{
			Connection conn = DriverManager.getConnection("jdbc:mysql://" + IP + "/school", User, Password);
			stmt = conn.createStatement();

			if (arr.size() == 0)
			{
				// error handling
				return null;
			}

			sql = "SELECT  " + arr.get(0) + " FROM " + arr.get(1);

			if (arr.size() > 1)
			{

				if (arr.size() > 2)
				{
					sql += " WHERE ";
					for (int i = 2; i < arr.size(); i += 2)
					{
						sql += arr.get(i) + "=\"" + arr.get(i + 1) + "\" ";
						if (i + 2 < arr.size())
							sql += "AND ";
					}
				}
				sql += ";";
				System.out.println("\nSQL: " + sql + "\n");
				ResultSet rs = stmt.executeQuery(sql);
				// need to change "is Logged" field!!!

				ResultSetMetaData metaData = rs.getMetaData();
				int count = metaData.getColumnCount(); // number of column

				while (rs.next())
				{
					String row = "";
					for (int i = 1; i <= count; i++)
					{
						row += metaData.getColumnLabel(i) + "=" + rs.getString(i) + ";";
					}
					if (row.endsWith(";"))
						row = row.substring(0, row.length() - 1);
					answer.add(row);
				}
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		return answer;
	}

	/**
	 * server select 
	 * @param arr
	 */
	protected Object select(ArrayList<String> arr)
	{
		Statement stmt;
		String sql = "";
		ArrayList<String> answer = new ArrayList<>();
		try
		{
			Class.forName("com.mysql.jdbc.Driver").newInstance();
		}
		catch (Exception ex)
		{
			System.out.println("Error - connection to DB");
		}
		try
		{
			Connection conn = DriverManager.getConnection("jdbc:mysql://" + IP + "/school", User, Password);
			stmt = conn.createStatement();

			if (arr.size() == 0)
			{
				// error handling
				return null;
			}

			sql = "SELECT * FROM " + arr.get(0);
			if (arr.size() >= 1)
			{
				if (arr.size() > 2)
				{
					sql += " WHERE ";
					for (int i = 1; i < arr.size(); i += 2)
					{
						sql += arr.get(i) + "=\"" + arr.get(i + 1) + "\" ";
						if (i + 2 < arr.size())
							sql += "AND ";
					}
				}

				sql += ";";
				System.out.println("\nSQL: " + sql + "\n");
				ResultSet rs = stmt.executeQuery(sql);
				// need to change "is Logged" field!!!

				ResultSetMetaData metaData = rs.getMetaData();
				int count = metaData.getColumnCount(); // number of column

				while (rs.next())
				{
					String row = "";
					for (int i = 1; i <= count; i++)
					{
						row += metaData.getColumnLabel(i) + "=" + rs.getString(i) + ";";
					}
					if (row.endsWith(";"))
						row = row.substring(0, row.length() - 1);
					answer.add(row);
				}
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}

		return answer;
	}

	/**
	 * histogram1 
	 * @param arr
	 */
	protected Object histogram1(ArrayList<String> arr)
	{
		Statement stmt;
		String sql = "";
		ArrayList<String> answer = new ArrayList<>();
		try
		{
			Class.forName("com.mysql.jdbc.Driver").newInstance();
		}
		catch (Exception ex)
		{
			System.out.println("Error - connection to DB");
		}
		try
		{
			Connection conn = DriverManager.getConnection("jdbc:mysql://" + IP + "/school", User, Password);
			stmt = conn.createStatement();

			if (arr.size() == 0)
			{
				// error handling
				return null;
			}

			/*
			 * sql = "SELECT S.SemesterID, CC.classId, AVG(P.gradeInCourse) AS avgGrade " + " FROM course_in_class CC, activity_in_semester S, pupil_in_course P " + " WHERE CC.teacherId=" + arr.remove(0) + " AND (";
			 */

			// TEST QUERY
			sql = "SELECT CC.classId, AVG(P.gradeInCourse) AS avgGrade " + " FROM course_in_class CC, activity_in_semester S, pupil_in_course P " + " WHERE CC.teacherId=" + arr.remove(0) + " AND (";

			for (int i = 0; i < arr.size(); i++)
				sql += "S.SemesterID=" + arr.get(i) + " OR ";

			if (sql.endsWith("OR "))
				sql = sql.substring(0, sql.length() - 3);
			sql += ") AND CC.courseId=S.ActivityID AND CC.courseId=P.courseID " + " GROUP BY CC.classId;";

			System.out.println("\nSQL: " + sql + "\n");
			ResultSet rs = stmt.executeQuery(sql);
			// need to change "is Logged" field!!!

			ResultSetMetaData metaData = rs.getMetaData();
			int count = metaData.getColumnCount(); // number of column

			while (rs.next())
			{
				String row = "";
				for (int i = 1; i <= count; i++)
				{
					row += metaData.getColumnLabel(i) + "=" + rs.getString(i) + ";";
				}
				if (row.endsWith(";"))
					row = row.substring(0, row.length() - 1);
				answer.add(row);
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}

		return answer;
	}

	/**
	 * histogram 2
	 * @param arr
	 */
	protected Object histogram2(ArrayList<String> arr)
	{
		Statement stmt;
		String sql = "";
		ArrayList<String> answer = new ArrayList<>();
		try
		{
			Class.forName("com.mysql.jdbc.Driver").newInstance();
		}
		catch (Exception ex)
		{
			System.out.println("Error - connection to DB");
		}
		try
		{
			Connection conn = DriverManager.getConnection("jdbc:mysql://" + IP + "/school", User, Password);
			stmt = conn.createStatement();

			if (arr.size() == 0)
			{
				// error handling
				return null;
			}

			sql = "SELECT CC.teacherId, AVG(P.gradeInCourse) AS avgGrade " + " FROM course_in_class CC, activity_in_semester S, pupil_in_course P " + " WHERE CC.classId=" + arr.remove(0) + " AND (";

			for (int i = 0; i < arr.size(); i++)
				sql += "S.SemesterID=" + arr.get(i) + " OR ";

			if (sql.endsWith("OR "))
				sql = sql.substring(0, sql.length() - 3);

			sql += ") AND CC.courseId=S.ActivityID AND CC.courseId=P.courseID " + " GROUP BY CC.teacherId;";

			System.out.println("\nSQL: " + sql + "\n");
			ResultSet rs = stmt.executeQuery(sql);
			// need to change "is Logged" field!!!

			ResultSetMetaData metaData = rs.getMetaData();
			int count = metaData.getColumnCount(); // number of column

			while (rs.next())
			{
				String row = "";
				for (int i = 1; i <= count; i++)
				{
					row += metaData.getColumnLabel(i) + "=" + rs.getString(i) + ";";
				}
				if (row.endsWith(";"))
					row = row.substring(0, row.length() - 1);
				answer.add(row);
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}

		return answer;
	}

	/**
	 * server histogram 3 
	 * @param arr 
	 */
	protected Object histogram3(ArrayList<String> arr)
	{
		Statement stmt;
		String sql = "";
		ArrayList<String> answer = new ArrayList<>();
		try
		{
			Class.forName("com.mysql.jdbc.Driver").newInstance();
		}
		catch (Exception ex)
		{
			System.out.println("Error - connection to DB");
		}
		try
		{
			Connection conn = DriverManager.getConnection("jdbc:mysql://" + IP + "/school", User, Password);
			stmt = conn.createStatement();

			if (arr.size() == 0)
			{
				// error handling
				return null;
			}

			sql = "SELECT CC.courseId, AVG(P.gradeInCourse) AS avgGrade " + " FROM course_in_class CC, activity_in_semester S, pupil_in_course P " + " WHERE CC.classId=" + arr.get(0) + " AND (";

			for (int i = 0; i < arr.size(); i++)
				sql += "S.SemesterID=" + arr.get(i) + " OR ";

			if (sql.endsWith("OR "))
				sql = sql.substring(0, sql.length() - 3);

			sql += ") AND CC.courseId=S.ActivityID AND CC.courseId=P.courseID " + " GROUP BY CC.courseId;";

			// TEST QUERY
			/*
			 * sql = "SELECT CC.classId, AVG(P.gradeInCourse) AS avgGrade " + " FROM course_in_class CC, activity_in_semester S, pupil_in_course P " + " WHERE CC.teacherId=" + arr.remove(0) + " AND (";
			 * 
			 * for (int i = 0; i < arr.size(); i++) sql += "S.SemesterID=" + arr.get(i) + " OR ";
			 * 
			 * if (sql.endsWith("OR ")) sql = sql.substring(0, sql.length() - 3); sql += ") AND CC.courseId=S.ActivityID AND CC.courseId=P.courseID " + " GROUP BY CC.classId;";
			 * 
			 * System.out.println("\nSQL: " + sql + "\n");
			 */

			System.out.println("\nSQL: " + sql + "\n");
			ResultSet rs = stmt.executeQuery(sql);

			ResultSetMetaData metaData = rs.getMetaData();
			int count = metaData.getColumnCount(); // number of column

			while (rs.next())
			{
				String row = "";
				for (int i = 1; i <= count; i++)
				{
					row += metaData.getColumnLabel(i) + "=" + rs.getString(i) + ";";
				}
				if (row.endsWith(";"))
					row = row.substring(0, row.length() - 1);
				answer.add(row);
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}

		return answer;
	}

	/**
	 * update 
	 * @param arr
	 */
	protected Object update(ArrayList<String> arr)
	{
		Statement stmt;
		String sql = "";
		int index = 0;
		ArrayList<String> answer = new ArrayList<>();
		try
		{
			Class.forName("com.mysql.jdbc.Driver").newInstance();
		}
		catch (Exception ex)
		{
			System.out.println("Error - connection to DB");
		}
		try
		{
			Connection conn = DriverManager.getConnection("jdbc:mysql://" + IP + "/school", User, Password);
			stmt = conn.createStatement();

			if (arr.size() == 0)
			{
				// error handling
				return null;
			}

			sql = "UPDATE " + arr.get(0);
			if (arr.size() > 1)
			{
				sql += " SET ";
				for (int i = 1; i < arr.size(); i += 2)
				{
					if (arr.get(i).equals("conditions"))
					{
						index = i + 1;
						break;
					}
					else
					{
						sql += arr.get(i) + "=\"" + arr.get(i + 1) + "\" ";
						if (i + 2 < arr.size())
							sql += ", ";
					}
				}
				if (sql.endsWith(", "))
					sql = sql.substring(0, sql.length() - 2);
				if (index != 0)
				{
					sql += " WHERE ";
					for (int i = index; i < arr.size(); i += 2)
					{
						sql += arr.get(i) + "=\"" + arr.get(i + 1) + "\" ";
						if (i + 2 < arr.size())
							sql += "AND ";
					}
				}
				else
				{
					System.out.println("Error - No Condition for WHERE");
					return null;
				}

			}
			sql += ";";
			System.out.println("\nSQL: " + sql + "\n");
			int rs = stmt.executeUpdate(sql);
			answer.add("" + rs);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		return answer;
	}

	/**
	 * delete 
	 * @param arr
	 */
	protected Object delete(ArrayList<String> arr)
	{
		Statement stmt;
		String sql = "";
		ArrayList<String> answer = new ArrayList<>();
		try
		{
			Class.forName("com.mysql.jdbc.Driver").newInstance();
		}
		catch (Exception ex)
		{
			System.out.println("Error - connection to DB");
		}
		try
		{
			Connection conn = DriverManager.getConnection("jdbc:mysql://" + IP + "/school", User, Password);
			stmt = conn.createStatement();

			if (arr.size() == 0)
			{
				// error handling
				return null;
			}

			sql = "DELETE FROM " + arr.get(0);
			if (arr.size() >= 3)
			{
				sql += " WHERE ";
				for (int i = 1; i < arr.size(); i += 2)
				{
					sql += arr.get(i) + "=\"" + arr.get(i + 1) + "\" ";
					if (i + 2 < arr.size())
						sql += "AND ";
				}
			}
			sql += ";";
			System.out.println("\nSQL: " + sql + "\n");
			int rs = stmt.executeUpdate(sql);
			answer.add("" + rs);

		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		return answer;
	}

	/**
	 * insert
	 * @param arr 
	 */
	protected Object insert(ArrayList<String> arr)
	{
		Statement stmt;
		String sql = "";
		ArrayList<String> answer = new ArrayList<>();
		int index = 0;
		try
		{
			Class.forName("com.mysql.jdbc.Driver").newInstance();
		}
		catch (Exception ex)
		{
			System.out.println("Error - connection to DB");
		}
		try
		{
			Connection conn = DriverManager.getConnection("jdbc:mysql://" + IP + "/school", User, Password);
			stmt = conn.createStatement();

			if (arr.size() == 0)
			{
				// error handling
				return null;
			}

			sql = "INSERT INTO " + arr.get(0);
			if (arr.size() >= 4)
			{
				sql += " (";
				for (int i = 1; i < arr.size(); i++)
				{
					if (arr.get(i).equals("values"))
					{
						index = i + 1;
						break;
					}
					else
					{
						sql += arr.get(i) + ", ";
					}
				}
				if (sql.endsWith(", "))
				{
					sql = sql.substring(0, sql.length() - 2);
					sql += ")";
				}
				sql += " VALUES (";
				for (int i = index; i < arr.size(); i++)
				{
					sql += "\"" + arr.get(i) + "\", ";
				}
				if (sql.endsWith(", "))
				{
					sql = sql.substring(0, sql.length() - 2);
					sql += ")";
				}
			}
			sql += ";";
			System.out.println("\nSQL: " + sql + "\n");
			int rs = stmt.executeUpdate(sql);
			answer.add("" + rs);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		return answer;
	}

	/**
	 * client connected
	 * @param client
	 */
	protected void clientConnected(ConnectionToClient client)
	{
		System.out.println("Client " + client.getId() + " connected, " + getNumberOfClients() + " clients are online");
	}

	/*
	 * public static void main(String[] args) throws IOException { int port = DEFAULT_PORT;
	 * 
	 * SchoolServer sv = new SchoolServer(port); try { sv.listen(); } catch (Exception ex) { System.out.println("ERROR - Could not listen for clients!"); } }
	 */
}
