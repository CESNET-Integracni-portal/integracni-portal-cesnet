package cz.cvut.fel.integracniportal.extension;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.util.Properties;

/**
 * @deprecated Use SessionPool
 */
@Component
class SessionContext {

    private static final Logger logger = Logger.getLogger(SessionContext.class);

    private String hostname;

    private int port;

    @Autowired
    private SshUserInfo sshUserInfo;

    private String remoteBaseDir;

    @Autowired
    private JSch jsch;

    private Session session;

    private int connectTimeout;

    private int serverKeepAliveInterval;

    private static final Properties usePasswordConfig = new Properties();

    public SessionContext() {

        usePasswordConfig.put("StrictHostKeyChecking", "no");
    }

    @PostConstruct
    public void init() {
        try {
            String keystorePath = sshUserInfo.getKeystorePath();
            if (keystorePath != null && !StringUtils.isEmpty(keystorePath)) {
                jsch.addIdentity(keystorePath);
            }

            getSession();

        } catch (Exception e) {
            logger.error("Unable to create a session!", e);
        }
    }

    public Session createSession() throws Exception {
        if (session == null || !session.isConnected()) {
            Session s = jsch.getSession(sshUserInfo.getUsername(), hostname);

            if (!StringUtils.isEmpty(sshUserInfo.getKeystorePath())) {
                s.setUserInfo(sshUserInfo);
            } else {
                String password = sshUserInfo.getPassword();
                if (StringUtils.isEmpty(password)) {
                    throw new IllegalArgumentException("Either keystore path or password must be specified.");
                } else {
                    s.setPassword(password);
                    s.setConfig(usePasswordConfig);
                }
            }

            s.setPort(port);
            s.connect(connectTimeout);
            session = s;
        }

        return session;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getRemoteBaseDir() {
        return remoteBaseDir;
    }

    public void setRemoteBaseDir(String remoteBaseDir) {
        this.remoteBaseDir = remoteBaseDir;
    }

    public JSch getJsch() {
        return jsch;
    }

    public void setJsch(JSch jsch) {
        this.jsch = jsch;
    }

    public SshUserInfo getSshUserInfo() {
        return sshUserInfo;
    }

    public void setSshUserInfo(SshUserInfo sshUserInfo) {
        this.sshUserInfo = sshUserInfo;
    }

    public Session getSession() {
        return session;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public int getServerKeepAliveInterval() {
        return serverKeepAliveInterval;
    }

    public void setServerKeepAliveInterval(int serverKeepAliveInterval) {
        this.serverKeepAliveInterval = serverKeepAliveInterval;
    }

}
