public class Main {
    public static void main(String[] args) {
        MailClient mailClient = new MailClient(25);
        try {
            mailClient.connect();
        } catch (Exception e) {
            System.out.println("Failed to open SMTP connection: " + e.getMessage());
        }

        String from = "test@mg.com";
        String[] to = {"subscribedaccount1@mg.com", "subscribedaccount2@mg.com"};
        String subject = "Birthday 1";
        String data = "Piet verjaars on tommorows";
        mailClient.sendMail(from, to, subject, data);
        mailClient.closeConnection();

    }
}
