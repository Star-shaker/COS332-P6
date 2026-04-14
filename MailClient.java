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

    public void sendMail(String from, String[] recipients, String subject, String data, String base64Image, String attachmentName) {
        // Throw err if connection not established
        if (mailSocket == null) {
            throw new RuntimeException("No connection with mailserver established");
        }

        try {
            // boundary string taken from the textbook
            // boundary string should be very unique such that a person writing an email
            // does not accidentally use it in their email body (which is HIGHLY unlikely in this case)

            // "The  encapsulation  boundary   is defined   as  a  line  consisting  entirely  of  two  hyphen
            // characters ("-", decimal code 45) followed by  the  boundary parameter value from the 
            // Content-Type header field." 
            // - RFC 1341

            String boundary = "------=_NextPart_000_04AD_01CBEAE1.BC8512D0";


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

            out.write("Subject: %s\r\n".formatted(subject));
            out.write("From: %s\r\n".formatted(from));
            out.write("To: %s\r\n".formatted(String.join(", ", recipients)));

            // Declare that we are using MIME
            out.write("MIME-Version: 1.0\r\n");
            out.write("Content-Type: multipart/mixed; boundary=\"%s\"\r\n".formatted(boundary));
            out.write("\r\n");

            // This is the plain text that we send
            out.write("--" + boundary + "\r\n"); // -- Says that this is the boundary
            out.write("Content-Type: text/plain; charset=\"utf-8\"\r\n");
            out.write("Content-Transfer-Encoding: 8bit\r\n\r\n");
            out.write(data + "\r\n");

            // This is the image that we send
            out.write("--" + boundary + "\r\n");
            out.write("Content-Type: image/png;\r\n");
            out.write("Content-Disposition: attachment; filename=\"%s\"\r\n".formatted(attachmentName));
            out.write("Content-Transfer-Encoding: base64\r\n");
            out.write("\r\n");
            out.write(base64Image + "\r\n");
            
            out.write("--" + boundary + "--\r\n"); // Absolute end of the MIME contents, so it will be --------=_NextPart_000_04AD_01CBEAE1.BC8512D0--
            out.write(".\r\n");
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