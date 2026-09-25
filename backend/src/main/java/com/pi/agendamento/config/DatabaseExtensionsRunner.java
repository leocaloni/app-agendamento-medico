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

/**
 * Objetos de banco que o Hibernate nao sabe gerar a partir das entidades.
 *
 * O projeto nao tem Flyway, entao nao ha migration onde por isto. Como o ddl-auto eh
 * create-drop, o schema eh recriado a cada start e estes comandos rodam de novo — todos
 * sao idempotentes. Roda antes do DevSeedRunner.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class DatabaseExtensionsRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseExtensionsRunner.class);

    // Busca por nome de medico sem sensibilidade a acento (fase 04).
    private static final String UNACCENT = "CREATE EXTENSION IF NOT EXISTS unaccent";

    // btree_gist permite combinar igualdade de uuid com sobreposicao de intervalo
    // no mesmo indice GiST, o que a constraint abaixo exige.
    private static final String BTREE_GIST = "CREATE EXTENSION IF NOT EXISTS btree_gist";

    /**
     * Protecao real contra agendamento sobreposto sob concorrencia.
     *
     * A UNIQUE(doctor_id, start_at) declarada na entidade so pega colisao de inicio exato:
     * uma primeira consulta de 50min as 10:00 e um retorno de 20min as 10:20 passam por ela.
     * A checagem de sobreposicao no service resolve o caso sequencial, mas duas requisicoes
     * simultaneas leem o banco antes de qualquer uma gravar — so uma constraint separa as duas.
     *
     * O predicado WHERE status = 'SCHEDULED' eh essencial: sem ele, uma consulta cancelada
     * continuaria bloqueando o horario para sempre.
     *
     * tstzrange eh meio-aberto [inicio, fim), entao consultas encostadas (10:00-10:50 e
     * 10:50-11:10) nao conflitam.
     *
     * Nao da para declarar isto em @Table: JPA nao tem EXCLUDE.
     */
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
            // Nao derruba o start para nao esconder o resto da aplicacao, mas o aviso
            // precisa ser barulhento: sem isto o sistema fica com um buraco silencioso.
            log.error("Banco: falha ao criar {} — {}. SQL: {}", what, consequence, sql, ex);
        }
    }
}
