package album.entry;

import help_dlg.HelpDialog;
import imageEdit.PictureEditActivity;

import map.BaiduMapActivity;
import network.HttpThread;
import network.NetworkUtils;

import albums.PhotoAlbumActivity;
import bluetooth.BluetoothChat;
import camera.CameraActivity;
import chat.ChatActivity;
import chat.FriendsListActivity;
import chat.SocketService;
import chat.SocketService.LocalBinder;

/**
 * 
 * @ClassName: MainViewActivity 
 * @Description:  ����������,������ģ������
 * @Author: Mr.Simple (�κ��)
 * @E-mail: bboyfeiyu@gmail.com 
 * @Date 2012-11-17 ����5:15:12 
 *
 */
public class MainViewActivity extends Activity implements OnClickListener,
		OnTouchListener, android.view.GestureDetector.OnGestureListener {

	// ʹ��HashSet���洢�û���Activity,Set�����?���ظ�Ԫ��
	public static Set<Activity> gAtcSet = new HashSet<Activity>();

	private ImageButton cameraBtn = null; 			// ��������ģ�鰴ť
	private ImageButton ablumsBtn = null; 			// �������ģ�鰴ť
	private ImageButton chatBtn = null; 			// ��������ģ�鰴ť
	private ImageButton mapBtn = null; 				// �����ͼģ�鰴ť
	private ImageButton psBtn = null; 				// ����༭ģ�鰴ť
	private ImageButton blueToothBtn = null; 		// ��������ģ�鰴ť
	private GestureDetector mGestureDetector = null;// ���ƴ���̽����

	private PopupWindow popupWindow; 		// ���˵�����
	private GridView menuGrid;				// ���ֲ˵��������ͼ
	public static int mThemeMode = 0; 		// ����ģʽ
	private final String SETTING = "usr_info"; // ������Ϣ�����ص�share
	private final String TAG = "MAINACTIVITY";

	private final String CHAT_ACTION = "chat.SocketService.chatMessage";// ������Ϣ�㲥����action
	private final String ONLINE_ACTION = "chat.SocketService.onlie"; 	// ������Ϣ�㲥����action
	public static SocketService mSocketService = null; 					// socket����
	private static MyBroadcastReciver mDataReceiver = null; 			// �㲥������

	private static Intent sIntent = null; 		// ���������intent
	private MediaPlayer mMediaPlayer = null; 	// ��ý�����
	private static int notifyTag = 0; 			// ���ͱ�ʶ
	private Vibrator mVibrator; 				// ��
	private String fName = ""; 					// ���ѵ�����
	public static boolean isNotify = true; 		// �Ƿ�������Ϣ�ı�ʶ
	private MyHandlerAlbum mHandlerAlb = null; 	// ������������������б�

	public static List<String> sFriendList = new ArrayList<String>();	// �����б�
	public static List<String> sAlbumList = new ArrayList<String>();	// ����б�

	// �˵�����
	private String[] menu_name_array = { "����", "ע��", "����", "����", "�˳�" };
	// ͼ������
	int[] menu_image_array = { android.R.drawable.ic_menu_set_as,
			android.R.drawable.ic_menu_agenda, android.R.drawable.ic_menu_help,
			android.R.drawable.ic_menu_info_details,
			android.R.drawable.ic_lock_power_off };
	

	/**
	 * (�� Javadoc,��д�ķ���) 
	 * @Title: onCreate
	 * @Description:  ҳ�洴��
	 * @param savedInstanceState 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		//�����ޱ���  
        requestWindowFeature(Window.FEATURE_NO_TITLE);  
		setContentView(R.layout.mainviewactivity);

		// ��ʼ����ť�����
		initComponents();
		// ��ȡ�������ߵĺ����б�,���ھ�̬������
		getAllUsersInfo();
		// ��ȡ�Լ�����������б�
		getNetAlbumsList();

		// ����ǰҳ����ӵ�map��
		MainViewActivity.addActivityToHashSet(this);
		// �ӱ��ض�ȡƤ��ģʽ
		readSkinModeFromLocal();
		// ���ʱ�������л�����
		autoChangeSkin();

		try{

			// �㲥������,���պ��ѷ�������Ϣ(ͨ��service�Ĺ㲥�����ݵ�)
			mDataReceiver = new MyBroadcastReciver();
			IntentFilter intentFilter = new IntentFilter(CHAT_ACTION);
			Log.d(TAG, "ע���� �� " + registerReceiver(mDataReceiver, intentFilter));
			
			// ����socket�������
			sIntent = new Intent(this, SocketService.class);
			sIntent.putExtra("key", "Service Start");
			startService(sIntent);
		}catch(Exception e){
			e.printStackTrace() ;
		}

		// �����������һ��socket��Ϣ,ʹ�÷��������пͻ��˵�·����Ϣ
		sendRouteMsg() ;
	}


	/**
	 * @Method: initComponents ����
	 * @Description: ��ʼ�����
	 */
	private void initComponents() {

		cameraBtn = (ImageButton) findViewById(R.id.cameraBtn);
		cameraBtn.setOnClickListener(this);

		ablumsBtn = (ImageButton) findViewById(R.id.ablumsBtn);
		ablumsBtn.setOnClickListener(this);

		chatBtn = (ImageButton) findViewById(R.id.chatBtn);
		chatBtn.setOnClickListener(this);

		mapBtn = (ImageButton) findViewById(R.id.mapBtn);
		mapBtn.setOnClickListener(this);

		psBtn = (ImageButton) findViewById(R.id.psBtn);
		psBtn.setOnClickListener(this);

		blueToothBtn = (ImageButton) findViewById(R.id.blueToothBtn);
		blueToothBtn.setOnClickListener(this);

		// ���ü�����mGestureDetector
		LinearLayout mainLayout = (LinearLayout) findViewById(R.id.parent);
		mGestureDetector = new GestureDetector(this); // ����ʶ��
		mainLayout.setOnTouchListener(this); // ���ô���������
		mainLayout.setLongClickable(true); // ���ÿɳ���

	}


	/**
	 * @Method: autoChangeSkin ����
	 * @Description: ���ʱ���л����ʵ�����ģʽ
	 */
	private void autoChangeSkin() {

		Calendar cal = Calendar.getInstance();
		int hour = cal.get(Calendar.HOUR_OF_DAY); // ��ȡʱ���е�Сʱ

		// ��ȡ�������,mainActivity����
		final LinearLayout layout = (LinearLayout) findViewById(R.id.parent);

		// �����ߵ㵽�糿�ߵ�֮�䣬���л���ҹ����������
		if ((hour > 18 || hour < 7) && (mThemeMode != 0 || mThemeMode != 2)) {
			mThemeMode = 0;
			layout.setBackgroundResource(R.drawable.bg_black);
			Toast.makeText(MainViewActivity.this,
					"������ҹ��" + hour + "��,���л���ҹ��ģʽ", Toast.LENGTH_SHORT).show();
			setTheme(android.R.style.Theme_Black);
		}
		// �ռ���������
		if ((hour > 6 && hour < 18) && (mThemeMode != 1 || mThemeMode != 3)) {
			mThemeMode = 1;
			layout.setBackgroundResource(R.drawable.bg_light_02);
			Toast.makeText(MainViewActivity.this,
					"�����ǰ���" + hour + "��,���л����ռ�ģʽ", Toast.LENGTH_SHORT).show();
		}

		Log.d("mThemeMode ", "" + mThemeMode);
		// �����ı�������ɫ
		changeTextColor(mThemeMode);
		// ��Ƥ��ģʽ���浽����
		saveSkinModeToLocal();
	}

	
	/**
	 * (�� Javadoc,��д�ķ���) 
	 * @Title: onClick
	 * @Description:  ����¼�,��������ģ��İ�ť 
	 * @param v 
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v) {

		// ���붯��,�ڵ����ť��ʱ���ж���
		Animation animation = AnimationUtils.loadAnimation(this,
				R.anim.scale_anim);
		// ʵ�����
		Intent intent = new Intent();
		int code = 0;

		if (v == cameraBtn) // ǰ�����յ�ҳ��
		{
			cameraBtn.startAnimation(animation);
			// ����Ҫ��ת����Ŀ�����
			intent.setClass(MainViewActivity.this, CameraActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			code = 1;
			// ��תҳ��
			startActivityForResult(intent, code);
			// ҳ���л��Ķ���Ч��
			overridePendingTransition(android.R.anim.fade_out, android.R.anim.fade_in);
			return ;
		}

		if (v == ablumsBtn) { // ǰ�����ҳ��
			// ���ð�ť����
			ablumsBtn.startAnimation(animation);
			// ����Ҫ��ת����Ŀ�����
			intent.setClass(MainViewActivity.this, PhotoAlbumActivity.class);
			intent.putExtra("id", LoginActivity.mineID);
			code = 2;

		}
		if (v == psBtn) // ǰ��༭����
		{
			psBtn.startAnimation(animation);
			// ����Ҫ��ת����Ŀ�����
			intent.setClass(MainViewActivity.this, PictureEditActivity.class);
			intent.putExtra("photopath", "");
			code = 3;

		}
		if (v == blueToothBtn) // �����������
		{
			blueToothBtn.startAnimation(animation);
			// ����Ҫ��ת����Ŀ�����
			intent.setClass(MainViewActivity.this, BluetoothChat.class);
			code = 4;
		}

		if (v == chatBtn) // �������
		{
			chatBtn.startAnimation(animation);
			// ����Ҫ��ת����Ŀ�����
			intent.setClass(MainViewActivity.this, FriendsListActivity.class);
			code = 5;

		}
		if (v == mapBtn) { // �����ͼ����

			mapBtn.startAnimation(animation);
			// ����Ҫ��ת����Ŀ�����
			intent.setClass(MainViewActivity.this, BaiduMapActivity.class);
			code = 6;
		}

		// ��תҳ��
		startActivityForResult(intent, code);
		// ҳ���л��Ķ���Ч��
		overridePendingTransition(R.anim.slide_left, R.anim.slide_right);

	}
	
	/**
	 * 
	 * @Method: sendMsgUDP
	 * @Description: �������������Ϣ,ʹ�÷�����·���а����ÿͻ��˵Ĺ������IP
	 * @return void ��������
	 * @throws
	 */
	private void sendRouteMsg() {
		new Thread() {
			@Override
			public void run() {
				Looper.prepare();
				try {
					String serverUrl = "199.36.75.40";
					InetAddress iaddr = InetAddress.getByName(serverUrl);
					// ��װ��Ϣpakcet,���ҵ�IP��ַ����������洢
					String mMsg = "$" + NetworkUtils.getLocalIpAddress() + "$";
					byte data[] = mMsg.getBytes("GB2312");
					// ����һ��DatagramPacket���󣬲�ָ��Ҫ�������ݰ��͵����統�е��ĸ���ַ���Լ��˿ں�
					DatagramPacket packet = new DatagramPacket(data,
							data.length, iaddr, 9876);

					SocketService.mUdpRevSocket.send(packet);
					Log.d("UDP", " Packet�Ѿ����� �� " + mMsg);

				} catch (Exception e) {
					Toast.makeText(MainViewActivity.this, "���ͳ�ʱ,��������...", 0)
							.show();
					e.printStackTrace();
				}
				Looper.loop();
			}

		}.start();

	}
		

	/**
	 * @Method: getAllUsersInfo ����
	 * @Description: ���������������,��ȡ���к��ѵ��б�
	 */
	private void getAllUsersInfo() {
		new Thread(new Runnable() {

			@Override
			public void run() {

				ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
				nameValuePairs.add(new BasicNameValuePair("protocol", "getIP"));// ��װ��ֵ��
				HttpThread h = new HttpThread(nameValuePairs, 7); // ��ȡ7 �����б�
				String msg = (String) h.sendInfo(); // ���շ������ķ���ֵ

				Looper mainLooper = Looper.getMainLooper(); // �õ����߳�loop
				mFHandler = new MyHandler(mainLooper); // �������̵߳�handler
				mFHandler.removeMessages(0); // �Ƴ����ж����е���Ϣ
				Message m = mFHandler.obtainMessage(1, 1, 1, msg); // ����Ϣ����message
				mFHandler.sendMessage(m);
				Log.d("Main", "Main����������б�");
			}
		}).start();
	}


	/**
	 *  ���շ��������ص����ߺ������,ʹ��mFHandler������Ϣ��UI�߳�
	 */
	private Handler mFHandler = new Handler();

	/**
	 * 
	 * @ClassName: MyHandler 
	 * @Description: ����������������ĺ����б����,�������ӵ������б���
	 * @Author: Mr.Simple (�κ��)
	 * @E-mail: bboyfeiyu@gmail.com 
	 * @Date 2012-11-17 ����5:19:06 
	 *
	 */
	private class MyHandler extends Handler {

		public MyHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {

			sFriendList.clear();
			String fList = msg.obj.toString().trim(); // ���յ����̵߳��ַ�
			if (fList.contains("error")) {
				Toast.makeText(MainViewActivity.this, "�����б����ʧ��,��鿴����...", 0)
						.show();
				// ��ȡ���ߺ����б�ʧ��,������ύ����
				getAllUsersInfo() ;
				return;
			}
			// ���û�������
			String arr[] = null;
			try {
				// ��������Ԫ��,��;;��Ϊ�ָ���
				arr = fList.split(";;");
				for (String item : arr) {
					// ����ݵ��뵽List��
					sFriendList.add(item);
					Log.d("��ȡ�����б���� : ", item);
				}

			} catch (Exception e) {
				Toast.makeText(MainViewActivity.this, "��Ǹ,�����б��ȡʧ��~~~", 0).show();
			}

		}
	}


	/**
	 * @Method: getNetAlbumsList ����
	 * @Description:  �������������������б�
	 */
	private void getNetAlbumsList() {
		Thread albThread = new Thread( rNetAlbum );
		albThread.start();
	}


	/**
	 * ������������б��runnable
	 */
	Runnable rNetAlbum = new Runnable() {
		String msg;

		@Override
		public void run() {
			ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("protocol", "getAlbums"));
			nameValuePairs.add(new BasicNameValuePair("id",
					LoginActivity.mineID));
			HttpThread h = new HttpThread(nameValuePairs, 11); // 11--��������б�
			msg = h.sendInfo().toString(); // ���շ������ķ���ֵ
			Log.d("������������б�", msg);
			sendMessage();
		}

		public void sendMessage() { // �̼߳���ݴ���
			// �õ����߳�loop
			Looper mainLooper = Looper.getMainLooper();
			// �������̵߳�handler
			mHandlerAlb = new MyHandlerAlbum(mainLooper);
			// �Ƴ����ж����е���Ϣ
			mHandlerAlb.removeMessages(0);
			// ����Ϣ����message
			Message m = mHandlerAlb.obtainMessage(1, 1, 1, msg);
			// ����message
			mHandlerAlb.sendMessage(m);
		}
	};

	/*
	 * ��ȡ��������б��Ĵ���
	 */
	private class MyHandlerAlbum extends Handler {
		public MyHandlerAlbum(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			// ��ȡ���
			String AlbumList = msg.obj.toString().trim();
			// ���建������
			String[] buf = null;
			try {
				buf = AlbumList.split(";;");
				// ��һ��Ϊ���󷵻صĳɹ�����ʧ�ܵı�־
				if ( buf[0].equals("error") ){
					Log.d("��������б��������", "����ERROR");
					// ��������
					getNetAlbumsList() ; 
				}
				
				// ����������б���뵽��̬����sAlbumList��
				for (String item : buf) {
					if ( !item.equals("error") || !item.equals("fail")){
						sAlbumList.add(item);
					}
					Log.d("MAIN�ӵ�����б�-->", item);
				}

			} catch (Exception e) {
				e.printStackTrace();
				// �ٴ�������������б�
				getNetAlbumsList() ; 
			}

		}
	}

	
	long exitTime = 0;
	/**
	 * (�� Javadoc,��д�ķ���) 
	 * @Title: onKeyDown
	 * @Description: �����¼�,��2���������ؼ�ʵ���˳�����Ĺ���
	 * @param keyCode
	 * @param event
	 * @return 
	 * @see android.app.Activity#onKeyDown(int, android.view.KeyEvent)
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
			case KeyEvent.KEYCODE_MENU: 	// �˵����µĲ���,�����˵�����
				openPopupwin();
				break;
	
			case KeyEvent.KEYCODE_BACK: {	// �˳�
				if ((System.currentTimeMillis() - exitTime) > 2000
						&& event.getAction() == KeyEvent.ACTION_DOWN) {
	
		            Toast.makeText(getApplicationContext(), "�ٰ�һ�ξ��˳�����...", 
							Toast.LENGTH_SHORT).show();  
					exitTime = System.currentTimeMillis();
	
				} else {
					// �˳�����
					killCurrentApp(this);
				}
				// ������Ҫ��!��Ȼ�����Ĭ�ϵĻ��˵���һ����
				return true;
			}
			default:
				break;
		}
		return super.onKeyDown(keyCode, event);
	}
	

	/**
	 * (�� Javadoc,��д�ķ���) 
	 * @Title: onResume
	 * @Description:  
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		
		super.onResume();
	}


	/**
	 * (�� Javadoc,��д�ķ���) 
	 * @Title: onRestart
	 * @Description:  ҳ�����»�ȡ����   
	 * @see android.app.Activity#onRestart()
	 */
	@Override
	protected void onRestart() {
		
		// �����һ��û�л�ȡ�����б������б�,����������ʱҪ��ȡ�б�
		if (sFriendList.size() == 0) {
			Log.d("�����б�", "���»�ȡ");
			getAllUsersInfo();
		}
		if (sAlbumList.size() == 0) {
			Log.d("����б�", "���»�ȡ");
			getNetAlbumsList();
		}
		super.onRestart();
	}

	
	/**
	 * (�� Javadoc,��д�ķ���) 
	 * @Title: onStop
	 * @Description:  
	 * @see android.app.Activity#onStop()
	 */
	@Override
	protected void onStop() {
		
		mHandler.removeCallbacks(vibRunnable); // ��runnable�Ƴ�
		if ( mFHandler != null ){
			mFHandler = null;
		} 
		
		try{
			bindService(sIntent, mConnection, Context.BIND_AUTO_CREATE); // �󶨷���,ʹ������activity����
		}catch(Exception e){
			e.printStackTrace() ;
		}
		super.onStop();
	}

	/******************************************************************************************
	 * ���� �� ���ҳ�� (non-Javadoc)
	 * 
	 * @see android.app.Activity#onRestart()
	 ******************************************************************************************/
	/**
	 * (�� Javadoc,��д�ķ���) 
	 * @Title: onDestroy
	 * @Description:  ���ҳ��,ֹͣ���� 
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	protected void onDestroy() {

		Log.d("", "OnDestory�����");
		stopService( sIntent ); // ֹͣ����
		
		unregisterReceiver( mDataReceiver );
		super.onDestroy();
	}


	/**
	 * @Method: getMenuAdapter ����
	 * @Description:  popupwindow's adapter.�˵����ڵ�������
	 * @param menuNameArray
	 * @param menuImageArray
	 * @return
	 */
	private ListAdapter getMenuAdapter(String[] menuNameArray,
			int[] menuImageArray) {

		// ���Դ�б�
		ArrayList<HashMap<String, Object>> dataSrc = new ArrayList<HashMap<String, Object>>();

		for (int i = 0; i < menuNameArray.length; i++) {
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("itemImage", menuImageArray[i]);
			map.put("itemText", menuNameArray[i]);
			dataSrc.add(map);
		}

		// ����������,ָ�����Դ����ʾģ��item_menu
		SimpleAdapter simperAdapter = new SimpleAdapter(this, dataSrc,
				R.layout.item_menu, new String[] { "itemImage", "itemText" },
				new int[] { R.id.item_image, R.id.item_text });
		return simperAdapter;

	}

	
	/**
	 * @Method: openPopupwin ����
	 * @Description: �˵�����---popupwindow
	 */
	private void openPopupwin() {

		LayoutInflater mLayoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		ViewGroup menuView = (ViewGroup) mLayoutInflater.inflate(
				R.layout.gridview_pop, null, true);

		menuGrid = (GridView) menuView.findViewById(R.id.gridview_popup);
		menuGrid.setAdapter(getMenuAdapter(menu_name_array, menu_image_array));
		menuGrid.requestFocus();

		// ����¼�
		menuGrid.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {

				Intent intent = new Intent(); // Intent ����
				switch (arg2) {

				case 0: // ����
					Toast.makeText(MainViewActivity.this, menu_name_array[0], 0)
							.show();
					intent.setClass(MainViewActivity.this,
							SettingActivity.class);
					startActivity(intent);
					overridePendingTransition(R.anim.slide_left,
							R.anim.slide_right);
					break;

				case 1: // ע��
					Toast.makeText(MainViewActivity.this, menu_name_array[1], 0)
							.show();
					userOffLine(); // �û������������������Ϣ
					LoginActivity.gNoAuto = 1;
					intent.setClass(MainViewActivity.this, LoginActivity.class);
					startActivity(intent);
					overridePendingTransition(R.anim.slide_left,
							R.anim.slide_right);
					break;

				case 2: // ����
					popupWindow.dismiss();
					openSkinDialog();
					break;

				case 3: // ����
					Toast.makeText(MainViewActivity.this, menu_name_array[3], 0)
							.show();
					HelpDialog helpDlg = new HelpDialog(MainViewActivity.this,
							R.string.main_help_text);
					helpDlg.showHelp();
					break;

				case 4: // �˳�
					popupWindow.dismiss();
					killCurrentApp(MainViewActivity.this);
					break;

				default:
					break;
				}

			}
		});
		popupWindow = new PopupWindow(menuView, LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT, true);

		popupWindow.setBackgroundDrawable(new BitmapDrawable());
		popupWindow.setAnimationStyle(R.style.PopupAnimation);
		// ���ø����ں���ʾ��λ��
		popupWindow.showAtLocation(findViewById(R.id.parent), Gravity.CENTER
				| Gravity.CENTER, 0, 0);
		popupWindow.update();

	}

	
	/**
	 * @Method: openSkinDialog ����
	 * @Description:  ����滻Ƥ��
	 */
	private void openSkinDialog() {

		// ��ȡ�������,mainActivity����
		final LinearLayout layout = (LinearLayout) findViewById(R.id.parent);

		View menuView = View.inflate(this, R.layout.gridview_dlg, null);
		String[] menuitems = { "ҹ��ģʽ", "�ռ�ģʽ", "��ɫ����", "ǳɫ����" }; // �����Ĳ˵�
		int[] imgs = { R.drawable.bg_black_s, R.drawable.bg_light_5_s,
				R.drawable.bg_red_s, R.drawable.bg_light_02_s };

		// ����AlertDialog
		final AlertDialog frameDialog = new AlertDialog.Builder(this).create();
		frameDialog.setView(menuView);

		menuGrid = (GridView) menuView.findViewById(R.id.gridview_dlg);
		menuGrid.setAdapter(getMenuAdapter(menuitems, imgs)); // ���ò˵���ͼ��
		menuGrid.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {

				switch (arg2) {

				case 0:
					frameDialog.dismiss();
					mThemeMode = 0;
					layout.setBackgroundResource(R.drawable.bg_black);
					break;

				case 1:
					frameDialog.dismiss();
					mThemeMode = 1;
					layout.setBackgroundResource(R.drawable.bg_light_5);
					break;

				case 2:
					frameDialog.dismiss();
					mThemeMode = 2;
					layout.setBackgroundResource(R.drawable.bg_red_2);
					break;

				case 3:
					frameDialog.dismiss();
					mThemeMode = 3;
					layout.setBackgroundResource(R.drawable.bg_light_02);
					break;

				default:
					break;
				} // end of switch

				// �ı��ı�����ɫ
				changeTextColor(mThemeMode);
				// ����Ƥ��ģʽ������
				saveSkinModeToLocal();
			} // end of click.
		});

		frameDialog.show();
	}


	/**
	 * @Method: changeTextColor ����
	 * @Description: �ı���������ı��ؼ�������ɫ
	 * @param mode   �ռ����ҹ��ģʽ
	 */
	private void changeTextColor(int mode) {
		// ��ɫ
		int color = 0;
		// ���������ı��ؼ��Ļ�ȡ
		TextView cameraTv = (TextView) findViewById(R.id.cameraTextView);
		TextView albumTv = (TextView) findViewById(R.id.albumTextView);
		TextView chatTv = (TextView) findViewById(R.id.chatTextView);
		TextView mapTv = (TextView) findViewById(R.id.mapTextView);
		TextView picEditTv = (TextView) findViewById(R.id.picTextView);
		TextView bluetoothTv = (TextView) findViewById(R.id.btTextView);

		// ����ģʽ���ֱ���Ϊ��ɫ
		if (mode % 2 != 0) {
			color = Color.BLACK;
		} else {
			// ҹ��ģʽ��Ϊ��ɫ��
			color = Color.rgb(150, 150, 150);
		}

		// �����ı�������ɫ
		cameraTv.setTextColor(color);
		albumTv.setTextColor(color);
		chatTv.setTextColor(color);
		mapTv.setTextColor(color);
		picEditTv.setTextColor(color);
		bluetoothTv.setTextColor(color);
	}


	/**
	 * @Method: saveSkinModeToLocal 
	 * @Description: ���û���Ƥ���ŵ�����
	 */
	private void saveSkinModeToLocal() {
		// �����û�����
		SharedPreferences ref = getSharedPreferences(SETTING, 0);
		ref.edit().putInt("skinMode", mThemeMode).commit();
		Log.d("", "����Ƥ��ģʽ�� " + mThemeMode);

	}


	/**
	 * @Method: readSkinModeFromLocal
	 * @Description: �ӱ����ļ��ж�ȡ�û������Ƥ����
	 */
	private void readSkinModeFromLocal() {
		SharedPreferences settings = getSharedPreferences(SETTING, 0); // ��ȡһ������

		mThemeMode = settings.getInt("skinMode", 0); // ȡ�������NAME
		Log.d("", "����Ƥ��ģʽ�� " + mThemeMode);

		final LinearLayout layout = (LinearLayout) findViewById(R.id.parent);
		checkSkin(layout);
	}

	
	/**
	 * 
	 * @ClassName: MyBroadcastReciver 
	 * @Description:  �V��������,���պ��Ѱl�́��������Ϣ���Ͼ���Ϣ
	 * @Author: Mr.Simple 
	 * @E-mail: bboyfeiyu@gmail.com 
	 * @Date 2012-11-7 ����6:13:55 
	 *
	 */
	private class MyBroadcastReciver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// ��ȡaction
			String action = intent.getAction();
			// ��ȡ��Ϣ
			String msg = intent.getStringExtra("broadCast");
			try {
				// ����㲥��������Ϣ
				postMessage(action, msg);
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

	} // ���ڽ���socket������Ϣ�㲥�Ľ�����

	
	/**
	 * @Method: postMessage
	 * @Description: �����������������Ϣ,��Service�㲥��������Ϣ
	 * @param action
	 * @param msg ����
	 * @return void ��������
	 * @throws
	 */
	private void postMessage(String action, String msg) throws Exception {
		if ("".equals(msg)) {
			Log.d("main����", "����Ϣ");
			return;
		}

		// �㲥����������Ϣ����
		if (action.equals(CHAT_ACTION)) {

			String buf[] = msg.split(";;") ;
			fName = buf[2];
			if ( SocketService.mMsgMap.size() > 0 
					&& SocketService.mMsgMap.get( fName ) == "NO"){
				
				Log.d("main�Ĺ㲥������", fName +"����Ϣ������");
			}else { 	// ���յ���Ϣ,��������Ϣ
				showNotification( msg );
			}
		} else if (action.equals(ONLINE_ACTION)) { // ������Ϣ,�û����߸��º����б�
			// �������ߵĹ㲥����
			// ��ȡ��Ϣ,����ݴ浽��������б���
			String buf[] = null;
			// ����ַ�,�����Ч���
			buf = msg.split(";;");
			// �����׷�ӵ��б���
			for (String item : buf) {
				MainViewActivity.sFriendList.add(item);
			}
		}
	}


	NotificationManager mNM;
	/**
	 * @Method: showNotification 
	 * @Description: ������Ϣ,���ѷ������Ķ���Ϣ
	 * @param msg   
	 * @throws
	 */
	private void showNotification(String msg) {

		mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

		mVibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE); // ��

		/****************************************************************
		 * �ĸ�������Ϊ�� 100 �����ӳٺ��� 300 ���룬��ͣ 200 ��������� 800 ����
		 **************************************************************** */
		long[] pattern = { 100, 500, 100, 200 }; // ���ָ����ģʽ������
		mVibrator.vibrate(pattern, 2); 			// -1���ظ�����-1Ϊ��pattern��ָ���±꿪ʼ�ظ�
		mHandler.postDelayed(vibRunnable, 1500);

		String friendIp = ""; // �Է���IP
		String friendName = "";
		String msgCont = ""; // ��ȡ��ʵ����Ϣ����,��ȥ����Ϣͷ��
		try {
			if (msg.contains(";;")) {
				// �ָ����
				String buf[] = msg.split(";;") ;
				// ����IP
				friendIp = buf[1] ;
				// �����ǳ�
				friendName = buf[2];
				// ��Ϣ����
				msgCont = buf[3] ;
				Log.d(TAG, "�ӵ�������Ϣ: " + msgCont);
			}
		} catch (Exception e) {
			Log.d("main��������", e.toString());
			mVibrator.cancel();
		}

		CharSequence tips = "���� " + friendName + " ������Ϣ";
		// ����ͼ�ꡢ���⡢ʱ����
		Notification notification = new Notification(R.drawable.ichat, tips,
				System.currentTimeMillis());
		notification.defaults |= Notification.DEFAULT_SOUND;
		notification.defaults |= Notification.DEFAULT_LIGHTS;
		notification.flags = Notification.FLAG_AUTO_CANCEL;

		// ����intent,������ChatActivity�������, ��֤��������²��Զ����Ӻ���
		Intent intent = new Intent(this, ChatActivity.class);
		intent.putExtra("friendName", friendName);
		intent.putExtra("pendingMsg", msgCont);
		intent.putExtra("friendIp", friendIp);

		// ����û�������͵�֪ͨ,����ת���������ChatActivity
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				intent, 0);
		// ������Ϣ����
		notification.setLatestEventInfo(this, "���� " + friendName + "������Ϣ", msgCont,
				contentIntent);

		// ����Ϣ���ͳ�ȥ
		mNM.notify("Notify" + notifyTag, notifyTag++, notification);
	}


	/**
	 *  ȡ����Ϣ��.
	 */
	private Handler mHandler = new Handler();
	Runnable vibRunnable = new Runnable() {

		@Override
		public void run() {
			mVibrator.cancel();
			if (mMediaPlayer != null) {
				mMediaPlayer.pause();
				mMediaPlayer.release();
				mMediaPlayer = null;
			}
		}
	};


	/**
	 * @Method: checkSkin
	 * @Description: �޸�Ƥ��,�����ҹ��,���Զ��л�ҹ��Ƥ��ģʽ
	 * @param layout
	 */
	public static void checkSkin(ViewGroup layout) {
		Log.d("�������", "�������");
		if (MainViewActivity.mThemeMode == 1) {
			layout.setBackgroundResource(R.drawable.bg_light_02);
		}

		if (MainViewActivity.mThemeMode == 2) {
			layout.setBackgroundResource(R.drawable.bg_red_2);
		}

		if (MainViewActivity.mThemeMode == 3) {
			layout.setBackgroundResource(R.drawable.bg_light_5);
		}

	}


	/**
	 * @Method: userOffLine
	 * @Description: �û����ߵ���Ϣ
	 */
	public static void userOffLine() {
		new Thread() {
			@Override
			public void run() {
				ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(); // ������
				nameValuePairs
						.add(new BasicNameValuePair("protocol", "offLine"));
				nameValuePairs.add(new BasicNameValuePair("id",
						LoginActivity.mineID));
				HttpThread h = new HttpThread(nameValuePairs, 10); // 10--�˳�
				h.sendInfo();
				Log.d("1", "�˳�");
			};
			// ���շ������ķ���ֵ
		}.start();
	}

	
	/**
	 * ����service�󶨵Ļص�������bindService()��
	 */
	private ServiceConnection mConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {

			// �����Ѿ��󶨵���LocalService����IBinder����ǿ������ת�����һ�ȡLocalServiceʵ��
			LocalBinder binder = (LocalBinder) service;
			mSocketService = binder.getService();
			Log.d("", "��ʼ��-------> mConnection");
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {

		}
	};


	/**
	 * @Method: addActivityToHashSet
	 * @Description: ��ҳ����ӵ�set��,������ȫ�˳�����
	 * @param atc    ����ӽ�����Activity
	 */
	public static void addActivityToHashSet(Activity atc) {

		if (gAtcSet.contains(atc)) {
			Log.d("SET", "�����Ѿ�����Set��");
		} else {
			Log.d("SET", "��������Ӵ���Set��");
			gAtcSet.add(atc);
		}

	}

	
	/**
	 * @Method: removeFromSet
	 * @Description:  ��Activity�Ƴ�
	 * @param atc
	 */
	public static void removeFromSet(Activity atc) {
		gAtcSet.remove(atc);
		Log.d("�����Ƴ�", "MainActivity");
	}


	/**
	 * @Method: releaseService
	 * @Description: �ͷŷ����е���Դ
	 */
	private static void releaseService() {
		SocketService.mUdpStop = true; // ֹͣ�����н�����Ϣ�Ŀ��Ʊ���
		if (SocketService.mUdpRevThread != null) {
			SocketService.mUdpRevThread.interrupt();
			SocketService.mUdpRevThread = null;
		}

	}

	
	/**
	 * @Method: killCurrentApp
	 * @Description:  �˳��������
	 * @param context
	 */
	public static void killCurrentApp(Context context) {
		// �û����ߵ���Ϣ
		userOffLine();

		try {
			Thread.sleep(600); // ˯�ߣ��ȴ��˳�
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		// �ͷŷ����еı���
		releaseService();

		// �����������
		for (Activity atc : gAtcSet) {
			atc.finish();
		}
		android.os.Process.killProcess(android.os.Process.myPid());

	} // end of killCurrentApp(Context);

	
	/**
	 * (�� Javadoc,��д�ķ���) 
	 * @Title: onDown
	 * @Description:   �������,�����л�ҳ��
	 * @param e
	 * @return 
	 * @see android.view.GestureDetector.OnGestureListener#onDown(android.view.MotionEvent)
	 */
	@Override
	public boolean onDown(MotionEvent e) {
		return false;
	}


	private int verticalMinDistance = 150;
	private int minVelocity = 0;
	/**
	 * (�� Javadoc,��д�ķ���) 
	 * @Title: onFling
	 * @Description:  ���ƻ���,�л���Ļ ������ ��������൱��һ����������Ȼ�п������������ߣ���e1Ϊ��������㣬e2Ϊ�������յ㣬
	 * 				  velocityXΪ����ˮƽ������ٶȣ�velocityYΪ������ֱ������ٶ�
	 * @param e1
	 * @param e2
	 * @param velocityX
	 * @param velocityY
	 * @return 
	 * @see android.view.GestureDetector.OnGestureListener#onFling(android.view.MotionEvent, android.view.MotionEvent, float, float)
	 */
	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		if (e1.getX() - e2.getX() > verticalMinDistance
				&& Math.abs(velocityX) > minVelocity) {

			// ���һ���������,�л�������Activity
			Intent cIntent = new Intent();
			cIntent.setClass(MainViewActivity.this, PhotoAlbumActivity.class);
			cIntent.putExtra("id", LoginActivity.mineID);
			startActivityForResult(cIntent, 2);
			overridePendingTransition(R.anim.slide_left, R.anim.slide_right);

		} else if (e2.getX() - e1.getX() > verticalMinDistance
				&& Math.abs(velocityX) > minVelocity) {
			// ���󻮶�������
			Intent bIntent = new Intent();
			bIntent.setClass(MainViewActivity.this, CameraActivity.class);
			bIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivityForResult(bIntent, 1);
			overridePendingTransition(android.R.anim.fade_out, android.R.anim.fade_in);

		}

		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {

	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {

	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		return false;
	}

	@Override
	public boolean onTouch(View arg0, MotionEvent event) {
		return mGestureDetector.onTouchEvent(event);
	}

} // end of class

