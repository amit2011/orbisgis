/*
 * The GDMS library (Generic Datasources Management System)
 * is a middleware dedicated to the management of various kinds of
 * data-sources such as spatial vectorial data or alphanumeric. Based
 * on the JTS library and conform to the OGC simple feature access
 * specifications, it provides a complete and robust API to manipulate
 * in a SQL way remote DBMS (PostgreSQL, H2...) or flat files (.shp,
 * .csv...). GDMS is produced  by the geomatic team of the IRSTV
 * Institute <http://www.irstv.cnrs.fr/>, CNRS FR 2488:
 *    Erwan BOCHER, scientific researcher,
 *    Thomas LEDUC, scientific researcher,
 *    Fernando GONZALEZ CORTES, computer engineer.
 *
 * Copyright (C) 2007 Erwan BOCHER, Fernando GONZALEZ CORTES, Thomas LEDUC
 *
 * This file is part of GDMS.
 *
 * GDMS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GDMS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GDMS. If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please consult:
 *    <http://orbisgis.cerma.archi.fr/>
 *    <http://sourcesup.cru.fr/projects/orbisgis/>
 *    <http://listes.cru.fr/sympa/info/orbisgis-developers/>
 *    <http://listes.cru.fr/sympa/info/orbisgis-users/>
 *
 * or contact directly:
 *    erwan.bocher _at_ ec-nantes.fr
 *    fergonco _at_ gmail.com
 *    thomas.leduc _at_ cerma.archi.fr
 */
package org.orbisgis.geoview.rasterProcessing.sql.customQuery;

import org.gdms.data.DataSource;
import org.gdms.data.DataSourceFactory;
import org.gdms.data.ExecutionException;
import org.gdms.data.SpatialDataSourceDecorator;
import org.gdms.data.metadata.DefaultMetadata;
import org.gdms.data.metadata.Metadata;
import org.gdms.data.metadata.MetadataUtilities;
import org.gdms.data.types.Type;
import org.gdms.data.types.TypeFactory;
import org.gdms.data.values.Value;
import org.gdms.data.values.ValueFactory;
import org.gdms.driver.DriverException;
import org.gdms.driver.ObjectDriver;
import org.gdms.driver.driverManager.DriverLoadException;
import org.gdms.driver.memory.ObjectMemoryDriver;
import org.gdms.sql.customQuery.CustomQuery;
import org.gdms.sql.function.FunctionValidator;
import org.gdms.sql.strategies.IncompatibleTypesException;
import org.gdms.sql.strategies.SemanticException;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.Point;

public class PointsToXYZ implements CustomQuery {
	public String getName() {
		return "PointsToXYZ";
	}

	public String getSqlOrder() {
		return "select PointsToXYZ(the_geom, [a field name]) from myTable;";
	}

	public String getDescription() {
		return "Extract X Y Z coordinates from a point. By default the z value corresponding to the geometry, but" +
				"the user can choose a field in the table.";
	}

	public ObjectDriver evaluate(DataSourceFactory dsf, DataSource[] tables,
			Value[] values) throws ExecutionException {
		try {
			final SpatialDataSourceDecorator sds = new SpatialDataSourceDecorator(
					tables[0]);
			sds.open();
			boolean zNotGeometry = false;
			int fieldIndex = 0 ;
			final String fieldz;
			if (values.length==1) {
				// if no spatial's field's name is provided, the default (first)
				// one is arbitrarily chosen.
				final String spatialFieldName = values[0].toString();
				sds.setDefaultGeometry(spatialFieldName);
			}
			
			else if (values.length==2) {
				final String spatialFieldName = values[0].toString();
				sds.setDefaultGeometry(spatialFieldName);
				
				fieldz = values[1].toString();
				fieldIndex = sds.getFieldIndexByName(fieldz);
				zNotGeometry = true;
			}
			
			final ObjectMemoryDriver driver = new ObjectMemoryDriver(
					getMetadata(null));
			
			
			long nbOfRows = sds.getRowCount();
			for (long rowIndex = 0; rowIndex < nbOfRows; rowIndex++) {
				
				final Geometry geometry = sds.getGeometry(rowIndex);
				double x =0;
				double y = 0 ;
				double z=0 ;
				if (geometry instanceof Point) {
					Point p = (Point) geometry;
					x = p.getCoordinates()[0].x;
					y = p.getCoordinates()[0].y;
					z = p.getCoordinates()[0].z;
				
					
				}
				
				if (zNotGeometry){
					Value zValue = sds.getFieldValue(rowIndex, fieldIndex);
					driver.addValues(new Value[] { 
							ValueFactory.createValue(x),ValueFactory.createValue(y),
							ValueFactory.createValue(zValue.getAsDouble()) });
				}
				
				else if (Float.isNaN((float) z)) {
					driver.addValues(new Value[] { 
							ValueFactory.createValue(x),ValueFactory.createValue(y),
							ValueFactory.createValue(z) });
				}
				
				
			}
			sds.cancel();

			return driver;
		} catch (DriverLoadException e) {
			throw new ExecutionException(e);
		} catch (DriverException e) {
			throw new ExecutionException(e);
		}
	}

	public Metadata getMetadata(Metadata[] tables) throws DriverException {
		return new DefaultMetadata(new Type[] {
				TypeFactory.createType(Type.DOUBLE),TypeFactory.createType(Type.DOUBLE),TypeFactory.createType(Type.DOUBLE) }, new String[] {
				"x", "y", "z"});
	}

	public void validateTypes(Type[] types) throws IncompatibleTypesException {
		FunctionValidator.failIfBadNumberOfArguments(this, types, 0, 2);
		if (1 == types.length) {
			FunctionValidator.failIfNotOfType(this, types[0], Type.GEOMETRY);
		}
		else if (2 == types.length) {
			FunctionValidator.failIfNotOfType(this, types[0], Type.GEOMETRY);
			FunctionValidator.failIfNotNumeric(this, types[1], Type.DOUBLE);
			FunctionValidator.failIfNotNumeric(this, types[1], Type.FLOAT);
			FunctionValidator.failIfNotNumeric(this, types[1], Type.INT);
		}
	}

	public void validateTables(Metadata[] tables) throws SemanticException,
			DriverException {
		FunctionValidator.failIfBadNumberOfTables(this, tables, 1);
		FunctionValidator.failIfNotSpatialDataSource(this, tables[0], 0);
		
	}
}