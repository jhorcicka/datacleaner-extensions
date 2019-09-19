package com.hi.datacleaner;

import java.io.File;
import java.io.FileNotFoundException;
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

@Named("MapDB Reader")
@Description("Reads the content of a given MapDB file.")
public class MapDbReader implements Transformer {
    private static String OUTPUT_COLUMN_KEY = "key";
    private static String OUTPUT_COLUMN_VALUE = "value";

    @Inject
    @Provided
    OutputRowCollector rowCollector;

    @Configured
    InputColumn<String> inputColumn;

    @Configured
    String filePath;

    private static <K, V> String getContent(final String name, final Map<K, V> map) {
        final StringBuilder content = new StringBuilder();
        content.append(name).append(":{");

        for (final Map.Entry<K, V> entry : map.entrySet()) {
            final K key = entry.getKey();
            final V value = entry.getValue();
            content.append(key).append(":").append(value).append(",");
        }

        content.append("}");
        final String output = content.toString();

        if (output.endsWith(",}")) {
            return output.substring(output.length() - 2) + "}";
        } else {
            return output;
        }
    }

    @Override
    public OutputColumns getOutputColumns() {
        return new OutputColumns(String.class, OUTPUT_COLUMN_KEY, OUTPUT_COLUMN_VALUE);
    }

    @Override
    public Object[] transform(final InputRow inputRow) {
        /*
        try {
            final DB db = DBMaker.fileDB(filePath).make(); // v 3.*

            for (String name : db.getAllNames()) {
                System.err.println("MYTODO: name=" + name);
            }

            for (final Map.Entry<String, Object> entry : db.getAll().entrySet()) {
                final String name = entry.getKey();
                final Object value = entry.getValue();
                System.err.println("MYTODO: " + name + ": " + value);

                if (value instanceof Map) {
                    final String outputValue = getContent(name, (Map<?, ?>) value);
                    rowCollector.putValues(name, outputValue);
                } else {
                    System.err.println(String.format("Unexpected type (%s) for '%s'.", value.getClass(), name));
                }
            }

            db.close();
        } catch (final Exception e) {
            e.printStackTrace();
        }
        */

        return new Object[]{};
    }

    private void createFile(final String path) {
        /*
        final DB db = get(path);
        final HTreeMap map = db.hashMap("record").createOrOpen();
        map.put("test-key", "test-value");
        db.close();
        */
    }
    
    private DB get(final String path) {
        //return DBMaker.newFileDB(new File(path)).make();
        return DBMaker.fileDB(path).make();
    }

    private void readFile(final String path) {
        final DB db = get(path);
        for (final String name : db.getAll().keySet()) {
        //for (final String name : db.getAllNames()) {
            for (final Map.Entry<String, Object> entry : db.getAll().entrySet()) {
                System.err.println("MYTODO: " + entry.getKey() + " => " + entry.getValue().toString());
                if (entry.getValue() instanceof Map) {
                    final Map map = (Map) entry.getValue();
                    for (final Object key : map.entrySet()) {
                        System.err.println(key); 
                    }
                }
            }
        }
        db.close();
    }


    public static void main(String[] args) {
        final MapDbReader reader = new MapDbReader();
        final String path = "/home/jakub/test.mapdb";
        //final String path = "/home/jakub/file.mapdb";
        //reader.createFile(path);
        reader.readFile(path);
        //reader.filePath = path;
        //reader.transform(null);
    }
}
