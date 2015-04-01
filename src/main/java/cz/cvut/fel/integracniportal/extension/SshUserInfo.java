package cz.cvut.fel.integracniportal.extension;

import com.jcraft.jsch.UserInfo;

/**
 * POJO representing the ssh user info.
 *
 * @author sso
 */
public class SshUserInfo implements UserInfo {

    private String username;
    private String keystorePath;
    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String getPassphrase() {
        return null;
    }

    @Override
    public boolean promptPassword(String message) {
        return false;
    }

    @Override
    public boolean promptPassphrase(String message) {
        return false;
    }

    @Override
    public boolean promptYesNo(String message) {
        // must be true to do silent login
        return true;
    }

    @Override
    public void showMessage(String message) {

    }

    @Override
    public String getPassword() {
        return password;
    }

    public void setPassword(final String password) {
        this.password = password;
    }

    public String getKeystorePath() {
        return keystorePath;
    }

    public void setKeystorePath(final String keystorePath) {
        this.keystorePath = keystorePath;
    }

}
