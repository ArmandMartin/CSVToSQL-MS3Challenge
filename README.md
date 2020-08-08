# CSVToSQL-MS3Challenge

## Summary
This is a command line application that takes a specified CSV file, and converts it into a SQL Database table.

## How to use it
When you download or clone the repository. Inside the the "CSVToSQL-MS3Challenge" folder run the following Maven command:<br />

<code>mvn clean package assembly:single</code><br />

After that command has been run navigate to "CSVToSQL-MS3Challenge/target". In this folder you will see two different jar files. Use the one called "csvtosql-0.0.1-SNAPSHOT-jar-with-dependencies.jar". To run it in the command line use the following argument layout:

<code>java -jar .\csvtosql-0.0.1-SNAPSHOT-jar-with-dependencies.jar &lt;CSV-DIRECTORY&gt; &lt;TARGET-DIRECTORY&gt; &lt;FILE-NAME&gt; &lt;TABLE-NAME&gt;</code><br />


## Approach
The application reads through the CSV rows and inserts them into a created Database table if the meet the parameters established by the header row. If any rows are found to be invalid when insertion is attempted they will instead be written to a new CSV file.


**conversion(String csvPath, String deliverPath, String fileName, String tableName)**<br />

Using the specified CSV file path. Conversion inserts all valid rows that match up with the CSV header
row into a data base table whose name has been specified. Rows that do not match up
are sent to a new CSV file whose name is equivalent to the specified table name + "-bad.csv". Created files
are made within the specified deliverPath directory. Then prints out the number of rows
successful, failed, and received.

**Auxiliary functions:**<br />
These functions contain responsibilities that I felt should be separate from **conversion(...)**:<br />
<ul>
	<li>createDBConnection(String path, String fileName)</li>
	<li>closeDBConnection()</li>
	<li>setCsvReader(String path)</li>
	<li>closeCsvReader()</li>
	<li>createChain(String[] items)**</li>
	<li>createTable(String[] headers, String tableName)</li>
	<li>tableInsert(String[] data, String tableName)</li>
</ul>

**Assumptions:**<br />
<ul>
	<li>First row of CSV represents the column heaader's</li>
	<li>Database is being created by application</li>
	<li>CSV of bad rows is being created by application</li>
</ul>

**Libraries Used:**<br />
**SQLITE-JDBC**: [https://mvnrepository.com/artifact/org.xerial/sqlite-jdbc](https://mvnrepository.com/artifact/org.xerial/sqlite-jdbc) <br/>
**OPENCSV**: [https://mvnrepository.com/artifact/com.opencsv/opencsv](https://mvnrepository.com/artifact/com.opencsv/opencsv) <br/>

