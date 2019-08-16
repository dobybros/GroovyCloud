package container.container.bean;

import org.quartz.SchedulerFactory;
import org.springframework.context.annotation.Bean;

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
}
