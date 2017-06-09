package com.cn.zmaproject.tesseractOCR;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.cn.zmaproject.tesseractOCR.OCRTest.ImageIOHelper;
/**
 * 
 * @author zhangmingan
 *
 */
public class tesseractUtil {
	private final static String LANG_OPTION = "-l"; // 英文字母小写l，并非数字1
	private final static String EOL = System.getProperty("line.separator");
	private static String tessPath = "D:\\Pictures\\Tesseract-OCR";
	private static String OS = System.getProperty("os.name").toLowerCase();

	
	public static String excuteImage(String parentPath, String fileName) throws Exception{

		File outputFile = new File(parentPath, "tesseractResult");
		StringBuffer strB = new StringBuffer();
		List<String> cmd = new ArrayList<String>();
		if (OS.indexOf("windows") >= 0) {
			cmd.add(tessPath + "\\tesseract");
		} else if (OS.indexOf("linux") >= 0) {
			cmd.add("tesseract");
		} else {
			cmd.add("tesseract");
		}
		cmd.add(fileName);
		cmd.add(outputFile.getName());
		cmd.add(LANG_OPTION);
		cmd.add("eng");

		ProcessBuilder pb = new ProcessBuilder();
		pb.directory(new File(parentPath));

		pb.command(cmd);
		pb.redirectErrorStream(true);

		Process process = pb.start();
		// tesseract.exe 1.jpg 1 -l chi_sim
		int w = process.waitFor();

		if (w == 0) {
			BufferedReader in = new BufferedReader(new InputStreamReader(
					new FileInputStream(outputFile.getAbsolutePath() + ".txt"),
					"UTF-8"));

			String str;
			while ((str = in.readLine()) != null) {
				strB.append(str).append(EOL);
			}
			in.close();
		} else {
			String msg;
			switch (w) {
			case 1:
				msg = "Errors accessing files.There may be spaces in your image's filename.";
				break;
			case 29:
				msg = "Cannot recongnize the image or its selected region.";
				break;
			case 31:
				msg = "Unsupported image format.";
				break;
			default:
				msg = "Errors occurred.";
			}
			throw new RuntimeException(msg);
		}
		new File(outputFile.getAbsolutePath() + ".txt").delete();
		return strB.toString();
	}
	
	
	public static String excuteImage(File imageFile) throws Exception{
		return excuteImage(imageFile.getParent(),imageFile.getName());
	}
	
	public static String excuteImage(byte[] imageFileBytes) throws Exception{
		ImageUtil.GenerateImage(imageFileBytes, tessPath, "workImage.jpg");
		return excuteImage(tessPath, "workImage.jpg");
	}
	
	public static String excuteImage(String imageStr) throws Exception{
		ImageUtil.GenerateImage(imageStr, tessPath, "workImage.jpg");
		return excuteImage(tessPath, "workImage.jpg");
	}
	
	
	
	
	
	
}
