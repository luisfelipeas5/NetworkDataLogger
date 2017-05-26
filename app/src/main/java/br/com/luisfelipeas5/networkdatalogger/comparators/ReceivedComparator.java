package br.com.luisfelipeas5.networkdatalogger.comparators;

import br.com.luisfelipeas5.networkdatalogger.model.AppData;

public class ReceivedComparator implements java.util.Comparator<AppData> {
    @Override
    public int compare(AppData o1, AppData o2) {
        long received1 = o1.getReceived();
        long received2 = o2.getReceived();

        if (received1 > received2) {
            return -1;
        } else if (received1 < received2) {
            return 1;
        }
        return 0;
    }
}
