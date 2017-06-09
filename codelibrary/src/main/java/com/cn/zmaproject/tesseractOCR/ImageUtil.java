package com.cn.zmaproject.tesseractOCR;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.imageio.ImageIO;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

@SuppressWarnings("restriction")
public class ImageUtil {
	
	/**
	 * 将图片文件转化为字节数组字符串，并对其进行Base64编码处理
	 * 
	 * @param imgFilePath
	 * @return
	 */
	public static String GetImageStr(String imgFilePath) {
		byte[] data = null;

		// 读取图片字节数组
		try {
			InputStream in = new FileInputStream(imgFilePath);
			data = new byte[in.available()];
			in.read(data);
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// 对字节数组Base64编码
		BASE64Encoder encoder = new BASE64Encoder();
		return encoder.encode(data);// 返回Base64编码过的字节数组字符串
	}
	
	/**
	 * 对字节数组字符串进行Base64解码并生成图片
	 * 
	 * @param imgStr
	 * @param imgFilePath
	 * @return
	 */
	public static boolean GenerateImage(String imgStr, String imgParentFilePath, String fileName) {
		if (imgStr == null) // 图像数据为空
			return false;
		BASE64Decoder decoder = new BASE64Decoder();
		try {
			// Base64解码
			byte[] bytes = decoder.decodeBuffer(imgStr);
			for (int i = 0; i < bytes.length; ++i) {
				if (bytes[i] < 0) {// 调整异常数据
					bytes[i] += 256;
				}
			}
			// 生成jpeg图片
			OutputStream out = new FileOutputStream(imgParentFilePath + "\\" + fileName);
			out.write(bytes);
			out.flush();
			out.close();
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	public static boolean GenerateImage(byte[] imgBytes,  String imgParentFilePath, String fileName) {
		if (imgBytes == null) // 图像数据为空
			return false;
		try {
			for (int i = 0; i < imgBytes.length; ++i) {
				if (imgBytes[i] < 0) {// 调整异常数据
					imgBytes[i] += 256;
				}
			}
			// 生成jpeg图片
			OutputStream out = new FileOutputStream(imgParentFilePath + "\\" + fileName);
			out.write(imgBytes);
			out.flush();
			out.close();
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	
	/**
	 * 替换图片背景色
	 * @param imageFile
	 * @param imgFilePath
	 * @return
	 */
	public static boolean colorImage(File imageFile, String imgFilePath) {
		try {
			/**
			 * 定义一个RGB的数组，因为图片的RGB模式是由三个 0-255来表示的 比如白色就是(255,255,255)
			 */
			int[] rgb = new int[3];
			/**
			 * 用来处理图片的缓冲流
			 */
			BufferedImage bi = null;

			/**
			 * 用ImageIO将图片读入到缓冲中
			 */
			bi = ImageIO.read(imageFile);

			/**
			 * 得到图片的长宽
			 */
			int width = bi.getWidth();
			int height = bi.getHeight();
			int minx = bi.getMinX();
			int miny = bi.getMinY();
			System.out.println("正在处理：" + imageFile.getName());

			/**
			 * 这里是遍历图片的像素，因为要处理图片的背色，所以要把指定像素上的颜色换成目标颜色 这里 是一个二层循环，遍历长和宽上的每个像素
			 */
			for (int i = minx; i < width; i++) {
				for (int j = miny; j < height; j++) {
					// System.out.print(bi.getRGB(jw, ih));
					/**
					 * 得到指定像素（i,j)上的RGB值，
					 */
					int pixel = bi.getRGB(i, j);
					/**
					 * 分别进行位操作得到 r g b上的值
					 */
					rgb[0] = (pixel & 0xff0000) >> 16;
					rgb[1] = (pixel & 0xff00) >> 8;
					rgb[2] = (pixel & 0xff);
					/**
					 * 进行换色操作，我这里是要把蓝底换成白底，那么就判断图片中rgb值是否在蓝色范围的像素
					 */
					if(rgb[0]<255&&rgb[0]>105 && rgb[1]<255&&rgb[1]>105 && rgb[2]<255&&rgb[2]>105 ){  
						/**
						 * 这里是判断通过，则把该像素换成白色
						 */
						bi.setRGB(i, j, 0xffffff);
					}

				}
			}
			System.out.println("\t处理完毕：" + imageFile.getName());
			/**
			 * 将缓冲对象保存到新文件中
			 */
			FileOutputStream ops = new FileOutputStream(new File("D:\\Pictures\\wan11.jpg"));
			ImageIO.write(bi, "jpg", ops);
			ops.flush();
			ops.close();
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * 
	 * @param sfile
	 *            需要去噪的图像
	 * @param destDir
	 *            去噪后的图像保存地址
	 * @throws IOException
	 */
	public static void cleanImage(File sfile, String destDir)
			throws IOException {
		File destF = new File(destDir);
		if (!destF.exists()) {
			destF.mkdirs();
		}

		BufferedImage bufferedImage = ImageIO.read(sfile);
		int h = bufferedImage.getHeight();
		int w = bufferedImage.getWidth();

		// 灰度化
		int[][] gray = new int[w][h];
		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y++) {
				int argb = bufferedImage.getRGB(x, y);
				// 图像加亮（调整亮度识别率非常高）
				int r = (int) (((argb >> 16) & 0xFF) * 1.1 + 30);
				int g = (int) (((argb >> 8) & 0xFF) * 1.1 + 30);
				int b = (int) (((argb >> 0) & 0xFF) * 1.1 + 30);
				if (r >= 255) {
					r = 255;
				}
				if (g >= 255) {
					g = 255;
				}
				if (b >= 255) {
					b = 255;
				}
				gray[x][y] = (int) Math
						.pow((Math.pow(r, 2.2) * 0.2973 + Math.pow(g, 2.2)
								* 0.6274 + Math.pow(b, 2.2) * 0.0753), 1 / 2.2);
			}
		}

		// 二值化
		int threshold = ostu(gray, w, h);
		BufferedImage binaryBufferedImage = new BufferedImage(w, h,
				BufferedImage.TYPE_BYTE_BINARY);
		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y++) {
				if (gray[x][y] > threshold) {
					gray[x][y] |= 0x00FFFF;
				} else {
					gray[x][y] &= 0xFF0000;
				}
				binaryBufferedImage.setRGB(x, y, gray[x][y]);
			}
		}

		// 矩阵打印
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				if (isBlack(binaryBufferedImage.getRGB(x, y))) {
					System.out.print("*");
				} else {
					System.out.print(" ");
				}
			}
			System.out.println();
		}

		ImageIO.write(binaryBufferedImage, "jpg",
				new File(destDir, sfile.getName()));
	}

	public static boolean isBlack(int colorInt) {
		Color color = new Color(colorInt);
		if (color.getRed() + color.getGreen() + color.getBlue() <= 300) {
			return true;
		}
		return false;
	}

	public static boolean isWhite(int colorInt) {
		Color color = new Color(colorInt);
		if (color.getRed() + color.getGreen() + color.getBlue() > 300) {
			return true;
		}
		return false;
	}

	public static int isBlackOrWhite(int colorInt) {
		if (getColorBright(colorInt) < 30 || getColorBright(colorInt) > 730) {
			return 1;
		}
		return 0;
	}

	public static int getColorBright(int colorInt) {
		Color color = new Color(colorInt);
		return color.getRed() + color.getGreen() + color.getBlue();
	}

	public static int ostu(int[][] gray, int w, int h) {
		int[] histData = new int[w * h];
		// Calculate histogram
		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y++) {
				int red = 0xFF & gray[x][y];
				histData[red]++;
			}
		}

		// Total number of pixels
		int total = w * h;

		float sum = 0;
		for (int t = 0; t < 256; t++)
			sum += t * histData[t];

		float sumB = 0;
		int wB = 0;
		int wF = 0;

		float varMax = 0;
		int threshold = 0;

		for (int t = 0; t < 256; t++) {
			wB += histData[t]; // Weight Background
			if (wB == 0)
				continue;

			wF = total - wB; // Weight Foreground
			if (wF == 0)
				break;

			sumB += (float) (t * histData[t]);

			float mB = sumB / wB; // Mean Background
			float mF = (sum - sumB) / wF; // Mean Foreground

			// Calculate Between Class Variance
			float varBetween = (float) wB * (float) wF * (mB - mF) * (mB - mF);

			// Check if new maximum found
			if (varBetween > varMax) {
				varMax = varBetween;
				threshold = t;
			}
		}

		return threshold;
	}


	public static void main(String[] args) throws IOException {
		// 测试从Base64编码转换为图片文件
		 String strImg =
		 "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";
		 GenerateImage(strImg, "D:\\Pictures", "wan.jpg");
		// 测试从图片文件转换为Base64编码
		System.out.println(GetImageStr("d:\\Pictures\\wan.jpg"));
		
		colorImage(new File("d:\\Pictures\\wan.jpg"), "D:\\Pictures\\wan11.jpg");
		
		cleanImage(new File("d:\\Pictures\\wan.jpg"), "d:\\Pictures\\");
	}
}
