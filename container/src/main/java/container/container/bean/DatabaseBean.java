package container.container.bean;

import com.dobybros.file.adapters.GridFSFileHandler;
import com.docker.storage.adapters.impl.DockerStatusServiceImpl;
import com.docker.storage.adapters.impl.LansServiceImpl;
import com.docker.storage.adapters.impl.SDockersServiceImpl;
import com.docker.storage.adapters.impl.ServersServiceImpl;
import com.docker.storage.mongodb.MongoHelper;
import com.docker.storage.mongodb.daos.DockerStatusDAO;
import com.docker.storage.mongodb.daos.LansDAO;
import com.docker.storage.mongodb.daos.SDockerDAO;
import com.docker.storage.mongodb.daos.ServersDAO;
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
        MongoHelper dockerStatusHelper = instance.getDockerStatusHelper();
        dockerStatusHelper.setHost(instance.getMongoHost());
        dockerStatusHelper.setConnectionsPerHost(Integer.valueOf(instance.getMongoConnectionsPerHost()));
        dockerStatusHelper.setDbName(instance.getDbName());
        dockerStatusHelper.setUsername(instance.getMongoUsername());
        dockerStatusHelper.setPassword(instance.getMongoPassword());
        return dockerStatusHelper;
    }
//    @Bean(initMethod = "init", destroyMethod = "disconnect")
    @Bean(destroyMethod = "disconnect")
    public MongoHelper logsHelper(){
        MongoHelper logsHelper = instance.getLogsHelper();
        logsHelper.setHost(instance.getMongoHost());
        logsHelper.setConnectionsPerHost(Integer.valueOf(instance.getMongoConnectionsPerHost()));
        logsHelper.setDbName(instance.getLogsDBName());
        logsHelper.setUsername(instance.getMongoUsername());
        logsHelper.setPassword(instance.getMongoPassword());
        return logsHelper;
    }
//    @Bean(initMethod = "init")
    @Bean
    public MongoHelper configHelper(){
        MongoHelper configHelper = instance.getConfigHelper();
        configHelper.setHost(instance.getMongoHost());
        configHelper.setConnectionsPerHost(Integer.valueOf(instance.getMongoConnectionsPerHost()));
        configHelper.setDbName(instance.getConfigDBName());
        configHelper.setUsername(instance.getMongoUsername());
        configHelper.setPassword(instance.getMongoPassword());
        return configHelper;
    }
//    @Bean(initMethod = "init")
    @Bean
    public DockerStatusDAO dockerStatusDAO(){
        DockerStatusDAO dockerStatusDAO = instance.getDockerStatusDAO();
        dockerStatusDAO.setMongoHelper(instance.getDockerStatusHelper());
        return dockerStatusDAO;
    }
//    @Bean(initMethod = "init")
    @Bean
    public ServersDAO serversDAO(){
        ServersDAO serversDAO = instance.getServersDAO();
        serversDAO.setMongoHelper(instance.getConfigHelper());
        return serversDAO;
    }
//    @Bean(initMethod = "init")
    @Bean
    public LansDAO lansDAO(){
        LansDAO lansDAO = instance.getLansDAO();
        lansDAO.setMongoHelper(instance.getConfigHelper());
        return lansDAO;
    }
    @Bean
    public DockerStatusServiceImpl dockerStatusService(){
        DockerStatusServiceImpl dockerStatusService = instance.getDockerStatusService();
        dockerStatusService.setDockerStatusDAO(instance.getDockerStatusDAO());
        return dockerStatusService;
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
        SDockerDAO sdockerDAO = instance.getSdockerDAO();
        sdockerDAO.setMongoHelper(instance.getConfigHelper());
        return sdockerDAO;
    }
//    @Bean(initMethod = "init")
//    public BulkLogDAO bulkLogDAO(){
//        BulkLogDAO bulkLogDAO = getBulkLogDAO();
//        bulkLogDAO.setMongoHelper(getLogsHelper());
//        return bulkLogDAO;
//    }
//    @Bean(initMethod = "init", destroyMethod = "disconnect")
    @Bean(destroyMethod = "disconnect")
    public MongoHelper gridfsHelper(){
        MongoHelper gridfsHelper = instance.getGridfsHelper();
        gridfsHelper.setHost(instance.getGridHost());
        gridfsHelper.setConnectionsPerHost(Integer.valueOf(instance.getGirdConnectionsPerHost()));
        gridfsHelper.setDbName(instance.getGridDbName());
        gridfsHelper.setUsername(instance.getGridUsername());
        gridfsHelper.setPassword(instance.getGridPassword());
        return gridfsHelper;
    }
//    @Bean(initMethod = "init")
    @Bean
    public GridFSFileHandler fileAdapter(){
        GridFSFileHandler fileAdapter = instance.getFileAdapter();
        fileAdapter.setResourceHelper(instance.getGridfsHelper());
        fileAdapter.setBucketName("imfs");
        return fileAdapter;
    }
}
