package org.datacleaner.beans.command;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.datacleaner.api.InputColumn;
import org.datacleaner.data.MockInputColumn;
import org.datacleaner.data.MockInputRow;
import org.elasticsearch.common.lang3.StringUtils;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ExternalCommandTransformerTest {
    private static final int PARAMETERS_COUNT = 3;
    ExternalCommandTransformer _transformer = new ExternalCommandTransformer();

    @Test
    public void testGetCommandTokensWithPlaceholder() {
        _transformer._command = "/path/to/bin -arguments " + ExternalCommandTransformer.ARGUMENTS_PLACEHOLDER
                + " > output.txt";
        final Map<InputColumn<?>, Object> parameters = getParametersMap();
        _transformer._columns = parameters.keySet().toArray(new InputColumn<?>[parameters.size()]);

        final List<String> tokens = _transformer.getCommandTokens(new MockInputRow(parameters));
        final String expectedResult = "/path/to/bin -arguments " + getParametersString() + " > output.txt";
        assertEquals(expectedResult, StringUtils.join(tokens, " "));
    }

    @Test
    public void testGetCommandTokensInOrder() {
        _transformer._command = "/path/to/bin -option VALUE";
        final Map<InputColumn<?>, Object> parameters = getParametersMap();
        _transformer._columns = parameters.keySet().toArray(new InputColumn<?>[parameters.size()]);

        final List<String> tokens = _transformer.getCommandTokens(new MockInputRow(parameters));
        final String expectedResult = "/path/to/bin -option VALUE " + getParametersString();
        assertEquals(expectedResult, StringUtils.join(tokens, " "));
    }

    private Map<InputColumn<?>, Object> getParametersMap() {
        final Map<InputColumn<?>, Object> parameters = new TreeMap<>();

        for (int i = 1; i <= PARAMETERS_COUNT; i++) {
            parameters.put(new MockInputColumn<>("column" + i), "value" + i);
        }

        return parameters;
    }

    private String getParametersString() {
        return StringUtils.join(getParametersMap().values(), ExternalCommandTransformer.INTERNAL_SEPARATOR);
    }

    @Test
    public void testTimeout() {
        _transformer._columns = Collections.emptySet().toArray(new InputColumn<?>[0]);
        _transformer._command = "sleep 3";
        _transformer._timeout = 1L;
        final Object[] results = _transformer.transform(null);

        assertEquals(ExternalCommandTransformer.TIMEOUT_REACHED, results[0]);
    }

    @Test
    public void testExistingCommand() {
        final Map<InputColumn<?>, Object> parameters = getParametersMap();
        _transformer._columns = parameters.keySet().toArray(new InputColumn<?>[parameters.size()]);
        _transformer._command = "echo";
        final Object[] results = _transformer.transform(new MockInputRow(parameters));

        final String expectedResult = getParametersString() + _transformer._separator;

        assertEquals(expectedResult, results[0]);
    }

    @Test
    public void testForbiddenCommand() {
        final Map<InputColumn<?>, Object> parameters = getParametersMap();
        _transformer._columns = parameters.keySet().toArray(new InputColumn<?>[parameters.size()]);
        _transformer._command = "rm /etc/file.txt";
        boolean exceptionRaised = false;

        try {
            _transformer.transform(null);
        } catch (Exception e) {
            exceptionRaised = true;
        }

        assertTrue(exceptionRaised);
    }
}