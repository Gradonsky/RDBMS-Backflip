
public class Attribute {
	public String name = "";
	public String type = "";
	public boolean notNull = false, isKey = false, isFKey = false;
	public String sup = ""; //Fuer Fremdschluessel. Dieses Feld enthaelt den Namen, auf den Referenziert wird

	public Attribute(String name, String type, boolean notNull, boolean isKey)
	{
		this.name = name;
		this.type = type;
		this.notNull = notNull;
		this.isKey = isKey;
	}

	public void addForeignKey(String source)
	{
		this.sup = source;
		this.isKey = true;
		this.isFKey = true;
	}
}
