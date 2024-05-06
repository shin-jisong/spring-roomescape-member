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
import roomescape.reservation.dao.FakeReservationDao;
import roomescape.reservation.dao.FakeReservationTimeDao;
import roomescape.reservation.dao.FakeThemeDao;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.domain.repository.ReservationRepository;
import roomescape.reservation.domain.repository.ReservationTimeRepository;
import roomescape.reservation.domain.repository.ThemeRepository;
import roomescape.reservation.dto.ThemeRequest;
import roomescape.reservation.dto.ThemeResponse;

@DisplayName("테마 로직 테스트")
class ThemeServiceTest {
    ReservationRepository reservationRepository;
    ReservationTimeRepository reservationTimeRepository;
    ThemeRepository themeRepository;
    ThemeService themeService;

    @BeforeEach
    void setData() {
        reservationRepository = new FakeReservationDao(reservationTimeRepository, themeRepository);
        reservationTimeRepository = new FakeReservationTimeDao(reservationRepository);
        themeRepository = new FakeThemeDao(reservationRepository);

        themeService = new ThemeService(themeRepository);
    }

    @DisplayName("테마 조회에 성공한다.")
    @Test
    void findAll() {
        //given
        long id = 1;
        String name = "name";
        String description = "description";
        String thumbnail = "thumbnail";
        Theme theme = new Theme(id, name, description, thumbnail);
        themeRepository.save(theme);
        //when
        List<ThemeResponse> themes = themeService.findAllThemes();

        //then
        assertAll(
                () -> assertThat(themes).hasSize(1),
                () -> assertThat(themes.get(0).name()).isEqualTo(name),
                () -> assertThat(themes.get(0).description()).isEqualTo(description),
                () -> assertThat(themes.get(0).thumbnail()).isEqualTo(thumbnail)
        );
    }

    @DisplayName("테마 생성에 성공한다.")
    @Test
    void create() {
        //given
        String name = "name";
        String description = "description";
        String thumbnail = "thumbnail";
        ThemeRequest themeRequest = new ThemeRequest(name, description, thumbnail);

        //when
        ThemeResponse themeResponse = themeService.create(themeRequest);

        //then
        assertAll(
                () -> assertThat(themeResponse.name()).isEqualTo(name),
                () -> assertThat(themeResponse.thumbnail()).isEqualTo(thumbnail),
                () -> assertThat(themeResponse.description()).isEqualTo(description)
        );
    }

    @DisplayName("테마 삭제에 성공한다.")
    @Test
    void delete() {
        //given
        long id = 1;
        String name = "name";
        String description = "description";
        String thumbnail = "thumbnail";
        Theme theme = new Theme(id, name, description, thumbnail);
        themeRepository.save(theme);

        //when
        themeService.delete(id);

        //then
        assertThat(themeRepository.findAll()).hasSize(0);
    }

    @DisplayName("존재하지 않는 테마를 삭제할 경우 예외가 발생한다.")
    @Test
    void deleteNotExistTheme() {
        // given & when & then
        assertThatThrownBy(() -> themeService.delete(1L))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("인기 테마 찾기에 성공한다.")
    @Test
    void findPopularThemes() {
        //given
        String name1 = "name1";
        String description1 = "description1";
        String thumbnail1 = "thumbnail1";
        themeService.create(new ThemeRequest(name1, description1, thumbnail1));

        String name2 = "name2";
        String description2 = "description2";
        String thumbnail2 = "thumbnail2";
        themeService.create(new ThemeRequest(name2, description2, thumbnail2));

        reservationRepository.save(new Reservation(1L, "siso", LocalDate.of(2100, 5, 5),
                new ReservationTime(1L, LocalTime.now()),
                new Theme(1L, name1, description1, thumbnail1)));

        //when
        List<ThemeResponse> themeResponses = themeService.findPopularThemes();

        //then
        assertAll(
                () -> assertThat(themeResponses.size()).isEqualTo(1),
                () -> assertThat(themeResponses.get(0)).isEqualTo(
                        new ThemeResponse(1L, name1, description1, thumbnail1))
        );
    }
}
