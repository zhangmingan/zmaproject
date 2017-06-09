/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cn.zmaproject.htmlUnit.crawler.httpClient;

import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import javax.net.ssl.SSLContext;
import org.apache.http.Header;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.message.BasicHeader;
import org.apache.log4j.Logger;

/**
 *
 * @author Administrator
 */
public class HttpFetcherConfig {

    private static final Logger logger = Logger.getLogger(HttpFetcherConfig.class);

    public static final Set<String> USE_PROXY_HOSTS = new HashSet();
    public static final Properties FETCH_CONFIG = new Properties();
    private static RequestConfig requestConfig = null;
    private static List<Header> requestHeaders = null;
    private static SSLConnectionSocketFactory sslConnectionSocketFactory = null;
    private static SSLContext sslContext = null;

    static {
        loadUseProxyHost();
        loadFetchConfig();
    }

    //获取自定义的SSLContext，信任所有证书
    public static SSLContext getSSLContext() throws Exception {
        if (sslContext == null) {
            sslContext = SSLContexts.custom().loadTrustMaterial(null, new TrustStrategy() {

                @Override
                public boolean isTrusted(X509Certificate[] xcs, String string) throws CertificateException {
                    return true;
                }
            }).build();
        }
        return sslContext;
    }

    //获取自定义的SSLConnectionSocketFactory，信任所有证书
    public static SSLConnectionSocketFactory getSslConnectionSocketFactory() throws Exception {
        if (sslConnectionSocketFactory != null) {
            return sslConnectionSocketFactory;
        }
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(getSSLContext());
        sslConnectionSocketFactory = sslsf;
        return sslConnectionSocketFactory;
    }

    public static List<Header> getRequestHeaders() {
        if (requestHeaders == null) {
            Header header0 = new BasicHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
            Header header1 = new BasicHeader("Accept-Encoding", "gzip, deflate");
            Header header2 = new BasicHeader("Accept-Language", "zh-CN,en-US;q=0.8,zh;q=0.5,en;q=0.3");
            Header header3 = new BasicHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64; rv:41.0) Gecko/20100101 Firefox/41.0");
            List<Header> headers = new ArrayList<>();
            headers.add(header0);
            headers.add(header1);
            headers.add(header2);
            headers.add(header3);
            requestHeaders = headers;
        }
        return requestHeaders;
    }

    public static RequestConfig getGlobalRequestConfig() {
        if (requestConfig == null) {
            requestConfig = RequestConfig.custom()
                    .setCookieSpec(CookieSpecs.STANDARD)
                    .setConnectTimeout(30000)
                    .setConnectionRequestTimeout(30000)
                    .setSocketTimeout(30000)
                    .setRedirectsEnabled(false)
                    .build();
        }
        return requestConfig;
    }

    private static void loadUseProxyHost() {
        try {
//            URL resource = ClassPathResourceLoader.getResource("http-use-proxy-hosts");
//            if (resource == null) {
//                resource = HttpFetcher.class.getResource("http-use-proxy-hosts");
//            }
//            List<String> lines = net.pgia.crawler.utils.TextUtils.getLines(resource.openStream());
//            USE_PROXY_HOSTS.addAll(lines);
        } catch (Exception e) {
            logger.error("read http-use-proxy-hosts fail", e);
        }
    }

    private static void loadFetchConfig() {
        try {
//            URL resource = ClassPathResourceLoader.getResource("http-fetcher.properties");
//            if (resource == null) {
//                resource = HttpFetcher.class.getResource("http-fetcher.properties");
//            }
//            if (resource != null) {
//                FETCH_CONFIG.load(resource.openStream());
//            }
        } catch (Exception e) {
            logger.error("read http-fetcher.properties fail", e);
        }
    }
}
