package com.shop.dashboard;

import com.shop.service.module.util.BannerBuilder;
import de.codecentric.boot.admin.server.config.EnableAdminServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableAdminServer
@SpringBootApplication
public class DashboardApplication {

    public static void main(String[] args) {

        SpringApplication springApplication = new SpringApplication(DashboardApplication.class);
        springApplication.setBanner(new BannerBuilder());
        springApplication.run(args);
    }

}
