package com.hi.datacleaner;

import java.io.File;
import java.util.Collection;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.checkerframework.checker.units.qual.K;
import org.datacleaner.api.Configured;
import org.datacleaner.api.Description;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;
import org.datacleaner.api.OutputColumns;
import org.datacleaner.api.OutputRowCollector;
import org.datacleaner.api.Provided;
import org.datacleaner.api.Transformer;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;

@Named("MapDB Reader")
@Description("Reads the content of a given MapDB file.")
public class MapDbReader implements Transformer {
    private static String OUTPUT_COLUMN_RECORD = "record";
    private static String OUTPUT_COLUMN_KEY = "key";
    private static String OUTPUT_COLUMN_VALUE = "value";

    @Inject
    @Provided
    OutputRowCollector rowCollector;

    @Configured
    InputColumn<String> inputColumn;

    @Configured(required = false)
    String filePath;

    private <K, V> void outputContent(final String name, final Map<K, V> map) {
        for (final Map.Entry<K, V> entry : map.entrySet()) {
            final K key = entry.getKey();
            final V value = entry.getValue();
            
            if (name != null && key != null && value != null) {
                rowCollector.putValues(name, key, value);
            }
        }
    }

    private <E> void outputContent(final String name, final Collection<E> collection) {
        for (final E value : collection) {
            if (name != null && value != null) {
                rowCollector.putValues(name, "", value);
            }
        }
    }

    @Override
    public OutputColumns getOutputColumns() {
        return new OutputColumns(String.class, OUTPUT_COLUMN_RECORD, OUTPUT_COLUMN_KEY, OUTPUT_COLUMN_VALUE);
    }
    
    private File getFile(final InputRow row) {
        File file = null; 
        
        if (inputColumn != null) {
            final String path = row.getValue(inputColumn);
            
            if (path != null && path.length() > 0) {
                file = new File(path);
            }
        }
        
        if (file == null && filePath != null && filePath.length() > 0) {
            file = new File(filePath);
        }
        
        return file;
    }

    @Override
    public Object[] transform(final InputRow inputRow) {
        try {
            final DB db = DBMaker.newFileDB(getFile(inputRow)).make();

            for (final Map.Entry<String, Object> entry : db.getAll().entrySet()) {
                final String name = entry.getKey();
                final Object value = entry.getValue();

                if (value instanceof Map) {
                    outputContent(name, (Map<?, ?>) value);
                } else if (value instanceof Collection) {
                    outputContent(name, (Collection<?>) value);
                } else {
                    System.err.println(String.format("Unexpected type (%s) for '%s'.", value.getClass(), name));
                }
            }

            db.close();
        } catch (final Exception e) {
            e.printStackTrace();
        }

        return new Object[]{};
    }

    private void createFile(final String path) {
        final DB db = get(path);
        final HTreeMap map = db.createHashMap("record").make();
        map.put("test-key-1", "test-value-1");
        map.put("test-key-2", "test-value-2");
        map.put("test-key-3", "test-value-3");
        db.commit();
        db.close();
    }

    private DB get(final String path) {
        return DBMaker.newFileDB(new File(path))
//                .cacheSoftRefEnable()
                //.closeOnJvmShutdown()
                .make();
    }

    private void readFile(final String path) {
        final DB db = get(path);
        for (final String name : db.getAll().keySet()) {
            for (final Map.Entry<String, Object> entry : db.getAll().entrySet()) {
                System.err.println("MYTODO: " + entry.getKey() + " => " + entry.getValue().toString());
            }
        }
        db.close();
    }

    public static void main(String[] args) {
        final MapDbReader reader = new MapDbReader();
        //final String path = "/home/jakub/desktop/di/mti/test.mapdb";

        final String path = "/home/jakub/desktop/di/mti/file.mapdb";
        //reader.createFile(path);
        reader.readFile(path);
    }
}
