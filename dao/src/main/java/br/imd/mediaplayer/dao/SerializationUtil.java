package br.imd.mediaplayer.dao;

import br.imd.mediaplayer.model.AbstractModel;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Iterator;

/**
 * Utility class to serialize and parse models, collections, arrays, strings and primitive types
 */
class SerializationUtil {

    static String serialize(Object o) {
        if (o == null) return "";

        if (o instanceof AbstractModel) {
            return Long.toString(((AbstractModel) o).getId());
        }

        if (o instanceof Iterable) {
            StringBuilder out = new StringBuilder();

            Iterator<?> i = ((Iterable<?>) o).iterator();

            while (i.hasNext()) {
                try {
                    out.append(URLEncoder.encode(serialize(i.next()), "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    out.append("???UTF-8 UNSUPPORTED???");
                }
                if (i.hasNext()) out.append("+");
            }


            return out.toString();
        }

        if (o.getClass().isArray()) {
            StringBuilder out = new StringBuilder();

            int length = Array.getLength(o);
            for (int i = 0; i < length; i++) {
                out.append(serialize(Array.get(o, i)));
                if (i < length - 1) out.append("+");
            }

            return out.toString();
        }

        try {
            return URLEncoder.encode(String.valueOf(o), "UTF-8").replaceAll("\\+", "%20");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return "???UTF-8 UNSUPPORTED???";
        }
    }

    @SuppressWarnings("unchecked")
    static <T> T parse(Type type, String serializedData) {
        Class<? super T> clazz = (Class<? super T>) type;
        if (String.class == clazz) {
            try {
                return (T) URLDecoder.decode(serializedData.replace("%20", "+"), "UTF-8");
            } catch (UnsupportedEncodingException ignored) {

            }
        } else if (long.class == clazz || Long.class == clazz) {
            return (T) new Long(serializedData);
        } else if (int.class == clazz || Integer.class == clazz) {
            return (T) new Integer(serializedData);
        } else if (short.class == clazz || Short.class == clazz) {
            return (T) new Short(serializedData);
        } else if (byte.class == clazz || Byte.class == clazz) {
            return (T) new Byte(serializedData);
        } else if (AbstractModel.class.isAssignableFrom(clazz)) {
            return (T) DAO.forModel(clazz.getCanonicalName()).get(Long.parseLong(serializedData));
        } else if (clazz.isArray()) {
            Class<?> elType = clazz.getComponentType();

            String[] els = serializedData.split("\\+");
            Object o = Array.newInstance(elType, els.length);
            for (int i = 0; i < els.length; i++) {
                try {
                    Array.set(o, i, parse(elType, URLDecoder.decode(els[i], "UTF-8")));
                } catch (UnsupportedEncodingException ignored) {

                }
            }
            return (T) o;
        } else if (char.class == clazz || Character.class == clazz) {
            return (T) new Character(serializedData.charAt(0));
        }
        return null;
    }

}
