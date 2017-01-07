
import java.util.ArrayList;

public class Entity {
	public String name = "";
	public ArrayList<Attribute> attributes = new ArrayList<Attribute>(); //Speichert die Attribute der Entitaet

	public Entity(String name)
	{
		this.name = name;
	}

	public void addAttribute(Attribute a)
	{
		attributes.add(a);
	}

	public ArrayList<Attribute> getKeys()
	{
		ArrayList<Attribute> l = new ArrayList<Attribute>();

		for(int i = 0; i < attributes.size(); i++)
		{
			if(attributes.get(i).isKey)
				l.add(attributes.get(i));
		}

		return l;
	}
}
