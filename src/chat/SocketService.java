package chat;


/**
 * 
 * @ClassName: SocketService 
 * @Description: Socket������,�ں�̨��������ա��㲥��Ϣ
 * @Author: Mr.Simple (�κ��)
 * @E-mail: bboyfeiyu@gmail.com 
 * @Date 2012-11-17 ����5:02:12 
 *
 */

public class SocketService extends Service {

	private int mStartId = 0;
	private final IBinder mBinder = new LocalBinder();  
	
	public static HashMap<String, String> mMsgMap = new HashMap<String, String>();
	
	public static UdpReceiveThread mUdpRevThread = null;	// UDP�����߳�,��̨����գģе���ݰ�

	public static DatagramSocket mUdpRevSocket = null;		// UDP SOCKET�����ں�̨�������
	public static boolean mUdpStop = false;					// socket�����Ƿ�ֹͣ�ı�ʶλ
	private final String TAG = "SocketService";
	private final int SOCKET_PORT = 8765;


	/*
	 * (�� Javadoc,��д�ķ���) 
	 * @Title: onCreate
	 * @Description:  ���񴴽���ִֻ��һ��
	 * @see android.app.Service#onCreate()
	 */
	@Override
	public void onCreate() {
		
		Log.d("", "Service����");

		initUdpSocket();						// ��ʼ��socket,�û�������Ϣ

	}


	/*
	 * (�� Javadoc,��д�ķ���) 
	 * @Title: onStart
	 * @Description: ��������
	 * @param intent
	 * @param startId 
	 * @see android.app.Service#onStart(android.content.Intent, int)
	 */
	@Override
	public void onStart(Intent intent, int startId) {
		
		Log.d(TAG, " �������� -----> id : " + startId);
		
		super.onStart(intent, startId);
	}

	
	/*
	 * (�� Javadoc,��д�ķ���) 
	 * @Title: onDestroy
	 * @Description:  �������
	 * @see android.app.Service#onDestroy()
	 */
	@Override
	public void onDestroy() {
		
		Log.d(TAG, "Service���");
		
		mUdpRevThread.interrupt();
		mUdpRevThread = null;
		
		this.stopSelf();							// �رշ���

	}

	
	/*
	 * (�� Javadoc,��д�ķ���) 
	 * @Title: onBind
	 * @Description: �����
	 * @param arg0
	 * @return 
	 * @see android.app.Service#onBind(android.content.Intent)
	 */
	@Override
	public IBinder onBind(Intent arg0) {
		
		Log.d(TAG,"�󶨷���");
		return mBinder;
	}

	
	/*
	 * (�� Javadoc,��д�ķ���) 
	 * @Title: onRebind
	 * @Description: �������°�
	 * @param intent 
	 * @see android.app.Service#onRebind(android.content.Intent)
	 */
	@Override
	public void onRebind(Intent intent) {
		
		Log.d(TAG, "���°�");
		super.onRebind(intent);
	}
	
	
	/*
	 * (�� Javadoc,��д�ķ���) 
	 * @Title: onStartCommand
	 * @Description:  ִ������
	 * @param intent
	 * @param flags
	 * @param startId
	 * @return 
	 * @see android.app.Service#onStartCommand(android.content.Intent, int, int)
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		mStartId = startId;
		return START_STICKY;
	}


	/*
	 * (�� Javadoc,��д�ķ���) 
	 * @Title: onUnbind
	 * @Description:  ����
	 * @param intent
	 * @return 
	 * @see android.app.Service#onUnbind(android.content.Intent)
	 */
	@Override
	public boolean onUnbind(Intent intent) {
		
		Log.d(TAG, "����");
		return super.onUnbind(intent);
	}
	

	/**
	 * @Method: initUdpSocket 
	 * @Description: Socket�ĳ�ʼ��  
	 * @throws
	 */
	private void initUdpSocket()
	{
		try {
			// �˿�ΪSOCKET_PORT=8765
			mUdpRevSocket = new DatagramSocket( SOCKET_PORT );
			Log.d("", "����UdpSocket��ʼ�� " );
			
			if (mUdpRevSocket != null)
			{
				mUdpRevThread = new UdpReceiveThread();
				mUdpRevThread.start();
			}
			
		} catch (SocketException e) {	
			Log.d(TAG, "**socket��ʼ��ʧ��") ;
			e.printStackTrace();
		}
	}
	
	   
	/**
     * @ClassName: UdpReceiveThread 
     * @Description: UDP��Ϣ�Ľ����߳� 
     * @Author: Mr.Simple 
     * @E-mail: bboyfeiyu@gmail.com 
     * @Date 2012-11-9 ����1:35:31 
     *
     */
	public class UdpReceiveThread extends Thread {
		public UdpReceiveThread() {

		}

		@Override
		public void run() {
	
			while ( !mUdpStop ) {
				Log.d(TAG, "Udp�����߳��Ѿ�����");
				try {

					byte[] buf = new byte[1024]; // ָ����������Ϣ�Ĵ�С
					DatagramPacket datagramPacket = new DatagramPacket(buf, 1024);

					Log.d(TAG, "Service Udp��Ϣ�ȴ���...(����)");
					mUdpRevSocket.receive(datagramPacket);
					Log.d(TAG, "****Udp��Ϣ�ѽ������****");

					// ��packet�ж�ȡ��Ϣ,���ҹ����һ���ַ�
					String msg = new String(datagramPacket.getData(), 0,
											datagramPacket.getLength(), "GB2312");
					Log.d(TAG, "Udp��Ϣ: " + msg);
					// �㲥udp��Ϣ
					broadCastMsg( msg );

				} catch (Exception e) {

					e.printStackTrace();
				}

			}
		}
	} // end of UdpReceiveThread

	

	/**
	 * @Method: stopMyService 
	 * @Description:  �Զ���ֹͣ������  
	 * @throws
	 */
	public void stopMyService() {

		Log.d(TAG, "ֹͣ���� --->  id : " + mStartId);

		this.stopSelf(mStartId);
	}
	

	/**
	 * @Method: broadCastMsg 
	 * @Description:  �㲥��Ϣ,��UI�߳��й㲥��Ϣ
	 * @param msg   
	 * @throws
	 */
	private void broadCastMsg(String msg)
	{
		// Ĭ�ϵ���Ϣ����
		String action = "chat.SocketService.chatMessage";
		if( msg == null || "".equals( msg ) ){
			Log.d(TAG, "���ܹ㲥����Ϣ");
			return ;
		}
				
		// ����TEXI��־���ʾ���Ǹ��µ�ͼ�ϵĹ��ڳ��⳵����Ϣ
		if ( msg.contains("TEXI")){
			action = "chat.SocketService.texi";
			
		} else if ( msg.contains("CHAT_MSG") ){
			// ������Ϣ�Ĺ㲥
			Log.d("�������ӵ�CHAT_MSG", msg);
		}

		// ��ݲ�ͬ�Ķ���ʵ��Intent
		Intent intent = new Intent( action );
		intent.putExtra("broadCast", msg);
		// ���͹㲥
		sendBroadcast( intent );
	}
	
	
	/**
	 * @ClassName: LocalBinder 
	 * @Description: ���ڿͻ���Binder���࣮��Ϊ��������֪�����,service��Զ��������ͻ�����ͬ�Ľ���У��������ǲ���Ҫ����IPC
	 * @Author: Mr.Simple 
	 * @E-mail: bboyfeiyu@gmail.com 
	 * @Date 2012-11-7 ����8:19:36 
	 *
	 */
    public class LocalBinder extends Binder {  
    	
    	public SocketService getService() {  
            // ���ر�service��ʵ��ͻ��ˣ����ǿͻ��˿��Ե��ñ�service�Ĺ�������  
            return SocketService.this;  
        }  
    } 
    
}	// END OF FILE
