package cz.cvut.fel.integracniportal.extension;

import com.jcraft.jsch.Session;
import org.apache.commons.pool2.KeyedObjectPool;
import org.apache.commons.pool2.impl.GenericKeyedObjectPool;
import org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig;
import org.springframework.stereotype.Component;

/**
 * @author Radek Jezdik
 */
@Component
public class SessionPool {

    private KeyedObjectPool<ServerInfo, Session> pool;

    public SessionPool() {
        GenericKeyedObjectPoolConfig config = new GenericKeyedObjectPoolConfig();
        config.setMaxWaitMillis(10000);
        pool = new GenericKeyedObjectPool<ServerInfo, Session>(new SessionFactory(), config);
    }

    /**
     * @return the org.apache.commons.pool.KeyedObjectPool class
     */
    public KeyedObjectPool<ServerInfo, Session> getPool() {
        return pool;
    }

}