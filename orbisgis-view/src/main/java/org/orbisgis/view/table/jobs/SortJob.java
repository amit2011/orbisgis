/*
 * OrbisGIS is a GIS application dedicated to scientific spatial simulation.
 * This cross-platform GIS is developed at French IRSTV institute and is able to
 * manipulate and create vector and raster spatial information. 
 * 
 * OrbisGIS is distributed under GPL 3 license. It is produced by the "Atelier SIG"
 * team of the IRSTV Institute <http://www.irstv.fr/> CNRS FR 2488.
 * 
 * Copyright (C) 2007-1012 IRSTV (FR CNRS 2488)
 * 
 * This file is part of OrbisGIS.
 * 
 * OrbisGIS is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * OrbisGIS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * OrbisGIS. If not, see <http://www.gnu.org/licenses/>.
 * 
 * For more information, please consult: <http://www.orbisgis.org/>
 * or contact directly:
 * info_at_ orbisgis.org
 */
package org.orbisgis.view.table.jobs;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;
import org.apache.log4j.Logger;
import org.gdms.data.DataSource;
import org.gdms.data.types.Type;
import org.gdms.data.values.Value;
import org.gdms.driver.DriverException;
import org.orbisgis.core.common.IntegerUnion;
import org.orbisgis.progress.ProgressMonitor;
import org.orbisgis.view.background.BackgroundJob;
import org.orbisgis.view.table.DataSourceTableModel;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;

/**
 * Sort the specified column and update the Table model.
 * The Data source is used to not store all the column values in memory
 * @author Nicolas Fortin
 */
public class SortJob implements BackgroundJob {

        protected final static I18n I18N = I18nFactory.getI18n(SortJob.class);
        private static final Logger LOGGER = Logger.getLogger(SortJob.class);
        private DataSourceTableModel model;
        private boolean ascending;
        private Integer columnToSort;

        public SortJob(boolean ascending, DataSourceTableModel model, int columnToSort) {
                this.ascending = ascending;
                this.model = model;
                this.columnToSort = columnToSort;
        }

        @Override
        public void run(ProgressMonitor pm) {
                if (model.getRowCount() < 2) {
                        return;
                }
                // Retrieve the index if the model have a restricted set of rows
                Collection<Integer> modelIndex = model.getIndexes();
                if (modelIndex == null) {
                        //Create an array [0 1 ..rows]
                        modelIndex = new IntegerUnion(0, model.getRowCount() - 1);
                }
                int rowCount = modelIndex.size();
                DataSource source = model.getDataSource();
                //Create a sorted collection, to follow the progression of order
                //The comparator will read the integer value and
                //use the data source to compare
                Comparator<Integer> comparator;
                try {
                        Type fieldType = source.getMetadata().getFieldType(columnToSort);
                        if (fieldType.getTypeCode() == Type.STRING) {
                                //Do not cache values
                                comparator = new SortValueComparator(source, columnToSort);
                        } else {
                                //Cache values
                                
                                pm.startTask(I18N.tr("Cache table values"), 100);
                                Value[] cache = new Value[rowCount];
                                for (int i = 0; i < source.getRowCount(); i++) {
                                        cache[i] = source.getFieldValue(i, columnToSort);
                                        if (i / 100 == i / 100.0) {
                                                if (pm.isCancelled()) {
                                                        return;
                                                } else {
                                                        pm.progressTo(100 * i / rowCount);
                                                }
                                        }
                                }
                                pm.endTask();
                                comparator = new SortValueCachedComparator(cache);
                        }
                        if (!ascending) {
                                comparator = Collections.reverseOrder(comparator);
                        }
                        Queue<Integer> columnValues = new PriorityQueue<Integer>(rowCount, comparator);
                        
                        int processedRows = 0;
                        for (int i : modelIndex) {
                                columnValues.add(i);
                                if (i / 100 == i / 100.0) {
                                        if (pm.isCancelled()) {
                                                return;
                                        } else {
                                                pm.progressTo(100 * processedRows / rowCount);
                                        }
                                }
                                processedRows++;
                        }
                        //Update the table model
                        model.setCustomIndex(columnValues);
                } catch (IllegalStateException ex) {
                        LOGGER.error(I18N.tr("Driver error"), ex);
                } catch (DriverException ex) {
                        LOGGER.error(I18N.tr("Driver error"), ex);
                }
                /*
                List<Boolean> order = new ArrayList<Boolean>();
                order.add(ascending);
                TreeSet<Integer> sortset = new TreeSet<Integer>(
                        new SortComparator(cache, order));
                for (int i = 0; i < rowCount; i++) {
                        if (i / 100 == i / 100.0) {
                                if (pm.isCancelled()) {
                                        break;
                                } else {
                                        pm.progressTo(100 * i / rowCount);
                                }
                        }
                        sortset.add(new Integer(i));
                }
                ArrayList<Integer> indexes = new ArrayList<Integer>();
                Iterator<Integer> it = sortset.iterator();
                while (it.hasNext()) {
                        Integer integer = (Integer) it.next();
                        indexes.add(integer);
                }
                TableComponent.this.indexes = indexes;
                SwingUtilities.invokeLater(new Runnable() {

                        @Override
                        public void run() {
                                fireTableDataChanged();
                        }
                });
                * 
                */
        }

        @Override
        public String getTaskName() {
                return I18N.tr("Table column sorting");
        }
}
