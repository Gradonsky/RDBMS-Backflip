package Rueckwaertssalto;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import com.mysql.jdbc.Statement;

/**
 * The class ExportDataToRM connects to an database and either generates an ERD (using the programm Graphviz) or a RM
 * 
 * @author Hannah Siegel
 * @version 2013-12-28
 */
public class ExportDataToRM {
	//Database information
	String m_graphvizpath="", m_errors = "",m_password = "", m_server = "", m_user = "", m_databasename = "", m_output="er";
	//Tables
	Hashtable<String, Table> m_tables = new Hashtable<String, Table>();
	
	/**
	 * The main function gets the homepath and starts the program 
	 * 
	 * @param arg - Program parameters
	 */
	public static void main(String arg[]){
		//String homepath= System.getProperty("user.home");
		new ExportDataToRM(arg);
		//C:\Users\Hannah\release\bin\neato.exe -Tpdf C:\Users\Hannah\ERD_%4.dot > x.pdf
	}
	
	/**
	 * The constructor of the ExportDataToRM class.
	 * Takes care of all the function calls.
	 * 
	 * @param arg - Program parameters 
	 * @param filename - user's homepath 
	 */
	public ExportDataToRM(String arg[]) {
		//checking input
		checkInput(arg);
		
		//opening database connection
		Connection con=databaseConnection();
		
		//getting the data needed to generate either RM or ERD
		getMetaData(con);

		//making ERD
		if(m_output.equals("er"))
			makeERD(m_graphvizpath);
		
		//making RM
		else if(m_output.equals("rm"))
			makeRM(m_graphvizpath);
		//write errors
		System.out.println(m_errors);

		if(m_errors.equals("")&&m_output.equals("rm")){
			System.out.println("Finish. :)");
		}
		try {
			//Runtime.getRuntime().exec(m_graphvizpath+"\\neato.exe -Tpdf "+m_graphvizpath+"\\ERD_"+m_databasename+".dot -o x.pdf");
			//neato.exe -Tpdf -ox.pdf C:\Users\Hannah\ERD_philharmoniker.dot
			//neato.exe -Tpdf -o"+m_databasename+".pdf "m_graphvizpath"+\ERD_"+m_databasename+".dot
			if(m_output.equals("er")){
				Runtime.getRuntime().exec(m_graphvizpath+"\\neato.exe -Tpdf -o\""+m_graphvizpath+"\\"+m_databasename+".pdf\" "+m_graphvizpath+"\\ERD_"+m_databasename+".dot");
				System.out.println("Das generierte PDF findet sich unter: "+m_graphvizpath+"\\"+m_databasename+".pdf");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * The function checkInput checks the program parameters and writes them into the database information variables
	 *
	 * @param arg program parameters that should be checked
	 */
	private void checkInput(String arg[]) {
	
		CheckArg inp = new CheckArg(arg);
		m_server=inp.getServer();
		m_database=inp.getServer();
		m_password=inp.getServer();
		m_user=inp.getServer();
		m_graphvizpath=inp.getServer();
		m_output=inp.getServer();
	

	}

	/**
	 * The function databaseConnection opens a new Connection to a database.
	 */
	private Connection databaseConnection() {
		Connection con=null;
		try {
			// DataSource Class
			com.mysql.jdbc.jdbc2.optional.MysqlDataSource d = new com.mysql.jdbc.jdbc2.optional.MysqlDataSource();
			d.setServerName(m_server);
			d.setDatabaseName(m_databasename);
			d.setUser(m_user);
			d.setPassword(m_password);
			con = d.getConnection();
			} catch (Exception e) {
				
			// if there was an error - printing it into the error messages.
			String error = e.toString();
			if (error != null && error.indexOf(':') >= 0) {
				if (error.contains(";"))
					error = error.substring(error.indexOf(':') + 1,error.indexOf(';')).trim();
				else if (error.contains("("))
					error = error.substring(error.indexOf(':') + 1,error.indexOf('(')).trim();
				else 
					error = error.substring(error.indexOf(':') + 1).trim();
			}
			m_errors = (error);
		}
		return con;

	}

	/**
	 * The method getMetaData fetches the Data of a database into the Tables Hashtable, using just! JDBC-Statements
	 * 
	 * @param con Database Connection
	 */
	private void getMetaData(Connection con) {
		try {

			// getting the statement for the jdbc
			Statement st = (Statement) con.createStatement();
			DatabaseMetaData dbm = con.getMetaData();

			// get the tables
			ResultSet tables = dbm.getTables(m_databasename, null, null, null);
			while (tables.next()) {
				String table_name = tables.getString("TABLE_NAME");
				Table t = new Table(table_name);
				
				// fetch each Attribute
				ResultSet rs_attributes = dbm.getColumns(m_databasename, null, table_name, null);
				while (rs_attributes.next()) {
					t.addAttribute(rs_attributes.getString("COLUMN_NAME"));
				}
				rs_attributes.close();

				// get the primary keys
				Hashtable <String, Attribute> temp_attributes = t.getAttributes();
				ResultSet rs_primaryKeys = dbm.getPrimaryKeys(m_databasename,
						null, table_name);
				while (rs_primaryKeys.next()) {
					temp_attributes.get(rs_primaryKeys.getString("COLUMN_NAME")).setPrimary(
							true);
				}
				rs_primaryKeys.close();
				
				//putting this table into the hashtable
				m_tables.put(table_name, t);
			}

			//getting the foreign keys
			tables = dbm.getTables(m_databasename, null, null, null);
			while (tables.next()) {
				String table_name = tables.getString("TABLE_NAME");
				ResultSet rs_ForeignKeys = dbm.getExportedKeys(m_databasename,
						null, table_name);
				while (rs_ForeignKeys.next()) {
					
					//putting the foreign key information into the table
					m_tables.get(rs_ForeignKeys.getString("FKTABLE_NAME"))
							.setForeignAttribute(
									rs_ForeignKeys.getString("FKCOLUMN_NAME"),
									rs_ForeignKeys.getString("PKTABLE_NAME"),
									rs_ForeignKeys.getString("PKCOLUMN_NAME"));
				}
			}

			st.close();
			tables.close();
			con.close();

		} catch (Exception e) {
			
			// if there was an error - printing it into the error messages.
			String error = e.toString();
			if (error != null && error.indexOf(':') >= 0) {
				if (error.contains(";"))
					error = error.substring(error.indexOf(':') + 1,error.indexOf(';')).trim();
				else if (error.contains("("))
					error = error.substring(error.indexOf(':') + 1,error.indexOf('(')).trim();
				else 
					error = error.substring(error.indexOf(':') + 1).trim();
			}
			m_errors = (error);
		}
	}

	/**
	 * The method makeERD generates the .dot file needed by the Graphviz program
	 * 
	 * @param filename homepath of the user
	 */
	private void makeERD(String filename) {
		
		// opening file
		Writer writer = null;
		try {
			
			//opening writer
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename + "\\ERD_" + m_databasename+ ".dot"), "utf-8"));
			String s = "";
			
			//generating dot file
			s += "digraph G { overlap=false";
			
			Vector<String> stringvector = new Vector<String>();
			
			//adding every table
			for (Enumeration<Table> e = m_tables.elements(); e.hasMoreElements();) {
				Table t_tamp = (e.nextElement());
				s += "\""
						+ t_tamp.getName()
						+ "\"[label=<<TABLE BORDER=\"0\" CELLBORDER=\"0\" CELLSPACING=\"0\" CELLPADDING=\"4\"><TR><TD><B>"
						+ t_tamp.getName() + "</B></TD></TR>";

				Hashtable<String, Attribute> x2 = t_tamp.getAttributes();
				
				//adding the primary keys first
				for (Enumeration<Attribute> e2 = x2.elements(); e2.hasMoreElements();) {
					Attribute m = e2.nextElement();
					if (m.isPrimary()) {
						s += "<TR><TD><U>" + m.getAttributeName()
								+ "</U></TD></TR>";
					}
				}
				
				//adding the foreign keys
				for (Enumeration<Attribute> e2 = x2.elements(); e2.hasMoreElements();) {
					Attribute m = e2.nextElement();
					if (m.getFatt() != null) {
						s += "<TR><TD><I>" + m.getAttributeName()
								+ "</I></TD></TR>";
					}
				}
				
				//adding the normal keys
				for (Enumeration<Attribute> e2 = x2.elements(); e2.hasMoreElements();) {
					Attribute m = e2.nextElement();
					if (m.getFatt() == null && m.isPrimary() == false) {
						s += "<TR><TD>" + m.getAttributeName() + "</TD></TR>";
					}
				}
				s += "</TABLE>> ,shape=box];";

				//adding the relations
				for (Enumeration<Attribute> e2 = x2.elements(); e2.hasMoreElements();) {
					Attribute m = e2.nextElement();
					if (m.getFtab() != null) {
						//stringvector.addElement(("\"" + t_tamp.getName()+ "\" -> \"" + m.getFtab() + "\" [arrowhead =\"none\" taillabel=\"(0,1)\" , headlabel=\"(0,*)\"];"));
						stringvector.addElement(t_tamp.getName()+"-"+ m.getFtab());
					}
				}
			}
			
			//searching for multiple relations -  deleting multiple relations
			for (int i = 0; i < stringvector.size(); ++i) {
				for (int j = 0; j < stringvector.size(); ++j) {
					if (stringvector.elementAt(i).equals(stringvector.elementAt(j))&& (i != j)) 
						stringvector.remove(j);
				}
			}
			
			//adding the relations to the DOT file
			for(int i=0; i<stringvector.size();++i){
				String temp[] = stringvector.elementAt(i).split("-");	 
				s+= "\""+"rel"+i+"\" [label=\""+"rel"+i+"\" shape=diamond];";
				s+="\""+temp[0]+"\" -> \""+"rel"+i+"\" [arrowhead =\"none\" taillabel=\"(0,1)\"];";
				s+="\""+temp[1]+"\" -> \""+"rel"+i+"\" [arrowhead =\"none\" taillabel=\"(0,*)\"];";
			} 
			
			s += "}";
			writer.write(s);
			writer.close();
			
		} catch (Exception e) {
			// if there was an error - printing it into the error messages.
			String error = e.toString();
			if (error != null && error.indexOf(':') >= 0) {
				if (error.contains(";"))
					error = error.substring(error.indexOf(':') + 1,error.indexOf(';')).trim();
				else if (error.contains("("))
					error = error.substring(error.indexOf(':') + 1,error.indexOf('(')).trim();
				else 
					error = error.substring(error.indexOf(':') + 1).trim();
			}
			m_errors = (error);
			
		} 
	}

	/**
	 * The method makeRM generates the .html file with the RM in it
	 * 
	 * @param filename homepath of the user
	 */
	private void makeRM(String filename) {
		
		// opening file
		Writer writer = null;
		try {
			writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(filename + "\\RM_" + m_databasename
							+ ".html"), "utf-8"));
			
			//generating the HTML for the RM
			String s = "";
			s += "<HTML><HEAD><META HTTP-EQUIV=\"CONTENT-TYPE\" CONTENT=\"text/html; charset=windows-1252\">";
			s += "<TITLE></TITLE><META NAME=\"GENERATOR\" CONTENT=\"LibreOffice 3.6  (Windows)\">";
			s += "<META NAME=\"CREATED\" CONTENT=\"0;0\"><META NAME=\"CHANGED\" CONTENT=\"0;0\">";
			s += "<STYLE TYPE=\"text/css\"></STYLE></HEAD><BODY LANG=\"en-CA\" DIR=\"LTR\"><PRE CLASS=\"western\">";
			
			for (Enumeration<Table> e = m_tables.elements(); e
					.hasMoreElements();) {
				Table t_tamp = (e.nextElement());
				s += t_tamp.getName() + "(";
				Hashtable<String, Attribute> x2 = t_tamp.getAttributes();
				
				for (Enumeration<Attribute> e2 = x2.elements(); e2.hasMoreElements();) {
					Attribute m = e2.nextElement();
					if (m.getFatt() != null) 
						s += m.getFtab() + ".";
					if (m.isPrimary()) 
						s += "<u>";
					s += m.getAttributeName();
					if (m.isPrimary()) 
						s += "</u>";
					if (e2.hasMoreElements()) 
						s += ",";
				}
				s += ")<br>";
			}
			s += "</PRE></BODY></HTML>";
			writer.write(s);
			System.out.println("File saved to: "+filename + "\\RM_" + m_databasename+ ".html");
			writer.close();
			
		} catch (Exception e) {
			// if there was an error - printing it into the error messages.
			String error = e.toString();
			if (error != null && error.indexOf(':') >= 0) {
				if (error.contains(";"))
					error = error.substring(error.indexOf(':') + 1,error.indexOf(';')).trim();
				else if (error.contains("("))
					error = error.substring(error.indexOf(':') + 1,error.indexOf('(')).trim();
				else 
					error = error.substring(error.indexOf(':') + 1).trim();
			}
			m_errors = (error);
			
		} 
	}
}
