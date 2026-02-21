package com.hospital.api_gateway;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.gateway.server.mvc.filter.BeforeFilterFunctions;
import org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import static org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions.route;
import static org.springframework.web.servlet.function.RequestPredicates.path;

@SpringBootApplication
@EnableDiscoveryClient
public class ApiGatewayApplication {

	@Value("${services.patient.url:http://localhost:8081}")
	private String patientServiceUrl;

	@Value("${services.appointment.url:http://localhost:8082}")
	private String appointmentServiceUrl;

	@Value("${services.medical-record.url:http://localhost:8083}")
	private String medicalRecordServiceUrl;

	@Value("${services.config-server.url:http://localhost:8888}")
	private String configServerUrl;

	public static void main(String[] args) {
		SpringApplication.run(ApiGatewayApplication.class, args);
	}

	@Bean
	public RouterFunction<ServerResponse> patientRoute() {
		return route("patient-service")
				.route(path("/api/patients/**"), HandlerFunctions.http())
				.before(BeforeFilterFunctions.uri(patientServiceUrl))
				.build();
	}

	@Bean
	public RouterFunction<ServerResponse> appointmentRoute() {
		return route("appointment-service")
				.route(path("/api/appointments/**"), HandlerFunctions.http())
				.before(BeforeFilterFunctions.uri(appointmentServiceUrl))
				.build();
	}

	@Bean
	public RouterFunction<ServerResponse> medicalRecordRoute() {
		return route("medical-record-service")
				.route(path("/api/medical-records/**"), HandlerFunctions.http())
				.before(BeforeFilterFunctions.uri(medicalRecordServiceUrl))
				.build();
	}

}
