package lhf.launcher.support;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

import org.apache.commons.net.telnet.TelnetClient;

/**
 * Telnet 구현체
 *
 * @author
 */
public class TelnetUtil {

    private TelnetClient telnet = new TelnetClient();
    private InputStream in;
    private PrintStream out;
    private String prompt = ">";

    public TelnetUtil(String server, String user, String password) {
        try {
            // Connect to the specified server
            telnet.connect(server, 23);

            // Get input and output stream references
            in = telnet.getInputStream();
            out = new PrintStream(telnet.getOutputStream());

            // Log the user on
            readUntil("login:");
            write(user);
            readUntil("assword:");
            write(password);

//            // Advance to a prompt
//            readUntil(prompt);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void su(String password) {
        try {
            write("su");
            readUntil("Password: ");
            write(password);
            prompt = "#";
            readUntil(prompt + " ");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    public String readUntil(String pattern) {
//        try {
//            char lastChar = pattern.charAt(pattern.length() - 1);
//            StringBuffer sb = new StringBuffer();
//            boolean found = false;
//            char ch = (char) in.read();
//            while (true) {
//                System.out.print(ch);
//                sb.append(ch);
//                if (ch == lastChar) {
//                    if (sb.toString().endsWith(pattern)) {
//                        return sb.toString();
//                    }
//                }
//                ch = (char) in.read();
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
    
    // command cursor 읽기
    public String readUntil( String pattern ) {
        try{
            char ch;
            StringBuffer sb = new StringBuffer();
            while ((ch = (char)in.read()) != -1) {
                sb.append(ch);
//                System.out.println(sb.toString());
                if (sb.toString().endsWith(pattern)) {
                    String found = sb.toString();
                    sb = new StringBuffer();
                    return found;
                }
            }
        }catch(IOException e){
            e.printStackTrace();
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }
    
    // command cursor 읽기
    public String readEnd(String pattern){
        try{
//    		BufferedReader br = new BufferedReader(new InputStreamReader(in));
//    		String line;
//    		StringBuffer sb = new StringBuffer();
//    		while ((line = br.readLine()) != null) {
//    			sb.append(line + '\n');
//    		}
            
//            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
//            int n = 0;
//            char[] cbuf = new char[1024];
//            while ((n = reader.read(cbuf)) > -1) { 
//                System.out.println(cbuf);
//            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public void write(String value) {
        try {
            out.println(value);
            out.flush();
            System.out.println(value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public String sendCommand(String command, String prompt) {
        try {
            write(command);
            return readEnd(prompt);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void disconnect() {
        try {
            telnet.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
//            TelnetUtil telnet = new TelnetUtil(
//                    "myserver", "userId", "Password");
//            System.out.println("Got Connection...");
////            telnet.sendCommand("ps -ef ");
//            System.out.println("run command");
////            telnet.sendCommand("ls ");
//            System.out.println("run command 2");
//            telnet.disconnect();
//            System.out.println("DONE");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
