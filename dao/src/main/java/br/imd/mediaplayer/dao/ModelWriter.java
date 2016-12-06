package br.imd.mediaplayer.dao;

import br.imd.mediaplayer.model.AbstractModel;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.List;

/**
 * Writer of models
 */
class ModelWriter {

    private OutputStreamWriter out;

    ModelWriter(OutputStream out) {
        this.out = new OutputStreamWriter(out);
    }

    <Model extends AbstractModel> void write(Model model, List<Field> persistedFields) throws IllegalAccessException, IOException {
        Iterator<Field> i = persistedFields.iterator();

        while (i.hasNext()) {
            out
                    .append(SerializationUtil.serialize(i.next().get(model)))
                    .append(i.hasNext() ? " " : "\n");
        }
        out.flush();
    }
}
