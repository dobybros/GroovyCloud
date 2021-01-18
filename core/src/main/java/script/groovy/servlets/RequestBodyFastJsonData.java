package script.groovy.servlets;

import com.alibaba.fastjson.JSONObject;

/**
 * Created by lick on 2021/1/14.
 * Description：
 */
public class RequestBodyFastJsonData implements BodyData<JSONObject> {
    private JSONObject data;
    RequestBodyFastJsonData(JSONObject jsonObject){
        this.data = jsonObject;
    }

    public JSONObject getData() {
        return data;
    }
}
