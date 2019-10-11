package client.imclient.data;

public abstract class IMMessage {
    private String id;
    private String contentType;

    public abstract byte[] toContentBytes();
    public abstract void fromContentBytes(byte[] contentBytes);

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
}
