import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import net.sourceforge.schemaspy.model.xml.SchemaMeta;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import schemacrawler.crawl.SchemaCrawler;
import schemacrawler.schemacrawler.SchemaCrawlerOptions;
import schemacrawler.tools.text.schema.SchemaDotFormatter;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;




/**
 * Die Klasse Prototyp
 * -> Auslesen mit syso von Metadata mit JDBC-API
 *
 * @author Gradonski Janusz , Marc Schwenzner
 *
 */
public class Prototyp {
	/*
	 * Die Connection-Daten
	 */
	private static String
		// Default-Einstellungen für Anus PC
			  username = "root",
			  password = "",
			  hostname = "localhost",
			  database = "premiere",
			  direction = "ASC",
			  sqlbefehl = "SELECT * FROM film;"
			  ;

	private static File outputf;
	private static Options options = new Options();
	private static SchemaCrawler schemacraw;
public static void main(String[] args) {



	/*
	 * Die Argumentoptionen
	 *
	 */

	options.addOption("h", "host", true,
			"Hostname des DBMS. Standard: localhost");
	options.addOption(
			"u",
			"user",
			true,
			"Benutzername. Standard: Benutzername des im Betriebssystem angemeldeten Benutzers");
	options.addOption(
			"p",
			"password",
			true,
			"Passwort. Alternativ kann ein Passwortprompt angezeigt werden. Standard: keins");
	options.addOption("d", "database", true, "Name der Datenbank");
	options.addOption("b", "befehl", true, "Befehl der an die Datenbank gesendet wird.");


	// Die Hilfe wird hier Automatisch generiert
	HelpFormatter formatter = new HelpFormatter();
	BasicParser parser = new BasicParser();


	CommandLine cl;
	try {
		cl = parser.parse(options, args);

	/*
	 * Option h speichert die Eingabe in die Variable hostname und ist
	 * zuständig so wie der Name schon sagt für den Hostname
	 */
	if (cl.hasOption('h')) {
		hostname = cl.getOptionValue("h");

	}

	/*
	 * Die Option u speichert die Eingabe in die Variable username.
	 */
	if (cl.hasOption('u')) {
		username = cl.getOptionValue("u");
	}

	/*
	 * Die Option p speichert die Eingabe in die Variable password.
	 */
	if (cl.hasOption('p')) {
		password = cl.getOptionValue("p");
	}

	/*
	 * Die Option d speichert die Eingabe in die Variable database.
	 */
	if (cl.hasOption('d')) {
		database = cl.getOptionValue("d");
	}

	/*
	 * Die Option b speichert die Eingabe in die Variable Befehl.
	 * Die Variable Befehl dient dazu um Das SQL-Befehl an die Datenbank zu feuern.
	 */
	if (cl.hasOption('b')) {
		sqlbefehl = cl.getOptionValue("b");
	}
	} catch (ParseException e) {
		System.out.println("Unexpected exception: " + e.getMessage());
	} catch (Exception e) {
		System.out.println("General exception: " + e.getMessage());
	}

  	// Database Connection
   	MysqlDataSource mds = new MysqlDataSource();
   	mds.setServerName(hostname);
   	mds.setDatabaseName(database);
   	mds.setUser(username);
   	mds.setPassword(password);


	try {


		Connection co = mds.getConnection();
		Statement stat = co.createStatement();

		System.out.println("---Connected---");
		System.out.println("Welcome "+username);

		ResultSet result = stat.executeQuery(sqlbefehl);
		ResultSetMetaData meta = result.getMetaData();







//		 System.out.println("GThe columns in the table are: ");
//		    System.out.println("STable: " + meta.getTableName(1));
//		    for  (int i = 1; i<= meta.getColumnCount(); i++){
//		      System.out.println("Column " +i  + " "+ meta.getColumnName(i));
//
//		    }

		// Auslesen von Metadaten und mittels syso ausgeben.
	      for( int i = 1; i <= meta.getColumnCount(); i++ ){
	         System.out.print( meta.getColumnLabel(i) + " " ) ;
	         System.out.println();
	      }

	      // LDurch resultset Loopen und ausgeben.
//	      while( result.next() )
//	         {
//	          for( int i = 1; i <= meta.getColumnCount(); i++ )
//	             System.out.print( result.getString(i) + " " ) ;
//	          System.out.println() ;
//	         }




			try {
				outputf = new File("output.txt");
				if (outputf.exists() == false) {
					outputf.createNewFile();
				}

				PrintWriter out = new PrintWriter(new FileWriter("output.txt", true));

				// Auslesen von Metadaten und mittels syso ausgeben.
				 out.append("-----------------------------");
			      for( int i = 1; i <= meta.getColumnCount(); i++ ){

			    	  out.append( meta.getColumnLabel(i) + " " ) ;
			         out.append("\n");
			      }

				while ( result.next()) {
					 for( int i = 1; i <= meta.getColumnCount(); i++ ){
						  System.out.print( result.getString(i) + " " ) ;
						  System.out.println() ;

						out.append( result.getString(i) + " " ) ;
						//Schreibt die Rows in die Datei
					 out.append("\n");

					 }

				}
				// Schließt den PrintWriter
				out.close();

			} catch (Exception e) {
				System.out.println("Unexpected exception: "
						+ e.getMessage());
			}
			System.out.println("Exported in file: output.txt");




	} catch (SQLException e) {
		System.out.println("Exception: " +e.getMessage());
	}


}
}
