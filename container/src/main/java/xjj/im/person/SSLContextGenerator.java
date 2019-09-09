package xjj.im.person;

import org.apache.mina.filter.ssl.KeyStoreFactory;
import org.apache.mina.filter.ssl.SslContextFactory;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;

/**
 * Created by zhanjing on 2017/9/1.
 * tcp ssl
 */
public class SSLContextGenerator {

    public SSLContext getSslContext() throws NoSuchAlgorithmException, CertificateException, NoSuchProviderException, KeyStoreException {

        SSLContext sslContext = null;
        try {
            File trustStoreFile = new File("/Users/admin/workSpace/msgservers/workspace-chat/Gateway/conf/certificate/truststore.jks");
            if (trustStoreFile.exists()) {
                final KeyStoreFactory trustStoreFactory = new KeyStoreFactory();
                trustStoreFactory.setDataFile(trustStoreFile);
                trustStoreFactory.setPassword("123456");

                final SslContextFactory sslContextFactory = new SslContextFactory();
                final KeyStore trustStore = trustStoreFactory.newInstance();
                sslContextFactory.setTrustManagerFactoryKeyStore(trustStore);
                sslContextFactory.setKeyManagerFactoryKeyStorePassword("techbrainwave");
                sslContext = sslContextFactory.newInstance();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return sslContext;

    }

}
