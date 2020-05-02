#### 1.@ControllerMapping
ControllerMapping标注在类级别上，标注此注解的类会在项目启动时被扫描，并且实例化成为一个bean，这样就可以处理HTPP请求了。

```
标明此类可以接受http请求

@ControllerMapping
class LoginController {
}
```
> 参数 :
- intercept : 指定拦截器类。所有路由到ControllerMapping标注的类的方法上的请求，都会被拦截器拦截(暂不支持多个拦截器)。 <br/>
> sample
```
@ControllerMapping(interceptClass = "intercepters/HtmlIntercepter.groovy")
class LoginController {
    @RequestMapping(uri = "@PREFIX/pages/user/login.html", method = GET)
    public Map login(HttpServletRequest request,
                     HttpServletResponse response){
        return [:]
    }
}
```
可以自定义拦截器，拦截器类需要实现RequestIntercepter接口，并且重写RequestIntercepter接口的invoke，invokeError方法。 RequestHolder对请求进行了封装。在invoke中调用RequestHolder的proceed方法执行ControllerMapping标注的类的方法，在出错时调运调用invokeError。
> sample
```
class CommonIntercepter extends RequestIntercepter {
    private static final String TAG = CommonIntercepter.class.getSimpleName();

    @Override
    public void invoke(RequestHolder holder) throws CoreException {
        Object returnValue = super.proceed(holder)
        String contentType = holder.getResponse().getContentType()
        if (contentType != "application/json") {
            ObjectResult result = new ObjectResult()
            result.setCode(1)
            if (returnValue != null) {
                result.setData(returnValue)
            }
            respond(holder.getResponse(), result)
        }
    }

    @Override
    public void invokeError(Throwable t, RequestHolder holder) {
        t.printStackTrace();
        LoggerEx.error(TAG, holder.getRequest().getRequestURI() + " occured error " + t.getMessage());
        if (t instanceof CoreException) {
            ObjectResult result = new ObjectResult()
            result.setCode(((CoreException) t).getCode())
            result.setData(((CoreException) t).getData())
            respond(holder.getResponse(), result);
        } else {
            holder.getResponse().sendError(500, t.getMessage());
        }
    }

}
```

#### 2.@RequestMapping
RequestMapping标注在ControllerMapping类下的方法上，能够将uri与此方法进行绑定，客户端对此uri发起请求，会执行与之绑定的方法。
> sample
```
@ControllerMapping
class TestController {
    @RequestMapping(uri = "/admin/test/{name}", method = GroovyServlet.POST)
    def test(HttpServletRequest request, HttpServletResponse response, @PathVariable(key="name") name, @RequestHeader(key = "Referer", required = true) String referer, @RequestParam(key = "sex") String sex) {

    }
}
```
对 /admin/test url 发起POST请求,会匹配到test方法，并且执行它。

*（注：被private修饰符修饰的方法不会被访问到,想要能够被http请求执行，必须为public 方法，且方法上必须要加@RequestMapping注解）*

> 参数
- method : 指定请求类型，只有请求类型与指定的匹配，才允许执行方法。通过GroovyServle得到请求类型。
- uri : 指定uri映射，如果请求uri与之匹配，则会匹配到对应的方法。 uri可以使用占位符,如 /admin/test/{name}， {name}就是一个占位符。可以在方法入参处通过@PathVariable("name")注解得到该占位符所接收的值。  
- responseType : 指定数据输出的类型，默认为json类型。

#### 3.@PathVariable
PathVariable用在被RequestMapping指定的方法入参上，可以匹配uri占位符接收的值。
> 参数
- key : 将方发参数与占位符所指定的值绑定

#### 4.@RequestHeader
RequestHeader注解可以指定在方法入参处，可以让方法参数与请求header头指定字段进行绑定。
> 参数
- key : 指定需要绑定的header字段。
- required : 请求是否必须存在key指定的字段，默认为false。

#### 5.@RequestParam
RequestParam与PathVariable, RequestHeader一样，标注在方法入参，用来接收请求参数。
> 参数
- key :  指定需要绑定的请求参数
















