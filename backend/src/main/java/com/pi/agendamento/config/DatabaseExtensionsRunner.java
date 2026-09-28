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

// cria no banco o que o hibernate nao gera: extensoes e a constraint de sobreposicao
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class DatabaseExtensionsRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseExtensionsRunner.class);

    // busca por nome de medico sem diferenciar acento
    private static final String UNACCENT = "CREATE EXTENSION IF NOT EXISTS unaccent";

    // permite uuid e intervalo no mesmo indice gist da constraint abaixo
    private static final String BTREE_GIST = "CREATE EXTENSION IF NOT EXISTS btree_gist";

    // barra sobreposicao sob concorrencia, que o service nao pega; so SCHEDULED, para liberar horario cancelado
    private static final String NO_OVERLAP = """
            DO $$
            BEGIN
                ALTER TABLE appointment ADD CONSTRAINT uk_appointment_no_overlap
                    EXCLUDE USING gist (
                        doctor_id WITH =,
                        tstzrange(start_at, end_at) WITH &&
                    ) WHERE (status = 'SCHEDULED');
            EXCEPTION
                WHEN duplicate_table THEN NULL;
                WHEN duplicate_object THEN NULL;
            END $$
            """;

    private final DataSource dataSource;

    public DatabaseExtensionsRunner(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    // executa os comandos idempotentes no start
    @Override
    public void run(String... args) {
        execute(UNACCENT, "extensao unaccent",
                "a busca por nome de medico (GET /api/doctors?name=...) vai falhar");
        execute(BTREE_GIST, "extensao btree_gist",
                "a constraint de sobreposicao de horario nao sera criada");
        execute(NO_OVERLAP, "constraint uk_appointment_no_overlap",
                "consultas sobrepostas criadas simultaneamente nao serao barradas pelo banco");
    }

    private void execute(String sql, String what, String consequence) {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute(sql);
            log.info("Banco: {} ok", what);
        } catch (SQLException ex) {
            // nao derruba o start, mas loga como erro
            log.error("Banco: falha ao criar {} — {}. SQL: {}", what, consequence, sql, ex);
        }
    }
}
