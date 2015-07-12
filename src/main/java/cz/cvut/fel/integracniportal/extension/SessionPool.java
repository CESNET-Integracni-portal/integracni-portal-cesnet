package cz.cvut.fel.integracniportal.extension;

import org.apache.commons.pool.KeyedObjectPool;
import org.apache.commons.pool.impl.GenericKeyedObjectPool;
import org.springframework.stereotype.Component;

/**
 * @author Radek Jezdik
 */
@Component
public class SessionPool {

    private KeyedObjectPool pool;

    public SessionPool() {
        GenericKeyedObjectPool.Config config = new GenericKeyedObjectPool.Config();
        config.maxWait = 10000;
        pool = new GenericKeyedObjectPool(new SessionFactory(), config);
    }

    public KeyedObjectPool getPool() {
        return pool;
    }

}