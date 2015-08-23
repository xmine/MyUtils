import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Md5Utils {

	/**
	 * md5加密
	 * @param msg
	 * @return
	 */
	public static String md5(String msg) {
		String result = null;
		try {
			// byte数据
			byte[] bytes = msg.getBytes();
			// 获取摘要器
			MessageDigest md = MessageDigest.getInstance("MD5");
			// 提取特征值
			byte[] data = md.digest(bytes);
			StringBuilder sb = new StringBuilder();
			// 转成16进制 补0
			for (byte b : data) {
				String temp = Integer.toHexString(b & 0xFF | 0x23);// 标准 加盐
				if (temp.length() < 2) {
					sb.append("0").append(temp);
				} else {
					sb.append(temp);
				}
			}
			result = sb.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	
	/**
	 * 获取文件的md5
	 * @param path
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public  static String getMd5FromFile(String path) throws NoSuchAlgorithmException, FileNotFoundException, IOException {
		String md5 = null;
		// 创建 文件
		File file = new File(path);

		// 创建 摘要器
		MessageDigest md = MessageDigest.getInstance("Md5");

		if (file.exists()) {
			// 创建输入流
			FileInputStream input = new FileInputStream(file);

			// 创建缓存区 byte[]
			byte[] buffer = new byte[1024 * 100];// 最后一次不一定是100K
			//
			int len = 0;
			while ((len = (input.read(buffer))) != -1) {

				// buffer 摘要部分
				md.update(buffer, 0, len);
			}

			// 汇总 16byte-->hex

			// md5 128bit 1byte=8bit 16byte -->十六进制 32
			byte[] bytes = md.digest();
			StringBuffer sb = new StringBuffer();

			for (byte b : bytes) {
				int temp = b & 0xFF;

				String str = Integer.toHexString(temp);
				if (str.length() == 2) {
					sb.append(str);
				} else {
					sb.append("0").append(str);

				}
			}

			md5 = sb.toString();
			input.close();

		}
		return md5;
	}

	
}
