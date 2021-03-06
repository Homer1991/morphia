/*
  Copyright (C) 2010 Olafur Gauti Gudmundsson
  <p/>
  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may
  obtain a copy of the License at
  <p/>
  http://www.apache.org/licenses/LICENSE-2.0
  <p/>
  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions
  and limitations under the License.
 */


package dev.morphia;


import dev.morphia.annotations.Embedded;
import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import dev.morphia.testmodel.Rectangle;
import org.bson.Document;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static dev.morphia.query.experimental.filters.Filters.eq;
import static dev.morphia.query.experimental.filters.Filters.in;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;


/**
 * @author Scott Hernandez
 */
public class TestIdField extends TestBase {

    @Test
    public void embeddedIds() {
        final MyId id = new MyId("1", "2");

        final EmbeddedId a = new EmbeddedId(id, "data");
        final EmbeddedId b = new EmbeddedId(new MyId("2", "3"), "data, too");

        getDs().save(a);
        getDs().save(b);

        assertEquals(a.data, getDs().find(EmbeddedId.class).filter(eq("_id", id)).first().data);

        final EmbeddedId embeddedId = getDs().find(EmbeddedId.class)
                                             .filter(in("_id", Arrays.asList(id))).iterator()
                                             .next();
        Assert.assertEquals(a.data, embeddedId.data);
        Assert.assertEquals(a.id, embeddedId.id);
    }

    @Test
    public void testIdFieldNameMapping() {
        final Rectangle r = new Rectangle(1, 12);
        getDs().save(r);
        final Document document = getMapper().toDocument(r);
        assertFalse(document.containsKey("id"));
        assertTrue(document.containsKey("_id"));
        assertEquals(4, document.size()); //_id, h, w, className
    }

    @Test
    public void testKeyAsId() {
        getMapper().map(KeyAsId.class);

        final Rectangle r = new Rectangle(1, 1);
        //        Rectangle r2 = new Rectangle(11,11);

        final Key<Rectangle> rKey = getMapper().getKey(getDs().save(r));
        //        Key<Rectangle> r2Key = ds.save(r2);
        final KeyAsId kai = new KeyAsId(rKey);
        final Key<KeyAsId> kaiKey = getMapper().getKey(getDs().save(kai));
        final KeyAsId kaiLoaded = getDs().find(KeyAsId.class).filter(eq("_id", rKey)).first();
        assertNotNull(kaiLoaded);
        assertNotNull(kaiKey);
    }

    @Test
    public void testMapAsId() {
        getMapper().map(MapAsId.class);

        final MapAsId mai = new MapAsId();
        mai.id.put("test", "string");
        final Key<MapAsId> maiKey = getMapper().getKey(getDs().save(mai));
        final MapAsId maiLoaded = getDs().find(MapAsId.class).filter(eq("_id", new Document("test", "string"))).first();
        assertNotNull(maiLoaded);
        assertNotNull(maiKey);
    }

    @Entity
    private static class KeyAsId {
        @Id
        private Key<Rectangle> id;

        private KeyAsId() {
        }

        KeyAsId(final Key<Rectangle> key) {
            id = key;
        }
    }

    @Entity
    private static class MapAsId {
        @Id
        private final Map<String, String> id = new HashMap<String, String>();
    }

    @Entity(useDiscriminator = false)
    public static class EmbeddedId {

        @Id
        private MyId id;
        private String data;

        public EmbeddedId() {
        }

        public EmbeddedId(final MyId myId, final String data) {
            id = myId;
            this.data = data;
        }
    }

    @Embedded
    public static class MyId {
        private String myIdPart1;
        private String myIdPart2;

        public MyId() {
        }

        public MyId(final String myIdPart1, final String myIdPart2) {
            this.myIdPart1 = myIdPart1;
            this.myIdPart2 = myIdPart2;
        }

        @Override
        public int hashCode() {
            int result = myIdPart1.hashCode();
            result = 31 * result + myIdPart2.hashCode();
            return result;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            final MyId myId = (MyId) o;

            if (!myIdPart1.equals(myId.myIdPart1)) {
                return false;
            }
            return myIdPart2.equals(myId.myIdPart2);
        }
    }
}
