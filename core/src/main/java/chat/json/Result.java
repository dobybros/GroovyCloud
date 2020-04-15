package chat.json;

public class Result<T> {
    private Integer code;
    private String msg;
    private T data;

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
    public boolean success(){
        return code == 1;
    }

    @Override
    public String toString() {
        return "{" +
                "code=" + code +
                ", msg='" + msg +
                '}';
    }
}