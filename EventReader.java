import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class EventReader 
{
    public ArrayList<Event> eventList;

    public EventReader(String fileName) 
    {
        this.eventList = new ArrayList<>();
    }

    public void readFile(String fileName) 
    {
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) 
        {
            String line;
            
            while ((line = reader.readLine()) != null) 
            {
                String[] parts = line.split(" ", 2);
                String[] dateParts = parts[0].split("/");

                try 
                {
                    int day = Integer.parseInt(dateParts[0]);
                    int month = Integer.parseInt(dateParts[1]);
                    String description = parts[1];

                    eventList.add(new Event(day, month, description));
                } 
                catch (NumberFormatException e) 
                {
                    System.out.println("Could not parse date numbers");
                }
            }
        } 
        catch (IOException e) 
        {
            System.out.println("Could not read file: " + e.getMessage());
        }
    }
}