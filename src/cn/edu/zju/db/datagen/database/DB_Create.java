package cn.edu.zju.db.datagen.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/*
 * This class is used to create the database structure
 */
public class DB_Create {

    /*
     * Database names:
     */
    private static String DB_MOOVET = "moovetemplate";
    private static String DB_MOOVEW = "moovework";

    /*
     * Table names and attribute names:
     */
    protected static String T_ITEM = "item";
    protected static String ITEM_NAME = "item_name";
    protected static String ITEM_ITEMID = "item_id";
    protected static String ITEM_GLOBALID = "item_globalid";

    protected static String T_FLOORITEM = "flooritem";
    protected static String FLOORITEM_FLOORID = "floor_id";

    protected static String T_FLOOR = "floor";
    protected static String FLOOR_BUILDINGID = "building_id";

    protected static String T_BUILDING = "building";
    protected static String BUILDING_FILEID = "file_id";

    protected static String T_PARTITION = "partition";
    protected static String PART_GEOM = "part_geom";

    protected static String T_DECOMPREL = "decomprel";
    protected static String DECO_ORIGINAL = "deco_original";
    protected static String DECO_DECOMP = "deco_decomp";

    protected static String T_ACCESSPOINT = "accesspoint";
    protected static String AP_TYPE = "ap_type";
    protected static String AP_LOCATION = "ap_location";
    protected static String AP_LINE = "ap_line";

    protected static String T_CONNECTOR = "connector";
    protected static String CONN_UPPERPOINT = "conn_upperpoint";
    protected static String CONN_UPPERFLOOR = "conn_upperfloor";

    protected static String T_APTOPART = "aptopart";
    protected static String A2P_APID = "ap_id";
    protected static String A2P_PARTID = "part_id";

    protected static String T_CONTOPART = "contopart";
    protected static String C2P_CONID = "con_id";
    protected static String C2P_PARTID = "part_id";

    protected static String T_CONNECTIVITY = "connectivity";
    protected static String CON_APID = "con_apid";
    protected static String CON_PART1ID = "con_part1id";
    protected static String CON_PART2ID = "con_part2id";

    protected static String T_ACCESSRULE = "accessrule";
    protected static String ACC_ID = "acc_id";
    protected static String ACC_CONID = "acc_conid";
    protected static String ACC_NAME = "acc_name";
    protected static String ACC_DIRECTION = "direction";

    /*
     * Deletes and creates the database "moovetemplate". To be used when changes to the database design
     * are made.
     */
    public static void RecreateMooveTempalte(Connection con) throws SQLException {

        PreparedStatement pst = null;

        String reset =
                "DROP DATABASE IF EXISTS " + DB_MOOVET;
        pst = con.prepareStatement(reset);
        pst.execute();
        System.out.println("Delete : " + DB_MOOVET);

        String create =
                "CREATE DATABASE " + DB_MOOVET + " TEMPLATE=postgis_23_sample";
        pst = con.prepareStatement(create);
        pst.execute();
        System.out.println("Create : " + DB_MOOVET);
    }

    /*
     * Deletes and creates the working database, moovework, based on the template database, moovetemplate.
     */
    public static void RecreateMooveWork(Connection con) throws SQLException {
        // http://stackoverflow.com/questions/3524368/postgres-delete-all-tables-or-cascade-a-delete
        // Lav en template til at gendanne den brugbare DB

        PreparedStatement pst = null;

        String deleteDB =
                "DROP DATABASE IF EXISTS " + DB_MOOVEW;
        pst = con.prepareStatement(deleteDB);
        pst.execute();
        System.out.println("Delete : " + DB_MOOVEW);

        String recreateDB =
                "CREATE DATABASE " + DB_MOOVEW + " TEMPLATE " + DB_MOOVET;
        pst = con.prepareStatement(recreateDB);
        pst.execute();
        System.out.println("Create : " + DB_MOOVEW);
    }

    /*
     * Creates all tables for the database, "moovetemplate", this should be used to change the design
     * of this database.
     */
    public static void CreateTablesForMooveTemplate(Connection con) throws SQLException {

        PreparedStatement pst = null;

        String item =
                "CREATE TABLE IF NOT EXISTS " + T_ITEM + " (" +
                        ITEM_ITEMID + " serial NOT NULL PRIMARY KEY," +
                        ITEM_GLOBALID + " varchar(50)," +
                        ITEM_NAME + " varchar(100))" +
                        "WITH (OIDS=FALSE);" +
                        "ALTER TABLE " + T_ITEM + " OWNER TO postgres;";

        pst = con.prepareStatement(item);
        pst.execute();
        System.out.println("Created table: " + T_ITEM);

        String uploads = "CREATE TABLE IF NOT EXISTS uploads (" +
                                 "upload_id serial NOT NULL PRIMARY KEY," +
                                 "upload_file_name character varying(255) NOT NULL," +
                                 "upload_file_type character varying(255) NOT NULL," +
                                 "upload_binary_file bytea NOT NULL," +
                                 "upload_created timestamp DEFAULT 'now' NOT NULL," +
                                 "upload_edited timestamp NOT NULL," +
                                 "upload_description text)";

        pst = con.prepareStatement(uploads);
        pst.execute();
        System.out.println("Created table: uploads");

        String building =
                "CREATE TABLE IF NOT EXISTS " + T_BUILDING + " (" +
                        ITEM_ITEMID + " serial NOT NULL PRIMARY KEY, " +
                        BUILDING_FILEID + " integer," +
                        "CONSTRAINT building_to_file_fkey FOREIGN KEY (" + BUILDING_FILEID + ")" +
                        "REFERENCES uploads (upload_id) MATCH SIMPLE " +
                        "ON UPDATE CASCADE ON DELETE CASCADE)" +
                        "INHERITS (item)" +
                        "WITH (OIDS=FALSE);" +
                        "ALTER TABLE " + T_BUILDING + " OWNER TO postgres;";

        pst = con.prepareStatement(building);
        pst.execute();
        System.out.println("Created table: " + T_BUILDING);

        String floor =
                "CREATE TABLE IF NOT EXISTS " + T_FLOOR + " (" +
                        ITEM_ITEMID + " serial NOT NULL PRIMARY KEY, " +
                        FLOOR_BUILDINGID + " integer," +
                        "CONSTRAINT floor_to_building_fkey FOREIGN KEY (" + FLOOR_BUILDINGID + ")" +
                        "REFERENCES " + T_BUILDING + " (" + ITEM_ITEMID + ") MATCH SIMPLE " +
                        "ON UPDATE CASCADE ON DELETE CASCADE)" +
                        "INHERITS (" + T_ITEM + ")" +
                        "WITH (OIDS=FALSE);" +
                        "ALTER TABLE " + T_FLOOR + " OWNER TO postgres;";

        pst = con.prepareStatement(floor);
        pst.execute();
        System.out.println("Created table: " + T_FLOOR);

        String partition =
                "CREATE TABLE IF NOT EXISTS " + T_PARTITION + " (" +
                        ITEM_ITEMID + " serial NOT NULL PRIMARY KEY," +
                        FLOORITEM_FLOORID + " integer," +
                        "CONSTRAINT part_to_floor_fkey FOREIGN KEY (" + FLOORITEM_FLOORID + ")" +
                        "REFERENCES " + T_FLOOR + " (" + ITEM_ITEMID + ") MATCH SIMPLE " +
                        "ON UPDATE CASCADE ON DELETE CASCADE)" +
                        "INHERITS (" + T_ITEM + ")" +
                        "WITH (OIDS=FALSE);" +
                        "ALTER TABLE " + T_PARTITION + " OWNER TO postgres; " +
                        "SELECT AddGeometryColumn('" + T_PARTITION + "', '" + PART_GEOM + "', -1, 'POLYGON', 2 );" +
                        "CREATE INDEX partition_geom_gist ON " + T_PARTITION + " USING GIST (" + PART_GEOM + ");";

        pst = con.prepareStatement(partition);
        pst.execute();
        System.out.println("Created table: " + T_PARTITION);

        String decomprel =
                "CREATE TABLE IF NOT EXISTS " + T_DECOMPREL + " (" +
                        DECO_ORIGINAL + " integer NOT NULL," +
                        DECO_DECOMP + " integer NOT NULL," +
                        "CONSTRAINT deco_pkey PRIMARY KEY (" + DECO_ORIGINAL + "," + DECO_DECOMP + ")," +
                        "CONSTRAINT deco_original_fk FOREIGN KEY (" + DECO_ORIGINAL + ")" +
                        "REFERENCES " + T_PARTITION + " (" + ITEM_ITEMID + ") MATCH SIMPLE " +
                        "ON UPDATE CASCADE ON DELETE CASCADE," +
                        "CONSTRAINT deco_decomp_fk FOREIGN KEY (" + DECO_DECOMP + ")" +
                        "REFERENCES " + T_PARTITION + " (" + ITEM_ITEMID + ") MATCH SIMPLE " +
                        "ON UPDATE CASCADE ON DELETE CASCADE)" +
                        "WITH (OIDS=FALSE);" +
                        "ALTER TABLE " + T_DECOMPREL + " OWNER TO postgres;";

        pst = con.prepareStatement(decomprel);
        pst.execute();
        System.out.println("Created table: " + T_DECOMPREL);

        String accesspoint =
                "CREATE TABLE IF NOT EXISTS " + T_ACCESSPOINT + " (" +
                        ITEM_ITEMID + " serial NOT NULL PRIMARY KEY," +
                        FLOORITEM_FLOORID + " integer," +
                        AP_TYPE + " integer," +
                        "CONSTRAINT ap_To_floor_fkey FOREIGN KEY (" + FLOORITEM_FLOORID + ")" +
                        "REFERENCES " + T_FLOOR + " (" + ITEM_ITEMID + ") MATCH SIMPLE " +
                        "ON UPDATE CASCADE ON DELETE CASCADE)" +
                        "INHERITS (" + T_ITEM + ")" +
                        "WITH (OIDS=FALSE);" +
                        "ALTER TABLE " + T_ACCESSPOINT + " OWNER TO postgres;" +
                        "SELECT AddGeometryColumn('" + T_ACCESSPOINT + "', '" + AP_LOCATION + "', -1, 'POINT', 2 );" +
                        "SELECT AddGeometryColumn('" + T_ACCESSPOINT + "', '" + AP_LINE + "', -1, 'LINESTRING', 2 );" +
                        "CREATE INDEX ap_location_gist ON " + T_ACCESSPOINT + " USING GIST (" + AP_LOCATION + ");" +
                        "CREATE INDEX ap_line_gist ON " + T_ACCESSPOINT + " USING GIST (" + AP_LINE + ");";

        pst = con.prepareStatement(accesspoint);
        pst.execute();
        System.out.println("Created table: " + T_ACCESSPOINT);

        String aptopart =
                "CREATE TABLE IF NOT EXISTS " + T_APTOPART + " (" +
                        A2P_APID + " integer NOT NULL," +
                        A2P_PARTID + " integer NOT NULL," +
                        "CONSTRAINT aptopart_pkey PRIMARY KEY (" + A2P_APID + "," + A2P_PARTID + ")," +
                        "CONSTRAINT aptopart_ap_fk FOREIGN KEY (" + A2P_APID + ")" +
                        "REFERENCES " + T_ACCESSPOINT + " (" + ITEM_ITEMID + ") MATCH SIMPLE " +
                        "ON UPDATE CASCADE ON DELETE CASCADE," +
                        "CONSTRAINT aptopart_part_fk FOREIGN KEY (" + A2P_PARTID + ")" +
                        "REFERENCES " + T_PARTITION + " (" + ITEM_ITEMID + ") MATCH SIMPLE " +
                        "ON UPDATE CASCADE ON DELETE CASCADE)" +
                        "WITH (OIDS=FALSE);" +
                        "ALTER TABLE " + T_APTOPART + " OWNER TO postgres;";

        pst = con.prepareStatement(aptopart);
        pst.execute();
        System.out.println("Created table: " + T_APTOPART);

        String connectivity =
                "CREATE TABLE IF NOT EXISTS " + T_CONNECTIVITY + " (" +
                        CON_APID + " integer NOT NULL," +
                        CON_PART1ID + " integer NOT NULL," +
                        CON_PART2ID + " integer NOT NULL," +
                        "CONSTRAINT connectivity_pkey PRIMARY KEY (" + CON_APID + ")," +
                        "CONSTRAINT connectivity_ap_fk FOREIGN KEY (" + CON_APID + ")" +
                        "REFERENCES " + T_ACCESSPOINT + " (" + ITEM_ITEMID + ") MATCH SIMPLE " +
                        "ON UPDATE CASCADE ON DELETE CASCADE," +
                        "CONSTRAINT connectivity_part1_fk FOREIGN KEY (" + CON_PART1ID + ")" +
                        "REFERENCES " + T_PARTITION + " (" + ITEM_ITEMID + ") MATCH SIMPLE " +
                        "ON UPDATE CASCADE ON DELETE CASCADE," +
                        "CONSTRAINT connectivity_part2_fk FOREIGN KEY (" + CON_PART2ID + ")" +
                        "REFERENCES " + T_PARTITION + " (" + ITEM_ITEMID + ") MATCH SIMPLE " +
                        "ON UPDATE CASCADE ON DELETE CASCADE)" +
                        "WITH (OIDS=FALSE);" +
                        "ALTER TABLE " + T_APTOPART + " OWNER TO postgres;";

        pst = con.prepareStatement(connectivity);
        pst.execute();
        System.out.println("Created table: " + T_CONNECTIVITY);

//		String connector = 
//		"CREATE TABLE IF NOT EXISTS "+T_CONNECTOR+" (" +
//		ITEM_ITEMID +" serial NOT NULL PRIMARY KEY," +
//		FLOORITEM_FLOORID +" integer NOT NULL," +
//		CONN_UPPERFLOOR +" integer," +
//		"CONSTRAINT conn_floor_fk FOREIGN KEY ("+FLOORITEM_FLOORID+")" +
//		"REFERENCES "+T_FLOOR+" ("+ITEM_ITEMID+") MATCH SIMPLE " +
//		"ON UPDATE CASCADE ON DELETE CASCADE)" +
//		"INHERITS ("+T_ACCESSPOINT+")" +  
//		"WITH (OIDS=FALSE);" +
//		"ALTER TABLE "+T_CONNECTOR+" OWNER TO postgres;" + 
//		"SELECT AddGeometryColumn('"+T_CONNECTOR+"', '"+CONN_UPPERPOINT+"', -1, 'POINT', 2 );";

        String connector2 =
                "CREATE TABLE IF NOT EXISTS " + T_CONNECTOR + " ( " +
                        ITEM_ITEMID + " serial NOT NULL PRIMARY KEY," +
                        ITEM_GLOBALID + " varchar(50)," +
                        ITEM_NAME + " varchar(100)," +
                        FLOORITEM_FLOORID + " integer NOT NULL," +
                        AP_TYPE + " integer," +
                        CONN_UPPERFLOOR + " integer," +
                        "CONSTRAINT conn_floor_fk FOREIGN KEY (" + FLOORITEM_FLOORID + ")" +
                        "REFERENCES " + T_FLOOR + " (" + ITEM_ITEMID + ") MATCH SIMPLE " +
                        "ON UPDATE CASCADE ON DELETE CASCADE);" +
                        "ALTER TABLE " + T_CONNECTOR + " OWNER TO postgres;" +
                        "SELECT AddGeometryColumn('" + T_CONNECTOR + "', '" + AP_LOCATION + "', -1, 'POINT', 2);" +
                        "SELECT AddGeometryColumn('" + T_CONNECTOR + "', '" + CONN_UPPERPOINT + "', -1, 'POINT', 2);";

        pst = con.prepareStatement(connector2);
        pst.execute();
        System.out.println("Created table: " + T_CONNECTOR);
//		
//		pst = con.prepareStatement(connector);
//		pst.execute();
//		System.out.println("Created table: "+T_CONNECTOR);
//		
        String contopart =
                "CREATE TABLE IF NOT EXISTS " + T_CONTOPART + " (" +
                        C2P_CONID + " integer NOT NULL," +
                        C2P_PARTID + " integer NOT NULL," +
                        "CONSTRAINT contopart_pkey PRIMARY KEY (" + C2P_CONID + "," + C2P_PARTID + ")," +
                        "CONSTRAINT contopart_con_fk FOREIGN KEY (" + C2P_CONID + ")" +
                        "REFERENCES " + T_CONNECTOR + " (" + ITEM_ITEMID + ") MATCH SIMPLE " +
                        "ON UPDATE CASCADE ON DELETE CASCADE," +
                        "CONSTRAINT contopart_part_fk FOREIGN KEY (" + C2P_PARTID + ")" +
                        "REFERENCES " + T_PARTITION + " (" + ITEM_ITEMID + ") MATCH SIMPLE " +
                        "ON UPDATE CASCADE ON DELETE CASCADE)" +
                        "WITH (OIDS=FALSE);" +
                        "ALTER TABLE " + T_CONTOPART + " OWNER TO postgres;";

        pst = con.prepareStatement(contopart);
        pst.execute();
        System.out.println("Created table: " + T_CONTOPART);

        String accessrule =
                "CREATE TABLE IF NOT EXISTS " + T_ACCESSRULE + " (" +
                        ACC_ID + " serial NOT NULL PRIMARY KEY," +
                        ACC_NAME + " varchar(100)," +
                        ACC_CONID + " integer NOT NULL," +
                        ACC_DIRECTION + " boolean NOT NULL," +
                        "CONSTRAINT accessrule_aptopart_fk FOREIGN KEY (" + ACC_CONID + ")" +
                        "REFERENCES " + T_CONNECTIVITY + " (" + CON_APID + ") MATCH FULL " +
                        "ON UPDATE CASCADE ON DELETE CASCADE)" +
                        "WITH (OIDS=FALSE);" +
                        "ALTER TABLE " + T_ACCESSRULE + " OWNER TO postgres;";

        pst = con.prepareStatement(accessrule);
        pst.execute();
        System.out.println("Created table: " + T_ACCESSRULE);

        String trigger_func =
                "CREATE FUNCTION ins_connectivity() RETURNS trigger AS '" +
                        "BEGIN " +
                        "IF tg_op = ''INSERT'' THEN " +
                        "IF EXISTS (SELECT " + A2P_PARTID + " AS partid FROM " + T_APTOPART + " WHERE " + A2P_APID + " = NEW." + A2P_APID + " ) THEN " +
                        "INSERT INTO " + T_CONNECTIVITY + "(" + CON_APID + ", " + CON_PART1ID + ", " + CON_PART2ID + ") " +
                        "SELECT NEW." + A2P_APID + ", " + A2P_PARTID + ", NEW." + A2P_PARTID + " FROM " + T_APTOPART + " WHERE " + A2P_APID + " = NEW." + A2P_APID + ";" +
                        "END IF;" +
                        "RETURN new;" +
                        "END IF;" +
                        "END" +
                        "' LANGUAGE plpgsql;";

        pst = con.prepareStatement(trigger_func);
        pst.execute();

        String trigger = "CREATE TRIGGER trig_a2p_connectivity BEFORE INSERT " +
                                 "ON " + T_APTOPART + " FOR each ROW " +
                                 "EXECUTE PROCEDURE ins_connectivity();";

        pst = con.prepareStatement(trigger);
        pst.execute();
        System.out.println("Created trigger");
    }
}