package connectors.mongodb.annotations.handlers;

import chat.utils.ClassFieldsHolder;
import org.bson.types.Binary;

import java.lang.reflect.Field;

/**
 * Created by zhanjing on 2017/9/15.
 */
public class MongodbClassFieldsHolder extends ClassFieldsHolder{
    public MongodbClassFieldsHolder(Class<?> documentClass, FieldIdentifier fieldIdentifier) {
        super(documentClass, fieldIdentifier);
    }

    @Override
    public Object convert(Object value, Field field) {
        Object valueObj = null;
        if(value instanceof Binary) {
            valueObj = ((Binary) value).getData();
        } else {
            valueObj = super.convert(value, field);
        }
        return valueObj;
    }
}
