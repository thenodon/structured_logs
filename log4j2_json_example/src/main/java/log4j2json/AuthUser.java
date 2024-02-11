package log4j2json;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;



public class AuthUser {
    private static final Logger logger = LogManager.getLogger(AuthUser.class);
    private String userId;
    private String userName;

    public AuthUser(String userId, String userName) {
        this.userId = userId;
        this.userName = userName;
    }

    public String getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }
    public void doStuff() {
        logger.info("doStuff");
    }
    public boolean doAuthenticate() throws Exception {
        // Define the message to be logged - should be a static constant string

        try {
            /*
             * Simulate a user authentication operation
             * If the user is invalid, throw an exception
             */

            // Continue to set MDC key-value pairs that will be of interest for the log message both
            // if the operation is successful and if it fails
            ThreadContext.put("user_id", this.userId);
            ThreadContext.put("user_name", this.userName);

            float random = (float) Math.random();
            if (random > 0.9) {
                throw new Exception("Program logic failed for user authentication");
            } else if (random > 0.6) {
                ThreadContext.put("status", "failed");
                logger.warn("User authenticated");

                return false;
            } else {
                // Log the operation as successful
                ThreadContext.put("status", "success");
                logger.info("User authenticated");
            }
        } catch (Exception e) {
            // Log the operation as failed
            ThreadContext.put("status", "failed");
            logger.error("User authentication", e);
            throw e;
        } finally {
            // Clear MDC after logging
            // Remember that the MDC is thread-bound, so it's important to clear it after logging
            // if they are not to be used in the thread anymore
            ThreadContext.remove("status");
        }
        return true;
    }


}
