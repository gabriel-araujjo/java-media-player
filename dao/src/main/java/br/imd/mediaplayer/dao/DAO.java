package br.imd.mediaplayer.dao;

import br.imd.mediaplayer.DaoConfig;
import br.imd.mediaplayer.model.AbstractModel;
import br.imd.mediaplayer.model.annotation.Model;
import br.imd.mediaplayer.model.annotation.Persist;
import javafx.collections.ObservableList;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * An abstract DAO
 */
public abstract class DAO<T extends AbstractModel> {

    private Class<T> modelClass;
    private List<Field> persistedFields;

    private Map<Long, T> modelsIdIndex;
    private TreeMap<T, T> models;
    private boolean manyFiles;
    private String fileMatchPattern;
    private List<Object> filePatternParts;
    private DataObservable<T> observable;

    protected DAO(String persistedFilePattern) {
        manyFiles = persistedFilePattern.contains("$");
        //noinspection unchecked
        modelClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        persistedFields = getPersistedFields(modelClass);

        modelsIdIndex = new HashMap<>();
        models = new TreeMap<>();

        if (manyFiles) {
            String fieldPattern = "([^$]*)" + //Something before a field (Optional)
                    "\\$\\{([^}]*)}" + // the field itself
                    "([^$]*)"; // Something after a field (Optional)
            filePatternParts = new LinkedList<>();
            Matcher fieldMatcher = Pattern.compile(fieldPattern).matcher(persistedFilePattern);

            while(fieldMatcher.find()) {
                if (!fieldMatcher.group(1).isEmpty())
                    filePatternParts.add(fieldMatcher.group(1));
                filePatternParts.add(getFieldByName(fieldMatcher.group(2)));
                if (!fieldMatcher.group(3).isEmpty())
                    filePatternParts.add(fieldMatcher.group(3));
            }
        } else {
            fileMatchPattern = persistedFilePattern;
        }

        observable = new DataObservable<>();

        readData();
    }

    public boolean insert(T model) {
        return insert(model, true);
    }

    private synchronized boolean insert(T model, boolean notifyChange) {
        if (model != null && modelsIdIndex.put(model.getId(), model) == null) {
            System.out.println("inserting model " + (model != null ? model.toString() : "null") + " " + notifyChange);
            models.put(model, model);
            if (notifyChange) observable.notifyInsert(model);
            return true;
        }
        return false;
    }

    public synchronized boolean update(T model) {
        T m = models.get(model);

        if (m == null) return false;

        if (m != model) {
            Class<?> clazz = modelClass;

            while (clazz != null) {
                for (Field field : clazz.getDeclaredFields()) {
                    if (field.getName().equals("id")) continue;
                    field.setAccessible(true);
                    try {
                        field.set(m, field.get(model));
                    } catch (IllegalAccessException ignore) {
                    }
                }

                Class<?> parentClass = clazz.getSuperclass();
                clazz = clazz != AbstractModel.class && clazz != parentClass ? parentClass : null;
            }

        }
        observable.notifyUpdate(m);
        return true;
    }

    public synchronized Collection<T> list() {
        return modelsIdIndex.values();
    }

    public synchronized T get(long id) {
//        System.err.println(String.format("Grabbing %s of id %d", modelClass.getName(), id));
        T model = modelsIdIndex.get(id);
//        System.err.println(String.format("%s", model != null? model.toString(): "null"));
        return model;
    }

    public synchronized boolean remove(T object) {
        T removed = models.remove(object);
        if (removed != null) {
            modelsIdIndex.remove(removed.getId());
            observable.notifyRemove(object);
            return true;
        }
        return false;
    }

    public synchronized T remove(long id) {
        T removed =  modelsIdIndex.remove(id);
        if (removed != null) {
            models.remove(removed);
            observable.notifyRemove(removed);
        }
        return removed;
    }

    private ObservableWrapper<T> observableVersion;

    public ObservableList<T> getObservableListVersion() {
        if (observableVersion == null) {
            observableVersion = new ObservableWrapper<T>(this);
        }
        return observableVersion;
    }

    private static List<Field> getPersistedFields(Class<?> clazz) {
        List<Field> fields = new LinkedList<>();

        while (clazz != null) {
            int position = 0;
            for (Field field : clazz.getDeclaredFields()) {
                if (field.isAnnotationPresent(Persist.class)) {
                    field.setAccessible(true);
                    fields.add(position++, field);
                }
            }

            Class<?> parentClass = clazz.getSuperclass();
            clazz = clazz != AbstractModel.class && clazz != parentClass ? parentClass : null;
        }

        return fields;
    }

    private Field getFieldByName(String name) {
        for (Field field : persistedFields) {
            if (field.getName().equals(name)) {
                return field;
            }
        }
        throw new IllegalArgumentException(
                "No \"" + name + "\" field of \"" + modelClass.getSimpleName() + "\" class" +
                " is persisted. Is \"" + name + "\" field annotated with @Persist?");
    }

    private String getFileMatchPattern() {
        if (fileMatchPattern != null) {
            if (manyFiles) {
                StringBuilder builder = new StringBuilder();
                for (Object o : filePatternParts) {
                    if (o instanceof Field) {
                        builder.append("[^_]+");
                    } else if (o instanceof String) {
                        builder.append(Pattern.quote((String) o));
                    }
                }
                fileMatchPattern = builder.toString();
            }
        }
        return fileMatchPattern;
    }

    /**
     * Return a stream with all files with data
     * @return a stream of files contain data
     */
    private Stream<File> getFiles() {
        if (manyFiles) {
            File[] allFiles = DaoConfig.DATA_DIRECTORY.listFiles();

            if (allFiles != null) {
                return Arrays.stream(allFiles).filter(f -> f.getName().matches(getFileMatchPattern()));
            } else {
                return Stream.empty();
            }
        } else {
            File file = new File(DaoConfig.DATA_DIRECTORY, getFileMatchPattern());
            if (file.exists()) {
                return Arrays.stream(new File[]{file});
            } else {
                return Stream.empty();
            }
        }
    }


    private void readData() {

        getFiles().forEach(file -> {
            System.out.println("reading files from " + file.getAbsolutePath());
            try {
                ModelReader reader = new ModelReader(new BufferedInputStream(new FileInputStream(file)));
                T model;
                do {
                    model = reader.read(modelClass, persistedFields);
                    insert(model, false);
                } while (model != null);
            } catch (IllegalAccessException | InstantiationException | IOException e) {
                e.printStackTrace();
            }
        });
    }

    private synchronized void persistDataInternal() throws IOException {
        // clear old files
        getFiles().forEach(File::delete);

        // write new models
        try {
            if (manyFiles) {
                for (AbstractModel model : modelsIdIndex.values()) {
                    OutputStream s = new BufferedOutputStream(outputStreamFor(model));
                    ModelWriter writer = new ModelWriter(s);
                    writer.write(model, persistedFields);
                    s.flush();
                    s.close();
                }
            } else {
                OutputStream s = new BufferedOutputStream(new FileOutputStream(new File(DaoConfig.DATA_DIRECTORY, getFileMatchPattern())));
                ModelWriter writer = new ModelWriter(s);
                for (AbstractModel model : modelsIdIndex.values()) {
                    writer.write(model, persistedFields);
                }
                s.flush();
                s.close();
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private OutputStream outputStreamFor(AbstractModel model) throws IOException {
        StringBuilder builder = new StringBuilder();
        try {
            for (Object o : filePatternParts) {
                if (o instanceof Field) {
                    builder.append(SerializationUtil.serialize(((Field) o).get(model)));
                } else {
                    builder.append(o.toString());
                }
            }

        } catch (IllegalAccessException ignore) {
        }
        return new FileOutputStream(new File(DaoConfig.DATA_DIRECTORY, builder.toString()));
    }

    private static final HashMap<String, DAO<? extends AbstractModel>> DAOS = new HashMap<>();

    @SuppressWarnings("unchecked")
    public static <T extends AbstractModel> DAO<T> forModel(final Class<T> clazz) {
        return forModel(clazz.getCanonicalName());
    }

    @SuppressWarnings("unchecked")
    public static <T extends AbstractModel> DAO<T> forModel(String clazzName) {
        if (DAOS.get(clazzName) != null) return (DAO<T>) DAOS.get(clazzName);
        try {
            Class<?> daoClass = Class.forName(clazzName+"DAO");
            Method getInstanceMethod = daoClass.getMethod("getInstance");
            DAO<T> dao = (DAO<T>) getInstanceMethod.invoke(null);
            DAOS.put(clazzName, dao);
            return dao;
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Class " +clazzName + " is not annotated with @" + Model.class.getSimpleName());
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException("WTF", e);
        }
    }

    public static void persistData() throws IOException {
        DAOS.values().parallelStream().forEach(d -> {
            try {
                d.persistDataInternal();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public synchronized boolean addObserver(DataObserver<T> dataObserver) {
        return observable.addObserver(dataObserver);
    }

    public synchronized boolean removeObserver(DataObserver<T> dataObserver) {
        return observable.removeObserver(dataObserver);
    }

    public synchronized T find(T model) {
        return models.get(model);
    }
}
