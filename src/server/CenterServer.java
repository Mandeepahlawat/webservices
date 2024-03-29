/**
 * The package contains the server classes 
 */
package server;

import javax.jws.WebMethod;
import javax.jws.WebService;

/**
 * @author karan
 * 
 */

@WebService
public interface CenterServer {

	/**
	 * Creates a Teacher record with the information passed and returns True/False
	 * whether the operation was successful or not.
	 * 
	 * @param firstName First Name of the teacher
	 * @param lastName Last Name of the teacher
	 * @param address Teachers' address
	 * @param phone Contact number
	 * @param specialization Subject being taught by the teacher e.g. french, maths, etc
	 * @param location One of the three locations i.e. mtl, lvl, ddo
	 * @param managerId Current manager Id to log details in the server
	 * @return True/False whether the operation was successful or not
	 * @throws RemoteException
	 */
	@WebMethod
	public boolean createTRecord(String firstName, String lastName, String address, String phone, String specialization,
			String location, String managerId);

	/**
	 * Creates a Student record with the information passed and returns True/False
	 * whether the operation was successful or not.
	 * 
	 * @param firstName First Name of the student
	 * @param lastName Last Name of the student
	 * @param courseRegistered List of courses registered by the student i.e. maths/french/science
	 * @param status Specifies whether the student status is active/inactive
	 * @param statusDate Latest date when student became active/inactive
	 * @param managerId Current manager Id to log details in the server
	 * @return True/False whether the operation was successful or not
	 * @throws RemoteException
	 */
	@WebMethod
	public boolean createSRecord(String firstName, String lastName, String courseRegistered, String status,
			String statusDate, String managerId);

	/**
	 * Evaluates the total number of records(teacher and student) at all the centers.
	 * 
	 * @param managerId Current manager Id to log details in the server
	 * @return total number of records
	 * @throws RemoteException
	 */
	@WebMethod
	public String getRecordCounts(String managerId);

	/**
	 * Edits the record with the given recordId and replaces the new field with
	 * older one.
	 * 
	 * @param recordId Record Id of the teacher/student
	 * @param fieldName older field value
	 * @param newValue new value
	 * @param managerId Current manager Id to log details in the server
	 * @return True/False whether the operation was successful or not
	 * @throws RemoteException
	 */
	@WebMethod
	public String editRecord(String recordId, String fieldName, String newValue, String managerId);

	/**
	 * Transfers the record from a server to the target server database.
	 * 
	 * @param managerId Current manager Id to log details in the server 
	 * @param recordId Record Id of the teacher/student
	 * @param targetCenterName Name of the target server
	 * @return
	 */
	@WebMethod
	public boolean transferRecord(String managerId, String recordId, String targetCenterName);

}
