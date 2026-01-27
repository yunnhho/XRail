package com.dev.XRail;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableJpaAuditing // BaseTimeEntity 작동용
@EnableScheduling  // 대기열 스케줄러 작동용
@SpringBootApplication
public class XRailApplication {

	public static void main(String[] args) {
		SpringApplication.run(XRailApplication.class, args);
	}

}
