package br.com.luisfelipeas5.networkdatalogger.comparators;

import br.com.luisfelipeas5.networkdatalogger.model.AppData;

public class TransmittedComparator implements java.util.Comparator<AppData> {
    @Override
    public int compare(AppData o1, AppData o2) {
        long transmitted1 = o1.getTransmitted();
        long transmitted2 = o2.getTransmitted();

        if (transmitted1 > transmitted2) {
            return -1;
        } else if (transmitted1 < transmitted2) {
            return 1;
        }
        return 0;
    }
}
