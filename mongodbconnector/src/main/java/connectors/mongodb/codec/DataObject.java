package connectors.mongodb.codec;

public abstract class DataObject {
	public static final String FIELD_ID = "_id";
	protected String id;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
}
