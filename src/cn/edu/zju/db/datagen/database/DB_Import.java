package cn.edu.zju.db.datagen.database;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import cn.edu.zju.db.datagen.database.spatialobject.AccessPoint;
import cn.edu.zju.db.datagen.database.spatialobject.Floor;
import cn.edu.zju.db.datagen.gui.VITA_Application;
import cn.edu.zju.db.datagen.ifc.dataextraction.ExtractBuildings;
import cn.edu.zju.db.datagen.ifc.dataextraction.ExtractDoors;
import cn.edu.zju.db.datagen.ifc.dataextraction.ExtractElevators;
import cn.edu.zju.db.datagen.ifc.dataextraction.ExtractFloors;
import cn.edu.zju.db.datagen.ifc.dataextraction.ExtractPartitions;
import cn.edu.zju.db.datagen.ifc.dataextraction.ExtractStairs;
import cn.edu.zju.db.datagen.ifc.dataextraction.IfcFileParser;
import cn.edu.zju.db.datagen.ifc.dataextraction.spatialobject.Building;
import cn.edu.zju.db.datagen.ifc.dataextraction.spatialobject.Door;
import cn.edu.zju.db.datagen.ifc.dataextraction.spatialobject.Elevator;
import cn.edu.zju.db.datagen.ifc.dataextraction.spatialobject.Partition;
import cn.edu.zju.db.datagen.ifc.dataextraction.spatialobject.Stair;
import cn.edu.zju.db.datagen.ifc.datamanipulation.Decomposition;
import cn.edu.zju.db.datagen.ifc.datamanipulation.Grid;
import cn.edu.zju.db.datagen.ifc.datamanipulation.Mapping;

public class DB_Import {

	private static boolean decompBeforeMapping = false;

	/*
	 * Runs every import method for a specific IFC file
	 */
	public static void importAll(Connection con, Integer fileID, File file) throws SQLException {

		try {
			IfcFileParser.readFile(file);
		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("You have extracted:");
		// datagen.gui.VITA.consoleArea.setText("You have extracted:" + "\n");
		importBuilding(con, fileID);
		ArrayList<cn.edu.zju.db.datagen.ifc.dataextraction.spatialobject.Floor> floors = importFloors(con);
		ArrayList<Partition> partitions = importPartitions(con);
		ArrayList<Door> doors = importDoors(con);
		importStairs(con, floors, partitions); // NEEDS FIXING FROM EXTRACTION
		// importElevators(con, floors, partitions); NEEDS FIXING FROM
		// EXTRACTION
		offsetAlignment(con, partitions, doors);

		DB_WrapperLoad.loadALL(con, fileID);
		if (decompBeforeMapping) { // because some partition is bad...set it to
									// be false
			decompose(con);
			DB_WrapperLoad.loadALL(con, fileID);
		}

		long startTime = System.nanoTime();
		mapD2P(con);
		long stopTime = System.nanoTime();
		long elapsedTime = stopTime - startTime;
		System.out.println("Elapsed time after mapping: " + elapsedTime / 1000000 + "ms");

		file.delete();
	}

	private static void offsetAlignment(Connection con, ArrayList<Partition> partitions, ArrayList<Door> doors) {
		Double minX = Double.MAX_VALUE;
		Double minY = Double.MAX_VALUE;
		for (Partition part : partitions) {
			for (Point2D.Double p : part.getPolyline()) {
				if (p.getX() < minX)
					minX = p.getX();
				if (p.getY() < minY)
					minY = p.getY();
			}
		}
		for (Door door : doors) {
			Point2D.Double p1 = (java.awt.geom.Point2D.Double) door.getRepLine().getP1();
			Point2D.Double p2 = (java.awt.geom.Point2D.Double) door.getRepLine().getP2();

			if (p1.getX() < minX)
				minX = p1.getX();
			if (p2.getX() < minX)
				minX = p2.getX();
			if (p1.getY() < minY)
				minY = p1.getY();
			if (p2.getY() < minY)
				minY = p2.getY();
		}

		Double absMinX = Math.abs(minX);
		Double absMinY = Math.abs(minY);

		// Adjust partition coordinates
		for (Partition part : partitions) {
			ArrayList<Point2D.Double> newPolyline = new ArrayList<Point2D.Double>();
			for (Point2D.Double p : part.getPolyline()) {
				if (minX < 0 && minY < 0)
					newPolyline.add(new Point2D.Double(p.getX() + absMinX, p.getY() + absMinY));
				else if (minX < 0 && minY >= 0)
					newPolyline.add(new Point2D.Double(p.getX() + absMinX, p.getY() - absMinY));
				else if (minX >= 0 && minY < 0)
					newPolyline.add(new Point2D.Double(p.getX() - absMinX, p.getY() + absMinY));
				else if (minX >= 0 && minY >= 0)
					newPolyline.add(new Point2D.Double(p.getX() - absMinX, p.getY() - absMinY));
			}
			try {
				DB_WrapperInsert.insertPartition(con, part.getName(), part.getGlobalID(), part.getFloorID(),
						newPolyline);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		// Adjust access point coordinate
		for (Door d : doors) {
			Point2D.Double placement = null;
			Line2D.Double line = null;
			Point2D.Double p1 = null;
			Point2D.Double p2 = null;
			if (minX < 0 && minY < 0) {
				placement = new Point2D.Double(d.getFinalCoord().getX() + absMinX, d.getFinalCoord().getY() + absMinY);
				p1 = new Point2D.Double(d.getRepLine().getP1().getX() + absMinX,
						d.getRepLine().getP1().getY() + absMinY);
				p2 = new Point2D.Double(d.getRepLine().getP2().getX() + absMinX,
						d.getRepLine().getP2().getY() + absMinY);
				line = new Line2D.Double(p1, p2);
			} else if (minX < 0 && minY >= 0) {
				placement = new Point2D.Double(d.getFinalCoord().getX() + absMinX, d.getFinalCoord().getY() - absMinY);
				p1 = new Point2D.Double(d.getRepLine().getP1().getX() + absMinX,
						d.getRepLine().getP1().getY() - absMinY);
				p2 = new Point2D.Double(d.getRepLine().getP2().getX() + absMinX,
						d.getRepLine().getP2().getY() - absMinY);
				line = new Line2D.Double(p1, p2);
			} else if (minX >= 0 && minY < 0) {
				placement = new Point2D.Double(d.getFinalCoord().getX() - absMinX, d.getFinalCoord().getY() + absMinY);
				p1 = new Point2D.Double(d.getRepLine().getP1().getX() - absMinX,
						d.getRepLine().getP1().getY() + absMinY);
				p2 = new Point2D.Double(d.getRepLine().getP2().getX() - absMinX,
						d.getRepLine().getP2().getY() + absMinY);
				line = new Line2D.Double(p1, p2);
			} else if (minX >= 0 && minY >= 0) {
				placement = new Point2D.Double(d.getFinalCoord().getX() - absMinX, d.getFinalCoord().getY() - absMinY);
				p1 = new Point2D.Double(d.getRepLine().getP1().getX() - absMinX,
						d.getRepLine().getP1().getY() - absMinY);
				p2 = new Point2D.Double(d.getRepLine().getP2().getX() - absMinX,
						d.getRepLine().getP2().getY() - absMinY);
				line = new Line2D.Double(p1, p2);
			}

			try {
				DB_WrapperInsert.insertAccessPointWithGeom(con, d.getName(), placement, d.getGlobalID(), d.getFloorID(),
						0, line);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/*
	 * Import buildings
	 */
	private static void importBuilding(Connection con, Integer fileID) throws SQLException {
		List<Building> buildings = ExtractBuildings.GetAllBuildingObjects();

		for (int i = 0; i < buildings.size(); i++) {
			Building b = buildings.get(i);
			DB_WrapperInsert.insertBuilding(con, b.getName(), b.getGlobalID(), fileID);
		}
	}

	/*
	 * Import floors
	 */
	private static ArrayList<cn.edu.zju.db.datagen.ifc.dataextraction.spatialobject.Floor> importFloors(Connection con)
			throws SQLException {

		ArrayList<cn.edu.zju.db.datagen.ifc.dataextraction.spatialobject.Floor> floors = ExtractFloors
				.GetAllFloorObjects();

		for (int i = 0; i < floors.size(); i++) {
			cn.edu.zju.db.datagen.ifc.dataextraction.spatialobject.Floor f = floors.get(i);
			DB_WrapperInsert.insertFloor(con, f.getName(), f.getGlobalID(), f.getBuildingID());
			DB_WrapperInsert.insertPartition(con, "OUTDOOR", new String("N/A-floor(" + f.getGlobalID() + ")"),
					f.getGlobalID(), new ArrayList<Point2D.Double>());
		}

		return floors;
	}

	private static ArrayList<Point2D.Double> checkIntermediate(Connection con, ArrayList<Point2D.Double> points) {

		// interList contains possible intermediate points
		ArrayList<Point2D.Double> interList = new ArrayList<Point2D.Double>();
		ArrayList<Integer> indexList = new ArrayList<Integer>();

		for (int i = 0; i < points.size(); i++) {
			// If interList is empty the first point of the polyline is added
			if (interList.isEmpty() || interList.size() == 1) {
				interList.add(points.get(i));
			} else if (interList.size() == 2) {
				interList.add(points.get(i));
				Line2D.Double line = new Line2D.Double(interList.get(0), interList.get(2));
				Boolean intersects = null;
				try {
					intersects = DB_WrapperSpatial.ST_Intersects_Line_Point(con, line, interList.get(1));
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (intersects) {
					indexList.add(i - 1);
					interList.remove(1);
				} else {
					interList.remove(0);
				}
			}
		}

		for (int j = indexList.size() - 1; j >= 0; j--) {
			points.remove((int) indexList.get(j));
		}

		// Check if start/end point is intermediate
		Line2D.Double line = new Line2D.Double(points.get(1), points.get(points.size() - 2));
		Boolean intersects = null;
		try {
			intersects = DB_WrapperSpatial.ST_Intersects_Line_Point(con, line, points.get(0));
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (intersects) {
			points.remove(points.size() - 1);
			points.remove(0);
			Point2D.Double newStart = points.get(0);
			points.add(new Point2D.Double(newStart.getX(), newStart.getY()));
		}
		return points;
	}

	private static ArrayList<Partition> importPartitions(Connection con) throws SQLException {
		ArrayList<Partition> partitions = ExtractPartitions.GetAllPartitionObjects(con);

		for (int i = 0; i < partitions.size(); i++) {
			Partition r = partitions.get(i);
			Grid.normalize(r);
			r.setPolyline(checkIntermediate(con, r.getPolyline()));
			DB_WrapperInsert.insertPartition(con, r.getName(), r.getGlobalID(), r.getFloorID(), r.getPolyline());
		}

		return partitions;
	}

	/*
	 * Import doors and connect them to mapped partitions
	 */
	private static ArrayList<Door> importDoors(Connection con) throws SQLException {

		ArrayList<Door> doors = (ArrayList<Door>) ExtractDoors.GetAllDoorObjects(con);

		for (int i = 0; i < doors.size(); i++) {
			Door d = doors.get(i);
			DB_WrapperInsert.insertAccessPointWithGeom(con, d.getName(), d.getFinalCoord(), d.getGlobalID(),
					d.getFloorID(), 0, d.getRepLine());
			DB_WrapperInsert.flushPartitionForAPConnections(con, d.getGlobalID()); // Lazy
																					// update
		}
		return doors;
	}

	// before use, call DB_WrapperLoad.loadAll() firstly
	public static void decompose(Connection con) throws SQLException {
		VITA_Application.txtConsoleArea
				.append("\nBefore decomposed partitions size is: " + DB_WrapperLoad.partitionDecomposedT.size() + "\n");

		for (cn.edu.zju.db.datagen.database.spatialobject.Partition p : DB_WrapperLoad.partitionT) {
			if (p.getPolygonGIS() != null && p.getPolygonGIS().numPoints() > 0) {
				Decomposition.decomposePart(con, p);
			}
		}
		VITA_Application.txtConsoleArea.append("After Decomposition, there are: \n");
		for (Floor floor : DB_WrapperLoad.floorT) {
			VITA_Application.txtConsoleArea
					.append(floor.getName() + " " + floor.getPartsAfterDecomposed().size() + " partitions\n");

		}
		VITA_Application.txtConsoleArea
				.append("In total: " + DB_WrapperLoad.partitionDecomposedT.size() + " partitions\n");

	}

	public static void mapD2P(Connection con) throws SQLException {
		int zero = 0;
		int one = 0;
		int two = 0;
		int more = 0;
		for (AccessPoint ap : DB_WrapperLoad.accesspointT) {
			if (ap.getApType().equals(0)) {
				ArrayList<cn.edu.zju.db.datagen.database.spatialobject.Partition> connParts = Mapping.MapD2P(con, ap);
				for (cn.edu.zju.db.datagen.database.spatialobject.Partition part : connParts) {
					DB_WrapperInsert.connectPartAndAP(con, part.getGlobalID(), ap.getGlobalID());
				}

				if (connParts.size() == 0) {
					zero = zero + 1;
				} else if (connParts.size() == 1) {
					one = one + 1;
				} else if (connParts.size() == 2) {
					two = two + 1;
				} else if (connParts.size() > 2) {
					more = more + 1;
				}
			}
		}
		System.out.println("------------------------");
		System.out.println("Mappings:");
		System.out.println("0: " + zero);
		System.out.println("1: " + one);
		System.out.println("2: " + two);
		System.out.println("2+: " + more);
		/*
		 * gui.IfcManagerApplet.consoleArea.append("-------------------" +
		 * "\n"); gui.IfcManagerApplet.consoleArea.append("Mappings: " + "\n");
		 * gui.IfcManagerApplet.consoleArea.append("0: " + zero + "\n");
		 * gui.IfcManagerApplet.consoleArea.append("1: " + one + "\n");
		 * gui.IfcManagerApplet.consoleArea.append("2: " + two + "\n");
		 * gui.IfcManagerApplet.consoleArea.append("2+: " + more + "\n");
		 */
	}

	/*
	 * Import stairs - Its assumed that all stairs have a connection to their
	 * 'lower' floor, but not always to the 'upper' floor. Hence, a connector is
	 * only created if an 'upper' floor exists for the stair. If this is the
	 * case, the two connectors are connected through the many-to-many
	 * relationship.
	 */
	public static void importStairs(Connection con,
			ArrayList<cn.edu.zju.db.datagen.ifc.dataextraction.spatialobject.Floor> floors,
			ArrayList<Partition> partitions) throws SQLException {

		List<Stair> stairs = ExtractStairs.GetAllStairObjects(floors, partitions);

		for (Stair st : stairs) {
			ArrayList<String> floorids = new ArrayList<String>();
			floorids.add(st.getFloorID());
			floorids.add(st.getUpperFloorID());

			ArrayList<String> partIDs = new ArrayList<String>();
			partIDs.add(st.getPartID());
			partIDs.add(st.getUpperPartID());
			Integer conid = DB_WrapperInsert.insertConnector(con, st.getName(), st.getGlobalID(), st.getCoord(),
					st.getFloorID(), st.getUpperFloorCoords(), st.getUpperFloorID());
			// Insert aptopart connection
			if (conid != 0) {
				DB_WrapperInsert.connectPartAndCon(con, st.getPartID(), conid);
				if (st.getUpperPartID() != null) {
					DB_WrapperInsert.connectPartAndCon(con, st.getUpperPartID(), conid);
				}
			}
		}
	}

	/*
	 * Import elevators - A connector is created for every partition that the
	 * elevator connects. These are connected to each other afterwards, to
	 * resemble the actual elevator.
	 */
	public static void importElevators(Connection con,
			ArrayList<cn.edu.zju.db.datagen.ifc.dataextraction.spatialobject.Floor> floors,
			ArrayList<Partition> partitions) throws SQLException {

		List<Elevator> elevators = ExtractElevators.GetAllElevatorObjects(floors, partitions);

		for (Elevator el : elevators) {
			ArrayList<Integer> connids = new ArrayList<Integer>();

			for (int i = 0; i < el.getConnFloorIDs().size() - 1; i++) {
				connids.add(DB_WrapperInsert.insertConnector(con, "placeholder", el.getGlobalID(), el.getCoord(),
						el.getConnFloorIDs().get(i), el.getCoord(), el.getConnFloorIDs().get(i + 1)));
			}

			for (int i = 0; i < connids.size(); i++) {
				DB_WrapperInsert.connectPartAndCon(con, el.getConnPartitionIDs().get(i), connids.get(i));
				DB_WrapperInsert.connectPartAndCon(con, el.getConnPartitionIDs().get(i + 1), connids.get(i));
			}
		}
	}
}
