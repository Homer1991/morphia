package dev.morphia.mapping.experimental;

import dev.morphia.Datastore;
import dev.morphia.mapping.Mapper;
import dev.morphia.mapping.codec.DocumentWriter;
import dev.morphia.mapping.codec.PropertyCodec;
import dev.morphia.mapping.codec.pojo.PropertyHandler;
import morphia.org.bson.codecs.pojo.TypeData;
import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.BsonTypeClassMap;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static dev.morphia.mapping.codec.references.ReferenceCodec.processId;

/**
 * Defines a codec for MorphiaReference values
 */
@SuppressWarnings("unchecked")
public class MorphiaReferenceCodec extends PropertyCodec<MorphiaReference> implements PropertyHandler {

    private final Mapper mapper;
    private BsonTypeClassMap bsonTypeClassMap = new BsonTypeClassMap();

    /**
     * Creates a codec
     *
     * @param datastore the datastore
     * @param field     the reference field
     * @param typeData  the field type data
     */
    public MorphiaReferenceCodec(final Datastore datastore, final Field field, final TypeData typeData) {
        super(datastore, field, (TypeData) typeData.getTypeParameters().get(0));
        mapper = datastore.getMapper();
    }

    @Override
    public MorphiaReference decode(final BsonReader reader, final DecoderContext decoderContext) {
        Mapper mapper = getDatastore().getMapper();
        Object value = mapper.getCodecRegistry()
                             .get(bsonTypeClassMap.get(reader.getCurrentBsonType()))
                             .decode(reader, decoderContext);
        value = processId(value, mapper, decoderContext);
        if (Set.class.isAssignableFrom(getTypeData().getType())) {
            return new SetReference<>(getDatastore(), getFieldMappedClass(), (List) value);
        } else if (Collection.class.isAssignableFrom(getTypeData().getType())) {
            return new ListReference<>(getDatastore(), getFieldMappedClass(), (List) value);
        } else if (Map.class.isAssignableFrom(getTypeData().getType())) {
            return new MapReference<>(getDatastore(), (Map) value, getFieldMappedClass());
        } else {
            return new SingleReference<>(getDatastore(), getFieldMappedClass(), value);
        }
    }

    @Override
    public Object encode(final Object value) {
        MorphiaReference<Object> wrap;
        if (value instanceof MorphiaReference) {
            wrap = (MorphiaReference<Object>) value;
        } else {
            wrap = MorphiaReference.wrap(value);
        }
        DocumentWriter writer = new DocumentWriter();
        writer.writeStartDocument();
        writer.writeName("ref");
        encode(writer, wrap, EncoderContext.builder().build());
        writer.writeEndDocument();
        return writer.getDocument().get("ref");
    }

    @Override
    public void encode(final BsonWriter writer, final MorphiaReference value, final EncoderContext encoderContext) {
        Object ids = value.getId(mapper, getDatastore(), getFieldMappedClass());
        Codec codec = mapper.getCodecRegistry().get(ids.getClass());
        codec.encode(writer, ids, encoderContext);
    }

    @Override
    public Class getEncoderClass() {
        return MorphiaReference.class;
    }
}
