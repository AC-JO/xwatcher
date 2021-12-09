package com.github.fenrir.xservertopologybuilder.configs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfig {

    private final String natsAddress;
    private final String xregistryAddress;
    private final String neo4jAddress;

    public ApplicationConfig(@Value("${XMessaging.NatsAddress}") String natsAddress,
                             @Value("${XServerTopologyBuilder.xregistry}") String xregistryAddress,
                             @Value("${Neo4j.address}") String neo4jAddress){
        this.natsAddress = natsAddress;
        this.xregistryAddress = xregistryAddress;
        this.neo4jAddress = neo4jAddress;
    }
}