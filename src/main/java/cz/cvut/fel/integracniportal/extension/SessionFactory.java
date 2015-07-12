package cz.cvut.fel.integracniportal.extension;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import cz.cvut.fel.integracniportal.exceptions.ServiceAccessException;
import org.apache.commons.pool2.BaseKeyedPooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

/**
 * This class is used to handle ssh Session inside the pool.
 *
 * @author Marco Castigliego
 */
public class SessionFactory extends BaseKeyedPooledObjectFactory<ServerInfo, Session> {

    @Override
    public Session create(ServerInfo serverInfo) throws Exception {
        Session session;
        try {
            JSch jsch = new JSch();
            session = jsch.getSession(serverInfo.getUsername(), serverInfo.getHostname(), serverInfo.getPort());
            session.setConfig("StrictHostKeyChecking", "no");

            session.setUserInfo(serverInfo);
            session.setTimeout(60000);
            session.setPassword(serverInfo.getPassword());
            session.connect();
        } catch (Exception e) {
            throw new ServiceAccessException("Unrecoverable error when trying to connect to server", e);
        }
        return session;
    }

    @Override
    public PooledObject<Session> wrap(Session session) {
        return new DefaultPooledObject<Session>(session);
    }

    @Override
    public boolean validateObject(ServerInfo key, PooledObject<Session> pooledObj) {
        return pooledObj.getObject().isConnected();
    }

    @Override
    public void destroyObject(ServerInfo key, PooledObject<Session> pooledObj) throws Exception {
        pooledObj.getObject().disconnect();
    }


}