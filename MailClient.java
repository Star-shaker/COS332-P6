import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.OutputStreamWriter;
import java.io.BufferedWriter;
import java.io.IOException;

public class MailClient {
    private int serverPort = 25;
    private Socket mailSocket;
    private BufferedReader in;
    private BufferedWriter out;

    // Param is Integer and not int to make it nullable, does not work so well though
    public MailClient(Integer serverPort) {
        if (serverPort != null) {
            this.serverPort = serverPort;
        }
    }

    // Rather throws raw errors from connect function than swallowing detail by
    // try-catching
    public void connect() throws UnknownHostException, IOException {
        mailSocket = new Socket("localhost", serverPort);
        in = new BufferedReader(new InputStreamReader(mailSocket.getInputStream(), StandardCharsets.UTF_8));
        out = new BufferedWriter(new OutputStreamWriter(mailSocket.getOutputStream(), StandardCharsets.UTF_8));

        // ? Abstract function to apply CRLF (\n\r) via method like mailcommand?
        // HELO or EHLO? It does not make a difference for this prac since we do not use
        // any SMTP extended protocol features
        out.write("HELO\r\n");
        out.flush();

        String heloResponse = in.readLine();
        // Mailhog should respond with 250 Hello
        System.out.println(heloResponse);
        assertSuccess(heloResponse);
    }

    public void sendMail(String from, String[] recipients, String subject, String data) {
        // Throw err if connection not established
        if (mailSocket == null) {
            throw new RuntimeException("No connection with mailserver established");
        }

        try {
            String serverResponse;
            out.write("MAIL FROM:<%s>\r\n".formatted(from));
            out.flush();
            serverResponse = in.readLine();
            // Mailhog responds with 250
            assertSuccess(serverResponse);

            for (int i = 0; i < recipients.length; i++) {
                out.write("RCPT TO:<%s>\r\n".formatted(recipients[i]));
                out.flush();
                serverResponse = in.readLine();
                assertSuccess(serverResponse);
            }

            out.write("DATA\r\n");
            out.flush();
            serverResponse = in.readLine();
            assertSuccess(serverResponse);

            out.write("Subject: %s\r\nFrom: %s\r\n\r\n".formatted(subject, from));
            out.write(data + "\r\n.\r\n");
            out.flush();
            serverResponse = in.readLine();
            assertSuccess(serverResponse);

        } catch (IOException e) {
            // Why do I print here and throw runtimeexceptions elsewhere?
            System.out.println("Failed to write to out stream: " + e.getMessage());
        }
    }

    // There arent destructors in java right? How to enforce/ensure this behaviour?
    // will just hardcode it in main program
    public void closeConnection() {
        if (mailSocket == null) {
            throw new RuntimeException("No connection to close");
        }

        try {
            out.write("QUIT");
            out.flush();

            mailSocket.close();

        } catch (IOException e) {
            // Why do I print here and throw runtimeexceptions elsewhere?
            System.out.println("Failed to write to out stream: " + e.getMessage());
        }

    }

    // Each succesfull server responds is in the 2xx or 3xx range
    private void assertSuccess(String serverResponse) {
        String responseCode = serverResponse.split(" ")[0];
        if (!(responseCode.charAt(0) == '2' || responseCode.charAt(0) == '3')) {
            throw new RuntimeException("Server responded with error code: " + serverResponse);
        }
    }
}