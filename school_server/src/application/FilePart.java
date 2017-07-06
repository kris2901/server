package application;

public class FilePart
{
	public String uploaderType;
	public String userId;
	public String courseId;
	public String assignmentName;
	public String fileName;
	public String description;
	public String title;
	public String date;
	public Double grade;
	public String comments;
	public Integer length;
	public Long totalLength;
	public byte[] buffer;
	public Integer order;

	public FilePart()
	{
	}
	
	/**
	 * FilePart(String uploaderType, String date, String title, String description, String userId, String courseId, Integer assignmentId, String fileName, Long totalLength, Integer length, byte[] buffer, Integer order, Double grade, String comments) function is a builder and contains the following values
	 * @param uploaderType
	 * @param date
	 * @param title
	 * @param description
	 * @param userId
	 * @param courseId
	 * @param assignmentId
	 * @param fileName
	 * @param totalLength
	 * @param length
	 * @param buffer
	 * @param order
	 * @param grade
	 * @param comments
	 */
	public FilePart(String uploaderType, String date, String title, String description, String userId, String courseId, String assignmentName, String fileName, Long totalLength, Integer length, byte[] buffer, Integer order, Double grade, String comments)
	{
		this.uploaderType = uploaderType;
		this.date = date;
		this.title = title;
		this.description = description;
		this.userId = userId;
		this.courseId = courseId;
		this.assignmentName = assignmentName;
		this.fileName = fileName;
		this.totalLength = totalLength;
		this.length = length;
		this.buffer = buffer;
		this.order = order;
		this.grade = grade;
		this.comments = comments;
	}
}
