package com.hi.datacleaner;

import java.net.URL;

import javax.inject.Inject;
import javax.inject.Named;

import org.datacleaner.api.Categorized;
import org.datacleaner.api.Configured;
import org.datacleaner.api.Description;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;
import org.datacleaner.api.OutputColumns;
import org.datacleaner.api.OutputRowCollector;
import org.datacleaner.api.Provided;
import org.datacleaner.api.Transformer;
import org.datacleaner.components.categories.ScriptingCategory;
import org.datacleaner.components.categories.TransformSuperCategory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

@Named("HTML selector")
@Description("Provide your own script and retrieve the results. ")
@Categorized(value = ScriptingCategory.class, superCategory = TransformSuperCategory.class)
public class HtmlSelector implements Transformer {
    private final String OUTPUT_COLUMN_NAME_TEXT = "Text";
    private final String OUTPUT_COLUMN_NAME_ATTRIBUTES = "Attributes";
    private final int TIMEOUT_MS = 5000;

    @Inject
    @Provided
    OutputRowCollector rowCollector;

    @Configured
    InputColumn<String> inputColumn;

    @Configured
    String selector;

    @Override
    public OutputColumns getOutputColumns() {
        return new OutputColumns(String.class, OUTPUT_COLUMN_NAME_TEXT, OUTPUT_COLUMN_NAME_ATTRIBUTES);
    }

    @Override
    public Object[] transform(final InputRow inputRow) {
        try {
            final Document document = getDocument(inputRow);
            final Elements selectedElements = document.select(selector);

            for (final Element element : selectedElements) {
                rowCollector.putValues(element.text(), element.attributes().toString());
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private Document getDocument(final InputRow inputRow) throws Exception {
        final String link = inputRow.getValue(inputColumn);
        final URL url = new URL(link);
        return Jsoup.parse(url, TIMEOUT_MS);
    }
}
