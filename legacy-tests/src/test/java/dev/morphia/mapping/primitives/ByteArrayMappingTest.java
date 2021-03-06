package dev.morphia.mapping.primitives;


import dev.morphia.TestBase;
import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import org.bson.types.ObjectId;
import org.junit.Assert;
import org.junit.Test;

import static dev.morphia.query.experimental.filters.Filters.eq;

public class ByteArrayMappingTest extends TestBase {
    @Test
    public void testCharMapping() {
        getMapper().map(ContainsByteArray.class);
        final ContainsByteArray entity = new ContainsByteArray();
        final Byte[] test = new Byte[]{6, 9, 1, -122};
        entity.ba = test;
        getDs().save(entity);
        final ContainsByteArray loaded = getDs().find(ContainsByteArray.class)
                                                .filter(eq("_id", entity.id))
                                                .first();

        for (int i = 0; i < test.length; i++) {
            final Byte c = test[i];
            Assert.assertEquals(c, entity.ba[i]);
        }
        Assert.assertNotNull(loaded.id);
    }

    @Entity
    private static class ContainsByteArray {
        @Id
        private ObjectId id;
        private Byte[] ba;
    }

}
