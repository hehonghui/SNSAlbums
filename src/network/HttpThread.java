package network;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

/**
 * HttpЭ�飺����post���󣬲����շ���ֵ
 * @author xxjgood
 * @param  ArrayList<NameValuePair>
 * @return String or Bitmap ���������ص��ַ��ͼƬ
 * @date   2012.4.20
 * 
 * ��ע��
 * deal = 1  	 ע��   			���سɹ���ʧ��
 * deal = 2  	 ��¼   			����ʧ�ܻ������
 * deal = 3  	 ����ĳ���			����ͼƬ��
 * deal = 100	 ����Сͼ			����Сͼ
 * deal = 101	 �����ͼ			���ش�ͼ
 * deal = 6		 �ϴ�ͼƬ			���سɹ�ʧ��
 * deal = 7		 ��ȡ�����б�		�������к���IP
 * deal = 8		 ����������		���غ������
 * deal = 10	 �˳�				���سɹ�ʧ��   
 * deal = 11	 ������� 			��������б�
 * deal = 12	 �½����			���سɹ���ʧ��
 * deal = 13	 ɾ�����			���سɹ���ʧ��
 * deal = 14	 �޸����			���سɹ���ʧ��
 * deal = 15	 ɾ��ͼƬ			���سɹ���ʧ��
 */

public class HttpThread { 
	
	private int deal = 0;						// Э��
	private String the_string_response = null;	// ��ŷ��ص�String
	private Bitmap bitmap = null;				// ��ŷ���ͼƬ��bitmap
	//private String url = "http://ass001.gotoip55.com/Android/receiveMessage.php";			// Ҫ���ӵ�url	
	private ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();	// ���͵�ArrayList
	private final String TAG = "HttpThread";
	public static String serverURL = "";				// ������URL,�����ý�������趨
	
	private String url = "http://199.36.75.40/Android/receiveMessage.php";
	

	/**
	 * 
	 * @Constructor: 
	 * @@param namevaluepairs
	 * @@param Deal
	 * @Description: ���캯��,��ʼ��Э���Ҫ���͵�����
	 * @param namevaluepairs
	 * @param Deal
	 */
	public HttpThread(ArrayList<NameValuePair> namevaluepairs,int Deal) {	
		
		nameValuePairs = namevaluepairs;										
		deal = Deal;
		
		// ͨ�����ý������÷�����IP
		if (!serverURL.equals(""))
		{
			url = serverURL;
			Log.d(TAG, "�ҵķ�����IP"+url);
		}
	}
	
	
	/**
	 * @Method: sendInfo
	 * @Description: HTTP POST����
	 * @return
	 */
	public Object sendInfo() {														
		try{ 						
			// httpClientЭ��
			HttpClient httpclient = new DefaultHttpClient(); 				
			// ���ó�ʱ����
			HttpParams params = httpclient.getParams();
			HttpConnectionParams.setConnectionTimeout(params, 8000);
			HttpConnectionParams.setSoTimeout(params, 8000);
			
			HttpPost httppost = new	HttpPost(url); 							// HttpPost
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs,HTTP.UTF_8));	// ����ݷ���entity
			HttpResponse response = httpclient.execute(httppost);			// �������
			
			// ���Э���жϽ���String or Bitmap
			if(deal < 100){			
				the_string_response = convertResponseToString(response); 	// ���շ���ֵ
				return the_string_response;
			}
			else if(deal == 100 || deal == 101){
				bitmap = convertResponseToBitmap(response);
				return bitmap;
			}
			else{
				return 0;
			}
		}catch(Exception e){
			Log.d("1", "Error in http connection " + e.toString());
			return "error";
		}
	}
	
	
	/**
	 * @Method: convertResponseToString
	 * @Description:   ���� http post ���ص�String
	 * @param response ���������ص��ַ��ɹ���ʧ�ܡ���ݣ�
	 * @return
	 * @throws IllegalStateException
	 * @throws IOException
	 */
	public String convertResponseToString(HttpResponse response)
			throws IllegalStateException, IOException {
		String res = null; // ���ص�String
		StringBuffer buffer = new StringBuffer(); // new Buffer
		InputStream inputStream = response.getEntity().getContent(); // getting
																		// inputStream
		int contentLength = (int) response.getEntity().getContentLength(); // getting
																			// content
																			// length
		contentLength = inputStream.available();
		// int contentLength = (int)inputStream.read();
		Log.d(TAG, "len : " + contentLength);

		if (contentLength < 0) {
			Log.d(TAG, "contentLength < 0");
		} else {
			byte[] data = new byte[512];
			int len = 0;
			try {
				while (-1 != (len = inputStream.read(data))) {
					buffer.append(new String(data, 0, len)); // converting to
																// string and
																// appending to
																// buffer
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

			try {

				inputStream.close(); // closing the stream
			} catch (IOException e) {
				e.printStackTrace();
			}

			res = buffer.toString(); // buffer to string
		}

		return res;
	}

	
	/**
	 * @Method: convertResponseToBitmap
	 * @Description:   ���� http post ���ص�ͼƬ
	 * @param response
	 * @return
	 * @throws IllegalStateException
	 * @throws IOException
	 */
	public Bitmap convertResponseToBitmap(HttpResponse response) throws IllegalStateException, IOException{
		InputStream inputStream = response.getEntity().getContent();	// ��ȡ��
		Bitmap bitmap = BitmapFactory.decodeStream(inputStream);		// ����ͼƬ
		
		try {
			inputStream.close(); 										// closing the stream��.. 
		}
		catch (IOException e) {
			e.printStackTrace(); 
		}
		
		return bitmap;
	}
}

