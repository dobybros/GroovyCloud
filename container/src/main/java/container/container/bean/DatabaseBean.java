package container.container.bean;

import com.docker.file.adapters.GridFSFileHandler;
import com.docker.storage.adapters.impl.*;
import com.docker.storage.mongodb.MongoHelper;
import com.docker.storage.mongodb.daos.*;
import com.docker.storage.redis.RedisSubscribeHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Auther: lick
 * @Description:
 * @Date:2019/5/26 16:59
 */
@Configuration
public class DatabaseBean{
    private BeanApp instance;
    DatabaseBean(){
        instance = BeanApp.getInstance();
    }
    @Bean
    public MongoHelper dockerStatusHelper(){
        return instance.getDockerStatusHelper();
    }
//    @Bean(initMethod = "init", destroyMethod = "disconnect")
    @Bean(destroyMethod = "disconnect")
    public MongoHelper logsHelper(){
        return instance.getLogsHelper();
    }
//    @Bean(initMethod = "init")
    @Bean
    public MongoHelper configHelper(){
        return instance.getConfigHelper();
    }
//    @Bean(initMethod = "init")
    @Bean
    public DockerStatusDAO dockerStatusDAO(){
        return instance.getDockerStatusDAO();
    }
    @Bean
    public ServiceVersionDAO serviceVersionDAO(){
        return instance.getServiceVersionDAO();
    }
//    @Bean(initMethod = "init")
    @Bean
    public ServersDAO serversDAO(){
        return instance.getServersDAO();
    }
//    @Bean(initMethod = "init")
    @Bean
    public LansDAO lansDAO(){
        return instance.getLansDAO();
    }
    @Bean
    public DockerStatusServiceImpl dockerStatusService(){
        return instance.getDockerStatusService();
    }

    @Bean
    public ServersServiceImpl serversService(){
        return instance.getServersService();
    }

    @Bean
    public LansServiceImpl lansService(){
        return new LansServiceImpl();
    }
    @Bean
    public SDockersServiceImpl sdockersService(){
        return new SDockersServiceImpl();
    }
//    @Bean(initMethod = "init")
    @Bean
    public SDockerDAO sdockerDAO(){
        return instance.getSdockerDAO();
    }
    @Bean(destroyMethod = "disconnect")
    public MongoHelper gridfsHelper(){
        return instance.getGridfsHelper();
    }
//    @Bean(initMethod = "init")
    @Bean
    public GridFSFileHandler fileAdapter(){
        return instance.getFileAdapter();
    }
    @Bean
    public ServiceVersionServiceImpl serviceVersionService(){
        return instance.getServiceVersionService();
    }
    @Bean
    public ScheduledTaskServiceImpl scheduledTaskService(){
        return instance.getScheduledTaskService();
    }
    @Bean
    public ScheduledTaskDAO scheduledTaskDAO(){
        return instance.getScheduledTaskDAO();
    }
    @Bean
    public MongoHelper scheduledTaskHelper(){
        return instance.getScheduledTaskHelper();
    }
    @Bean(destroyMethod = "shutdown")
    public RedisSubscribeHandler redisSubscribeHandler(){
        return instance.getRedisSubscribeHandler();
    }
    @Bean
    public MongoHelper repairHelper(){
        return instance.getRepairHelper();
    }
    @Bean
    public RepairDAO repairDAO(){
        return instance.getRepairDAO();
    }

    @Bean
    public RepairServiceImpl repairService(){
        return instance.getRepairService();
    }
}
