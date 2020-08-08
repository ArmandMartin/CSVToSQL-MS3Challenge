package com.armand.ms3challenge;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;

public class CsvToSql {
	/**
	 * Abbreviations: DB = database
	 */

	private static Connection conn = null;
	private static CSVReader csvReader = null;
	private static String headersChain;
	private static int columnNum = -1;
	private static int received = 0;
	private static int successful = 0;
	private static int failed = 0;

	public static void main(String args[]) {
		if (args.length != 4) {
			System.out.println("This application requires 4 arguemnts. " + args.length
					+ "/4 where found. Please try again using the following:\n"
					+ "arg 1: csv path, arg 2: directory path for db and \"bad\" csv file to be created, arg 3: db fileName, arg 4: db and csv table name.");
			System.exit(0);
		}
		// arg 0: csv path
		File csv = new File(args[0]);
		if (!csv.exists() && !csv.canRead() && !(args[0].endsWith(".csv"))) {
			System.out.println(".csv path specified either does not exist or can not be read.");
			System.exit(0);
		}
		// arg 1: directory path for db and "bad" csv file to be created
		File directory = new File(args[1]);
		if (!directory.exists() && !directory.isDirectory()) {
			System.out.println("Directory path specified either does not exist or can not be read or written to.");
			System.exit(0);
		}
		// arg 2: db fileName
		// arg 3: db and csv table name
		if(args[2].isBlank() || args[3].isBlank()) {
			System.out.println("Either the specified filename or table name was blank");
			System.exit(0);
		}

		conversion(args[0], args[1], args[2], args[3]);
		System.out.println("Conversion has been complete.");
	}

	/**
	 * Creates a Database to connect to using the specified directory path and
	 * filename
	 * 
	 * @param path
	 * @param fileName
	 */
	private static void createDBConnection(String path, String fileName) {
		String url = "jdbc:sqlite:" + path + "\\" + fileName + ".db";

		try {
			conn = DriverManager.getConnection(url);
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
	}

	/**
	 * Closes Database connection
	 */
	private static void closeDBConnection() {
		try {
			conn.close();
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
	}

	/**
	 * Sets the CSV Reader using the specified path
	 * 
	 * @param path
	 */
	private static void setCsvReader(String path) {
		try {
			csvReader = new CSVReader(new FileReader(path));
		} catch (FileNotFoundException e) {
			System.out.println(e.getMessage());
		}
	}

	/**
	 * Closes the CSV reader
	 */
	private static void closeCsvReader() {
		try {
			csvReader.close();
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}

	/**
	 * Sets up arguments for SQL from a String[] like so: <br>
	 * A,B,C,...,J
	 * 
	 * @param items {A,B,C,...,J}
	 * @return A,B,C,...,J
	 */
	private static String createChain(String[] items) {
		String chain = "";
		for (int i = 0; i < items.length; i++) {
			chain = chain + items[i];
			if (i + 1 < items.length) {// As long as the next index would return a value put add a comma to the string
				chain = chain + ",";
			}
		}
		return chain;
	}

	/**
	 * Creates a table using specified column headers and table name
	 * 
	 * @param headers
	 * @param tableName
	 */
	private static void createTable(String[] headers, String tableName) {
		// build prepared SQL statement
		String sql = "CREATE TABLE IF NOT EXISTS " + tableName + "(";
		sql = sql + createChain(headers) + ")";
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(sql);
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
	}

	/**
	 * Inserts items from a String[] into the specified table. <br>
	 * Returns false when the data passed is not valid and true when the data has been successfully inserted.
	 * 
	 * @param data
	 * @param tableName
	 * @return boolean
	 */
	private static boolean tableInsert(String[] data, String tableName) {
		// build prepared SQL statement
		String sql = "INSERT INTO " + tableName + "(";
		sql = sql + headersChain + ")";
		sql = sql + " VALUES(";

		for (int i = 0; i < data.length; i++) {
			if (data[i].isEmpty() || data.length != columnNum) {// validate data before entry and return false if invalid
				return false;
			}
			sql = sql + "?";
			if (i + 1 < data.length) {
				sql = sql + ",";
			}

		}
		sql = sql + ")";

		try {
			PreparedStatement pstmt = conn.prepareStatement(sql);

			for (int i = 1; i <= data.length; i++) {//set data elements for prepared statement
				pstmt.setString(i, data[i - 1]);
			}
			pstmt.executeUpdate();
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
		return true;
	}

	/**
	 * Using the specified CSV file path. Conversion inserts all valid rows that
	 * match up with the CSV header row into <br>
	 * a data base table whose name has been specified. Rows that do not match up
	 * are sent to a new CSV file whose <br>
	 * name is equivalent to the specified table name + "-bad.csv". Created files
	 * are made within the specified<br>
	 * deliverPath directory. Then prints out the number of rows successful, failed, and received.
	 * 
	 * @param csvPath
	 * @param deliverPath
	 * @param fileName
	 * @param tableName
	 */
	public static void conversion(String csvPath, String deliverPath, String fileName, String tableName) {
		// Establish DB connection and CSV reader
		createDBConnection(deliverPath, fileName);
		setCsvReader(csvPath);

		boolean success = true; // used to indicate that a row could not be inserted into a table
		String[] data = null; // holds rows taken from CSV file
		CSVWriter writer = null; // CSV writer to create CSV file of bad rows

		try {
			while ((data = csvReader.readNext()) != null) {// As long as CSV has rows to read keep feeding them into data and increment received
				received++;
				if (columnNum == -1) {// if columnNum == -1 then this is the first row read, and it will be used to model the DB table as the headers
					columnNum = data.length;
					createTable(data, tableName);
					headersChain = createChain(data);
				}
				success = tableInsert(data, tableName);

				if (!success) {//  write to CSV when insertion failure is detected and increment failed
					if (writer == null) {// set up the CSV writer if it has not been done yet
						try {
							writer = new CSVWriter(new FileWriter(deliverPath + fileName + "-bad.csv"));
						} catch (IOException e) {
							System.out.println(e.getMessage());
						}
					}
					failed++;
					writer.writeNext(data);
				} else {// increment successful when "success" == true
					successful++;
				}
			}

		} catch (IOException e) {
			System.out.println(e.getMessage());
		} catch (CsvValidationException e) {
			System.out.println(e.getMessage());
		}
		
		// Close out CSV writer, reader, and DB connection
		try {
			writer.close();
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
		closeCsvReader();
		closeDBConnection();
		
		// Print results
		System.out.println("Received rows: " + received);
		System.out.println("Succesful rows: " + successful);
		System.out.println("Failed rows: " + failed);
	}
}
