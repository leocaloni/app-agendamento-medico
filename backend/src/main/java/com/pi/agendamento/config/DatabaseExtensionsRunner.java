package com.pi.agendamento.config;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;


@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class DatabaseExtensionsRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseExtensionsRunner.class);

    private final DataSource dataSource;

    public DatabaseExtensionsRunner(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void run(String... args) {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("CREATE EXTENSION IF NOT EXISTS unaccent");
            log.info("Extensao unaccent disponivel");
        } catch (SQLException ex) {
            log.error("Nao foi possivel criar a extensao unaccent — a busca por nome de medico"
                    + " (GET /api/doctors?name=...) vai falhar. Crie manualmente com:"
                    + " CREATE EXTENSION IF NOT EXISTS unaccent;", ex);
        }
    }
}
