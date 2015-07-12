package cz.cvut.fel.integracniportal.extension;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.Session;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Component
public class SshDataSource {

    private static final Logger logger = Logger.getLogger(SshDataSource.class);

    @Autowired
    private SessionPool sessionPool;

    @Autowired
    private ServerInfo serverInfo;

    @Autowired
    private SessionTemplate sessionTemplate;

    @Autowired
    private RetryTemplate retryTemplate;

    @PostConstruct
    private void init() throws Exception {
//        doCreateSession();
    }

//    @Scheduled(initialDelay = 10000, fixedRate = 60000)
//    private void keepServerAlive() {
//        try {
//            doSendKeepAliveMessage();
//        } catch (Exception e) {
//            try {
//                retryTemplate.execute(new RetryCallback<Void>() {
//                    @Override
//                    public Void doWithRetry(RetryContext context) throws Exception {
//                        doCreateSession();
//                        doSendKeepAliveMessage();
//                        return null;
//                    }
//                });
//            } catch (Exception ee) {
//                logger.warn("Session beyond repair.", ee);
//            }
//        }
//
//    }

    public ChannelExec getSshChannel() throws Exception {

        ChannelExec sshChannel;

        try {
            sshChannel = doOpenSshChannel();
        } catch (Exception ex) {
            logger.warn("Problem with the jsch session.  Retrying...", ex);
            sshChannel = retryTemplate.execute(
                    new RetryCallback<ChannelExec>() {
                        @Override
                        public ChannelExec doWithRetry(final RetryContext context) throws Exception {
//                            doCreateSession();
                            logger.warn("Retrying #" + context.getRetryCount());
                            return doOpenSshChannel();
                        }
                    });
        }

        return sshChannel;

    }

    public ChannelSftp getSftpChannel() throws Exception {

        ChannelSftp sftpChannel;

        try {
            sftpChannel = doOpenSftpChannel();
        } catch (Exception ex) {
            logger.warn("Problem with the jsch session.  Retrying...", ex);
            sftpChannel = retryTemplate.execute(
                    new RetryCallback<ChannelSftp>() {
                        @Override
                        public ChannelSftp doWithRetry(final RetryContext context) throws Exception {
//                            doCreateSession();
                            logger.warn("Retrying #" + context.getRetryCount());
                            return doOpenSftpChannel();
                        }
                    });
        }

        return sftpChannel;

    }


//    /**
//     * Sends a keep alive message.
//     * The session has to be safe-guarded by a lock, in case
//     * the session is in the process of changing hands in doCreateSession().
//     *
//     * @throws Exception
//     */
//    private void doSendKeepAliveMessage() throws Exception {
//
//        sessionTemplate.execute(new SessionCallback<Void>() {
//            @Override
//            public String getName() {
//                return "keep alive";
//            }
//
//            @Override
//            public Void execute() throws Exception {
//                sessionContext.getSession().sendKeepAliveMsg();
//                return null;
//            }
//        });
//    }


//    private void doCreateSession() throws Exception {
//
//        sessionTemplate.execute(new SessionCallback<Object>() {
//            @Override
//            public String getName() {
//                return "create session";
//            }
//
//            @Override
//            public Object execute() throws Exception {
//                sessionPool.getPool().borrowObject(serverInfo);
//                return null;
//
//            }
//        });
//
//    }

    private ChannelExec doOpenSshChannel() throws Exception {

        return sessionTemplate.execute(new SessionCallback<ChannelExec>() {
            @Override
            public String getName() {
                return "open ssh channel";
            }

            @Override
            public ChannelExec execute() throws Exception {
                Session s = getSession();

                return (ChannelExec) s.openChannel("exec");
            }
        });

    }

    private ChannelSftp doOpenSftpChannel() throws Exception {

        return sessionTemplate.execute(new SessionCallback<ChannelSftp>() {
            @Override
            public String getName() {
                return "open ssh channel";
            }

            @Override
            public ChannelSftp execute() throws Exception {
                return (ChannelSftp) getSession().openChannel("sftp");
            }
        });

    }

    private Session getSession() throws Exception {
        return (Session) sessionPool.getPool().borrowObject(serverInfo);
    }

    @PreDestroy
    private void destroy() throws Exception {

        sessionTemplate.execute(new SessionCallback<Void>() {
            @Override
            public String getName() {
                return "destroy";
            }

            @Override
            public Void execute() throws Exception {
                sessionPool.getPool().close();
                return null;
            }
        });
    }

    public void returnSession(Session session) throws Exception {
        sessionPool.getPool().returnObject(serverInfo, session);
    }

    public void setSessionPool(SessionPool sessionPool) {
        this.sessionPool = sessionPool;
    }

    public void setServerInfo(ServerInfo serverInfo) {
        this.serverInfo = serverInfo;
    }

    public void setSessionTemplate(SessionTemplate sessionTemplate) {
        this.sessionTemplate = sessionTemplate;
    }

    public void setRetryTemplate(RetryTemplate retryTemplate) {
        this.retryTemplate = retryTemplate;
    }

}

