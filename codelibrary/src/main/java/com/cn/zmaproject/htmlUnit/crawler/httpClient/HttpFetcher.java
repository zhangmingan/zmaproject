package com.cn.zmaproject.htmlUnit.crawler.httpClient;

import java.io.IOException;
import java.io.InputStream;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.http.Consts;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CookieStore;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;
import org.apache.log4j.Logger;

import com.cn.zmaproject.htmlUnit.proxy.ProxyInfo;

/**
 * http协议请求类 支持cookie 支持代理
 *
 * @author Administrator
 */
public class HttpFetcher {

    private static final Logger logger = Logger.getLogger(HttpFetcher.class);
    public CloseableHttpClient httpClient = null;
    private final CookieStore cookieStore = new BasicCookieStore();
    private final RequestConfig requestConfig = HttpFetcherConfig.getGlobalRequestConfig();
    private ProxyInfo proxyInfo = null;
    private final HttpClientContext context = HttpClientContext.create();
    private static Map<String, ProxyInfo> PROXY_INFOS = new HashMap<>();

    static {
        //socks 代理帐号密码管理
        Authenticator.setDefault(new Authenticator() {

            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                String requestingHost = getRequestingHost();
                int requestingPort = getRequestingPort();
                ProxyInfo proxyInfo = PROXY_INFOS.get(requestingHost + "+" + requestingPort);
                System.out.println(proxyInfo);
                if (proxyInfo != null) {
                    //密码认证
                    String userName = proxyInfo.getUserName();
                    String password = proxyInfo.getPassword();
                    if (userName != null) {
                        return new PasswordAuthentication(userName, (password == null ? "" : password).toCharArray());
                    }
                }
                return null;
            }
        });
    }

    public HttpFetcher() {

    }

    private void init() {
        HttpClientBuilder hbuilder = HttpClients.custom().setDefaultCookieStore(cookieStore).setDefaultRequestConfig(requestConfig).setDefaultHeaders(HttpFetcherConfig.getRequestHeaders())
                .setRetryHandler(new DefaultHttpRequestRetryHandler(0, false));
        //传入代理相关参数
        if (proxyInfo != null) {
            String type = proxyInfo.getType();
            if (type.equals(ProxyInfo.PROXY_TYPE_HTTP)) {
                String userName = proxyInfo.getUserName();
                String password = proxyInfo.getPassword();
                Credentials credentials = null;
                if (userName != null) {
                    credentials = new UsernamePasswordCredentials(userName, password == null ? "" : password);
                    CredentialsProvider credsProvider = new BasicCredentialsProvider();
                    credsProvider.setCredentials(new AuthScope(new HttpHost(proxyInfo.getHost(), proxyInfo.getPort())), credentials);
                    hbuilder.setDefaultCredentialsProvider(credsProvider);
                }
                try {
                    hbuilder.setSSLSocketFactory(HttpFetcherConfig.getSslConnectionSocketFactory());
                } catch (Exception e) {
                    logger.error("", e);
                }
            }
            if (type.equals(ProxyInfo.PROXY_TYPE_SOCKET)) {
                PROXY_INFOS.put(proxyInfo.getHost() + "+" + proxyInfo.getPort(), proxyInfo);
                try {
                    //socks 代理 支持http、https，信任任何证书
                    Registry<ConnectionSocketFactory> reg = RegistryBuilder.<ConnectionSocketFactory>create()
                            .register("http", new PlainConnectionSocketFactory() {
                                @Override
                                public Socket createSocket(final HttpContext context) throws IOException {
                                    InetSocketAddress socksaddr = (InetSocketAddress) context.getAttribute("socks.address");
                                    Proxy proxy = new Proxy(Proxy.Type.SOCKS, socksaddr);
                                    return new Socket(proxy);
                                }
                            })
                            .register("https", new SSLConnectionSocketFactory(HttpFetcherConfig.getSSLContext()) {
                                @Override
                                public Socket createSocket(final HttpContext context) throws IOException {
                                    InetSocketAddress socksaddr = (InetSocketAddress) context.getAttribute("socks.address");
                                    Proxy proxy = new Proxy(Proxy.Type.SOCKS, socksaddr);
                                    return new Socket(proxy);
                                }
                            }).build();
                    BasicHttpClientConnectionManager basicHttpClientConnectionManager = new BasicHttpClientConnectionManager(reg);
                    hbuilder.setConnectionManager(basicHttpClientConnectionManager);
                    context.setAttribute("socks.address", new InetSocketAddress(proxyInfo.getHost(), proxyInfo.getPort()));
                } catch (Exception e) {
                    logger.error("", e);
                }
            }
        } else {
            try {
                hbuilder.setSSLSocketFactory(HttpFetcherConfig.getSslConnectionSocketFactory());
            } catch (Exception e) {
                logger.error("", e);
            }
        }
        httpClient = hbuilder.build();
    }

    public boolean isUseProxy(String url) {
        String host = null;
		try {
			host = new URL(url).getHost().toLowerCase();
		} catch (MalformedURLException e) {
			logger.error("", e);
		}
        if (host == null) {
            return false;
        }
        return HttpFetcherConfig.USE_PROXY_HOSTS.contains(host);
    }

    //设置请求属性
    private void setConfig(HttpRequestBase httpRequestBase, String url) {
        if (proxyInfo == null && isUseProxy(url)) {
            String host = HttpFetcherConfig.FETCH_CONFIG.getProperty("fetch.proxy.host");
            String port = HttpFetcherConfig.FETCH_CONFIG.getProperty("fetch.proxy.port");
            if (host == null || port == null || host.isEmpty() || port.isEmpty()) {
                logger.warn(url + "==>proxy setting has error!");
            }
            try {
                HttpHost httpHost = new HttpHost(host, Integer.parseInt(port));
                httpRequestBase.setConfig(RequestConfig.copy(requestConfig).setProxy(httpHost).build());
            } catch (Exception e) {
                logger.warn(url + "==>proxy setting has error!", e);
            }
        } else if (proxyInfo != null) {
            if (proxyInfo.getType().equals(ProxyInfo.PROXY_TYPE_HTTP)) {
                httpRequestBase.setConfig(RequestConfig.copy(requestConfig).setProxy(new HttpHost(proxyInfo.getHost(), proxyInfo.getPort())).build());
            }
        } else {
            httpRequestBase.setConfig(requestConfig);
        }
    }

    public WebResource post(String url, Map<String, String> params, Map<String, String> headers) throws Exception {
        HttpPost post = new HttpPost(url);
        setConfig(post, url);
        if (params != null && !params.isEmpty()) {
            List<NameValuePair> formparams = new ArrayList<>();
            Set<String> keys = params.keySet();
            for (String key : keys) {
                String value = params.get(key);
                formparams.add(new BasicNameValuePair(key, value));
            }
            post.setEntity(new UrlEncodedFormEntity(formparams, Consts.UTF_8));
        }
        if (headers != null && !headers.isEmpty()) {
            Set<String> keys = headers.keySet();
            for (String key : keys) {
                String value = headers.get(key);
                post.setHeader(key, value);
            }
        }
        return fetch(post);
    }

    public WebResource post(String url, Map<String, String> params) throws Exception {
        return post(url, params, null);
    }

    public WebResource post(String url) throws Exception {
        return post(url, null, null);
    }

    public WebResource get(String url, Map<String, String> headers) throws Exception {
        HttpGet get = new HttpGet(url);
        setConfig(get, url);
        if (headers != null && !headers.isEmpty()) {
            Set<String> keys = headers.keySet();
            for (String key : keys) {
                String value = headers.get(key);
                get.setHeader(key, value);
            }
        }
        return fetch(get);
    }

    public WebResource get(String url) throws Exception {
        return get(url, null);
    }

    public WebResource fetch(HttpRequestBase httpRequestBase) throws Exception {
        if (httpRequestBase == null) {
            return null;
        }
        WebResource wr = null;
        try {
            if (httpClient == null) {
                init();
            }
            CloseableHttpResponse response = httpClient.execute(httpRequestBase, context);
            Header[] allHeaders = response.getAllHeaders();
            Map<String, Object> attr = new HashMap<>();
            if (allHeaders != null) {
                for (Header header : allHeaders) {
                    attr.put(header.getName(), header.getValue());
                }
            }
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                InputStream content = entity.getContent();
                wr = new WebResource();
                wr.setInputStream(content);
                wr.setStatusCode(response.getStatusLine().getStatusCode());
                wr.setAttributes(attr);
                wr.setHeaders(allHeaders);
                wr.setHttpRequestBase(httpRequestBase);
            }
        } catch (Exception e) {
            logger.error("fetch error:" + httpRequestBase.getURI(), e);
            if (httpRequestBase.isAborted()) {
                httpRequestBase.abort();
            }
            httpRequestBase.releaseConnection();
            throw e;
        }
        return wr;
    }

    public void closeClient() {
        if (httpClient != null) {
            try {
                httpClient.close();
            } catch (Exception e) {
                logger.error("httpclient close occur exception!", e);
            }
        }
    }

    public ProxyInfo getProxyInfo() {
        return proxyInfo;
    }

    public void setProxyInfo(ProxyInfo proxyInfo) {
        this.proxyInfo = proxyInfo;
    }

    public static void main(String[] args) throws IOException, Exception {
        HttpFetcher hf = new HttpFetcher();
      ProxyInfo pi = new ProxyInfo(ProxyInfo.PROXY_TYPE_HTTP, "119.57.105.198", 8080, "", "");
      hf.setProxyInfo(pi);
        
        WebResource wr = hf.get("http://wenshu.court.gov.cn");
        System.out.println(wr.getAsTxt());
        System.out.println(wr.getStatusCode());
    }
}
