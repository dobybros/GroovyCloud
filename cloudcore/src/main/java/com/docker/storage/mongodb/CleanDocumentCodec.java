package com.docker.storage.mongodb;

import org.bson.*;
import org.bson.assertions.Assertions;
import org.bson.codecs.*;
import org.bson.codecs.configuration.CodecRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.Arrays.asList;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;

public class CleanDocumentCodec implements CollectibleCodec<CleanDocument> {

    private static final String ID_FIELD_NAME = "_id";
    private static final CodecRegistry DEFAULT_REGISTRY = fromProviders(asList(new ValueCodecProvider(),
            new BsonValueCodecProvider(),
            new DocumentCodecProvider()));
    private static final BsonTypeClassMap DEFAULT_BSON_TYPE_CLASS_MAP = new BsonTypeClassMap();

    private final BsonTypeClassMap bsonTypeClassMap;
    private final CodecRegistry registry;
    private final IdGenerator idGenerator;
    private final Transformer valueTransformer;

    /**
     * Construct a new instance with a default {@code CodecRegistry} and
     */
    public CleanDocumentCodec() {
        this(DEFAULT_REGISTRY, DEFAULT_BSON_TYPE_CLASS_MAP);
    }

    /**
     * Construct a new instance with the given registry and BSON type class map.
     *
     * @param registry         the registry
     * @param bsonTypeClassMap the BSON type class map
     */
    public CleanDocumentCodec(final CodecRegistry registry, final BsonTypeClassMap bsonTypeClassMap) {
        this(registry, bsonTypeClassMap, null);
    }

    /**
     * Construct a new instance with the given registry and BSON type class map. The transformer is applied as a last step when decoding
     * values, which allows users of this codec to control the decoding process.  For example, a user of this class could substitute a
     * value decoded as a Document with an instance of a special purpose class (e.g., one representing a DBRef in MongoDB).
     *
     * @param registry         the registry
     * @param bsonTypeClassMap the BSON type class map
     * @param valueTransformer the value transformer to use as a final step when decoding the value of any field in the document
     */
    public CleanDocumentCodec(final CodecRegistry registry, final BsonTypeClassMap bsonTypeClassMap, final Transformer valueTransformer) {
        this.registry = Assertions.notNull("registry", registry);
        this.bsonTypeClassMap = Assertions.notNull("bsonTypeClassMap", bsonTypeClassMap);
        this.idGenerator = Assertions.notNull("idGenerator", new ObjectIdGenerator());
        this.valueTransformer = valueTransformer != null ? valueTransformer : new Transformer() {
            @Override
            public Object transform(final Object value) {
                return value;
            }
        };
    }

    @Override
    public boolean documentHasId(final CleanDocument document) {
        return document.containsKey(ID_FIELD_NAME);
    }

    @Override
    public BsonValue getDocumentId(final CleanDocument document) {
        if (!documentHasId(document)) {
            throw new IllegalStateException("The document does not contain an _id");
        }

        Object id = document.get(ID_FIELD_NAME);
        if (id instanceof BsonValue) {
            return (BsonValue) id;
        }

        BsonDocument idHoldingDocument = new BsonDocument();
        BsonWriter writer = new BsonDocumentWriter(idHoldingDocument);
        writer.writeStartDocument();
        writer.writeName(ID_FIELD_NAME);
        writeValue(writer, EncoderContext.builder().build(), id);
        writer.writeEndDocument();
        return idHoldingDocument.get(ID_FIELD_NAME);
    }

    @Override
    public CleanDocument generateIdIfAbsentFromDocument(final CleanDocument document) {
        if (!documentHasId(document)) {
            document.put(ID_FIELD_NAME, idGenerator.generate());
        }
        return document;
    }

    @Override
    public void encode(final BsonWriter writer, final CleanDocument document, final EncoderContext encoderContext) {
        writeMap(writer, document, encoderContext);
    }

    @Override
    public CleanDocument decode(final BsonReader reader, final DecoderContext decoderContext) {
    	CleanDocument document = new CleanDocument();

        reader.readStartDocument();
        while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
            String fieldName = reader.readName();
            document.put(fieldName, readValue(reader, decoderContext));
        }

        reader.readEndDocument();

        return document;
    }

    @Override
    public Class<CleanDocument> getEncoderClass() {
        return CleanDocument.class;
    }

    private void beforeFields(final BsonWriter bsonWriter, final EncoderContext encoderContext, final Map<String, Object> document) {
        if (encoderContext.isEncodingCollectibleDocument() && document.containsKey(ID_FIELD_NAME)) {
            bsonWriter.writeName(ID_FIELD_NAME);
            writeValue(bsonWriter, encoderContext, document.get(ID_FIELD_NAME));
        }
    }

    private boolean skipField(final EncoderContext encoderContext, final String key) {
        return encoderContext.isEncodingCollectibleDocument() && key.equals(ID_FIELD_NAME);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void writeValue(final BsonWriter writer, final EncoderContext encoderContext, final Object value) {
        if (value == null) {
            writer.writeNull();
        } else if (Iterable.class.isAssignableFrom(value.getClass())) {
            writeIterable(writer, (Iterable<Object>) value, encoderContext.getChildContext());
        } else if (Map.class.isAssignableFrom(value.getClass())) {
            writeMap(writer, (Map<String, Object>) value, encoderContext.getChildContext());
        } else {
            Codec codec = registry.get(value.getClass());
            encoderContext.encodeWithChildContext(codec, writer, value);
        }
    }

    private void writeMap(final BsonWriter writer, final Map<String, Object> map, final EncoderContext encoderContext) {
        writer.writeStartDocument();

        beforeFields(writer, encoderContext, map);

        for (final Map.Entry<String, Object> entry : map.entrySet()) {
            if (skipField(encoderContext, entry.getKey())) {
                continue;
            }
            Object value = entry.getValue();
            if(value != null) {
                writer.writeName(entry.getKey());
                writeValue(writer, encoderContext, value);
            }
        }
        writer.writeEndDocument();
    }

    private void writeIterable(final BsonWriter writer, final Iterable<Object> list, final EncoderContext encoderContext) {
        writer.writeStartArray();
        for (final Object value : list) {
            writeValue(writer, encoderContext, value);
        }
        writer.writeEndArray();
    }

    private Object readValue(final BsonReader reader, final DecoderContext decoderContext) {
        BsonType bsonType = reader.getCurrentBsonType();
        if (bsonType == BsonType.NULL) {
            reader.readNull();
            return null;
        } else if (bsonType == BsonType.ARRAY) {
           return readList(reader, decoderContext);
        } else if (bsonType == BsonType.BINARY) {
            byte bsonSubType = reader.peekBinarySubType();
            if (bsonSubType == BsonBinarySubType.UUID_STANDARD.getValue() || bsonSubType == BsonBinarySubType.UUID_LEGACY.getValue()) {
                return registry.get(UUID.class).decode(reader, decoderContext);
            }
        }
        return valueTransformer.transform(registry.get(bsonTypeClassMap.get(bsonType)).decode(reader, decoderContext));
    }

    private List<Object> readList(final BsonReader reader, final DecoderContext decoderContext) {
        reader.readStartArray();
        List<Object> list = new ArrayList<Object>();
        while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
            list.add(readValue(reader, decoderContext));
        }
        reader.readEndArray();
        return list;
    }
}
