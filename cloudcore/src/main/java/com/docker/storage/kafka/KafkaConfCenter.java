package com.docker.storage.kafka;

import org.apache.commons.lang.StringUtils;

import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

public class KafkaConfCenter {
        public static final String FIELD_PRODUCE =  "produce";
        public static final String FIELD_CONSUMER = "consumer";
        public static final String FIELD_SEPARATOR = ".";
        private Properties producerConf;
        private Properties consumerConf;

        public void filterKafkaConf(Properties properties,String... name){
               Set<String> propertyNames = properties.stringPropertyNames();
               Iterator<String> it = propertyNames.iterator();
               while (it.hasNext()){
                   String propertyName = it.next();
                   for (int i = 0; i < name.length; i++) {
                       String kname = name[i];
                       if(propertyName.indexOf(kname) != -1){
                           if(kname.equals(FIELD_PRODUCE)){
                               if(producerConf == null)
                                    producerConf = new Properties();
                               String newPropertyName = StringUtils.substringAfter(propertyName,FIELD_PRODUCE+FIELD_SEPARATOR);
                               producerConf.put(newPropertyName,properties.get(propertyName));
                           }else{
                               if(consumerConf == null)
                                    consumerConf = new Properties();
                               String newPropertyName = StringUtils.substringAfter(propertyName,FIELD_CONSUMER+FIELD_SEPARATOR);
                               consumerConf.put(newPropertyName,properties.get(propertyName));
                           }
                       }
                   }


               }
        }

        public Properties getProducerConf() {
            return producerConf;
        }

        public void setProducerConf(Properties producerConf) {
            this.producerConf = producerConf;
        }

        public Properties getConsumerConf() {
            return consumerConf;
        }

        public void setConsumerConf(Properties consumerConf) {
            this.consumerConf = consumerConf;
        }
}
