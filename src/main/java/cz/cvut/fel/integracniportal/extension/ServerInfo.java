package cz.cvut.fel.integracniportal.extension;

import com.jcraft.jsch.UserInfo;

/**
 * @author Radek Jezdik
 */
public class ServerInfo implements UserInfo {

    private String hostname;

    private int port;

    private String username;

    private String password;

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

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || ((Object) this).getClass() != o.getClass()) return false;

        ServerInfo that = (ServerInfo) o;

        if (port != that.port) return false;
        if (hostname != null ? !hostname.equals(that.hostname) : that.hostname != null) return false;
        if (password != null ? !password.equals(that.password) : that.password != null) return false;
        if (username != null ? !username.equals(that.username) : that.username != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = hostname != null ? hostname.hashCode() : 0;
        result = 31 * result + port;
        result = 31 * result + (username != null ? username.hashCode() : 0);
        result = 31 * result + (password != null ? password.hashCode() : 0);
        return result;
    }
}
