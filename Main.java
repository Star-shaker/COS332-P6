public class Main {
    public static void main(String[] args) {
        ReminderHandler reminderHandler = new ReminderHandler();

        MailClient mailClient = new MailClient(25);
        try {
            mailClient.connect();
        } catch (Exception e) {
            System.out.println("Failed to open SMTP connection: " + e.getMessage());
        }

        String from = "test@mg.com";
        String[] to = {"subscribedaccount1@mg.com", "subscribedaccount2@mg.com"};
        String subject = "Upcoming Events Reminder";
        String data = "";

        for (Event event : reminderHandler.reminders) 
        {
            data += event.day + "/" + event.month + " " + event.description + "\n";
        }

        mailClient.sendMail(from, to, subject, data);
        mailClient.closeConnection();
    }
}
