package container.container.bean;

import com.dobybros.file.adapters.GridFSFileHandler;
import com.docker.storage.adapters.impl.DockerStatusServiceImpl;
import com.docker.storage.adapters.impl.LansServiceImpl;
import com.docker.storage.adapters.impl.SDockersServiceImpl;
import com.docker.storage.adapters.impl.ServersServiceImpl;
import com.docker.storage.mongodb.MongoHelper;
import com.docker.storage.mongodb.daos.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

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
}
