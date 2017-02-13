package cn.edu.zju.db.datagen.indoorobject.station;

public class WIFI extends Station {

    double rssiAt1 = -30;

    @Override
    public void getRSSI() {
        // TODO Auto-generated method stub

    }

    @Override
    public void run() {
        // TODO Auto-generated method stub

    }

    /* RSSI(dBM) = -10nlog10(d) + A
     * where n is between 2.7 to 4.3, d is distance, A is RSSI at 1 meter
     * http://electronics.stackexchange.com/questions/83354/calculate-distance-from-rssi
     * A Practical Path Loss Model For Indoor WiFi Positioning Enhancement--ntu.edu.sg
     * */
    @Override
    public Pack createPackage(double distance) {
        double value = (-10) * 3 * Math.log10(distance) + rssiAt1;
        double noise = Math.random() * 4;
        value -= noise;
        int RSSI = (int) value;
        RSSIPackage pack = new RSSIPackage(this.currentLocation, getId(), System.currentTimeMillis(), RSSI);
        return pack;
    }


}
