package bluepill.server.dto.logroom;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class DayLogTimeSlot {
    private Integer timeSlot;
    private List<DayLogEntry> entries;
}
