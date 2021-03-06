package com.github.fenrir.xtraceprocessor.services;

import com.github.fenrir.xtraceprocessor.processors.Processor;
import com.github.fenrir.xtraceprocessor.processors.tracebuilder.TraceBuilderProcessor;
import com.github.fenrir.xtraceprocessor.processors.persistence.PersistenceProcessor;
import com.github.fenrir.xtraceprocessor.entities.trace.Span;
import com.github.fenrir.xtraceprocessor.entities.trace.OpenTelemetryTraceData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Service
public class ProcessorService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessorService.class);

    private final PersistenceProcessor persistenceFilter;
    private final Processor rootFilter;

    public ProcessorService(){
        this.persistenceFilter = new PersistenceProcessor();
        this.rootFilter = new TraceBuilderProcessor();
    }

    private final ThreadPoolExecutor filterProcessorExecutor = new ThreadPoolExecutor(
            10, 10000, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<>()
    );

    private int num = 0;

    public void reportOtelTraceData(OpenTelemetryTraceData traceData){
        if(traceData == null) return;
        for(OpenTelemetryTraceData.ResourceSpan resourceSpan : traceData.resourceSpans){
            for(OpenTelemetryTraceData.InstrumentationLibrarySpan instrumentationLibrarySpan : resourceSpan.instrumentationLibrarySpans){
                for(int i = 0; i < instrumentationLibrarySpan.spans.length; i++){
                    Span _span = Span.create(resourceSpan.resource, instrumentationLibrarySpan.spans[i]);
                    filterProcessorExecutor.submit(() -> {
                        this.persistenceFilter.doProcess(_span);
                        // this.rootFilter.doProcess(_span);
                    });
                }
            }
        }
    }

    public Set<String> getServiceNames(){
        return this.persistenceFilter.getServiceNames();
    }

    public Set<String> getServiceInterfaceNames(String serviceName){
        return this.persistenceFilter.getServiceInterfaceNames(serviceName);
    }

    public Map<String, Set<String>> getDownstreamInterfaceNames(String serviceName, String interfaceName) {
        return this.persistenceFilter.getDownstreamInterfaceNames(serviceName, interfaceName);
    }
}
