package br.imd.mediaplayer.dao;

import br.imd.mediaplayer.model.AbstractModel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Reader of models
 */
class ModelReader {

    private BufferedReader in;

    ModelReader(InputStream in) {
        this.in = new BufferedReader(new InputStreamReader(in));
    }

    <T extends AbstractModel> T read(Class<T> clazz, List<Field> fields) throws IOException, IllegalAccessException, InstantiationException {
        String line = in.readLine();
        System.out.println("line = \"" + line + "\"");
        List<String> parts = parts(line);

        System.out.println("parts = " + Arrays.toString(parts.toArray()));

        if (parts.size() != fields.size()) return null;

        T model = clazz.newInstance();

        Iterator<String> partsIterator = parts.iterator();
        for (Field f : fields) {
            if (f.getName().equals("id")) {
                Long id = SerializationUtil.parse(Long.class, partsIterator.next());
                if (id != null) {
                    model.setId(id);
                }
            } else {
                Method setter = getSetter(f);
                if (setter != null) {
                    try {
                        setter.invoke(model, (Object) SerializationUtil.parse(f.getGenericType(), partsIterator.next()));
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }
                } else {
                    f.set(model, SerializationUtil.parse(f.getGenericType(), partsIterator.next()));
                }
            }
        }
        return model;
    }

    private List<String> parts(String line) {
        List<String> parts = new LinkedList<>();

        if (line == null) return parts;

        int partStart = 0;
        for (int i = 0; i < line.length(); i++) {
            if (line.charAt(i) == ' ') {
                parts.add(line.substring(partStart, i));
                partStart = i + 1;
            }
        }
        if (partStart < line.length())
            parts.add(line.substring(partStart, line.length()));
        else if (partStart == line.length())
            parts.add("");

        return parts;
    }

    private Method getSetter(Field field) {
        String setterName = "set" + field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1);
        try {
            return field.getDeclaringClass().getMethod(setterName, field.getType());
        } catch (NoSuchMethodException e) {
            return null;
        }
    }
}
