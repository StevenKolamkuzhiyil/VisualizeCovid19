package com.stevenkolamkuzhiyil.Covid19.util;

import com.opencsv.CSVReader;
import com.stevenkolamkuzhiyil.Covid19.exception.OutdatedDataException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Stream;

public class ReadCSV {

    /**
     * The expected header of the csv file.
     */
    final List<String> header;
    /**
     * The url to the csv file.
     */
    final String urlStr;

    public ReadCSV(List<String> header, String urlStr) {
        this.header = header;
        this.urlStr = urlStr;
    }

    public List<Map<String, Object>> read() throws IOException {
        List<Map<String, Object>> dataList = new ArrayList<>();

        URL url = new URL(urlStr);
        BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
        CSVReader reader = new CSVReader(in);

        String[] currentHeader = reader.readNext();
        if (!headerMatches(currentHeader)) throw new OutdatedDataException("Header information is outdated.");

        String[] nextLine;
        while ((nextLine = reader.readNext()) != null) {
            Map<String, Object> record = mapFromRecord(currentHeader, nextLine);
            addNewestRecordToList(dataList, record);
        }
        in.close();
        reader.close();

        return dataList;
    }

    /**
     * @param header The header of the csv file.
     * @param record A line of the csv file which holds a record.
     * @return A key value pair collection of the record.
     * @brief Create a map from a csv record line.
     */
    private Map<String, Object> mapFromRecord(String[] header, String[] record) {
        Map<String, Object> map = new HashMap<>();
        if (header.length != record.length) return null;

        for (int i = 0; i < header.length; i++) {
            map.put(header[i], record[i]);
        }
        return map;
    }

    private void addNewestRecordToList(List<Map<String, Object>> list, Map<String, Object> record) {
        Map<String, Object> matchingRecord = list.stream()
                .filter(e -> e.get("geoId").equals(record.get("geoId")))
                .findFirst().orElse(null);

        if (matchingRecord == null) {
            list.add(record);
            return;
        }

        if (isNewerRecord((String) matchingRecord.get("dateRep"), (String) record.get("dateRep"))) {
            list.remove(matchingRecord);
            list.add(record);
        }

    }

    private boolean headerMatches(String[] otherHeader) {
        return Stream.of(otherHeader).allMatch(header::contains);
    }

    private boolean isNewerRecord(String date1, String date2) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        try {
            Date d1 = sdf.parse(date1);
            Date d2 = sdf.parse(date2);
            return d1.compareTo(d2) < 0;
        } catch (ParseException ignore) {
        }

        return false;
    }
}
