package roomescape.service;

import org.springframework.stereotype.Service;
import roomescape.domain.ReservationDate;
import roomescape.domain.Theme;
import roomescape.dto.SaveThemeRequest;
import roomescape.repository.ThemeRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class ThemeService {
    private final ThemeRepository themeRepository;

    public ThemeService(ThemeRepository themeRepository) {
        this.themeRepository = themeRepository;
    }

    public List<Theme> getThemes() {
        return themeRepository.findAll();
    }

    public Theme saveTheme(final SaveThemeRequest saveThemeRequest) {
        return themeRepository.save(saveThemeRequest.toTheme());
    }

    public void deleteTheme(final Long themeId) {
        validateThemeExist(themeId);

        themeRepository.deleteById(themeId);
    }

    private void validateThemeExist(final Long themeId) {
        if (!themeRepository.existById(themeId)) {
            throw new NoSuchElementException("해당 id의 테마 정보가 존재하지 않습니다.");
        }
    }


    public List<Theme> getPopularThemes() {
        final ReservationDate startAt = new ReservationDate(LocalDate.now().minusDays(7));
        final ReservationDate endAt = new ReservationDate(LocalDate.now().minusDays(1));
        final int maximumThemeCount = 10;

        return themeRepository.findPopularThemes(startAt, endAt, maximumThemeCount);
    }
}