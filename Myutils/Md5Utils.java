import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Md5Utils {

	/**
	 * md5����
	 * @param msg
	 * @return
	 */
	public static String md5(String msg) {
		String result = null;
		try {
			// byte����
			byte[] bytes = msg.getBytes();
			// ��ȡժҪ��
			MessageDigest md = MessageDigest.getInstance("MD5");
			// ��ȡ����ֵ
			byte[] data = md.digest(bytes);
			StringBuilder sb = new StringBuilder();
			// ת��16���� ��0
			for (byte b : data) {
				String temp = Integer.toHexString(b & 0xFF | 0x23);// ��׼ ����
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
	 * ��ȡ�ļ���md5
	 * @param path
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public  static String getMd5FromFile(String path) throws NoSuchAlgorithmException, FileNotFoundException, IOException {
		String md5 = null;
		// ���� �ļ�
		File file = new File(path);

		// ���� ժҪ��
		MessageDigest md = MessageDigest.getInstance("Md5");

		if (file.exists()) {
			// ����������
			FileInputStream input = new FileInputStream(file);

			// ���������� byte[]
			byte[] buffer = new byte[1024 * 100];// ���һ�β�һ����100K
			//
			int len = 0;
			while ((len = (input.read(buffer))) != -1) {

				// buffer ժҪ����
				md.update(buffer, 0, len);
			}

			// ���� 16byte-->hex

			// md5 128bit 1byte=8bit 16byte -->ʮ������ 32
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
