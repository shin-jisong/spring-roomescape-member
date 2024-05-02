package roomescape.reservation.dao;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.repository.ReservationRepository;
import roomescape.reservation.domain.repository.ReservationTimeRepository;

public class FakeReservationTimeDao implements ReservationTimeRepository {
    private final Map<Long, ReservationTime> reservationTimes = new HashMap<>();
    private final ReservationRepository reservationRepository;

    public FakeReservationTimeDao(final ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    @Override
    public ReservationTime save(final ReservationTime reservationTime) {
        reservationTimes.put((long) reservationTimes.size() + 1, reservationTime);
        return new ReservationTime((long) reservationTimes.size(), reservationTime.getStartAt());
    }

    @Override
    public List<ReservationTime> findAll() {
        return reservationTimes.values()
                .stream()
                .toList();
    }

    @Override
    public Optional<ReservationTime> findById(final long timeId) {
        return Optional.of(reservationTimes.get(timeId));
    }

    @Override
    public boolean deleteById(final long timeId) {
        if (!reservationTimes.containsKey(timeId)) {
            return false;
        }
        reservationTimes.remove(timeId);
        return true;
    }

    @Override
    public boolean existsByStartAt(final LocalTime time) {
        List<ReservationTime> times = reservationTimes.values().stream()
                .filter(reservationTime -> reservationTime.getStartAt().equals(time))
                .toList();
        return !times.isEmpty();
    }

    @Override
    public Set<ReservationTime> findReservedTime(LocalDate date, long themeId) {
        return null;
    }
}
