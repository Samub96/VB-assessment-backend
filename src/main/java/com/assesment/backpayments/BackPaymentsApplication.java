package com.assesment.backpayments;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Punto de entrada de la aplicación y bootstrap de configuración global.
 */
@SpringBootApplication
@EnableFeignClients
public class BackPaymentsApplication {

    public static void main(String[] args) {
        SpringApplication.run(BackPaymentsApplication.class, args);
    }

}
