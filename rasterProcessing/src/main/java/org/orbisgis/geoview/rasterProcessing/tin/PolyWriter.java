package org.orbisgis.geoview.rasterProcessing.tin;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.gdms.data.DataSource;
import org.gdms.data.SpatialDataSourceDecorator;
import org.gdms.driver.DriverException;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

class PolyWriter {
	private PrintWriter out;
	private SpatialDataSourceDecorator sds;

	private int vertexIdx;
	private List<Vertex> listOfVertices;
	private List<Edge> listOfEdges;
	private List<Coordinate> listOfHoles;

	PolyWriter(final File file, final DataSource dataSource)
			throws DriverException {
		sds = new SpatialDataSourceDecorator(dataSource);
		try {
			out = new PrintWriter(new FileOutputStream(file));
		} catch (FileNotFoundException e) {
			throw new DriverException(e);
		}
	}

	void write() throws DriverException {
		listOfVertices = new ArrayList<Vertex>();
		listOfEdges = new ArrayList<Edge>();
		listOfHoles = new ArrayList<Coordinate>();

		preProcess();

		// write node header part...
		out.printf("%d 2 1 0\n", listOfVertices.size());

		// write node body part...
		for (int pointIdx = 1; pointIdx <= listOfVertices.size(); pointIdx++) {
			out.printf("%d %g %g %d\n", pointIdx,
					listOfVertices.get(pointIdx).coordinate.x, listOfVertices
							.get(pointIdx).coordinate.y, listOfVertices
							.get(pointIdx).gid);
		}

		// write edge header part...
		out.printf("%d 0\n", listOfEdges.size());

		// write edge body part...
		for (int edgeIdx = 1; edgeIdx <= listOfEdges.size(); edgeIdx++) {
			out.printf("%d %d %d\n", edgeIdx,
					listOfEdges.get(edgeIdx).startVertexIdx, listOfEdges
							.get(edgeIdx).endVertexIdx);
		}

		// write hole header part...
		out.printf("%d\n", listOfHoles.size());

		// write hole body part...

		out.flush();
		out.close();
	}

	private void preProcess() throws DriverException {
		vertexIdx = 1;
		for (long rowIndex = 0; rowIndex < sds.getRowCount(); rowIndex++) {
			final Geometry g = sds.getGeometry(rowIndex);
			preProcess(g, rowIndex);
		}
	}

	private void preProcess(final Geometry g, final long rowIndex) {
		if (g instanceof Point) {
			preProcess((Point) g, rowIndex);
		} else if (g instanceof LineString) {
			preProcess((LineString) g, rowIndex);
		} else if (g instanceof Polygon) {
			preProcess((Polygon) g, rowIndex);
		} else {
			preProcess((GeometryCollection) g, rowIndex);
		}
	}

	private void preProcess(final Point p, final long rowIndex) {
		listOfVertices.add(new Vertex(p.getCoordinate(), rowIndex));
		vertexIdx++;
	}

	private void preProcess(final LineString ls, final long rowIndex) {
		for (int i = 0; i < ls.getNumPoints() - 1; i++) {
			listOfVertices.add(new Vertex(ls.getCoordinateN(i), rowIndex));
			vertexIdx++;

			listOfEdges.add(new Edge(vertexIdx, vertexIdx + 1));
		}
		// at least : add the linestring last vertex...
		listOfVertices.add(new Vertex(ls.getCoordinateN(ls.getNumPoints() - 1),
				rowIndex));
		vertexIdx++;
	}

	private void preProcess(final Polygon poly, final long rowIndex) {
	}

	private void preProcess(final GeometryCollection gc, final long rowIndex) {
		for (int i = 0; i < gc.getNumGeometries(); i++) {
			preProcess(gc.getGeometryN(i), rowIndex);
		}
	}

	void close() {
		out.close();
	}

	private class VertexComparator implements Comparator<Vertex> {
		public int compare(Vertex o1, Vertex o2) {
			final int compareX = TriangleUtilities.floatingPointCompare(
					o1.coordinate.x, o2.coordinate.x);
			final int compareY = TriangleUtilities.floatingPointCompare(
					o1.coordinate.y, o2.coordinate.y);
			if (0 == compareX) {
				return compareY;
			} else {
				return compareX;
			}
		}
	}

	private class Edge {
		long startVertexIdx;
		long endVertexIdx;

		Edge(long startVertexIdx, long endVertexIdx) {
			this.startVertexIdx = startVertexIdx;
			this.endVertexIdx = endVertexIdx;
		}
	}

	private class Vertex {
		Coordinate coordinate;
		long gid;

		Vertex(final Coordinate coordinate, final long gid) {
			this.coordinate = coordinate;
			this.gid = gid;
		}
	}
}