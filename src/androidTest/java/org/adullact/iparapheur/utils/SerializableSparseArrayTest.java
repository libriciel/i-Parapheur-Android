/*
 * iParapheur Android
 * Copyright (C) 2016-2019 Libriciel
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.adullact.iparapheur.utils;

import android.support.test.filters.SmallTest;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;


@RunWith(AndroidJUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@SmallTest
public class SerializableSparseArrayTest {

    private static byte[] sSerializedBytes;


    @Test
    public void order01_writeObject() throws Exception {

        SerializableSparseArray<String> serializedSparseArray = new SerializableSparseArray<>();
        serializedSparseArray.put(1, null);
        serializedSparseArray.put(3, "");
        serializedSparseArray.put(4, "Test4");
        serializedSparseArray.put(5, "Test5");

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out = new ObjectOutputStream(bos);
        out.writeObject(serializedSparseArray);
        out.flush();
        sSerializedBytes = bos.toByteArray();
        bos.close();

        Assert.assertNotNull(sSerializedBytes);
    }


    @Test
    public void order02_readObject() throws Exception {

        ByteArrayInputStream bis = new ByteArrayInputStream(sSerializedBytes);
        ObjectInput in = new ObjectInputStream(bis);
        //noinspection unchecked
        SerializableSparseArray<String> deserializedSparseArray = (SerializableSparseArray<String>) in.readObject();

        in.close();

        Assert.assertNotNull(deserializedSparseArray);
        Assert.assertNull(deserializedSparseArray.get(0));
        Assert.assertNull(deserializedSparseArray.get(1));
        Assert.assertEquals("", deserializedSparseArray.get(3));
        Assert.assertEquals("Test4", deserializedSparseArray.get(4));
        Assert.assertEquals("Test5", deserializedSparseArray.get(5));
    }

}