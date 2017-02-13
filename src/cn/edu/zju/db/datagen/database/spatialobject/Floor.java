package cn.edu.zju.db.datagen.database.spatialobject;

import cn.edu.zju.db.datagen.algorithm.ReferencePoint;
import cn.edu.zju.db.datagen.indoorobject.station.Station;
import cn.edu.zju.db.datagen.spatialgraph.D2DGraph;
import org.khelekore.prtree.PRTree;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Floor extends Item {

    private Building building = new Building();
    private ArrayList<Partition> partitions = new ArrayList<Partition>();
    private ArrayList<AccessPoint> accessPoints = new ArrayList<AccessPoint>();
    private ArrayList<Connector> connectors = new ArrayList<Connector>();
    private Set<Floor> conFloors = new HashSet<Floor>();
    private ArrayList<Station> stations = new ArrayList<Station>();

    private List<ReferencePoint> referencePoints = new ArrayList<ReferencePoint>();

    private ArrayList<Partition> partsAfterDecomposed = new ArrayList<Partition>();
    private PRTree<Partition> partitionsRTree;
    private PRTree<Station> stationsRTree;
    private D2DGraph d2dGraph;

    public Floor() {
    }

    public ArrayList<Connector> getConnectors() {
        return connectors;
    }

    public void setConnectors(ArrayList<Connector> connectors) {
        this.connectors = connectors;
    }

    public ArrayList<AccessPoint> getAccessPoints() {
        return accessPoints;
    }

    public void setAccessPoints(ArrayList<AccessPoint> aps) {
        this.accessPoints = aps;
    }

    public ArrayList<Partition> getPartitions() {
        return partitions;
    }

    public void setPartitions(ArrayList<Partition> parts) {
        this.partitions = parts;
    }

    public ArrayList<Partition> getPartsAfterDecomposed() {
        return partsAfterDecomposed;
    }

    public void setPartsAfterDecomposed(ArrayList<Partition> parts) {
        partsAfterDecomposed = parts;
    }

    public Building getBuilding() {
        return building;
    }

    public void setBuilding(Building building) {
        this.building = building;
    }

    public Set<Floor> getConFloors() {
        return this.conFloors;
    }

    public void setConFloors(Set<Floor> floors) {
        this.conFloors = floors;
    }

    public ArrayList<Station> getStations() {
        return this.stations;
    }

    public void setStations(ArrayList<Station> stations) {
        this.stations = stations;
    }

    public PRTree<Partition> getPartitionsRTree() {
        return partitionsRTree;
    }

    public void setPartitionsRTree(PRTree<Partition> rtree) {
        this.partitionsRTree = rtree;
    }

    public PRTree<Station> getStationsRTree() {
        return stationsRTree;
    }

    public void setStationsRTree(PRTree<Station> rtree) {
        this.stationsRTree = rtree;
    }

    public D2DGraph getD2dGraph() {
        return d2dGraph;
    }

    public void setD2dGraph(D2DGraph d2dGraph) {
        this.d2dGraph = d2dGraph;
    }

    @Override
    public String toString() {
        return this.name;
    }

    public Partition getOutdoorPart() {
        for (Partition p : partitions) {
            if (p.getName().equals("OUTDOOR")) {
                return p;
            }
        }
        return null;
    }

    public List<ReferencePoint> getReferencePoints() {
        return referencePoints;
    }

    public void setReferencePoints(List<ReferencePoint> referencePoints) {
        this.referencePoints = referencePoints;
    }
}
