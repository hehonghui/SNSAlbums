package network;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import org.apache.http.conn.util.InetAddressUtils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings.Secure;


/**
 * @ClassName: NetInfomation 
 * @Description:    ��ȡ�����������Ϣ
 * @Author: Mr.Simple (�κ��)
 * @E-mail: bboyfeiyu@gmail.com 
 * @Date 2012-11-17 ����7:40:03 
 *
 */
public class NetworkUtils {
	
	
	/**
	 * @Method: getNetworkStatus
	 * @Description:  �ж��Ƿ�����������
	 * @param context
	 * @return
	 */
	public static boolean getNetworkStatus(Context context)
	{
		ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
		// WIFI���
		NetworkInfo info = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		// boolean isWifiOn = info.isAvailable();
		boolean isWifiConnected = info.isConnected();
		if ( isWifiConnected )
		{
			return isWifiConnected;
		}
		
		// mobile������
		info = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		boolean isMobileConnected = info.isConnected();
		return isMobileConnected;
		
	}

	
	/**
	 * @Method: getLocalIpAddress
	 * @Description:   ��ȡ����IP��ַ
	 * @return
	 */
	public static String getLocalIpAddress() {  
	
	        try{ 
	             for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) { 
	                 NetworkInterface intf = en.nextElement();   
	                    for (Enumeration<InetAddress> enumIpAddr = intf   
	                            .getInetAddresses(); enumIpAddr.hasMoreElements();) {   
	                        InetAddress inetAddress = enumIpAddr.nextElement();   
	                        if (!inetAddress.isLoopbackAddress() && InetAddressUtils.isIPv4Address(inetAddress.getHostAddress())) {   
	                             
	                            return inetAddress.getHostAddress().toString();   
	                        }   
	                    }   
	             } 
	        }catch (SocketException e) { 
	        	e.printStackTrace();
	        } 
	         
	        return null;  
	    
	    }  	// end of getLocalIpAddress

	

	/**
	 * @Method: checkNetWorkStatus
	 * @Description:  ����Ƿ��Ѿ�����������
	 * @param context
	 * @return
	 */
	public static boolean checkNetWorkStatus(Context context) {  
        boolean netSataus = false;  
        ConnectivityManager lxfManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);  
        lxfManager.getActiveNetworkInfo();  
        if (lxfManager.getActiveNetworkInfo() != null) {  
            netSataus = lxfManager.getActiveNetworkInfo().isAvailable();  
        }  
        return netSataus;  
    }
	

	/**
	 * @Method: getMobileID
	 * @Description:  ��ȡ�ֻ��Ψһ���
	 * @param context
	 * @return
	 */
	public static String getMobileID(Context context)
	{
		return Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
	}

}
