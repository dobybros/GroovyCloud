package container.container.bean;

import chat.scheduled.QuartzHandler;
import com.dobybros.chat.tasks.RPCClientAdapterMapTask;
import org.quartz.SchedulerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

/**
 * Created by lick on 2019/6/9.
 * Description：
 */
//@Configuration
public class UtilsBean {
    private BeanApp instance;
    UtilsBean(){
        instance = BeanApp.getInstance();
    }
    @Bean
    public SchedulerFactory schedulerFactory(){
        return instance.getSchedulerFactory();
    }
//    @Bean
//    public QuartzHandler quartzHandler(){
//        return instance.getQuartzHandler();
//    }
}
