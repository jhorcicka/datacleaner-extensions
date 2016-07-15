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
package org.datacleaner.beans.transform;

import org.datacleaner.api.InputColumn;
import org.datacleaner.data.MockInputColumn;
import org.datacleaner.data.MockInputRow;
import org.datacleaner.test.MockOutputRowCollector;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ColumnsToRowsTransformerTest {
    private static final int COLUMN_COUNT = 3;
    private static final String COLUMN_PREFIX = "column-";
    private static final String VALUE_PREFIX = "value-";

    @Test
    public void testTransform() throws Exception {
        final InputColumn<String>[] inputColumns = new InputColumn[COLUMN_COUNT];
        final MockInputRow inputRow = new MockInputRow();

        for (int i = 0; i < COLUMN_COUNT; i++) {
            inputColumns[i] = new MockInputColumn<>(COLUMN_PREFIX + (i + 1), String.class);
            inputRow.put(inputColumns[i], VALUE_PREFIX + (i + 1));
        }

        final ColumnsToRowsTransformer transformer = new ColumnsToRowsTransformer();
        transformer._inputColumns = inputColumns;
        transformer._rowCollector = new MockOutputRowCollector();

        assertEquals(0, transformer.getOutputColumns().getColumnCount());

        final Object[] result = transformer.transform(inputRow);
        assertEquals(0, result.length);
        assertEquals(COLUMN_COUNT, ((MockOutputRowCollector) transformer._rowCollector).getOutput().size());

        for (int i = 0; i < COLUMN_COUNT; i++) {
            final Object[] row = ((MockOutputRowCollector) transformer._rowCollector).getOutput().get(i);
            assertEquals(VALUE_PREFIX + (i + 1), row[0]);
        }
    }
}