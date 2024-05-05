package roomescape.reservation.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.member.domain.Member;
import roomescape.member.domain.repository.MemberRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.domain.repository.ReservationRepository;
import roomescape.reservation.domain.repository.ReservationTimeRepository;
import roomescape.reservation.domain.repository.ThemeRepository;
import roomescape.reservation.dto.ReservationRequest;
import roomescape.reservation.dto.ReservationResponse;

@Service
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;

    public ReservationService(ReservationRepository reservationRepository,
                              ReservationTimeRepository reservationTimeRepository, ThemeRepository themeRepository,
                              MemberRepository memberRepository) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
    }

    public List<ReservationResponse> findAllReservations() {
        return reservationRepository.findAll().stream().map(ReservationResponse::from).toList();
    }

    public ReservationResponse create(ReservationRequest reservationRequest) {
        ReservationTime reservationTime = reservationTimeRepository.findById(reservationRequest.timeId());
        Theme theme = themeRepository.findById(reservationRequest.themeId());
        LocalDate date = LocalDate.parse(reservationRequest.date());

        validateTimeExist(reservationTime, reservationRequest.timeId());
        validateThemeExist(theme, reservationRequest.themeId());
        validateReservationDuplicate(reservationRequest, date);

        Member member = memberRepository.save(new Member(reservationRequest.name()));
        Reservation reservation = reservationRepository.save(
                new Reservation(reservationRequest.name(), date, reservationTime, theme));
        reservationRepository.saveReservationList(member.getId(), reservation.getId());

        return ReservationResponse.from(reservation);
    }

    public void delete(long reservationId) {
        if (!reservationRepository.deleteById(reservationId)) {
            throw new IllegalArgumentException(String.format("잘못된 예약입니다. id=%d를 확인해주세요.", reservationId));
        }
    }

    private void validateTimeExist(ReservationTime reservationTime, long timeId) {
        if (reservationTime == null) {
            throw new IllegalArgumentException(
                    String.format("잘못된 예약 시간입니다. id=%d를 확인해주세요.", timeId));
        }
    }

    private void validateThemeExist(Theme theme, long themeId) {
        if (theme == null) {
            throw new IllegalArgumentException(
                    String.format("잘못된 테마입니다. id=%d를 확인해주세요.", themeId));
        }
    }

    private void validateReservationDuplicate(ReservationRequest reservationRequest, LocalDate date) {
        if (reservationRepository.existBy(date, reservationRequest.timeId(), reservationRequest.themeId())) {
            throw new IllegalArgumentException("예약 시간이 중복되었습니다.");
        }
    }
}
