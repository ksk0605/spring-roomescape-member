package roomescape.repository;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public class H2ReservationRepository implements ReservationRepository {
    private final NamedParameterJdbcTemplate template;

    public H2ReservationRepository(final NamedParameterJdbcTemplate template) {
        this.template = template;
    }

    @Override
    public List<Reservation> findAll() {
        final String sql = """
                SELECT 
                r.id as reservation_id, r.name as reservation_name, r.date as reservation_date, 
                rt.id as time_id, rt.start_at as reservation_time, 
                th.id as theme_id, th.name as theme_name, th.description as theme_description, th.thumbnail as theme_thumbnail 
                FROM reservation as r 
                inner join reservation_time as rt 
                on r.time_id = rt.id 
                inner join theme as th 
                on r.theme_id = th.id
                """;

        return template.query(sql, itemRowMapper());
    }

    private RowMapper<Reservation> itemRowMapper() {
        return ((rs, rowNum) -> Reservation.createInstance(
                rs.getLong("reservation_id"),
                rs.getString("reservation_name"),
                rs.getDate("reservation_date").toLocalDate(),
                new ReservationTime(rs.getLong("time_id"), rs.getTime("reservation_time").toLocalTime()),
                Theme.of(
                        rs.getLong("theme_id"),
                        rs.getString("theme_name"),
                        rs.getString("theme_description"),
                        rs.getString("theme_thumbnail"))
        ));
    }

    @Override
    public Optional<Reservation> findById(final Long reservationId) {
        final String sql = """
                SELECT 
                r.id as reservation_id, r.name as reservation_name, r.date as reservation_date, 
                rt.id as time_id, rt.start_at as reservation_time, 
                th.id as theme_id, th.name as theme_name, th.description as theme_description, th.thumbnail as theme_thumbnail 
                FROM reservation as r 
                inner join reservation_time as rt 
                on r.time_id = rt.id 
                inner join theme as th 
                on r.theme_id = th.id 
                WHERE r.id = :reservationId
                """;

        try {
            final MapSqlParameterSource param = new MapSqlParameterSource()
                    .addValue("reservationId", reservationId);
            final Reservation reservation = template.queryForObject(sql, param, itemRowMapper());

            return Optional.of(reservation);
        } catch (final EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Reservation save(final Reservation reservation) {
        final String sql = "INSERT INTO reservation(name, date, time_id, theme_id) VALUES (:name, :date, :timeId, :themeId)";
        final MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("name", reservation.getClientName().getValue())
                .addValue("date", reservation.getDate().getValue())
                .addValue("timeId", reservation.getTime().getId())
                .addValue("themeId", reservation.getTheme().getId());
        final KeyHolder keyHolder = new GeneratedKeyHolder();
        template.update(sql, param, keyHolder);

        final long savedReservationId = keyHolder.getKey().longValue();

        return reservation.copyWithId(savedReservationId);
    }

    @Override
    public void deleteById(final Long reservationId) {
        final String sql = "DELETE FROM reservation WHERE id = :id";
        final MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("id", reservationId);
        template.update(sql, param);
    }

    @Override
    public boolean existByDateAndTimeIdAndThemeId(final LocalDate date, final Long timeId, final Long themeId) {
        final String sql = "SELECT EXISTS(SELECT 1 FROM reservation WHERE date = :date AND time_id = :timeId AND :themeId)";
        final MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("date", date)
                .addValue("timeId", timeId)
                .addValue("themeId", themeId);

        return Boolean.TRUE.equals(template.queryForObject(sql, param, Boolean.class));
    }

    @Override
    public boolean existByTimeId(final Long reservationTimeId) {
        final String sql = "SELECT EXISTS(SELECT 1 FROM reservation WHERE time_id = :timeId)";
        final MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("timeId", reservationTimeId);

        return Boolean.TRUE.equals(template.queryForObject(sql, param, Boolean.class));
    }

    @Override
    public List<Reservation> findAllByDateAndThemeId(final LocalDate date, final Long themeId) {
        final String sql = """
                SELECT 
                r.id as reservation_id, r.name as reservation_name, r.date as reservation_date, 
                rt.id as time_id, rt.start_at as reservation_time, 
                th.id as theme_id, th.name as theme_name, th.description as theme_description, th.thumbnail as theme_thumbnail 
                FROM reservation as r 
                inner join reservation_time as rt 
                on r.time_id = rt.id 
                inner join theme as th 
                on r.theme_id = th.id 
                WHERE date = :date and theme_id = :themeId
                """;

        final MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("date", date)
                .addValue("themeId", themeId);

        return template.query(sql, param, itemRowMapper());
    }
}
