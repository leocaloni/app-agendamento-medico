package com.pi.agendamento.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.pi.agendamento.entity.Doctor;

// acesso a medicos e busca com filtros
public interface DoctorRepository extends JpaRepository<Doctor, UUID> {

    // indica se o crm ja esta cadastrado na uf
    boolean existsByCrmNumberAndCrmUf(String crmNumber, String crmUf);

    // busca o medico pelo usuario
    Optional<Doctor> findByUserId(UUID userId);

    // busca paginada so de ids e nota; COUNT(DISTINCT) porque os joins de especialidade e convenio repetem cada review
    @Query(value = """
            SELECT d.id AS doctor_id,
                   u.full_name AS name,
                   COALESCE(AVG(r.rating), 0) AS rating,
                   COUNT(DISTINCT r.id) AS reviews
            FROM doctor d
            JOIN users u ON u.id = d.user_id
            LEFT JOIN doctor_specialty ds ON ds.doctor_id = d.id
            LEFT JOIN doctor_health_plan dhp ON dhp.doctor_id = d.id
            LEFT JOIN review r ON r.doctor_id = d.id
            WHERE (CAST(:name AS text) IS NULL
                   OR unaccent(lower(u.full_name)) LIKE unaccent(lower(concat('%', CAST(:name AS text), '%'))))
              AND (CAST(:specialtyId AS uuid) IS NULL OR ds.specialty_id = CAST(:specialtyId AS uuid))
              AND (CAST(:city AS text) IS NULL OR lower(d.city) = lower(CAST(:city AS text)))
              AND (CAST(:state AS text) IS NULL OR lower(d.state) = lower(CAST(:state AS text)))
              AND (CAST(:healthPlanId AS uuid) IS NULL OR dhp.health_plan_id = CAST(:healthPlanId AS uuid))
              AND u.active = true
            GROUP BY d.id, u.full_name
            HAVING (CAST(:minRating AS double precision) IS NULL
                    OR COALESCE(AVG(r.rating), 0) >= CAST(:minRating AS double precision))
            """,
            countQuery = """
            SELECT COUNT(*) FROM (
                SELECT d.id
                FROM doctor d
                JOIN users u ON u.id = d.user_id
                LEFT JOIN doctor_specialty ds ON ds.doctor_id = d.id
                LEFT JOIN doctor_health_plan dhp ON dhp.doctor_id = d.id
                LEFT JOIN review r ON r.doctor_id = d.id
                WHERE (CAST(:name AS text) IS NULL
                       OR unaccent(lower(u.full_name)) LIKE unaccent(lower(concat('%', CAST(:name AS text), '%'))))
                  AND (CAST(:specialtyId AS uuid) IS NULL OR ds.specialty_id = CAST(:specialtyId AS uuid))
                  AND (CAST(:city AS text) IS NULL OR lower(d.city) = lower(CAST(:city AS text)))
                  AND (CAST(:state AS text) IS NULL OR lower(d.state) = lower(CAST(:state AS text)))
                  AND (CAST(:healthPlanId AS uuid) IS NULL OR dhp.health_plan_id = CAST(:healthPlanId AS uuid))
                  AND u.active = true
                GROUP BY d.id
                HAVING (CAST(:minRating AS double precision) IS NULL
                        OR COALESCE(AVG(r.rating), 0) >= CAST(:minRating AS double precision))
            ) total
            """,
            nativeQuery = true)
    Page<Object[]> search(
            @Param("name") String name,
            @Param("specialtyId") UUID specialtyId,
            @Param("city") String city,
            @Param("state") String state,
            @Param("minRating") Double minRating,
            @Param("healthPlanId") UUID healthPlanId,
            Pageable pageable);

    // segunda query da busca: carrega as entidades dos ids da pagina de uma vez, evitando N+1 nas relacoes lazy
    @Query("""
            SELECT DISTINCT d FROM Doctor d
            JOIN FETCH d.user
            LEFT JOIN FETCH d.specialties
            LEFT JOIN FETCH d.acceptedPlans
            WHERE d.id IN :ids
            """)
    List<Doctor> findAllWithDetails(@Param("ids") Collection<UUID> ids);

    // busca o medico com usuario, especialidades e convenios
    @Query("""
            SELECT d FROM Doctor d
            JOIN FETCH d.user
            LEFT JOIN FETCH d.specialties
            LEFT JOIN FETCH d.acceptedPlans
            WHERE d.id = :id
            """)
    Optional<Doctor> findByIdWithDetails(@Param("id") UUID id);
}
