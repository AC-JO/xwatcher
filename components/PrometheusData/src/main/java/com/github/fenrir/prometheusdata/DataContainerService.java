package com.github.fenrir.prometheusdata;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DataContainerService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DataContainerService.class);
    public DataContainerService(){
        LOGGER.info("DataContainerService ...");
    }

    private static final Map<String, DataContainer> dataContainerMap = new ConcurrentHashMap<>();

    public static void registerDataContainer(DataContainer dataContainer){
        dataContainerMap.put(dataContainer.getName(), dataContainer);
    }

    public String getMetricPlainTextString(String name){
        DataContainer dataContainer = dataContainerMap.getOrDefault(name, null);
        if(dataContainer != null){
            return dataContainer.getMetricPlainTextString();
        }
        return null;
    }
}
