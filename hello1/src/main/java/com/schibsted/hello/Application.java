package com.schibsted.hello;

import io.prometheus.client.spring.boot.EnablePrometheusEndpoint;
import io.prometheus.client.spring.boot.ExportMetricsToPrometheus;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication()
@EnablePrometheusEndpoint()
@ExportMetricsToPrometheus()
public class Application {
  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }
}
