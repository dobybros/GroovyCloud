package proxycontainer.proxycontainer;

import com.dobybros.gateway.script.GroovyServletScriptDispatcher;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import script.groovy.servlets.GroovyServletDispatcher;
@ComponentScan(basePackages = {"com", "proxycontainer"})
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class, DataSourceTransactionManagerAutoConfiguration.class, MongoAutoConfiguration.class, MongoDataAutoConfiguration.class})
public class ProxyContainerApplication {
	@Bean
	public ServletRegistrationBean servletRegistrationBean() {
		//用ServletRegistrationBean包装servlet
		ServletRegistrationBean registrationBean
				= new ServletRegistrationBean(new GroovyServletDispatcher());
		registrationBean.setLoadOnStartup(1);
		registrationBean.addUrlMappings("/rest/*");
		registrationBean.setName("groovyDispatcherServlet");
		return registrationBean;
	}
	@Bean
	public ServletRegistrationBean baseServletRegistrationBean() {
		//用ServletRegistrationBean包装servlet
		ServletRegistrationBean baseServletRegistrationBean
				= new ServletRegistrationBean(new GroovyServletScriptDispatcher());
		baseServletRegistrationBean.setLoadOnStartup(1);
		baseServletRegistrationBean.addUrlMappings("/base/*");
		baseServletRegistrationBean.setName("InternalGroovyServlet");
		return baseServletRegistrationBean;
	}
	public static void main(String[] args) {
		SpringApplication.run(ProxyContainerApplication.class, args);
	}

}
