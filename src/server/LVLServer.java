package server;

import logger.LogManager;
import utility.Record;
import utility.Student;
import utility.Teacher;
import java.util.ArrayList;
import java.util.HashMap;

import javax.jws.WebService;
import javax.xml.ws.Endpoint;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

@WebService(endpointInterface = "server.CenterServer")

public class LVLServer extends Thread implements Runnable {

	public static HashMap<String, ArrayList<Record>> lvlDB = new HashMap<String, ArrayList<Record>>();
	private static HashMap<String, String> idToLastName = new HashMap<String, String>();
	private static int count = 0;
	private LogManager lvlLogger;
	static String location = "lvl";
	public static int LVLport = 2345;
	public static boolean LVLFlag;

	public LVLServer() throws Exception {
		super();
	}
	
	public void initializeLogger() {
		lvlLogger = new LogManager("lvl");
		lvlLogger.mLogger.setUseParentHandlers(true);
	}

	public boolean createTRecord(String firstName, String lastName, String address, String phone, String specialization,
			String location, String managerId) {
		Record objRecord = new Teacher(firstName, lastName, address, phone, specialization, location);

		// locking the server database for synchronized access
		synchronized (lvlDB) {

			// checking if the key already exists in hash map
			if (lvlDB.containsKey(lastName.substring(0, 1))) {
				lvlDB.get(lastName.substring(0, 1)).add(objRecord);
			} else {
				ArrayList<Record> alRecord = new ArrayList<Record>();
				alRecord.add(objRecord);
				lvlDB.put(lastName.substring(0, 1), alRecord);
			}

			idToLastName.put(objRecord.getRecordId(), lastName.substring(0, 1));

			count++;
		}

		// adding the operation to the log file
		lvlLogger.mLogger.info(managerId + " created Teacher record with values: " + objRecord + '\n');

		return true;
	}

	public boolean createSRecord(String firstName, String lastName, String courseRegistered, String status,
			String statusDate, String managerId) {

		Record objRecord = new Student(firstName, lastName, courseRegistered, status, statusDate);

		// locking the server database for synchronized access
		synchronized (lvlDB) {

			// checking if the key already exists in hash map
			if (lvlDB.containsKey(lastName.substring(0, 1))) {
				lvlDB.get(lastName.substring(0, 1)).add(objRecord);
			} else {
				ArrayList<Record> alRecord = new ArrayList<Record>();
				alRecord.add(objRecord);
				lvlDB.put(lastName.substring(0, 1), alRecord);
			}

			idToLastName.put(objRecord.getRecordId(), lastName.substring(0, 1));

			count++;
		}
		
		// adding the operation to the log file
		lvlLogger.mLogger.info(managerId + " created Student record with values: " + objRecord + '\n');

		return true;

	}

	public String getRecordCounts(String managerId) {

		String str = location + " " + count + "\n";

		DatagramSocket socket1 = null;
		DatagramSocket socket2 = null;
		byte[] message1 = location.getBytes();
		byte[] message2 = location.getBytes();

		try {
			// locking the server database for synchronized access
			synchronized (lvlDB) {

				lvlLogger.mLogger.info(managerId + " sent request for total record count" + '\n');
				socket1 = new DatagramSocket();
				socket2 = new DatagramSocket();
				InetAddress address = InetAddress.getByName("localhost");

				DatagramPacket request1 = new DatagramPacket(message1, message1.length, address, MTLServer.MTLport);
				socket1.send(request1);
				lvlLogger.mLogger.info(location + " sever sending request to mtl sever for total record count" + '\n');

				byte[] receive1 = new byte[1000];
				DatagramPacket reply1 = new DatagramPacket(receive1, receive1.length);
				socket1.receive(reply1);
				lvlLogger.mLogger
						.info("mtl server sent response to " + location + " sever for total record count " + '\n');

				str = str.concat(new String(reply1.getData()));
				str = str.trim();
				str = str.concat("\n");

				DatagramPacket request2 = new DatagramPacket(message2, message2.length, address, DDOServer.DDOport);
				socket2.send(request2);
				lvlLogger.mLogger.info(location + " sever sending request to ddo sever for total record count" + '\n');

				byte[] receive2 = new byte[1000];
				DatagramPacket reply2 = new DatagramPacket(receive2, receive2.length);
				socket2.receive(reply2);
				lvlLogger.mLogger
						.info("ddo server sent response to " + location + " sever for total record count " + '\n');

				str = str.concat(new String(reply2.getData()));
				str = str.trim();
				str = str.concat("\n");
			}
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			socket1.close();
			socket2.close();
		}

		lvlLogger.mLogger.info("Get record count query is used, total count is : \n" + str + '\n');
		return str;
	}

	public String editRecord(String recordId, String fieldName, String newValue, String managerId) {

		lvlLogger.mLogger.info(
				managerId + " sent request to edit Record with ID: " + recordId + " new value is: " + newValue + '\n');
		String key;

		if (idToLastName.containsKey(recordId))
			key = idToLastName.get(recordId);
		else {
			lvlLogger.mLogger.info("Record couldn't be updated as record value: " + recordId + " doesnt exist" + "\n");
			
			return "The given record id doesn't exist";
		}

		StringBuilder output = new StringBuilder();
		
		// locking the server database for synchronized access
		synchronized (lvlDB) {
			
			for (Record temp : lvlDB.get(key)) {
				String id = temp.getRecordId();
				if (id.equalsIgnoreCase(recordId)) {
					if (recordId.startsWith("ST")) {
						if (fieldName.equalsIgnoreCase("status")) {
							output.append(printMessage(((Student) temp).getStatus(), newValue));
							((Student) temp).setStatus(newValue);
							lvlLogger.mLogger.info("Record Updated, new value: " + ((Student) temp) + '\n');
						} else if (fieldName.equalsIgnoreCase("statusDate")) {
							output.append(printMessage(((Student) temp).getStatusDate(), newValue));
							((Student) temp).setStatusDate(newValue);
							lvlLogger.mLogger.info("Record Updated, new value: " + ((Student) temp) + '\n');
						} else if (fieldName.equalsIgnoreCase("courseRegistered")) {
							output.append(printMessage(((Student) temp).getCourseRegistered(), newValue));
							((Student) temp).setCourseRegistered(newValue);
							lvlLogger.mLogger.info("Record Updated, new value: " + ((Student) temp) + '\n');
						} else {
							return "The given field name is invalid for student record";
						}
					} else if (recordId.startsWith("TR")) {
						if (fieldName.equalsIgnoreCase("address")) {
							output.append(printMessage(((Teacher) temp).getAddress(), newValue));
							((Teacher) temp).setAddress(newValue);
							lvlLogger.mLogger.info("Record Updated, new value: " + ((Teacher) temp) + '\n');
						} else if (fieldName.equalsIgnoreCase("phone")) {
							output.append(printMessage(((Teacher) temp).getPhone(), newValue));
							((Teacher) temp).setPhone(newValue);
							lvlLogger.mLogger.info("Record Updated, new value: " + ((Teacher) temp) + '\n');
						} else if (fieldName.equalsIgnoreCase("location")) {
							output.append(printMessage(((Teacher) temp).getLocation(), newValue));
							((Teacher) temp).setLocation(newValue);
							lvlLogger.mLogger.info("Record Updated, new value: " + ((Teacher) temp) + '\n');
						} else {
							return "The given field name is invalid for teacher record";
						}
					}
				}
			}			
		}
		
		return output.toString();

	}

	public String printMessage(String str1, String str2) {
		return "Old Value:" + str1 + " " + " New value updated:" + str2;
	}

	public boolean transferRecord(String managerId, String recordId, String targetCenterName) {
		
		lvlLogger.mLogger.info(managerId + " sent request to transfer Record with ID: " + recordId + " to center: "
				+ targetCenterName + '\n');
		String key;
		if (idToLastName.containsKey(recordId))
			key = idToLastName.get(recordId);
		else {
			lvlLogger.mLogger.info("Record couldn't be updated as record value: " + recordId + " doesnt exist" + "\n");
			return false;
		}

		// locking the server database for synchronized access
		synchronized (lvlDB) {

			for (int i = 0; i < lvlDB.get(key).size(); i++) {

				Record temp = lvlDB.get(key).get(i);
				String id = temp.getRecordId();

				if (id.equalsIgnoreCase(recordId)) {

					DatagramSocket socket = null;
					String transferContent = "";

					if (recordId.startsWith("ST")) {
						String courses = ((Student) temp).getCourseRegistered();
						transferContent = id + "|" + ((Student) temp).getFirstName() + "|"
								+ ((Student) temp).getLastName() + "|" + courses + "|" + ((Student) temp).getStatus()
								+ "|" + ((Student) temp).getStatusDate();
					} else if (recordId.startsWith("TR")) {
						transferContent = id + "|" + ((Teacher) temp).getFirstName() + "|"
								+ ((Teacher) temp).getLastName() + "|" + ((Teacher) temp).getAddress() + "|"
								+ ((Teacher) temp).getPhone() + "|" + ((Teacher) temp).getSpecialization() + "|"
								+ ((Teacher) temp).getLocation();
					}

					byte[] message = transferContent.getBytes();

					try {
						lvlLogger.mLogger.info(managerId + " sent request for record transfer" + '\n');
						socket = new DatagramSocket();
						InetAddress address = InetAddress.getByName("localhost");

						DatagramPacket request = new DatagramPacket(message, message.length, address,
								targetCenterName.equalsIgnoreCase("ddo") ? DDOServer.DDOport : MTLServer.MTLport);
						socket.send(request);
						lvlLogger.mLogger.info(location + " sever sending request to " + targetCenterName
								+ " sever for record transfer" + '\n');

						byte[] receive = new byte[1000];
						DatagramPacket reply = new DatagramPacket(receive, receive.length);
						socket.receive(reply);
						lvlLogger.mLogger.info(targetCenterName + " server sent response to " + location
								+ " sever regarding the record transfer " + '\n');

						String replyStr = new String(reply.getData()).trim();

						if (replyStr.equals("success")) {
							lvlDB.get(key).remove(i);
							count--;
							idToLastName.remove(recordId);
							// break the loop if there is a single record in the ArrayList for a key
							if (lvlDB.get(key).isEmpty()) {
								lvlDB.remove(key);
								break;
							}
						} else {
							return false;
						}

					} catch (SocketException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					} finally {
						socket.close();
					}
				}
			}
		}

		return true;
	}

	public static void main(String[] args) throws Exception {
		
		LVLServer lvl = new LVLServer();
		lvl.initializeLogger();
		
		LVLFlag = true;
		ArrayList<Record> alRecordInitialLVL = new ArrayList<Record>();
		Record objRecord = new Student("lvlFirstName", "lvlLastName", "maths", "active", "11/11/2015");
		alRecordInitialLVL.add(objRecord);
		lvlDB.put("l", alRecordInitialLVL);
		idToLastName.put("ST101", "l");
		LVLFlag = false;
		count++;
		System.out.println("Laval Server is started");
		System.out.println("Dummy Student record with id - ST101 is created for testing");
		lvl.lvlLogger.mLogger.info("Dummy Student record is created for testing with values: "+objRecord);
		Thread threadSocket = new Thread(lvl);
		threadSocket.start();

		try {
			Endpoint endPoint = Endpoint.publish("http://localhost:2345/ws/Laval", lvl);
			if (endPoint.isPublished()) {
				System.out.println("Laval server published successfully");
			}
			System.out.println("Laval Server ready and waiting ...");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		DatagramSocket socket = null;

		try {
			socket = new DatagramSocket(LVLport);
			byte[] get = new byte[1000];
			byte[] send = new byte[1000];

			while (true) {
				DatagramPacket request = new DatagramPacket(get, get.length);
				socket.receive(request);

				String requestContent = new String(request.getData()).trim();
				String[] requestChunks = requestContent.split("\\|");

				if (requestContent.startsWith("ST") || requestContent.startsWith("TR")) {

					String recordId;
					String firstName;
					String lastName;
					Record objRecord;
					
					switch (requestContent.substring(0, 2)) {
					case "ST":
						recordId = requestChunks[0];
						firstName = requestChunks[1];
						lastName = requestChunks[2];
						String courseRegistered = requestChunks[3];
						String status = requestChunks[4];
						String statusDate = requestChunks[5];
						objRecord = new Student(firstName, lastName, courseRegistered, status, statusDate);

						// locking the server database for synchronized access
						synchronized (lvlDB) {

							// checking if the key already exists in hash map
							if (lvlDB.containsKey(lastName.substring(0, 1))) {
								lvlDB.get(lastName.substring(0, 1)).add(objRecord);
							} else {
								ArrayList<Record> alRecord = new ArrayList<Record>();
								alRecord.add(objRecord);
								lvlDB.put(lastName.substring(0, 1), alRecord);
							}

							// use the old recordID
							objRecord.setRecordId(recordId);
							idToLastName.put(objRecord.getRecordId(), lastName.substring(0, 1));

							count++;
						}
						
						// adding the operation to the log file
						lvlLogger.mLogger.info("Student record transfered with values: " + objRecord + '\n');

						break;

					case "TR":
						recordId = requestChunks[0];
						firstName = requestChunks[1];
						lastName = requestChunks[2];
						String address = requestChunks[3];
						String phone = requestChunks[4];
						String specialization = requestChunks[5];
						String location = requestChunks[6];
						objRecord = new Teacher(firstName, lastName, address, phone, specialization, location);

						// locking the server database for synchronized access
						synchronized (lvlDB) {

							// checking if the key already exists in hash map
							if (lvlDB.containsKey(lastName.substring(0, 1))) {
								lvlDB.get(lastName.substring(0, 1)).add(objRecord);
							} else {
								ArrayList<Record> alRecord = new ArrayList<Record>();
								alRecord.add(objRecord);
								lvlDB.put(lastName.substring(0, 1), alRecord);
							}

							// use the old recordID
							objRecord.setRecordId(recordId);
							idToLastName.put(objRecord.getRecordId(), lastName.substring(0, 1));

							count++;
						}
						
						// adding the operation to the log file
						lvlLogger.mLogger.info("Teacher record transfered with values: " + objRecord + '\n');
						break;
					}

					send = ("success").getBytes();
					DatagramPacket reply = new DatagramPacket(send, send.length, request.getAddress(),
							request.getPort());
					socket.send(reply);
				} else {
					lvlLogger.mLogger.info(" Laval server received request to send '\n'");
					send = (location + " " + count).getBytes();
					DatagramPacket reply = new DatagramPacket(send, send.length, request.getAddress(),
							request.getPort());
					socket.send(reply);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			socket.close();
		}
	}
}
