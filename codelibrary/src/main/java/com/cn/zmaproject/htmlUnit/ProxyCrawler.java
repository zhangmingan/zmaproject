package com.cn.zmaproject.htmlUnit;



import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONArray;
import com.cn.zmaproject.commonUtil.poiExcel.ExcelUtil;
import com.cn.zmaproject.htmlUnit.crawler.httpClient.HttpFetcher;
import com.cn.zmaproject.htmlUnit.crawler.httpClient.WebResource;
import com.cn.zmaproject.htmlUnit.proxy.ProxyInfo;
import com.cn.zmaproject.htmlUnit.proxy.ProxyManager;

/**
 * @date 2017.06.08
 * @author zhangmingan
 * 代理爬虫类
 **/
public class ProxyCrawler {
	private static final Logger logger = LoggerFactory.getLogger(ProxyCrawler.class);
	private static String kuaidailiProxyUrl = "http://www.kuaidaili.com/proxylist/%s/";
	private static String xicidailiProxyUrl = "http://www.xicidaili.com/nt/%s";
	
	public static void kuaidailiCrawler(){

		ProxyInfo proxy = ProxyManager.getUsableProxy();
		HttpFetcher fetcher = new HttpFetcher();
		if(proxy != null){
			fetcher.setProxyInfo(proxy);
		}
		try {
			for(int i = 1; i < 11; i++){
				proxy = ProxyManager.getUsableProxy();
				if(proxy != null){
					fetcher.setProxyInfo(proxy);
				}
				WebResource wResource = fetcher.get(ProxyCrawler.kuaidailiProxyUrl.replace("%s", i+""));
				Document doc = Jsoup.parse(wResource.getAsTxt());
				Element proxyDiv = doc.getElementById("index_free_list");
				Elements trs = proxyDiv.getElementsByTag("tr");
				for(Element tr:trs){
					ProxyInfo proxyNew = new ProxyInfo();
					for(Element td:tr.getElementsByTag("td")){
						if(td.attr("data-title").equals("匿名度") && td.text().equals("透明")){
							ProxyManager.pushProxy(proxyNew);
						}
						switch (td.attr("data-title")) {
						case "IP":proxyNew.setHost(td.text());break;
						case "PORT":proxyNew.setPort(Integer.parseInt(td.text()));break;
						case "类型":proxyNew.setType(td.text().toLowerCase());;break;
						}
					}
				}
			}
			logger.info("目前代理栈{}",ProxyManager.query());
			logger.info("目前可用代理{}",ProxyManager.proxySet);
		} catch (Exception e) {
			logger.error("访问快代理异常{}",e);
		}
	
	}
	public static void xicidailiCrawler(){

		ProxyInfo proxy = ProxyManager.getUsableProxy();
		HttpFetcher fetcher = new HttpFetcher();		
		if(proxy != null){
			fetcher.setProxyInfo(proxy);
		}else {
			return;
		}
		Map<String, String> xicidailiHeaders= new HashMap<String, String>();
		xicidailiHeaders.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36");
		xicidailiHeaders.put("Upgrade-Insecure-Requests", "1");
		xicidailiHeaders.put("Referer", "http://www.xicidaili.com/");
		xicidailiHeaders.put("Cookie", "_free_proxy_session=BAh7B0kiD3Nlc3Npb25faWQGOgZFVEkiJWYyMWI5MzllMjYwZDMzY2QyZTkxMzY0ZjM0NjE1MGFjBjsAVEkiEF9jc3JmX3Rva2VuBjsARkkiMW4vbEV6cVBjV1JiTi9SbTVBTWZpTmdxMDRpVUE2WVZoZ3VvZnp1dXlqdzQ9BjsARg%3D%3D--80d3160eed4f4d05e5c01927fbcf29adf00e9f8a; Hm_lvt_0cf76c77469e965d2957f0553e6ecf59=1496912409; Hm_lpvt_0cf76c77469e965d2957f0553e6ecf59=1496912492");
		xicidailiHeaders.put("If-None-Match", "W/'fce2d095d16017481d6bd05cac6bb90a'");
		xicidailiHeaders.put("Host", "www.xicidaili.com");
		xicidailiHeaders.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
		xicidailiHeaders.put("Accept-Encoding", "gzip, deflate, sdch");
		xicidailiHeaders.put("Accept-Language", "zh-CN,zh;q=0.8");
		xicidailiHeaders.put("Cache-Control", "max-age=0");
		xicidailiHeaders.put("Connection", "keep-alive");
		for(int i = 1; i < 496; i++){
			try {
					proxy = ProxyManager.getUsableProxy();
					if(proxy != null){
						fetcher.setProxyInfo(proxy);
					}else {
						return;
					}
					WebResource wResource = fetcher.get(ProxyCrawler.xicidailiProxyUrl.replace("%s", i+""),xicidailiHeaders);
					Document doc = Jsoup.parse(wResource.getAsTxt());
					Element proxyDiv = doc.getElementById("ip_list");
					Elements trs = proxyDiv.getElementsByTag("tr");
					for(Element tr:trs){
						ProxyInfo proxyNew = new ProxyInfo();
						Elements tds = tr.getElementsByTag("td");
						if(tds == null || tds.size() == 0)
							continue;
						proxyNew.setHost(tds.get(1).text());
						proxyNew.setPort(Integer.parseInt(tds.get(2).text()));
						proxyNew.setType(tds.get(5).text().toLowerCase());
						if(proxyNew != null){
							ProxyManager.pushProxy(proxyNew);
						}
					}
					logger.info("目前代理栈{}",ProxyManager.query());
			} catch (Exception e) {
				logger.error("访问xici代理异常{}",e);
				continue;
			}
		}
		logger.info("目前可用代理{}",ProxyManager.proxySet);
	}
	public static void main(String[] args) {
		kuaidailiCrawler();
		xicidailiCrawler();
		new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					try {
						ProxyManager.getUsableProxy();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				
			}
		}).start();
		new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					try {
						int count = ProxyManager.proxySet.size();
				        JSONArray ja = new JSONArray();
				        Iterator<ProxyInfo> proxyIte = ProxyManager.proxySet.iterator();
				        while(proxyIte.hasNext()){
				        	ProxyInfo proxyInfo = proxyIte.next();
				        	ja.add(proxyInfo);
				        }
				        Map<String,String> headMap = new LinkedHashMap<String,String>();
				        headMap.put("host","IP");
				        headMap.put("port","端口");
				        headMap.put("type","socket/http");

				        String title = "测试";
				        OutputStream outXlsx = new FileOutputStream("C://Users//zhangmingan//Desktop//demo.xlsx");
				        System.out.println("正在导出xlsx....");
				        Date d2 = new Date();
				        ExcelUtil.exportExcelX(title,headMap,2,ja,null,0,outXlsx);
				        System.out.println("共"+count+"条数据,执行"+(new Date().getTime()-d2.getTime())+"ms");
				        outXlsx.close();
				        logger.info("目前代理栈{}",ProxyManager.query());
						logger.info("目前可用代理{}",ProxyManager.proxySet);
						Thread.sleep(20000);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				
			}
		}).start();
	}
}
