package com.xmine.multidownload;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import com.xmine.multidownload.R;

public class MainActivity extends Activity {
	private EditText et_path;// ����EditText
	private EditText et_threadNumber;// ����EditText
	private int threadCount;// �����̵߳�����
	private LinearLayout ll;// ����LinearLayout
	private int runningThread; // �������е��߳�
	private String path;// ����·��
	private List<ProgressBar> pbs;// �������ȵļ���

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		// ����·��
		et_path = (EditText) findViewById(R.id.et_path);
		et_threadNumber = (EditText) findViewById(R.id.et_threadnumber);
		// ��̬��ӽ����� �����̵߳ĸ���
		ll = (LinearLayout) findViewById(R.id.ll);
		// ����һ������ ����� progressbar������
		pbs = new ArrayList<ProgressBar>();
	}

	// �����ť ���� ����
	public void click(View v) {
		path = et_path.getText().toString().trim();
		String number = et_threadNumber.getText().toString().trim();
		threadCount = Integer.parseInt(number);
		// �����һ��
		ll.removeAllViews();
		for (int i = 0; i < threadCount; i++) {
			// ��̬��� ������
			ProgressBar pbBar = (ProgressBar) View.inflate(
					getApplicationContext(), R.layout.item, null);
			pbs.add(pbBar);
			ll.addView(pbBar);
		}
		// ��ʼ����
		new Thread() {
			public void run() {
				// ��ȡ�ļ��Ĵ�С
				try {
					// (1) ����һ��url���� ����������ַ
					URL url = new URL(path);
					// (2)��ȡHttpURLConnection ���Ӷ���
					HttpURLConnection conn = (HttpURLConnection) url
							.openConnection();
					// (3)���ò��� ����get����
					conn.setRequestMethod("GET");
					// (4)������������ĳ�ʱʱ��
					conn.setConnectTimeout(5000);
					// (5)��ȡ���������ص�״̬��
					int code = conn.getResponseCode();
					// 200 �����ȡ��������Դȫ���ɹ�
					if (code == 200) {
						// (6)��ȡ�ļ���С
						int length = conn.getContentLength();
						System.out.println("length:" + length);
						runningThread = threadCount;
						// �ڿͻ��˴���һ����С�ͷ�����һģһ�����ļ�
						RandomAccessFile raf = new RandomAccessFile(Environment
								.getExternalStorageDirectory().getPath()
								+ "/"
								+ getFileName(path), "rw");
						raf.setLength(length);
						// ÿ���߳����صĴ�С
						int blockSize = length / threadCount;
						// �����ÿ���߳����صĿ�ʼλ�úͽ���λ��
						for (int i = 0; i < threadCount; i++) {
							// ÿ���߳����صĿ�ʼλ��
							int startIndex = i * blockSize;
							// ÿ���߳����صĽ���λ��
							int endIndex = (i + 1) * blockSize - 1;
							// �����һ���߳�ʱ
							if (i == threadCount - 1) {
								endIndex = length - 1;
							}
							// ��������߳�
							DownThread downThread = new DownThread(startIndex,
									endIndex, i);
							// �����߳�
							downThread.start();
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			};
		}.start();
	}

	// ȥ���������ص��߳�
	private class DownThread extends Thread {
		// ��ʼλ��
		private int startIndex;
		// ����λ��
		private int endIndex;
		// �߳�id
		private int threadId;
		// ��ǰ�̵߳�������
		private int pbmaxSize;
		// ����жϹ�������ǰ�߳��ϴ����ص�λ��
		private int pblastPosition;

		// ���췽��
		public DownThread(int startIndex, int endIndex, int threadId) {
			this.startIndex = startIndex;
			this.endIndex = endIndex;
			this.threadId = threadId;
		}

		@Override
		public void run() {
			try {
				// �������Ĵ�С
				pbmaxSize = endIndex - startIndex;
				// ����һ��url���� ����������ַ
				URL url = new URL(path);
				// ��ȡHttpURLConnection ���Ӷ���
				HttpURLConnection conn = (HttpURLConnection) url
						.openConnection();
				// ���ò��� ����get����
				conn.setRequestMethod("GET");
				// ������������ĳ�ʱʱ��
				conn.setConnectTimeout(5000);
				// ��ȡ�����ļ�
				File file = new File(Environment.getExternalStorageDirectory()
						.getPath() + "/" + threadId + ".txt");
				// �ļ�����ʱ��ȡλ��
				if (file.exists() && file.length() > 0) {
					FileInputStream fis = new FileInputStream(file);
					BufferedReader bufr = new BufferedReader(
							new InputStreamReader(fis));
					// �ϴ����ص�λ��
					String lastPositionn = bufr.readLine();
					int lastPosition = Integer.parseInt(lastPositionn);
					// �ϴ����صĽ���λ��
					pblastPosition = lastPosition - startIndex;
					// ���¸�ֵ
					startIndex = lastPosition;
					// ����ͷ��Ϣ�����߷�����ÿ���߳����صĿ�ʼλ�úͽ���λ��
					conn.setRequestProperty("Range", "bytes=" + lastPosition
							+ "-" + endIndex);
					// �ر���Դ
					bufr.close();
				} else {
					// ����ͷ��Ϣ�����߷�����ÿ���߳����صĿ�ʼλ�úͽ���λ��
					conn.setRequestProperty("Range", "bytes=" + startIndex
							+ "-" + endIndex);
				}
				// ��ȡ���������ص�״̬��
				int code = conn.getResponseCode();
				// 206���󲿷���Դ
				if (code == 206) {
					// ʹ��RandomAccessFile���д��
					RandomAccessFile raf = new RandomAccessFile(Environment
							.getExternalStorageDirectory().getPath()
							+ "/"
							+ getFileName(path), "rw");
					raf.seek(startIndex);
					// ��ȡ��
					InputStream in = conn.getInputStream();
					int len = -1;
					byte[] buffer = new byte[1024 * 1024];
					// ����ǰ�߳����صĴ�С
					int total = 0;
					while ((len = in.read(buffer)) != -1) {
						raf.write(buffer, 0, len);
						total += len;
						// ��ǰ�߳����ص�λ��
						int currenThreadPosition = startIndex + total;
						// ʹ��RandomAccessFile���д��
						RandomAccessFile raff = new RandomAccessFile(
								Environment.getExternalStorageDirectory()
										.getPath() + "/" + threadId + ".txt",
								"rwd");
						// д�뵱ǰλ��
						raff.write(String.valueOf(currenThreadPosition)
								.getBytes());
						// �ر���Դ
						raff.close();
						// ���½�����������������
						pbs.get(threadId).setMax(pbmaxSize);
						// ���½����������õ�ǰ����
						pbs.get(threadId).setProgress(pblastPosition + total);
					}
					// �ͷ���Դ
					raf.close();
					// �Ѵ�����txt�ļ�ɾ����ͬ������
					synchronized (DownThread.class) {
						runningThread--;
						if (runningThread == 0) {
							// ���ļ�ɾ��
							for (int i = 0; i < threadCount; i++) {
								File delfile = new File(Environment
										.getExternalStorageDirectory()
										.getPath()
										+ "/" + i + ".txt");
								delfile.delete();
							}
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	// ��ȡ�����ļ�������
	public String getFileName(String path) {
		int start = path.lastIndexOf("/") + 1;
		return path.substring(start);
	}
}
