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

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.metamodel.query.Query;
import org.apache.metamodel.schema.ColumnType;
import org.datacleaner.api.Categorized;
import org.datacleaner.api.Configured;
import org.datacleaner.api.Description;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;
import org.datacleaner.api.MultiStreamComponent;
import org.datacleaner.api.OutputDataStream;
import org.datacleaner.api.OutputRowCollector;
import org.datacleaner.components.categories.CompositionCategory;
import org.datacleaner.job.output.OutputDataStreamBuilder;
import org.datacleaner.job.output.OutputDataStreams;

@Named("Columns to rows")
@Description("Transforming columns to rows can be useful when you want to treat multiple columns in the same manner. " 
        + "For example if you want to check several conditions on all of them in the same way. "
        + "\nExample: for c in Row[x,y,z]: checkABC(c) & checkDEF(c) & checkGHI(c)")
@Categorized(value = CompositionCategory.class)
public class ColumnsToRowsTransformer extends MultiStreamComponent {
    
    @Inject
    @Configured(required = true)
    InputColumn<?>[] _inputColumns;

    OutputRowCollector _rowCollector;

    @Override
    public OutputDataStream[] getOutputDataStreams() {
        final OutputDataStreamBuilder outputDataStreamBuilder = OutputDataStreams.pushDataStream("output");
        outputDataStreamBuilder.withColumn("cols-to-rows-output", ColumnType.STRING);
        final OutputDataStream stream = outputDataStreamBuilder.toOutputDataStream();

        return new OutputDataStream[] { stream };
    }

    @Override
    public void initializeOutputDataStream(OutputDataStream stream, Query q, OutputRowCollector collector) {
        _rowCollector = collector;
    }

    @Override
    protected void run(final InputRow inputRow) {
        for (int i = 0; i < _inputColumns.length; i++) {
            final String value = (String) inputRow.getValue(_inputColumns[i]);
            _rowCollector.putValues(value);
        }
    }
}
