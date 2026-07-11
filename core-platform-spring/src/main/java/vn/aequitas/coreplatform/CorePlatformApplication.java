package vn.aequitas.coreplatform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Application entry point. Equivalent to the .NET {@code Program.cs} /
 * {@code WebApplication} bootstrap. Component scanning starts from this
 * package and covers the domain / application / infrastructure / web layers.
 */
@SpringBootApplication
public class CorePlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(CorePlatformApplication.class, args);
    }
}
