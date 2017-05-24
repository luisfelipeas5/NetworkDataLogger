package br.com.luisfelipeas5.networkdatalogger.model;

public class AppData {

    private String name;
    private long transmitted;
    private long received;

    public AppData(String name, long transmitted, long received) {
        this.name = name;
        this.transmitted = transmitted;
        this.received = received;
    }

    public String getName() {
        return name;
    }

    public String getShortName() {
        String[] names = name.split("\\.");
        if (names.length > 1) {
            return names[names.length - 1];
        }
        return name;
    }

    public long getTransmitted() {
        return transmitted;
    }

    public long getReceived() {
        return received;
    }
}
