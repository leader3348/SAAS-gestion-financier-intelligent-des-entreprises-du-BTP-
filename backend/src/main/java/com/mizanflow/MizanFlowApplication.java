package com.mizanflow;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
@SpringBootApplication @EnableScheduling @ConfigurationPropertiesScan
public class MizanFlowApplication { public static void main(String[] a){ SpringApplication.run(MizanFlowApplication.class,a);} }
