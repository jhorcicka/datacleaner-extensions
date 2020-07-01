package com.hi.datacleaner;

import java.util.Random;

import javax.inject.Named;

import org.datacleaner.api.Categorized;
import org.datacleaner.api.Configured;
import org.datacleaner.api.Description;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;
import org.datacleaner.api.OutputColumns;
import org.datacleaner.api.Transformer;
import org.datacleaner.components.categories.TextCategory;
import org.datacleaner.components.categories.TransformSuperCategory;

@Named("Random data generator")
@Description("Generates random data (numbers between 0 and 100; and strings of length 10).")
@Categorized(value = TextCategory.class, superCategory = TransformSuperCategory.class)
public class RandomDataGenerator implements Transformer {
    private final int LETTERS_COUNT = 26;
    private final String OUTPUT_COLUMN_RANDOM_NUMBER = "Random number";
    private final String OUTPUT_COLUMN_RANDOM_STRING = "Random string";

    @Configured
    InputColumn<String> inputColumn;

    @Configured(required = false)
    Integer maxNumber = 100;

    @Configured(required = false)
    Integer stringLength = 10;

    private Random random = new Random();

    @Override
    public OutputColumns getOutputColumns() {
        return new OutputColumns(String.class, OUTPUT_COLUMN_RANDOM_NUMBER, OUTPUT_COLUMN_RANDOM_STRING);
    }

    @Override
    public Object[] transform(final InputRow inputRow) {
        return new Object[] { generateRandomNumber(), generateRandomString() };
    }

    private Integer generateRandomNumber() {
        return random.nextInt(maxNumber);
    }

    private String generateRandomString() {
        final StringBuffer buffer = new StringBuffer(stringLength);
        for (int i = 0; i < stringLength; i++) {
            char baseLetter = (random.nextInt() % 2 == 0) ? 'a' : 'A';
            buffer.append((char) (random.nextInt(LETTERS_COUNT) + baseLetter));
        }
        return buffer.toString();
    }
}
