package io.javabrains.coronavirustracker.services;


import io.javabrains.coronavirustracker.models.LocationStats;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

@Service
public class CoronaVirusDataService {
    private final Logger logger = LoggerFactory.getLogger(CoronaVirusDataService.class);
    public List<LocationStats> getAllStats() {
        return allStats;
    }

    private static String VIRUS_DATA_URL = "https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_covid19_confirmed_global.csv";

    private List<LocationStats> allStats = new ArrayList<>();
    @PostConstruct //WHEN YOU CONSTRUCT THE INSTANCE OF THIS SERVICE, EXECUTE THIS METHOD AFTER YOU ARE DONE CONSTRUCTING
    @Scheduled(cron = "0 0 */1 * * *") //RUN EVERY 1HR OF THE DAY SO IT WILL NOT OUT DATE (second, minute, hr, day)
    public void fetchVirusData() throws IOException, InterruptedException { //SENDING A EXCEPTION IF THE CLIENT REQUEST FAILS
        logger.info("Let's check for updates!");
        List<LocationStats> newStats = new ArrayList<>();
        HttpClient client = HttpClient.newHttpClient(); //CREATING A CLIENT
        HttpRequest request = HttpRequest.newBuilder() //CREATED THE REQUEST
                .uri(URI.create(VIRUS_DATA_URL)) //grabbing the URL
                .build(); //building it
        HttpResponse<String> httpResponse = client.send(request, HttpResponse.BodyHandlers.ofString()); //THIS IS GOING TO GIVE ME AN HTTP RESPONSE. TAKE THE BODY AND TURN IT INTO A STRING

        StringReader csvBodyReader = new StringReader(httpResponse.body()); //StringReader is an instance of reader. Used to parse a string
        Iterable<CSVRecord> records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(csvBodyReader); //PARSING IT USING THE OPEN SOURCE LIBRARY THAT WE GOT
        for (CSVRecord record : records) { //LOOPING THROUGH THE RECORDS AFTER IT IS PARSED CSV
           /* String state = record.get("Province/State"); //PULLING OUT ONE COLUMN VALUE AND PRINTING IT
            System.out.println(state);*/
            LocationStats locationStat = new LocationStats();
            locationStat.setState(record.get("Province/State"));
            locationStat.setCountry(record.get("Country/Region"));
           // locationStat.setLatestTotalCases(Integer.parseInt(record.get(record.size() -1)));
            int latestCases = Integer.parseInt(record.get(record.size() -1));
            int prevDayCases = Integer.parseInt(record.get(record.size() -2));
            locationStat.setLatestTotalCases(latestCases);
            locationStat.setDiffFromPrevDay(latestCases - prevDayCases);
            //System.out.println(locationStat);
            newStats.add(locationStat);
        }

        this.allStats = newStats; //AFTER IT EXECUTES THE LOOP, GIVE ME NEW STATS
    }

}
