package com.cn.zmaproject.htmlUnit.proxy;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cn.zmaproject.htmlUnit.crawler.httpClient.HttpFetcher;
import com.cn.zmaproject.htmlUnit.crawler.httpClient.WebResource;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlTable;
import com.gargoylesoftware.htmlunit.util.NameValuePair;

import net.sf.json.JSONObject;
/**
 * @date 2017.06.08
 * @author zhangmingan
 * 代理管理控制类
 */
public class ProxyManager {
	
	private static Logger logger = LoggerFactory.getLogger(ProxyManager.class);
	
	private final static Stack<ProxyInfo> proxyStack = new Stack<ProxyInfo>();		//备用
	public final static Set<ProxyInfo> proxySet = new HashSet<ProxyInfo>();		//正在使用
	
	public static Stack<ProxyInfo> query(){
		return proxyStack;
	}
	public static void pushProxy(ProxyInfo proxyInfo){
		proxyStack.push(proxyInfo);
	}
	public static ProxyInfo getUsableProxy(){
		ProxyInfo topProxy = proxyStack.isEmpty()?null:proxyStack.peek();
		if(topProxy == null)
			return null;
		proxyStack.pop();
		if(checkProxy(topProxy)){
			proxySet.add(topProxy);
			return topProxy;
		}
		return getUsableProxy();
	}
	private void releaseProxy(ProxyInfo proxyInfo){
		proxySet.remove(proxyInfo);
		proxyStack.push(proxyInfo);
	}
	private static boolean checkProxy(ProxyInfo proxyInfo){
		 String host = proxyInfo.getHost();
	        if (host != null) {
	            if (host.equals("localhost") || host.equals("127.0.0.1")) {//本机ip不用测试
	                return false;
	            }
	        }
	        logger.info("test ip:" + host + ":" + proxyInfo.getPort() + "...");
	        HttpFetcher hf = new HttpFetcher();
	        hf.setProxyInfo(new ProxyInfo(proxyInfo.getType(), proxyInfo.getHost(),
	        		proxyInfo.getPort(), proxyInfo.getUserName(), proxyInfo.getPassword()));
	        WebResource wr = null;
	        try {
	            wr = hf.get("http://www.baidu.com");
	            if (wr != null && wr.getStatusCode() == 200) {
	                logger.info("test ip success:" + host + ":" + proxyInfo.getPort());
	                return true;
	            } else {
	                logger.warn("test ip fail:" + host + ":" + proxyInfo.getPort() + " " + (wr == null ? "" : wr.getStatusCode()));
	                
	            }
	        }catch (Exception e){
	        	logger.error("test ip fail:" + host + ":" + proxyInfo.getPort(), e);
	        }finally {
	        	if (wr != null) {
                    wr.closeRequset();
                }
			}
		return false;
	}
	public static void main(String[] args) {
		
		ProxyManager pGbl = new ProxyManager();
		
		
		
		
		
		
		String inputJson ="{\"url\": \"http://www.kuaidaili.com\",\"method\": \"get.post\","
				 + "\"data\": \"String\",\"resultType\": \"html.json\","
				+ "\"rule-json\": \"result.data=com.tuniu.model;\","
				 + "\"rule-html\": {\"modelName\":\"com.cn.zmaproject.htmlUnit.proxyUtil.ProxyInfo\",\"modelScope\":\"result.html.table(0).tr(2-end)\","
				 + "\"modelValue\":\"element.tr().td(0).text=model.host;element.tr().td(1).text=model.port;\"}}";

//				{
//				    "url": "123",
//				    "method": "get.post",
//				    "data": "String",
//				    "resultType": "html.json",
//				    "rule-json": "result.data=com.tuniu.model;",
//				    "rule-html": "result.html.table(1-end).tr.td(idcard)=com.tuniu.model.idcard;"
//				}
//		
		JSONObject inputJsonObject = JSONObject.fromObject(inputJson);
		JSONObject rule = inputJsonObject.getJSONObject("rule-html");
		HtmlPage page = pGbl.getBjEntCreditPageInfo(inputJsonObject.getString("url"),0);
		pGbl.getBackListDetailInfoByHtml(page,rule);
	}
	
	public HtmlPage getBjEntCreditPageInfo(String url, int pageNum) {
		HtmlPage Page = null;
		WebClient webClient = new WebClient(BrowserVersion.CHROME);
		webClient.getOptions().setCssEnabled(false);
		webClient.getOptions().setJavaScriptEnabled(false);
		webClient.getOptions().setRedirectEnabled(false);
		webClient.getOptions().setTimeout(240000);
		try {
			WebRequest webRequest = new WebRequest(new URL(url), HttpMethod.GET);
			List<NameValuePair> reqParam = new ArrayList<NameValuePair>();
			reqParam.add(new NameValuePair("pageNos", String.valueOf(pageNum)));
			webRequest.setRequestParameters(reqParam);
			logger.info("begin to get web page info");
			Page = webClient.getPage(webRequest);
			if (StringUtils.isBlank(Page.asXml())) {
				logger.info("----------get page info error,url={},pageNum={}",url,pageNum);
				webClient.closeAllWindows();
				return null;
			} else {
				logger.info("get page info success,url=:{},pageNum=:{}", url,pageNum);
			}
		} catch (Exception e) {
//			logger.error("**********get page info error url={},pageNum={},error info={}",url,pageNum,e);
			return null;
		}
		webClient.closeAllWindows();
		return Page;
	}
	
	public  List<Object> getBackListDetailInfoByHtml(HtmlPage page, JSONObject rule) {
		Class<?> dd = null ;
		try {
			dd = Class.forName(rule.getString("modelName"));
		} catch (Exception e1) {
			logger.error("model info error",e1);
			return null;
		}
		
		List<Object> entCreditMainDtosList = new ArrayList<Object>();
		try {
			logger.info("------------current page begin deal------------");
			String modelScope= rule.getString("modelScope");
			if(modelScope.contains("table")){
				String scope = modelScope.substring(modelScope.indexOf("table(")+6, modelScope.indexOf(").tr"));
				String[] scopes = scope.split("-");
				DomNodeList<DomElement> tableList = page.getElementsByTagName("table");
				String modelValue= rule.getString("modelValue");
				if(scopes.length>1){	//table=model
					for(int i=Integer.parseInt(scopes[0]);i<(scopes[1].equals("end")?tableList.size():Integer.parseInt(scopes[1]));i++){
						HtmlTable table = (HtmlTable)page.getElementsByTagName("table").get(i);
						
						if(StringUtils.isNoneBlank(table.asXml())){
//							{\"modelScope\":\"result.html.table(1-end).tr()\",\"modelValue\":\"element.tr(1).td(3).text=model.idcard;\"}
							String[] values = modelValue.split(";");
//							for(int j=0;j<values.length;j++){
//								String[] colomnValues = values[j].split("=");
//								EntCreditMainDto entCreditMainDto = (EntCreditMainDto) dd.newInstance();
//								int tr = Integer.parseInt(colomnValues[0].substring(colomnValues[0].indexOf("tr(")+3, colomnValues[0].indexOf(").td")));
//								int td = Integer.parseInt(colomnValues[0].substring(colomnValues[0].indexOf(".td(")+4, colomnValues[0].indexOf(").text")));
//								java.lang.reflect.Method method = null;
//								String colomn = colomnValues[1].split("\\.")[1];
//								method = dd.getDeclaredMethod("set"+colomn.substring(0, 1).toUpperCase()+colomn.substring(1),
//										String.class);
//								if(!method.isAccessible()){
//									method.setAccessible(true);
//								}
//								method.invoke(entCreditMainDto,table.getRow(tr).getCell(td).asText());
//								logger.info("entCreditMainDto info:{}",entCreditMainDto.toString());
//								entCreditMainDtosList.add(entCreditMainDto);
//							}
						}
					}
				}else if(scopes.length == 1){	//tr=model
					HtmlTable table = (HtmlTable)page.getElementsByTagName("table").get(Integer.parseInt(scopes[0]));
					if(StringUtils.isNoneBlank(table.asXml())){
//						{\"modelScope\":\"result.html.table(1).tr(1-end)\",\"modelValue\":\"element.td(3).text=model.idcard;\"}
						String trScope = modelScope.substring(modelScope.indexOf(".tr(")+4,modelScope.length()-1);
						String[] trScopes = trScope.split("-");
						for(int i=Integer.parseInt(trScopes[0]);i<(trScopes[1].equals("end")?table.getRowCount():Integer.parseInt(trScopes[1]));i++){
							String[] values = modelValue.split(";");
							for(int j=0;j<values.length;j++){
								String[] colomnValues = values[j].split("=");
								ProxyInfo proxyInfo = (ProxyInfo) dd.newInstance();
								int td = Integer.parseInt(colomnValues[0].substring(colomnValues[0].indexOf(".td(")+4, colomnValues[0].indexOf(").text")));
								java.lang.reflect.Method method = null;
								String colomn = colomnValues[1].split("\\.")[1];
								if(ProxyInfo.class.getDeclaredField(colomn).getType().getTypeName().equals(int.class.getName())){
									method = dd.getDeclaredMethod("set"+colomn.substring(0, 1).toUpperCase()+colomn.substring(1),
											int.class);
								}else{
									method = dd.getDeclaredMethod("set"+colomn.substring(0, 1).toUpperCase()+colomn.substring(1),
											String.class);
								}
								
								if(!method.isAccessible()){
									method.setAccessible(true);
								}
								if(method.getParameterTypes()[0].getName().equals("int")){
									method.invoke(proxyInfo,Integer.parseInt(table.getRow(j).getCell(td).asText()));
								}else{
									method.invoke(proxyInfo,table.getRow(j).getCell(td).asText());
								}
								
								logger.info("entCreditMainDto info:{}",proxyInfo.toString());
								entCreditMainDtosList.add(proxyInfo);
							}
						}
						
					}
				}
				
				
				
			}
			
			logger.info("------------current page end deal------------");
			return entCreditMainDtosList;
		} catch (Exception e) {
			logger.error("deal with the deail info error",e);
			return null;
		}
	}
	
}
