package bluepill.server.repository.logroom;

import java.time.LocalDate;
import java.util.List;

public interface LogPhotoRepositoryCustom {

    List<DayLogRow> findDayLog(Long roomId, LocalDate date);
}
