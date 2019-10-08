package container.container.bean;

import com.docker.rpc.remote.stub.RpcCacheManager;
import com.docker.storage.cache.CacheStorageFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by lick on 2019/9/27.
 * Description：
 */
@Configuration
public class MemoryCacheBean {
    private BeanApp instance;
    MemoryCacheBean(){
        instance = BeanApp.getInstance();
    }
    @Bean
    RpcCacheManager rpcCacheManager(){
        return instance.getRpcCacheManager();
    };
    @Bean
    CacheStorageFactory cacheStorageFactory(){
        return instance.getCacheStorageFactory();
    }
}
