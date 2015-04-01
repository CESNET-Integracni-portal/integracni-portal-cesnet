package cz.cvut.fel.integracniportal.extension;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This class encapsulates the shared operations.
 * It reduces the boiler plate code around synchronizing the session.
 *
 * @author sso
 */
@Component
class SessionTemplate implements SessionOperations {

    private static final Logger logger = Logger.getLogger(SessionTemplate.class);

    private final Lock lock;

    SessionTemplate() {
        lock = new ReentrantLock();
    }

    @Override
    public final <T> T execute(SessionCallback<T> callback) throws Exception {
        return doExecute(callback);
    }

    protected <T> T doExecute(SessionCallback<T> callback) throws Exception {
        try {
            if (lock.tryLock(1000, TimeUnit.MILLISECONDS)) {
                logger.debug("obtained lock: " + callback.getName());
                try {
                    return callback.execute();
                } finally {
                    lock.unlock();
                    logger.debug("unlocked: " + callback.getName());
                }
            }
        } catch (InterruptedException ie) {
            logger.error(ie);
        }
        return null;
    }

}
