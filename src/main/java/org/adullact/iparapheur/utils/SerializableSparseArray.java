/*
 * <p>SerializableSparseArray<br/>
 *
 * Original work Asaf Pinhassi www.mobiledev.co.il<br/>
 * Modified work Copyright (C) 2016 Adullact-Projet.</p>
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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;


public class SerializableSparseArray<E> extends android.util.SparseArray<E> implements Serializable {


    public SerializableSparseArray() {
        super();
    }


    /**
     * This method is private but it is called using reflection by Java serialization mechanism.
     * It overwrites the default object serialization.
     * <p>
     * IMPORTANT
     * The access modifier for this method MUST be set to <b>private</b> otherwise {@link java.io.StreamCorruptedException} will be thrown.
     *
     * @param oos the stream the data is stored into
     * @throws IOException an exception that might occur during data storing
     */
    private void writeObject(ObjectOutputStream oos) throws IOException {
        Object[] data = new Object[size()];

        for (int i = data.length - 1; i >= 0; i--) {
            Object[] pair = {keyAt(i), valueAt(i)};
            data[i] = pair;
        }
        oos.writeObject(data);
    }


    /**
     * This method is private, but it is called using reflection by Java serialization mechanism.
     * It overwrites the default object serialization.
     * <p>
     * IMPORTANT :
     * The access modifier for this method MUST be set to private otherwise {@link java.io.StreamCorruptedException} will be thrown.
     *
     * @param ois the stream the data is read from
     * @throws IOException            an exception that might occur during data reading
     * @throws ClassNotFoundException this exception will be raised when read class is not known by the current ClassLoader
     */
    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        Object[] data = (Object[]) ois.readObject();
        for (int i = data.length - 1; i >= 0; i--) {
            Object[] pair = (Object[]) data[i];
            //noinspection unchecked
            this.append((Integer) pair[0], (E) pair[1]);
        }
    }

}