import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import schemacrawler.crawl.SchemaCrawler;
import schemacrawler.schemacrawler.SchemaCrawlerOptions;
import schemacrawler.tools.text.schema.SchemaDotFormatter;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.*;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

/**
 * Die Klasse Prototyp
 * -> Auslesen mit syso von Metadata mit JDBC-API
 *
 * @author Gradonski Janusz , Marc Schwenzner
 *
 */
public class Prototypnew {
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
	private static ArrayList<Entity> exisitingTable = new ArrayList<Entity>();
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

		ResultSet result = stat.executeQuery("SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_TYPE = 'BASE TABLE' AND TABLE_SCHEMA='" + database + "';"); // Die Namen der Tabellen abfragen

		ResultSetMetaData meta = result.getMetaData();
		ArrayList<String> entities = new ArrayList<String>();

		//Mit der Schleife das gesamte Resultset durchlaufen und jedes Element in die eben erstellte Liste speichern
		while (result.next())
		{
			//Ergebnis gleich dem User zeigen
			entities.add(result.getString(1));
			//System.out.println("Gefundene Entitaet: " + result.getString(1));
		}

		//Das Resultset schließen
		result.close();

		if (entities.size() > 0) // Wenn Tabellen enthalten sind:
		{
			// Fuer alle Entitaeten die Attribute lesen.
			// Diese bekomme ich ueber die Namen der einzelnen Spalten
			for (int i = 0; i < entities.size(); i++)
			{
				ArrayList<String> pks = new ArrayList<String>(); // Primaerschluessel der Tabelle
				ArrayList<String> fks = new ArrayList<String>(); // Fremdschluessel der Tabelle
				ArrayList<String> fks_ = new ArrayList<String>();

				Entity einfo = new Entity(entities.get(i)); // Information ueber die Tabelle

				// Die Primaerschluessel der Tabelle bekommen:
				DatabaseMetaData dbm = co.getMetaData();
				ResultSet keys = dbm.getPrimaryKeys(null, null, einfo.name); //Primärschlüssel der Tabelle auslesen

				while (keys.next())
				{
					//Zu der Auflistung mit den gefundenen Primaerschluesseln den eben gefundenen Einfuegen
					pks.add(keys.getString("COLUMN_NAME"));
				}

				keys.close(); //ResultSet schließen (aufraeumen)
				ResultSet fKeys = dbm.getImportedKeys(co.getCatalog(), null, einfo.name); //Die Fremdschluessel lesen

				while (fKeys.next())
				{
					fks.add(fKeys.getString("FKCOLUMN_NAME"));
					fks_.add(fKeys.getString("PKTABLE_NAME") + "." + fKeys.getString("PKCOLUMN_NAME")); // Tabellenname.Schuesselname des Fremdschluessels (Zum Anzeigen von Beziehungen im ERD)
				}

				fKeys.close();

				// Abfrage machen, um die Attribue der Entitaet zu bekommen
				ResultSet rs = stat.executeQuery("SELECT * FROM " + entities.get(i));
				ResultSetMetaData rsm = rs.getMetaData();

				int cols = rsm.getColumnCount();

				//Für Jedes Attribut einer Entitaet
				for (int u = 1; u <= cols; u++)
				{
					String colname = rsm.getColumnLabel(u); //Den Namen des Attributes
					String coltype = rsm.getColumnTypeName(u); //Den Datentypen auslesen
					boolean isKey = false; //Ist ein Schlüssel

					//Die eben ausgelesenen Daten speichern
					Attribute ainfo = new Attribute(colname, coltype, false, isKey);

					//Herausfinden, ob das Untersuchte Attribut ein Schluessel ist
					for (int a = 0; a < pks.size(); a++)
					{
						//Taucht das Attribut in der Tabelle mit den Schluesseln auf, so ist das Attribut ein Schluessel
						if (pks.get(a).equals(colname))
						{
							ainfo.isKey = true; //Das Attribut zu einem Schluessel setzen
						}
					}

					// Auf Fremdschluessel pruefen
					for (int s = 0; s < fks.size(); s++)
					{
						if (fks.get(s).equals(colname))
						{
							ainfo.addForeignKey(fks_.get(s)); // Fremdschluessel Quelle einfuegen
						}
					}

					einfo.addAttribute(ainfo); //Der Entitaet das eben gefundene Attribut anfuegen (Die Entitaet mit dem Attribut verknuepfen)

					/**
					//Dem Benutzer ausgeben, was gefunden wurde:
					if (ainfo.isFKey)
						System.out.println("Gefundenes Attribut in Tabelle: " + einfo.name + ", mit dem Namen:" + colname + ", des Typs: " + coltype + ", ist Schluessel: " + ainfo.isKey + " mit der Referenz auf: " + ainfo.sup);
					else
						System.out.println("Gefundenes Attribut in Tabelle: " + einfo.name + ", mit dem Namen:" + colname + ", des Typs: " + coltype + ", ist Schluessel: " + ainfo.isKey);
				**/
				}

				// Die EntityInformation speichern, anstatt sie zu
				// verwerfen:
				exisitingTable.add(einfo);
			}
		}

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
