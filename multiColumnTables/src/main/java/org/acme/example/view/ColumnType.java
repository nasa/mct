package org.acme.example.view;

public enum ColumnType {
	ID("ID"),
	TITLE("Title"),
	VALUE("Value"),
	TIME("Time");
		
	private ColumnType(final String text) {
	}
	
	public static String getDisplayName(String name) {
		if(name.equals("ID"))
			return "ID";
		else if(name.equals("TITLE"))
			return "Title";
		else if(name.equals("VALUE"))
			return "Value";
		else if(name.equals("TIME"))
			return "Time";
		else
			return "(unknown type)";
	}
}

