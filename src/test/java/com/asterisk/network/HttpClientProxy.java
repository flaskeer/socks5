package com.asterisk.network;

import com.asterisk.network.socks5.auth.PasswordAuth;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;

/**
 * @author donghao
 * @version 1.0
 *          2017/7/27.
 */
public class HttpClientProxy {

    public static void main(String[] args) throws IOException {
        String username = "test";
        String password = "test";

        Proxy proxy = new Proxy(Proxy.Type.SOCKS,new InetSocketAddress("127.0.0.1",11080));
        Authenticator.setDefault(new Authenticator() {

            private PasswordAuthentication authentication = new PasswordAuthentication(username,password.toCharArray());

            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return authentication;
            }
        });
        OkHttpClient client = new OkHttpClient.Builder().proxy(proxy).build();
        Request request = new Request.Builder().url("https://www.google.com").build();
        Response response = client.newCall(request).execute();
        System.out.println(response.code());
        System.out.println(response.body().string());
        client.dispatcher().executorService().shutdown();
        client.connectionPool().evictAll();
    }

}
