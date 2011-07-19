/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.csvtosql;

import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.AssertionFailedException;
import org.supercsv.io.CsvMapReader;
import org.supercsv.prefs.CsvPreference;

/**
 * 
 * @author Balaji Ambresh Rajkumar
 * @author Mohsen Vakilian
 * 
 */
public class MapPersister {
	private static final int VARCHAR_SIZE= 100;

	Connection connection;

	private Statement statement;

//mysql
//	private static final char QUOTE= '\`';

	private static final char QUOTE= '\"';

	String dbName= "CodingSpectator";

	String tableName= "AllData";

//	public MapPersister(String server, String account, String password)
//			throws ClassNotFoundException, SQLException {
//		Class.forName("com.mysql.jdbc.Driver");
//		connection= DriverManager.getConnection("jdbc:mysql://" + server,
//				account, password);
//		statement= connection.createStatement();
//	}

	public MapPersister(String server, String account, String password)
			throws ClassNotFoundException, SQLException {
		// Load the HSQL Database Engine JDBC driver
		// hsqldb.jar should be in the class path or made part of the current jar
		Class.forName("org.hsqldb.jdbcDriver");
		// connect to the database.   This will load the db files and start the
		// database if it is not alread running.
		// db_file_name_prefix is used to open or create files that hold the state
		// of the db.
		// It can contain directory names relative to the
		// current working directory
		connection= DriverManager.getConnection("jdbc:hsqldb:file:" + server, account, password);
		statement= connection.createStatement();
	}

	protected void createDB(String dbName) throws SQLException {
//mysql
//		statement.executeUpdate("DROP DATABASE IF EXISTS " + dbName);
//		statement.executeUpdate("CREATE DATABASE " + dbName);
//		statement.execute("USE " + dbName);
	}

	public void shutdown() throws SQLException {
		Statement st= connection.createStatement();

		// db writes out to files and performs clean shuts down
		// otherwise there will be an unclean shutdown
		// when program ends
		st.execute("SHUTDOWN");
		connection.close(); // if there are no other open connection
	}

	private String quoted(String name) {
		return QUOTE + name + QUOTE;
	}

	// Order refactorings by their invocations.
	public void answer1() throws SQLException {
		int count;
		String id;

		//mysql
//		statement.execute("USE " + dbName);
		String query= String.format("SELECT COUNT(%s), %s from %s WHERE LENGTH(%s) != 0 AND %s != %s GROUP BY %s ORDER BY COUNT(%s) DESC;", quoted("id"), quoted("id"), quoted(tableName),
				quoted("id"), quoted("refactoring kind"), "'ECLIPSE'", quoted("id"), quoted("id"));
		ResultSet executeQuery= executeQuery(query);
		while (executeQuery.next()) {

			id= executeQuery.getString(2);

			count= executeQuery.getInt(1);
			System.out.println(id + " | " + count);
		}
	}

	// Order refactorings by their performed or cancelled.
	public void answer2() throws SQLException {
		int count;
		String id;

		//mysql
//		statement.execute("USE " + dbName);
		String query= String
				.format("SELECT COUNT(%s), %s from %s  WHERE LENGTH(%s) != 0 GROUP BY %s ORDER BY COUNT(%s) DESC;",
						quoted("refactoring kind"), quoted("refactoring kind"), quoted(tableName), quoted("refactoring kind"), quoted("refactoring kind"), quoted("refactoring kind"),
						quoted("refactoring kind"), quoted("refactoring kind"));
		ResultSet executeQuery= executeQuery(query);
		while (executeQuery.next()) {

			id= executeQuery.getString(2);

			count= executeQuery.getInt(1);
			System.out.println(id + " | " + count);
		}
	}

	// Order refactorings by their performed or cancelled.
	public void answer3() throws SQLException {
		String id;

		//mysql
//		statement.execute("USE " + dbName);
		String query= String.format("SELECT count(%s) FROM %s WHERE %s = 'true';", quoted("id"), quoted(tableName), quoted("invoked-by-quickassist"));
		ResultSet executeQuery= executeQuery(query);
		while (executeQuery.next()) {
			id= executeQuery.getString(1);
			System.out.println(id);
		}
	}

	public void loadCsvToMySql(String filePath) throws IOException, SQLException {

		createDB(dbName);
		CsvMapReader reader= new CsvMapReader(new FileReader(filePath), CsvPreference.EXCEL_PREFERENCE);
		String[] csvHeader= reader.getCSVHeader(true);

		List<String> toRemove= Arrays.asList("code-snippet", "selection-text", "comment");

		List<String> columnHeaders= Arrays.asList(csvHeader);
		List<String> filteredColumnHeaders= filterUnnecessaryColumns(columnHeaders, toRemove);
		List<String> nulledColumnHeaders= nullUnnecessaryColumnds(columnHeaders, toRemove);

		if (!(nulledColumnHeaders.size() == columnHeaders.size()))
			throw new AssertionFailedException("Nulled column does not have same number of elements as original header");

		createTable(tableName, filteredColumnHeaders);

		connection.setAutoCommit(false);

		StringBuilder insertString= new StringBuilder("INSERT INTO " + QUOTE + tableName + QUOTE + " values (");
		for (int i= 1; i <= filteredColumnHeaders.size(); i++) {
			insertString.append("?");
			if (i < filteredColumnHeaders.size()) {
				insertString.append(",");
			}
		}
		insertString.append(")");
		PreparedStatement preparedStatement= connection.prepareStatement(insertString.toString());


		Map<String, String> row;
		int progress= 0;
		while ((row= reader.read(nulledColumnHeaders.toArray(new String[] {}))) != null) {
			int index= 1;
			for (String key : filteredColumnHeaders) {
				String value= row.get(key);
				if (value.length() > VARCHAR_SIZE) {
					value= value.substring(0, VARCHAR_SIZE);
					System.err.println("\n>>>Truncated value at: " + key + " is: " + value + " and exceeds HSQLDB data capacity!");
				}

				preparedStatement.setString(index, value);
				index++;
			}
			preparedStatement.execute();

			if (progress++ % 100 == 0) {
				System.out.print('>');
			}

		}

		connection.commit();
		reader.close();
	}

	private List<String> nullUnnecessaryColumnds(List<String> columnHeaders, List<String> toRemove) {
		ArrayList<String> copy= new ArrayList<String>();
		for (String string : columnHeaders) {
			if (toRemove.contains(string))
				copy.add(null);
			else
				copy.add(string);
		}
		return copy;
	}

	private ArrayList<String> filterUnnecessaryColumns(List<String> columnHeaders, List<String> toRemove) {
		ArrayList<String> copy= new ArrayList<String>(columnHeaders);
		copy.removeAll(toRemove);
		return copy;
	}

	protected void createTable(String tableName, List<String> fields) throws SQLException {

		statement.execute("DROP TABLE " + tableName + " IF EXISTS");

		StringBuilder command= new StringBuilder("CREATE TABLE ");
		command.append(QUOTE).append(tableName).append(QUOTE).append('(');
		for (String field : fields) {
			command.append(defineColumnNameAndType(field)).append(',');
		}
		int length= command.length();
		command.replace(length - 1, length, ")");
		System.out.println("command is: " + command);
		statement.executeUpdate(command.toString());
	}

	protected String defineColumnNameAndType(String field) {
		StringBuilder fieldDefinition= new StringBuilder();

		fieldDefinition.append(QUOTE).append(field).append(QUOTE).append(' ');
		if (field.contains("TIMESTAMP")) {
			fieldDefinition.append("varchar(20)");
		} else {
			fieldDefinition.append("varchar(" + VARCHAR_SIZE + ")");
			//mysql
			//fieldDefinition.append("text");
		}
		return fieldDefinition.toString();
	}

	public ResultSet executeQuery(String query) throws SQLException {
		return statement.executeQuery(query);
	}
}
