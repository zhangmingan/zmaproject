package com.cn.zmaproject.htmlUnit.parser;

import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
/**
 * @date 2017.06.08
 * @author zhangmingan
 * 超文本语言java解析类
 */
public class JsoupParser {
	private static final Logger logger = LoggerFactory.getLogger(JsoupParser.class);
	public static List<JSONObject> parseHtmlByRuleObject(String htmlText,JSONObject ruleObject){
		Document htmlDoc = Jsoup.parse(htmlText);
		return null;
	}
	
	private static Element getElementById(Document doc, String id){
		return doc.getElementById(id);
	}
}
