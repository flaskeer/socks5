package com.asterisk.network.socks5.auth;

import java.io.IOException;
import java.util.Properties;

/**
 * @author donghao
 * @version 1.0
 *          2017/7/27.
 */
public class PasswordAuth {

    private static Properties properties;

    static {
        properties = new Properties();
        try {
            properties.load(PasswordAuth.class.getResourceAsStream("/auth.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean auth(String user,String password) {
        String configPassword = properties.getProperty(user);
        if (configPassword != null) {
            if (password.equals(configPassword)) {
                return true;
            }
        }
        return false;
    }

}
