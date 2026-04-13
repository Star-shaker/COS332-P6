import java.util.ArrayList;
import java.time.LocalDate;

public class ReminderHandler
{
    // This will hold all the events that an email should be sent for
    public ArrayList<Event> reminders;

    ReminderHandler()
    {
        this.reminders = new ArrayList<>();
        EventReader reader = new EventReader("events.txt");
        
        LocalDate today = LocalDate.now();
        LocalDate targetDate = today.plusDays(6);
        
        int targetDay = targetDate.getDayOfMonth();
        int targetMonth = targetDate.getMonthValue();

        for (Event event : reader.eventList) 
        {
            if (event.day == targetDay && event.month == targetMonth) 
            {
                reminders.add(event);
            }
        }
    }
}