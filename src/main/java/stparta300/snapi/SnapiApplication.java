package stparta300.snapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing; // 추가함

@EnableJpaAuditing // ← 추가함
@SpringBootApplication
public class S