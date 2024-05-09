package roomescape.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
import roomescape.member.domain.repository.MemberRepository;
import roomescape.member.dto.LoginMember;
import roomescape.reservation.dao.FakeMemberDao;
import roomescape.reservation.dao.FakeReservationDao;
import roomescape.reservation.dao.FakeReservationTimeDao;
import roomescape.reservation.dao.FakeThemeDao;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.domain.repository.ReservationRepository;
import roomescape.reservation.domain.repository.ReservationTimeRepository;
import roomescape.reservation.domain.repository.ThemeRepository;
import roomescape.reservation.dto.ReservationRequest;
import roomescape.reservation.dto.ReservationResponse;

@DisplayName("예약 로직 테스트")
class ReservationServiceTest {
    ReservationRepository reservationRepository;
    ReservationTimeRepository reservationTimeRepository;
    ThemeRepository themeRepository;
    MemberRepository memberRepository;
    ReservationService reservationService;

    Member member;
    Theme theme;
    ReservationTime reservationTime;

    @BeforeEach
    void setUp() {
        reservationRepository = new FakeReservationDao(reservationTimeRepository, themeRepository);
        reservationTimeRepository = new FakeReservationTimeDao(reservationRepository);
        themeRepository = new FakeThemeDao(reservationRepository);
        memberRepository = new FakeMemberDao();
        reservationService = new ReservationService(
                reservationRepository,
                reservationTimeRepository,
                themeRepository,
                memberRepository
        );

        reservationTime = reservationTimeRepository.save(new ReservationTime(LocalTime.MIDNIGHT));
        theme = themeRepository.save(new Theme("name", "description", "thumbnail"));
        member = memberRepository.save(new Member("name", "email@email.com", "Password", Role.MEMBER));
    }

    @DisplayName("예약 조회에 성공한다.")
    @Test
    void find() {
        //given
        LocalDate date = LocalDate.now().plusYears(1);

        reservationService.create(new ReservationRequest(date.toString(), reservationTime.getId(), theme.getId()),
                new LoginMember(member.getId(), member.getName(), member.getEmail(), member.getRole()));

        //when
        List<ReservationResponse> reservations = reservationService.findAllReservations();

        //then
        assertAll(
                () -> assertThat(reservations).hasSize(1),
                () -> assertThat(reservations.get(0).date()).isEqualTo(date),
                () -> assertThat(reservations.get(0).time().id()).isEqualTo(reservationTime.getId()),
                () -> assertThat(reservations.get(0).time().startAt()).isEqualTo(reservationTime.getStartAt())
        );
    }

    @DisplayName("예약 생성에 성공한다.")
    @Test
    void create() {
        //given
        String date = "2100-04-18";
        ReservationRequest reservationRequest = new ReservationRequest(date, reservationTime.getId(), theme.getId());

        //when
        ReservationResponse reservationResponse = reservationService.create(reservationRequest,
                new LoginMember(1, "test", "test@email.com", Role.MEMBER));

        //then
        assertAll(
                () -> assertThat(reservationResponse.date()).isEqualTo(date),
                () -> assertThat(reservationResponse.time().id()).isEqualTo(reservationTime.getId())
        );
    }

    @DisplayName("존재하지 않는 시간으로 예약할 시 예외가 발생한다.")
    @Test
    void NotExistTimeReservation() {
        //given
        String date = "2099-04-18";
        reservationRepository.save(new Reservation(1L, LocalDate.parse(date), reservationTime, theme));

        ReservationRequest reservationRequest = new ReservationRequest(date, reservationTime.getId() + 1, theme.getId());

        //when & then
        assertThatThrownBy(() -> reservationService.create(reservationRequest,
                new LoginMember(member.getId(), member.getName(), member.getEmail(), member.getRole())))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("존재하지 않는 테마로 예약할 시 예외가 발생한다.")
    @Test
    void NotExistThemeReservation() {
        //given
        String date = "2099-04-18";

        ReservationRequest reservationRequest = new ReservationRequest(date, reservationTime.getId(), theme.getId() + 1);

        //when & then
        assertThatThrownBy(() -> reservationService.create(reservationRequest,
                new LoginMember(member.getId(), member.getName(), member.getEmail(), member.getRole())))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("날짜, 시간, 테마가 중복되게 예약할 시 예외가 발생한다.")
    @Test
    void duplicateReservation() {
        //given
        String date = "2099-04-18";
        long timeId = reservationTime.getId();
        long themeId = theme.getId();

        ReservationRequest reservationRequest = new ReservationRequest(date, timeId, themeId);
        reservationService.create(reservationRequest,
                new LoginMember(member.getId(), member.getName(), member.getEmail(), member.getRole()));

        //when & then
        assertThatThrownBy(() -> reservationService.create(reservationRequest,
                new LoginMember(member.getId(), member.getName(), member.getEmail(), member.getRole())))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("예약 삭제에 성공한다.")
    @Test
    void delete() {
        //given
        String date = "2099-04-18";
        long timeId = reservationTime.getId();
        long themeId = theme.getId();

        ReservationRequest reservationRequest = new ReservationRequest(date, timeId, themeId);
        ReservationResponse reservation = reservationService.create(reservationRequest,
                new LoginMember(member.getId(), member.getName(), member.getEmail(), member.getRole()));

        //when
        reservationService.delete(reservation.id());

        //then
        assertThat(reservationRepository.findAll()).hasSize(0);
    }

    @DisplayName("존재하지 않는 예약 삭제 시 예외가 발생한다.")
    @Test
    void deleteNotExistReservation() {
        // give & when & then
        assertThatThrownBy(() -> reservationService.delete(5L))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
