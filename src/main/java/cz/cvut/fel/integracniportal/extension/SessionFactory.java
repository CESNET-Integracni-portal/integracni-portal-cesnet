package cz.cvut.fel.integracniportal.extension;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import cz.cvut.fel.integracniportal.exceptions.ServiceAccessException;
import org.apache.commons.pool.BaseKeyedPoolableObjectFactory;

/**
 * This class is used to handle ssh Session inside the pool.
 *
 * @author Marco Castigliego
 */
public class SessionFactory extends BaseKeyedPoolableObjectFactory {

    @Override
    public Object makeObject(Object o) throws Exception {
        ServerInfo serverInfo = (ServerInfo) o;
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
    public boolean validateObject(Object key, Object obj) {
        return ((Session) obj).isConnected();
    }

    @Override
    public void destroyObject(Object key, Object obj) throws Exception {
        ((Session) obj).disconnect();
    }



}