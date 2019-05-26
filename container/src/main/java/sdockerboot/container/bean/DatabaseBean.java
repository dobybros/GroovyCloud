package sdockerboot.container.bean;

import com.dobybros.chat.storage.mongodb.daos.BulkLogDAO;
import com.dobybros.file.adapters.GridFSFileHandler;
import com.docker.storage.mongodb.MongoHelper;
import com.docker.storage.mongodb.daos.LansDAO;
import com.docker.storage.mongodb.daos.SDockerDAO;
import com.docker.storage.mongodb.daos.ServersDAO;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Auther: lick
 * @Description:
 * @Date:2019/5/26 16:59
 */
@Configuration
public class DatabaseBean extends BeanApp{
    @Bean(initMethod = "init")
    public MongoHelper dockerStatusHelper(){
        MongoHelper dockerStatusHelper = getDockerStatusHelper();
        dockerStatusHelper.setHost(getMongoHost());
        dockerStatusHelper.setConnectionsPerHost(Integer.valueOf(getMongoConnectionsPerHost()));
        dockerStatusHelper.setDbName(getDbName());
        dockerStatusHelper.setUsername(getMongoUsername());
        dockerStatusHelper.setPassword(getMongoPassword());
        return dockerStatusHelper;
    }
    @Bean(initMethod = "init", destroyMethod = "disconnect")
    public com.dobybros.chat.storage.mongodb.MongoHelper logsHelper(){
        com.dobybros.chat.storage.mongodb.MongoHelper logsHelper = getLogsHelper();
        logsHelper.setHost(getMongoHost());
        logsHelper.setConnectionsPerHost(Integer.valueOf(getMongoConnectionsPerHost()));
        logsHelper.setDbName(getLogsDBName());
        logsHelper.setUsername(getMongoUsername());
        logsHelper.setPassword(getMongoPassword());
        return logsHelper;
    }
    @Bean(initMethod = "init")
    public MongoHelper configHelper(){
        MongoHelper configHelper = getConfigHelper();
        configHelper.setHost(getMongoHost());
        configHelper.setConnectionsPerHost(Integer.valueOf(getMongoConnectionsPerHost()));
        configHelper.setDbName(getConfigDBName());
        configHelper.setUsername(getMongoUsername());
        configHelper.setPassword(getMongoPassword());
        return configHelper;
    }
    @Bean(initMethod = "init")
    public ServersDAO serversDAO(){
        ServersDAO serversDAO = getServersDAO();
        serversDAO.setMongoHelper(getConfigHelper());
        return serversDAO;
    }
    @Bean(initMethod = "init")
    public LansDAO lansDAO(){
        LansDAO lansDAO = getLansDAO();
        lansDAO.setMongoHelper(getConfigHelper());
        return lansDAO;
    }
    @Bean(initMethod = "init")
    public SDockerDAO sdockerDAO(){
        SDockerDAO sdockerDAO = getSdockerDAO();
        sdockerDAO.setMongoHelper(getConfigHelper());
        return sdockerDAO;
    }
    @Bean(initMethod = "init")
    public BulkLogDAO bulkLogDAO(){
        BulkLogDAO bulkLogDAO = getBulkLogDAO();
        bulkLogDAO.setMongoHelper(getLogsHelper());
        return bulkLogDAO;
    }
    @Bean(initMethod = "init", destroyMethod = "disconnect")
    public com.dobybros.chat.storage.mongodb.MongoHelper gridfsHelper(){
        com.dobybros.chat.storage.mongodb.MongoHelper gridfsHelper = getGridfsHelper();
        gridfsHelper.setHost(getGridHost());
        gridfsHelper.setConnectionsPerHost(Integer.valueOf(getGirdConnectionsPerHost()));
        gridfsHelper.setDbName(getGridDbName());
        gridfsHelper.setUsername(getGridUsername());
        gridfsHelper.setPassword(getGridPassword());
        return gridfsHelper;
    }
    @Bean(initMethod = "init")
    public GridFSFileHandler fileAdapter(){
        GridFSFileHandler fileAdapter = getFileAdapter();
        fileAdapter.setResourceHelper(getGridfsHelper());
        fileAdapter.setBucketName("imfs");
        return fileAdapter;
    }

}
