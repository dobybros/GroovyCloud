package container.container;

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
@ComponentScan(basePackages = {"com", "container", "chat", "script", "connectors"})
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class, DataSourceTransactionManagerAutoConfiguration.class, MongoAutoConfiguration.class, MongoDataAutoConfiguration.class})
public class ContainerApplication {
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
	public static void main(String[] args) {
		SpringApplication.run(ContainerApplication.class, args);
	}

}
