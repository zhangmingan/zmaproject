package com.cn.zmaproject.htmlUnit.crawler.cookie;

import java.io.IOException;
import java.util.List;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @date 2017.06.08
 * @author zhangmingan
 *
 */
@SuppressWarnings("deprecation")
public class CookieManager {
		private static final Logger logger = LoggerFactory.getLogger(CookieManager.class);
	    public static String bluidCookie(org.apache.http.client.HttpClient httpClient, String url){
	    	StringBuilder sb = new StringBuilder();
	    	HttpGet post = new HttpGet(url);
	        @SuppressWarnings("unused")
			HttpResponse response;
			try {
				response = httpClient.execute(post);
				CookieStore cookieStore = ((org.apache.http.impl.client.AbstractHttpClient)httpClient).getCookieStore();
			    ((org.apache.http.impl.client.AbstractHttpClient)httpClient).setCookieStore(cookieStore);
			    post.releaseConnection();
			    HttpGet postNew = new HttpGet(url);
			    response = httpClient.execute(postNew);
		        List<Cookie> cookies = ((org.apache.http.impl.client.AbstractHttpClient)httpClient).getCookieStore().getCookies();
		        for(Cookie cookie: cookies)
		            sb.append(cookie.getName() + "=" + cookie.getValue() + ";");
		        // 除了HttpClient自带的Cookie，自己还可以增加自定义的Cookie
		        
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return sb.toString();
	    }
		public static String bluidCookieOld(org.apache.commons.httpclient.HttpClient httpClient, String url){
	
			String cookies = null;
			try {
				String jsluid = login(httpClient, url);
				if(StringUtils.isBlank(jsluid)){
					return null;
				}
	//		    String jsl_clearance = dealTempResult(httpClient, jsluid,url);
	//		    if(StringUtils.isBlank(jsl_clearance)){
	//		    	return null;
	//		    }
				cookies = postJsessionId(httpClient, jsluid,url);
				return cookies;
			} catch (Exception e) {
				logger.error("get cookies error:{}",e);
				return null;
			}
			
		}
		
		//获取jsluid
		public static String login(org.apache.commons.httpclient.HttpClient httpclient,String loginUrl){ 
	        //模拟登陆，按实际服务器端要求选用 Post 或 Get 请求方式
			GetMethod  method = new GetMethod(loginUrl);
			method.setRequestHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.93 Safari/537.36");
	        //设置登陆时要求的信息，一般就用户名和密码，验证码自己处理了
	       /* NameValuePair[] data = {
	                new NameValuePair("submit", "")
	        };
	        postMethod.setRequestBody(data);*/
	        try {
	            //设置 HttpClient 接收 Cookie,用与浏览器一样的策略
	        	httpclient.getParams().setCookiePolicy(org.apache.commons.httpclient.cookie.CookiePolicy.BROWSER_COMPATIBILITY);
	        	httpclient.executeMethod(method);
	            //String result = postMethod.getResponseBodyAsString();
	            //String result = "<script>var dc=\"\";var t_d={hello:\"world\",t_c:function(x){if(x===\"\")return;if(x.slice(-1)===\";\"){x=x+\" \";};if(x.slice(-2)!==\"; \"){x=x+\"; \";};dc=dc+x;}};(function(a){eval(function(p,a,c,k,e,d){e=function(c){return(c<a?\"\":e(parseInt(c/a)))+((c=c%a)>35?String.fromCharCode(c+29):c.toString(36))};if(!''.replace(/^/,String)){while(c--)d[e(c)]=k[c]||e(c);k=[function(e){return d[e]}];e=function(){return'\\\\w+'};c=1;};while(c--)if(k[c])p=p.replace(new RegExp('\\\\b'+e(c)+'\\\\b','g'),k[c]);return p;}('b d=[6,4,0,5,3,2,1];b o=[];b p=0;g(b i=d.c;i--;){o[d[i]]=a[i]}o=o.m(\\'\\');g(b i=0;i<o.c;i++){l(o.q(i)===\\';\\'){s(o,p,i);p=i+1}}s(o,p,o.c);j s(t,r,n){k.h(t.y(r,n))};w(\"f.e=f.e.v(/[\\\\?|&]u-x/, \\'\\')\",z);',36,36,'|||||||||||var|length||href|location|for|t_c||function|t_d|if|join||||charAt||||captcha|replace|setTimeout|challenge|substring|1500'.split('|'),0,{}));})(['th=/;', 'pires=Mon, 07-De', '__jsl_clearance=1', 'c-15 11:29:53 GMT;Pa', 'YXPzI%3D;Ex', '0|ESE%2B6MtnjImsFXyYAknUT6', '449484193.517|']);document.cookie=dc;</script>";
	
				// 获得登陆后的 Cookie
	        	org.apache.commons.httpclient.Cookie[] cookies = httpclient.getState().getCookies();
				String tmpcookies = "";
				for (org.apache.commons.httpclient.Cookie c : cookies) {
					tmpcookies += c.toString()+";";//获取jsuid
	            }
				logger.info("first:get the jsluid={}",tmpcookies);
	            return tmpcookies;
	 
	        } catch (Exception e) {
	        	logger.error("get the jsluid error",e);
	        	return null;
	        }finally{
	        	method.releaseConnection();
	        }  
		}
		
		//获取jsl_clearance
		public static String dealTempResult(org.apache.commons.httpclient.HttpClient httpclient, String tmpcookies, String dataUrl) {
			// 进行登陆后的操作
			GetMethod method = new GetMethod(dataUrl);
			// 每次访问需授权的网址时需带上前面的 cookie 作为通行证
			method.setRequestHeader("cookie", tmpcookies);
			// 你还可以通过 PostMethod/GetMethod 设置更多的请求后数据
			// 例如，referer 从哪里来的，UA 像搜索引擎都会表名自己是谁，无良搜索引擎除外
			// getMethod.setRequestHeader("Referer",
			// "http://shixin.court.gov.cn/index_publish_new.jsp");
			method.setRequestHeader("User-Agent",
					"Mozilla/5.0 (Macintosh; Intel Mac OS X 10.10; rv:42.0) Gecko/20100101 Firefox/42.0");
			try {
				httpclient.executeMethod(method);
				String result = IOUtils.toString(method.getResponseBodyAsStream(), "utf-8");
				if(result.contains("502")){
					return null;
				}
				result = (result.substring(8, result.length() - 10).replaceAll("setTimeout", "aetTimeout")
						.replaceAll("document.cookie=dc;", ""));
	
				ScriptEngineManager mgr = new ScriptEngineManager();
				ScriptEngine engine = mgr.getEngineByExtension("js");
				// engine.eval("setTimeout(\"print('aa')\",5000)");
				engine.eval("var aetTimeout = new Function('x12','y12','x12');" + result);
				String jsl_clearance = (String) engine.get("dc");
				logger.info("get the jsl_clearance is ={}", jsl_clearance);
	
				return jsl_clearance;
			} catch (Exception e) {
				logger.error("get the jsl_clearance error: {}", e);
				return null;
			}finally{
	        	method.releaseConnection();
	        }
		}
		
	    //获取JSSESIONID
		public static String postJsessionId(org.apache.commons.httpclient.HttpClient httpclient,String tmpcookies,String imageUrl){ 
			GetMethod method = new GetMethod(imageUrl);
		 	method.getParams().setCookiePolicy(org.apache.commons.httpclient.cookie.CookiePolicy.IGNORE_COOKIES);
	        //每次访问需授权的网址时需带上前面的 cookie 作为通行证
		 	method.setRequestHeader("Cookie",tmpcookies);
	        //你还可以通过 PostMethod/GetMethod 设置更多的请求后数据
	        //例如，referer 从哪里来的，UA 像搜索引擎都会表名自己是谁，无良搜索引擎除外  
		 	//getMethod.setRequestHeader("Referer", "http://shixin.court.gov.cn/index_publish_new.jsp");
		 	method.setRequestHeader("User-Agent","Mozilla/5.0 (Macintosh; Intel Mac OS X 10.10; rv:42.0) Gecko/20100101 Firefox/42.0");
		 	String jsessionid = "";
		 	try {
		 		httpclient.executeMethod(method);
				for(int i=0;i<method.getResponseHeaders().length;i++){
					if(method.getResponseHeaders()[i].getName().equalsIgnoreCase("Set-Cookie")){
						jsessionid = method.getResponseHeaders()[i].getValue();
					}
				}
				String cookies = tmpcookies+jsessionid;
				logger.info("get the whole cookies is ={}",cookies );
				return cookies;
			} catch (Exception e) {
				logger.error("get result error {}",e);	
				return null;
			}finally{
	        	method.releaseConnection();
	        }
		}
		public static void main(String[] args) {
			org.apache.http.client.HttpClient httpClient = new DefaultHttpClient();
//			httpClient.getHostConfiguration().setProxy("221.237.154.58", 9797);
//			httpClient.getParams().setAuthenticationPreemptive(true);
			bluidCookie(httpClient, "https://www.baidu.com");
		}
}
