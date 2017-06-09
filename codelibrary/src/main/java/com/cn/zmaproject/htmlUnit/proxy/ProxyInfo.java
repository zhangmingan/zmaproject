package com.cn.zmaproject.htmlUnit.proxy;

import java.util.Objects;

/**
 * 代理信息
 *
 * @author Administrator
 */
public class ProxyInfo {

    public static final String PROXY_TYPE_HTTP = "http";
    public static final String PROXY_TYPE_SOCKET = "socket";

    private String type;
    private String host;
    private int port;
    private String userName;
    private String password;

    public ProxyInfo(String type, String host, int port) {
        this.type = type;
        this.host = host;
        this.port = port;
    }

    public ProxyInfo(String type, String host, int port, String userName, String password) {
        this.type = type;
        this.host = host;
        this.port = port;
        this.userName = userName;
        this.password = password;
    }

    public ProxyInfo() {
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.type);
        hash = 97 * hash + Objects.hashCode(this.host);
        hash = 97 * hash + this.port;
        hash = 97 * hash + Objects.hashCode(this.userName);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ProxyInfo other = (ProxyInfo) obj;
        if (!Objects.equals(this.type, other.type)) {
            return false;
        }
        if (!Objects.equals(this.host, other.host)) {
            return false;
        }
        if (this.port != other.port) {
            return false;
        }
        if (!Objects.equals(this.userName, other.userName)) {
            return false;
        }
        if (!Objects.equals(this.password, other.password)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ProxyInfo{" + "type=" + type + ", host=" + host + ", port=" + port + ", userName=" + userName + ", password=" + password + '}';
    }

}
