package chat;

import network.NetworkUtils;
import album.entry.LoginActivity;
import album.entry.MainViewActivity;
import album.entry.R;

/**
 * @ClassName: ChatActivity 
 * @Description: ������� 
 * @Author: Mr.Simple (�κ��)
 * @E-mail: bboyfeiyu@gmail.com 
 * @Date 2012-11-17 ����7:20:37 
 *
 */

public class ChatActivity extends Activity implements OnClickListener{
	
	   private Button sendBtn;											// ���Ͱ�ť
	   private String mFriendIp = null;									// ���ѵ�IP,�������Ĭ������
	   private ListView mMsgListView;									// �Ի��б�ListView
	   private DetailEntity mMsgEntity = null;							// �Զ������Ϣ����
	   private ArrayList<DetailEntity> conversationList = null;			// �Ի�ʵ���б�,����Ϣ�ķ�װ��
	   private  String mFriName;										// �����ǳ�
	   
	   final String TAG = "CHATACTIVITY";
	   private final String CHAT_ACTION = "chat.SocketService.chatMessage";	// ����㲥��Ϣ��action
	   private MyBroadcastReciver mMsgReceiver = null;					// �㲥������
	   private Vibrator mVibrator; 										// ��
	   private String mMsg = "";										// Ҫ���͵���Ϣ
	   public static int gMyIcon = 0;									// �Լ���ͷ������

	   
		/**
		 * ���� �� ҳ�洴��,���г�ʼ��
		 * (non-Javadoc)
		 * @see android.app.ActivityGroup#onCreate(android.os.Bundle)
		 */
	   @Override
	   public void onCreate(Bundle savedInstanceState) {
		   
		    super.onCreate(savedInstanceState);
		    setContentView(R.layout.bluetooth);				// ����������һ��Ĳ���.
		      
		    Intent intent = getIntent();
		    mFriName = intent.getStringExtra("friendName");		// �����ǳ�
		    mFriendIp = intent.getStringExtra("friendIp");		// ����IP
		    // ��ȡ������֪ͨ�����洫��������Ϣ
		    String msg = intent.getStringExtra("pendingMsg");
		    Log.d(TAG, "�ӵ���������Ϣ:" + msg);    
		    
		    // ���ñ���Ϊ���������
		    setTitle("��" + mFriName+ "������");
		    Log.d(TAG, "���� : "+ mFriName + " ���� IP : " + mFriendIp);
		    
			// �����뵽�������,�����øú����û�����Ϣ����mainActvity�㲥
	      	SocketService.mMsgMap.put(mFriName, "NO");	      	
	      	// ��ʼ����ݳ�Ա��,���������б?��������
		    initActivity();								
		    
		    // ����Ǵ����������뵽�ý���,�򽫸���Ϣ��ӵ����ѻỰ�б���
		    if ( msg != null && mFriName != null ){
		    	addConversationMsg(mFriName, msg, 2);
		    }
			
			// ��
			mVibrator = (Vibrator)getSystemService(VIBRATOR_SERVICE);     

	     
	   }	// end of onCreate().
	   
	   
	/**
	 * @Method: initActivity
	 * @Description: ��ʼ�����Ա�������ؼ�����������
	 */
	private void initActivity() {

		sendBtn = (Button) findViewById(R.id.button_send);// ���Ͱ�ť
		sendBtn.setOnClickListener(this);

		// �㲥������,���պ��ѷ�������Ϣ(ͨ��service�Ĺ㲥������),ʹ�ö�̬ע��
		mMsgReceiver = new MyBroadcastReciver();

		IntentFilter chatFilter = new IntentFilter(CHAT_ACTION);
		Log.d(TAG, "����������Ƿ�ע��ɹ�: " + registerReceiver(mMsgReceiver, chatFilter));

		// ��Ϣ�б��ʼ��
		conversationList = new ArrayList<DetailEntity>();
		// ��ʼ�������������Ϣ��¼�б�
		mMsgListView = (ListView) findViewById(R.id.conversationList);

		// ��ȡ��ǰ����
		final LinearLayout layout = (LinearLayout) findViewById(R.id.bluetoothLayout);
		MainViewActivity.checkSkin(layout); // ���ñ���,������Ƥ��
		MainViewActivity.addActivityToHashSet(this); // ����ǰҳ����ӵ�activity map��

	}

	/**
	 * ���� �� ��ť����¼� (non-Javadoc)
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 * 
	 */
	@Override
	public void onClick(View v) {

		// ��ȡ������е���Ϣ�ı�
		EditText msgEdit = (EditText) findViewById(R.id.edit_text_out);
		mMsg = msgEdit.getText().toString();

		if (mMsg.trim().length() == 0) {
			Toast.makeText(ChatActivity.this, "���ܷ��Ϳ���Ϣ,����������", 0).show();
			return;
		}

		msgEdit.setText(""); // ��������
		Log.d(TAG, "Ҫ���͵���Ϣ �� " + mMsg);
		addConversationMsg("��˵ : ", mMsg, 1); // ��������Ϣ��ӵ��б���
		try {
			// ����Ϣͨ��socket���ͳ�ȥ
			//sendPost( mMsg );
			sendMsgUDP() ;
		} catch (Exception e) {
			Toast.makeText(this, "����ʧ��,�������쳣~~~", 0).show();
			
		}

	}

	 
	/**
	 * @Method: sendMsgUDP
	 * @Description: ������Ϣ
	 */
	private void sendMsgUDP() {
		Log.d(TAG, "����IP : " + mFriendIp);
		new Thread() {
			@Override
			public void run() {
				try {
					String myIP = NetworkUtils.getLocalIpAddress();
					// ����Զ�̷�������IP���ַ
					InetAddress iaddr = InetAddress.getByName("199.36.75.40");
					// ��װ��Ϣpakcet
					mMsg = "CHAT_MSG" + ";;" + mFriendIp + ";;" + myIP + ";;"
							+ LoginActivity.mineName + ";;" + mMsg;
					byte data[] = mMsg.getBytes("GB2312");
					// ����һ��DatagramPacket���󣬲�ָ��Ҫ�������ݰ��͵����統�е��ĸ���ַ���Լ��˿ں�
					DatagramPacket packet = new DatagramPacket(data,
							data.length, iaddr, 9876);
					
					// ����socket�����send�������������
					SocketService.mUdpRevSocket.send( packet ) ;
					Log.d("UDP", " Packet�Ѿ����� �� " + mMsg);

				} catch (Exception e) {
					Toast.makeText(ChatActivity.this, "���ͳ�ʱ,��������...", 0)
							.show();
					e.printStackTrace();
				}
			}
		}.start();

	}
	
	
	/**
	 * ���ܣ� socket��Ϣ����,���պ�����Ϣ���Ҹ���UI�߳�
	 */
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0: // �ҵ���Ϣ
				addConversationMsg("��˵ ", (msg.obj).toString(), 1);
				break;
			case 1: // ���ѵ���Ϣ
				addConversationMsg(mFriName, (msg.obj).toString(), 2);
				break;
			default:
				break;

			} // end of switch()

		}// end of handleMessage(Message msg)
	};
		   
	   
	/**
	 * @Method: addConversationMsg
	 * @Description:
	 * @param fname
	 *            �����ǳ�
	 * @param content
	 *            �����ǳ�
	 * @param layoutId
	 *            ʹ���ĸ����� (1��ʾ�Լ�������Ϣ����ʾ����,2��ʾΪ���ѷ�������Ϣ����)
	 * @return void ��������
	 * @throws
	 */
	private void addConversationMsg(String fname, String content, int layoutId) {
		// �������ȡϵͳʱ��
		SimpleDateFormat formatter = new SimpleDateFormat("MM-dd HH:mm:ss");
		Date curDate = new Date(System.currentTimeMillis()); // ��ȡ��ǰʱ��
		String date = formatter.format(curDate);

		// �Լ�����ȥ����Ϣ
		if (layoutId == 1) {
			// �����Ϣ�Ի�ʵ�����
			mMsgEntity = new DetailEntity(fname, date, content,
					R.layout.list_say_me_item);
		} else {
			// �����Ϣ�Ի�ʵ�����, ���ѷ�������Ϣ,ʹ�ò�ͬ����ʾ��ʽ
			mMsgEntity = new DetailEntity(fname, date, content,
					R.layout.list_say_he_item);
		}

		// ������ʵ����ӵ�List<DetailEntity>��
		conversationList.add( mMsgEntity );
		// ����������
		mMsgListView.setAdapter(new DetailAdapter(ChatActivity.this,
				conversationList));

	}

	
	/**
	 * @ClassName: MyBroadcastReciver 
	 * @Description: �㲥������, ����socket����������Ϣ,���ҽ���Ϣ��ӵ������б� 
	 * @Author: Mr.Simple 
	 * @E-mail: bboyfeiyu@gmail.com 
	 * @Date 2012-11-9 ����1:37:03 
	 *
	 */
	private class MyBroadcastReciver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {

			String action = intent.getAction();
			// ������Ϣ
			if (action.equals(CHAT_ACTION)) {
				Log.d(TAG, "����������յ��㲥") ;
				// ��ù㲥����Ϣ,�����յ�����Ϣ
				String msg = intent.getStringExtra("broadCast");
				String fName = ""; // �����ǳ�
				String newMsg = ""; // ����Ϣ
				
				if ( msg.contains(";;") ){
					Log.d(TAG, "�����б�ӵ������Ϊ" + msg) ;
					try{
						// ��ȡ���
						String[] cont = msg.split(";;");
						mFriendIp = cont[1];
						fName = cont[2];
						newMsg = cont[3];
						
						long[] pattern = { 200, 500, 100, 200 }; // ���ָ����ģʽ������
						mVibrator.vibrate(pattern, 2); 			// -1���ظ�����-1Ϊ��pattern��ָ���±꿪ʼ�ظ�
						mHandler.postDelayed(vibRunnable, 1100);
						addConversationMsg(fName, newMsg, 2); // ����Ϣ��ӵ������б�
					}catch(Exception e){
						Log.d(TAG, "**��Ϣ�������**") ;
						e.printStackTrace() ;
					}
				}
			} // end of if
		} // enf of onReceive

	} // end of MyBroadcastReciver
	  
	
	/**
	 * ���� �� ȡ����Ϣ��
	 */
	Runnable vibRunnable = new Runnable() {
			
		@Override
		public void run() {
			mVibrator.cancel();
		}
	};
		
	
	/**
	 * ���� �� ҳ��ֹͣ�ص�����
	 */
	@Override
	protected void onStop() {
		
		SocketService.mMsgMap.clear();	
		// ����ҳ���Set���Ƴ�
		MainViewActivity.removeFromSet( this );
		super.onStop();
	}


	/**
	 * (�� Javadoc,��д�ķ���) 
	 * @Title: onDestroy
	 * @Description:  
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		if (mMsgReceiver != null){
			unregisterReceiver(mMsgReceiver);
		}
		super.onDestroy();
	}

}	// end of class
