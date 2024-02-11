package log4j2json;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;


public class LogMain {

    private static final Logger logger = LogManager.getLogger(LogMain.class);

    public static void main(String[] args) throws Exception {
        System.err.println("Example of log4j2 with MDC and ThreadContext");
        // Define the message to be logged - should be a static constant string
        String operation = "User Authentication";

        // Set MDC (Mapped Diagnostic Context) key-value pairs
        // This is where the dynamic context information is set
        ThreadContext.put("client_ip", "192.168.1.100");
        ThreadContext.put("source", "authentication-service");
        try  {
            AuthUser authUser = new AuthUser("123456", "john.doe@example.com");

            // Call the doAuthenticate method
            if (authUser.doAuthenticate()) {
                authUser.doStuff();
            }
        } catch (Exception e) {
            logger.error("Failed whole operation", e);
        }
        finally {
            // Clear MDC after logging
            // Remember that the MDC is thread-bound, so it's important to clear it after logging
            // Since this is the "top" of the thread, it's important to clear it after logging
            ThreadContext.clearAll();
        }
    }
}