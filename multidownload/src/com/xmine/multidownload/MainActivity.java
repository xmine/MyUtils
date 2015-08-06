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
	private EditText et_path;// 声明EditText
	private EditText et_threadNumber;// 声明EditText
	private int threadCount;// 定义线程的数据
	private LinearLayout ll;// 声明LinearLayout
	private int runningThread; // 正在运行的线程
	private String path;// 声明路径
	private List<ProgressBar> pbs;// 声明进度的集合

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		// 下载路径
		et_path = (EditText) findViewById(R.id.et_path);
		et_threadNumber = (EditText) findViewById(R.id.et_threadnumber);
		// 动态添加进度条 根据线程的个数
		ll = (LinearLayout) findViewById(R.id.ll);
		// 定义一个集合 里面存 progressbar的引用
		pbs = new ArrayList<ProgressBar>();
	}

	// 点击按钮 进行 下载
	public void click(View v) {
		path = et_path.getText().toString().trim();
		String number = et_threadNumber.getText().toString().trim();
		threadCount = Integer.parseInt(number);
		// 先清除一下
		ll.removeAllViews();
		for (int i = 0; i < threadCount; i++) {
			// 动态添加 进度条
			ProgressBar pbBar = (ProgressBar) View.inflate(
					getApplicationContext(), R.layout.item, null);
			pbs.add(pbBar);
			ll.addView(pbBar);
		}
		// 开始下载
		new Thread() {
			public void run() {
				// 获取文件的大小
				try {
					// (1) 创建一个url对象 参数就是网址
					URL url = new URL(path);
					// (2)获取HttpURLConnection 链接对象
					HttpURLConnection conn = (HttpURLConnection) url
							.openConnection();
					// (3)设置参数 发送get请求
					conn.setRequestMethod("GET");
					// (4)设置链接网络的超时时间
					conn.setConnectTimeout(5000);
					// (5)获取服务器返回的状态码
					int code = conn.getResponseCode();
					// 200 代表获取服务器资源全部成功
					if (code == 200) {
						// (6)获取文件大小
						int length = conn.getContentLength();
						System.out.println("length:" + length);
						runningThread = threadCount;
						// 在客户端创建一个大小和服务器一模一样的文件
						RandomAccessFile raf = new RandomAccessFile(Environment
								.getExternalStorageDirectory().getPath()
								+ "/"
								+ getFileName(path), "rw");
						raf.setLength(length);
						// 每个线程下载的大小
						int blockSize = length / threadCount;
						// 计算出每个线程下载的开始位置和结束位置
						for (int i = 0; i < threadCount; i++) {
							// 每个线程下载的开始位置
							int startIndex = i * blockSize;
							// 每个线程下载的结束位置
							int endIndex = (i + 1) * blockSize - 1;
							// 当最后一个线程时
							if (i == threadCount - 1) {
								endIndex = length - 1;
							}
							// 开启多个线程
							DownThread downThread = new DownThread(startIndex,
									endIndex, i);
							// 开启线程
							downThread.start();
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			};
		}.start();
	}

	// 去服务器下载的线程
	private class DownThread extends Thread {
		// 开始位置
		private int startIndex;
		// 结束位置
		private int endIndex;
		// 线程id
		private int threadId;
		// 当前线程的最大进度
		private int pbmaxSize;
		// 如果中断过，代表当前线程上次下载的位置
		private int pblastPosition;

		// 构造方法
		public DownThread(int startIndex, int endIndex, int threadId) {
			this.startIndex = startIndex;
			this.endIndex = endIndex;
			this.threadId = threadId;
		}

		@Override
		public void run() {
			try {
				// 进度条的大小
				pbmaxSize = endIndex - startIndex;
				// 创建一个url对象 参数就是网址
				URL url = new URL(path);
				// 获取HttpURLConnection 链接对象
				HttpURLConnection conn = (HttpURLConnection) url
						.openConnection();
				// 设置参数 发送get请求
				conn.setRequestMethod("GET");
				// 设置链接网络的超时时间
				conn.setConnectTimeout(5000);
				// 获取保存文件
				File file = new File(Environment.getExternalStorageDirectory()
						.getPath() + "/" + threadId + ".txt");
				// 文件存在时读取位置
				if (file.exists() && file.length() > 0) {
					FileInputStream fis = new FileInputStream(file);
					BufferedReader bufr = new BufferedReader(
							new InputStreamReader(fis));
					// 上次下载的位置
					String lastPositionn = bufr.readLine();
					int lastPosition = Integer.parseInt(lastPositionn);
					// 上次下载的结束位置
					pblastPosition = lastPosition - startIndex;
					// 重新赋值
					startIndex = lastPosition;
					// 设置头信息，告诉服务器每个线程下载的开始位置和结束位置
					conn.setRequestProperty("Range", "bytes=" + lastPosition
							+ "-" + endIndex);
					// 关闭资源
					bufr.close();
				} else {
					// 设置头信息，告诉服务器每个线程下载的开始位置和结束位置
					conn.setRequestProperty("Range", "bytes=" + startIndex
							+ "-" + endIndex);
				}
				// 获取服务器返回的状态码
				int code = conn.getResponseCode();
				// 206请求部分资源
				if (code == 206) {
					// 使用RandomAccessFile随机写入
					RandomAccessFile raf = new RandomAccessFile(Environment
							.getExternalStorageDirectory().getPath()
							+ "/"
							+ getFileName(path), "rw");
					raf.seek(startIndex);
					// 获取流
					InputStream in = conn.getInputStream();
					int len = -1;
					byte[] buffer = new byte[1024 * 1024];
					// 代表当前线程下载的大小
					int total = 0;
					while ((len = in.read(buffer)) != -1) {
						raf.write(buffer, 0, len);
						total += len;
						// 当前线程下载的位置
						int currenThreadPosition = startIndex + total;
						// 使用RandomAccessFile随机写入
						RandomAccessFile raff = new RandomAccessFile(
								Environment.getExternalStorageDirectory()
										.getPath() + "/" + threadId + ".txt",
								"rwd");
						// 写入当前位置
						raff.write(String.valueOf(currenThreadPosition)
								.getBytes());
						// 关闭资源
						raff.close();
						// 更新进度条，设置最大进度
						pbs.get(threadId).setMax(pbmaxSize);
						// 更新进度条，设置当前进度
						pbs.get(threadId).setProgress(pblastPosition + total);
					}
					// 释放资源
					raf.close();
					// 把创建的txt文件删掉，同步处理
					synchronized (DownThread.class) {
						runningThread--;
						if (runningThread == 0) {
							// 把文件删除
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

	// 获取下载文件的名称
	public String getFileName(String path) {
		int start = path.lastIndexOf("/") + 1;
		return path.substring(start);
	}
}
