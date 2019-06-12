package script.groovy.servlets.grayreleased;

import javax.servlet.http.Cookie;

/**
 * Created by lick on 2019/5/30.
 * Description：
 */
public class GrayReleased {
    public  static ThreadLocal<GrayReleased> grayReleasedThreadLocal = new ThreadLocal<>();
    public static final String COOKIETYPE = "serviceVersionType";
    public static String defaultVersion = "default";

    private String type;//cookie中指定type，数据库中有对应type的s各个ervice的版本

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public static String getCookieValue(Cookie[] cookies) {
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(COOKIETYPE)) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
