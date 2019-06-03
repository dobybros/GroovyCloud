package connectors.mongodb.codec;

import connectors.mongodb.annotations.handlers.MongoDBHandler;
import org.bson.codecs.Codec;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;

public class DataObjectCodecProvider implements CodecProvider {
	private Class<?> collectionClass;
	private MongoDBHandler mongoDBHandler;
    public DataObjectCodecProvider(Class<?> collectionClass, MongoDBHandler mongoDBHandler) {
    	this.collectionClass = collectionClass;
    	this.mongoDBHandler = mongoDBHandler;
	}

	@Override
    @SuppressWarnings("unchecked")
    public <T> Codec<T> get(Class<T> clazz, CodecRegistry registry) {
        if (DataObject.class.isAssignableFrom(clazz)) {
            return (Codec<T>) new DataObjectCodec(this.collectionClass, registry, this.mongoDBHandler);
        }
        return null;
    }
}