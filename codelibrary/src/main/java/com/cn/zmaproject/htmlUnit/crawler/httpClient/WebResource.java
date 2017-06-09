package com.cn.zmaproject.htmlUnit.crawler.httpClient;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;


import org.apache.http.Header;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.log4j.Logger;

/**
 * http协议返回资源类
 * @author Administrator
 */
public class WebResource {

    private static final Logger logger = Logger.getLogger(WebResource.class);
    private Map<String, Object> attributes;
    private Header[] headers;
    private int statusCode;
    private InputStream inputStream;
    private HttpRequestBase httpRequestBase;

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }
    
    public Header[] getHeaders() {
    	return headers;
    }
    
    public void setHeaders(Header[] headers) {
    	this.headers = headers;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public HttpRequestBase getHttpRequestBase() {
        return httpRequestBase;
    }

    public void setHttpRequestBase(HttpRequestBase httpRequestBase) {
        this.httpRequestBase = httpRequestBase;
    }

    public void save2File(String path) throws IOException {
        File f = new File(path);
        if (!f.getParentFile().exists()) {
            f.getParentFile().mkdirs();
        }
        byte[] b = new byte[4096];
        try (BufferedInputStream bis = new BufferedInputStream(inputStream);
                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(path))) {
            while (true) {
                int read = bis.read(b);
                if (read == -1) {
                    break;
                }
                bos.write(b, 0, read);
            }
        } finally {
            closeRequset();
        }
    }

    public byte[] getAsByteArray() throws IOException {
        byte[] b = new byte[4096];
        try (BufferedInputStream bis = new BufferedInputStream(inputStream); ByteArrayOutputStream dos = new ByteArrayOutputStream()) {
            while (true) {
                int read = bis.read(b);
                if (read == -1) {
                    break;
                }
                dos.write(b, 0, read);
            }
            return dos.toByteArray();
        } finally {
            closeRequset();
        }
    }

    public String getAsTxt() throws IOException {
        try {
            String charset = "UTF-8";
            String contentType = (String) attributes.get("Content-Type");
            if(contentType!=null){
                int idx = contentType.indexOf("charset=");
                if(idx>=0){
                    charset = contentType.substring(idx+8);
                }
            }
            StringBuilder sb = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream,charset))) {
                while (true) {
                    String line = br.readLine();
                    if (line != null) {
                        sb.append(line);
                    } else {
                        break;
                    }
                }
            }
            return sb.toString();
        } finally {
            closeRequset();
        }
    }

    public void closeRequset() {
        if (httpRequestBase != null) {
            if (httpRequestBase.isAborted()) {
                httpRequestBase.abort();
            }
            httpRequestBase.releaseConnection();
        }
    }
}
