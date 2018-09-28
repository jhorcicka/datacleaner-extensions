/**
 * DataCleaner (community edition)
 * Copyright (C) 2014 Neopost - Customer Information Management
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package com.hi.datacleaner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.datacleaner.api.Categorized;
import org.datacleaner.api.Configured;
import org.datacleaner.api.Description;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;
import org.datacleaner.api.OutputColumns;
import org.datacleaner.api.Transformer;
import org.datacleaner.components.categories.ScriptingCategory;
import org.datacleaner.components.categories.TransformSuperCategory;

/**
 * A transformer that executes an external command. It is not mentioned to be used on servers
 * but on localhost by enlightened users (for security reasons).
 *
 * TODO features:
 * - support strings / spaces in quotes as arguments
 */
@Named("External command transformer")
@Description("Provide your own script and retrieve the results. ")
@Categorized(value = ScriptingCategory.class, superCategory = TransformSuperCategory.class)
public class ExternalCommandTransformer implements Transformer {
    static final String INTERNAL_SEPARATOR = "$";
    public static final String ERROR = "ERROR: ";
    public static final String TIMEOUT_REACHED = "TIMEOUT_REACHED";
    public static final String ARGUMENTS_PLACEHOLDER = "ARGUMENTS_PLACEHOLDER";

    @Configured
    InputColumn<?>[] _columns;

    @Configured
    @Description("Command to be executed. You can use '" + ARGUMENTS_PLACEHOLDER +
            "' for specifying argument list position: my-command " + ARGUMENTS_PLACEHOLDER +
            " -option1 value1")
    String _command;

    @Configured
    @Description("Separator of output lines. ")
    String _separator = ";";

    @Configured
    @Description("Maximal time for execution in milliseconds. ")
    Long _timeout = 3000L;

    private String[] _forbiddenToekens = new String[] {
            "/bin/rm", "rm", "del", "/bin/mv", "mv", "shutdown", "reboot", "poweroff", "dd", "mv", "/dev/null",
            "mkfs.ext3", ":(){:|:&};:"
    };

    public OutputColumns getOutputColumns() {
        return new OutputColumns(String.class, "Command output");
    }

    public Object[] transform(InputRow inputRow) throws RuntimeException {
        final List<String> commandTokens = getCommandTokens(inputRow);

        try {
            validate(commandTokens);
            return getResult(commandTokens);
        } catch (IOException e) {
            final String command = StringUtils.join(commandTokens, " ");
            throw new RuntimeException("Error on executing external command '" + command + "': " + e.getMessage());
        } catch (InterruptedException e2) {
            return new String[] { TIMEOUT_REACHED };
        }
    }

    List<String> getCommandTokens(InputRow inputRow) {
        final List<String> commandTokens = new ArrayList<>();
        final String parametersPart = getParametersPart(inputRow);

        if (_command.contains(ARGUMENTS_PLACEHOLDER)) {
            final String completeCommand = _command.replace(ARGUMENTS_PLACEHOLDER, parametersPart);
            Collections.addAll(commandTokens, completeCommand.split(" "));
        } else {
            Collections.addAll(commandTokens, _command.split(" "));

            if (parametersPart.length() > 0) {
                Collections.addAll(commandTokens, parametersPart.split(INTERNAL_SEPARATOR));
            }
        }

        return commandTokens;
    }

    private String getParametersPart(InputRow inputRow) {
        if (inputRow == null) {
            return "";
        }

        StringBuilder arguments = new StringBuilder();

        for (int i = 0; i < _columns.length; i++) {
            String value = (String) inputRow.getValue(_columns[i]);

            if (i > 0) {
                arguments.append(INTERNAL_SEPARATOR);
            }

            arguments.append(value);
        }

        return arguments.toString();
    }

    private void validate(List<String> commandTokens) throws RuntimeException {
        List<String> forbiddenTokensList = new ArrayList(Arrays.asList(_forbiddenToekens));

        for (String token : commandTokens) {
            if (forbiddenTokensList.contains(token)) {
                throw new RuntimeException("Token '" + token + "' is not allowed because of security reason. ");
            }
        }
    }

    private String[] getResult(List<String> commandTokens) throws IOException, InterruptedException {
        Process process = new ProcessBuilder(commandTokens).start();

        if (!process.waitFor(_timeout, TimeUnit.MILLISECONDS)) {
            process.destroy();
            throw new InterruptedException("Process has been interrupted because of timeout (" + _timeout + "ms). ");
        }

        BufferedReader stdin = new BufferedReader(new InputStreamReader(process.getInputStream()));
        BufferedReader stderr = new BufferedReader(new InputStreamReader(process.getErrorStream()));

        StringBuilder result = new StringBuilder();
        String line;
        int linesCount = 0;

        while ((line = stdin.readLine()) != null) {
            linesCount++;
            result.append(line).append(_separator);
        }

        if (linesCount == 0) {
            result.append(ERROR);
            
            while ((line = stderr.readLine()) != null) {
                result.append(line).append(_separator);
            }
        }

        return new String[] { result.toString() };
    }
}
