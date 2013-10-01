package album.entry;

import network.HttpThread;
import network.NetworkUtils;


/*
* Copyright (c) 2012,UIT-ESPACE
* All rights reserved.
*
* �ļ���ƣ�LoginActivity.java  
* ժ Ҫ�� �û���¼ҳ��
* 
* ���ܣ�
* 1.�û���¼
*  
* ��ǰ�汾��1.1
* �� �ߣ��κ��
* ������ڣ�2012��11��3��
*
* ȡ��汾��1.0
* ԭ���� ��������
* ������ڣ�2012��9��12��
* 
*/

/**
 * 
 * @ClassName: LoginActivity 
 * @Description: ��¼ҳ��
 * @Author: Mr.Simple 
 * @E-mail: bboyfeiyu@gmail.com 
 * @Date 2012-11-7 ����8:17:46 
 *
 */

public class LoginActivity extends Activity {
  
	private Button loginBtn = null;			// ��¼��ť
	private EditText userEdit = null;		// �û�ID�����
	private EditText pwdEdit = null;		// �û����������
	private TextView regTextView = null;	// ע����ı����
	private CheckBox remPwdBox;				// �Ƿ��ס����CheckBox
	private CheckBox autoLoginBox = null;	// �Զ���¼
	
	private MyHandler mHandler = null ;		// UI�߳��е� Handler
	private Thread loginThread = null;		// ����һ�����߳�
	public static String mineID = null;		// �û�id
	public static String mineName = null;	// �û�name
	private String password = null;			// �û�����
	private String localIP = null;
	private MyProgressDialog mProgressDlg = null;	// ��½ʱ�Ľ����
	private final String SETTING = "usr_info";
	public static int gNoAuto = 0;			// ע��ʱ���Զ���½
	private boolean mCancleLogin = false;	// �����ؼ��ʱ��ȡ���¼
	private ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

	
	/**
	 * (�� Javadoc,��д�ķ���) 
	 * <p>Title: onCreate</p> 
	 * <p>Description: </p> 
	 * @param savedInstanceState 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		// ����Ϊ�ޱ���ģʽ
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.login);
        
        // ��������Ƿ����
        checkNetWorkStatus();				

		 // ��ȡ�û���
		mineID = ((EditText)findViewById(R.id.idEdit)).getText().toString();
		Log.d("��¼���û�ID �� ", mineID);
		
		// ������Ի���
		mProgressDlg = new MyProgressDialog(this, "��¼��,���Ժ�...");
       
        setTitle("Login");
        loginBtn = (Button)findViewById(R.id.loginBtn);
        loginBtn.setOnClickListener(new LoginButtonClick());
        
        userEdit = (EditText)findViewById(R.id.idEdit);
        pwdEdit = (EditText)findViewById(R.id.pwdEdit);
        userEdit.setText("android");
        pwdEdit.setText("android");
        
        // �Ƿ񱣴������checkBox
        remPwdBox = (CheckBox)findViewById(R.id.remPwd);			
        autoLoginBox = (CheckBox)findViewById(R.id.autoLogin);
        
        // �ӱ��ض�ȡ����
        readInfoFromLocal();										
        
        // ���ע��
        regTextView = (TextView)findViewById(R.id.register);
        regTextView.setClickable( true );
        regTextView.setOnClickListener(new OnRegisterClick());
        	
        // ����ǰҳ����ӵ�map�� ,�����˳����Ӧ�ó���
        MainViewActivity.addActivityToHashSet( this );
        // ע����������Ҫ�ٴ���ӵ��б�
        if (gNoAuto != 0){
        	MainViewActivity.addActivityToHashSet( this );
        }
        
    }
    
    
    /**
     * 
     * @ClassName: LoginButtonClick 
     * @Description:  �ڲ��࣬��¼��ť����Ӧ���������̣߳���ɵ�¼
     * @Author: xxjgood
     * @E-mail: bboyfeiyu@gmail.com 
     * @Date 2012-11-17 ����4:47:59 
     *
     */
	class LoginButtonClick implements OnClickListener {

		@Override
		public void onClick(View v) {

			// �Ƿ�ȡ���¼�Ŀ��Ʊ���
			mCancleLogin = false;

			saveInfoToLocal(); // �����û����뵽����
			checkNetWorkStatus();

			if ("".equals(userEdit.getText().toString().trim())) {
				Toast.makeText(LoginActivity.this, "�û�����Ϊ�գ�", 1).show();
				return;
			} else {
				mineID = userEdit.getText().toString();
			}

			if ("".equals(pwdEdit.getText().toString().trim())) {
				Toast.makeText(LoginActivity.this, "���벻��Ϊ�գ�", 1).show();
				return;
			} else
				password = pwdEdit.getText().toString();

			// ������¼�߳�
			loginThread = new Thread( runnable ); 
			loginThread.start();
			
			// �������ʾ
			mProgressDlg.show(); 
			gNoAuto = 0;
		}

	} // end of Click
	
	
	/**
	 * Run����,����¼��Ϣ�ύ��������
	 */
	Runnable runnable = new Runnable()						
	{
		String msg = null; 									// Ҫ���͸����̵߳�String
		@Override
		public void run()
		{
			packData(); 									// ������
			HttpThread h = new HttpThread(nameValuePairs,2);// 2--��¼
			msg = (String)h.sendInfo(); 					// ���շ������ķ���ֵ
			sendMessage();									// ������Ϣ�����߳�
		}
					
		public void sendMessage(){							// �̼߳���ݴ���
		      Looper mainLooper = Looper.getMainLooper ();	// �õ����߳�loop
		      mHandler = new MyHandler(mainLooper);			// �������̵߳�handler
		      mHandler.removeMessages(0);						// �Ƴ����ж����е���Ϣ
		      Message m = mHandler.obtainMessage(1, 1, 1, msg);// ����Ϣ����message
		      mHandler .sendMessage(m);						// ����message
		}
	};		// end of Runnable
				
			
	/**
	 * 
	 * @Method: packData 
	 * @Description: ���Ҫ���͵������������  
	 * @throws
	 */
	void packData(){
				
		mineID = userEdit.getText().toString();
		password = pwdEdit.getText().toString();
					
		nameValuePairs.add(new BasicNameValuePair("protocol","landon"));// ��װ��ֵ��
		nameValuePairs.add(new BasicNameValuePair("id", mineID));
		nameValuePairs.add(new BasicNameValuePair("password", password));
		nameValuePairs.add(new BasicNameValuePair("ip", localIP));
	}
	

	/**
	 * 
	 * @ClassName: OnRegisterClick 
	 * @Description:   ������뵽ע�����
	 * @Author: xxjgood
	 * @E-mail: bboyfeiyu@gmail.com 
	 * @Date 2012-11-17 ����4:53:21 
	 *
	 */
	class OnRegisterClick implements OnClickListener{

		@Override
		public void onClick(View v) {
			
			Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
			startActivity(intent);
			overridePendingTransition(R.anim.slide_left, R.anim.slide_right);
		}

	}
	
	
	/**
	 * 
	 * @Method: checkNetWorkStatus 
	 * @Description: ��������Ƿ����
	 * @return   
	 * @throws
	 */
	private boolean checkNetWorkStatus() {
		boolean netSataus = false;
		ConnectivityManager cwjManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

		cwjManager.getActiveNetworkInfo();

		if (cwjManager.getActiveNetworkInfo() != null) {
			netSataus = cwjManager.getActiveNetworkInfo().isAvailable();
			netSataus = true;
			
			localIP = NetworkUtils.getLocalIpAddress();
	        Log.d("IP : ", localIP);
		}

		// ���粻����,����ʾ��������
		if (netSataus == false) {
			Builder b = new AlertDialog.Builder(this).setTitle("û�п��õ�����")
					.setMessage("�뿪��GPRS��WIFI��������");
			b.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int whichButton) {
					// ���粻����,�����������intent
					Intent mIntent = new Intent("/");
					ComponentName comp = new ComponentName("com.android.settings",
																"com.android.settings.WirelessSettings");
					mIntent.setComponent(comp);
					mIntent.setAction("android.intent.action.VIEW");
					startActivityForResult(mIntent, 0); // �����������ɺ���Ҫ�ٴν��в�����������д�������룬�����ﲻ����д
				}
			}).setNeutralButton("ȡ��", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int whichButton) {
					dialog.cancel();
				}
			}).show();
		}
		return netSataus;
	}
	

	/**
	 * 
	 * @ClassName: MyHandler 
	 * @Description:  MyHandler,�������̷߳��͵���Ϣ�������д���
	 * @Author: xxjgood
	 * @E-mail: bboyfeiyu@gmail.com 
	 * @Date 2012-11-17 ����4:54:35 
	 *
	 */
	private class MyHandler extends Handler {

		/**
		 * 
		 * @Constructor: 
		 * @param looper
		 * @Description: ���캯��
		 * @param looper
		 */
		public MyHandler(Looper looper) {
			super(looper);
		}

		/*
		 * (�� Javadoc,��д�ķ���) 
		 * <p>Title: handleMessage</p> 
		 * <p>Description: </p> 
		 * @param msg 
		 * @see android.os.Handler#handleMessage(android.os.Message)
		 */
		@Override
		public void handleMessage(Message msg) {
			String result = "";
			try {
				result = msg.obj.toString(); // ���յ����̵߳��ַ�
				result = result.trim(); // ȥ�ո�
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			// ��ȡ���������ص��û���¼��Ϣ
			String s2[] = result.split(";;");
			// ����¼�ɹ�������ת��������
			if (!mCancleLogin && s2[0].equals("success")
					&& !result.contains("error")) {

				mineName = s2[1];
				Log.d("�ҵ��ǳ�", mineName);
				Intent intent = new Intent(LoginActivity.this, MainViewActivity.class);
				// ע��õط��Ǵ�����ݸ������activity,��ȡID�ŵ�
				startActivity(intent);
			} else if (s2[0].equals("fail")) { // ��ʾ����

				Toast.makeText(LoginActivity.this, "���°�? �˻������������,����������...", 0)
						.show();
				Animation shake = AnimationUtils.loadAnimation(
						LoginActivity.this, R.layout.shake);
				findViewById(R.id.pwdEdit).startAnimation(shake);
			} else {
				Toast.makeText(LoginActivity.this, "��,������粻������,Ԫ��,����ô��? ", 1)
						.show();
				Log.d("1", s2[0] + "");
			}

			// ��¼��ȿ�����
			mProgressDlg.dismiss();
			mCancleLogin = false;
		}
	}
		
	
	/**
	 * @Method: saveInfoToLocal 
	 * @Description: �����û���¼��Ϣ������ ��������ס���롢�Զ���¼�ȣ� 
	 * @throws
	 */
	private void saveInfoToLocal() {
		boolean bAutoLogin = false;

		// �����û�����
		SharedPreferences ref = getSharedPreferences(SETTING, 0);
		if (remPwdBox.isChecked()) {
			String pwd = pwdEdit.getText().toString();
			Random rd = new Random();
			int num = rd.nextInt(9);
			pwd = "s" + num + pwd + "ns" + rd.nextInt(9);
			ref.edit().putString("pwd", pwd).putBoolean("rem", true).commit();

		} else {
			ref.edit().putString("pwd", "").putBoolean("rem", false).commit();
		}

		// �Զ���¼
		if (autoLoginBox.isChecked()) {
			bAutoLogin = true;
		}

		ref.edit().putBoolean("autoLogin", bAutoLogin) // �Զ���¼
				.putString("id", userEdit.getText().toString()).commit();
	}

	/**
	 * 
	 * @Method: readInfoFromLocal
	 * @Description: ���� �� �ӱ����ļ��ж�ȡ�û���ݵ�
	 * @throws
	 */
	private void readInfoFromLocal() {
		SharedPreferences settings = getSharedPreferences(SETTING, 0); // ��ȡһ������

		String id = settings.getString("id", "");
		String pwd = settings.getString("pwd", ""); // ȡ�����������
		if (pwd.length() >= 6) {
			pwd = pwd.substring(2, pwd.length() - 3); // ����
			Log.d("�����ȡ", pwd);
		}
		boolean rem = settings.getBoolean("rem", false);
		boolean auto = settings.getBoolean("autoLogin", false);

		if (!id.equals("") && !pwd.equals("")) {
			userEdit.setText(id);
			pwdEdit.setText(pwd);
		}

		if (rem) {
			remPwdBox.setChecked(true);
		} else {
			remPwdBox.setChecked(false);
		}

		if (auto && gNoAuto == 0) // gNoAuto Ϊ���û����ע��ʱ���Զ���½
		{
			autoLoginBox.setChecked(true);
			loginThread = new Thread(runnable); // ����һ�����߳�
			loginThread.start();
			mProgressDlg.show(); // �������ʾ
		} else {
			autoLoginBox.setChecked(false);
		}

	}

	long exitTime = 0;

	/*
	 * (�� Javadoc,��д�ķ���) �����¼���Ӧ,��������������"back"���˳�
	 * @Title: onKeyDown
	 * @Description:
	 * @param keyCode
	 * @param event
	 * @return
	 * @see android.app.Activity#onKeyDown(int, android.view.KeyEvent)
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		switch (keyCode) {

		case KeyEvent.KEYCODE_BACK: // �˳�
		{
			// ȡ���¼
			mCancleLogin = true;
			if ((System.currentTimeMillis() - exitTime) > 2000
					&& event.getAction() == KeyEvent.ACTION_DOWN) {

				Toast.makeText(getApplicationContext(), "�ٰ�һ�ξ��˳���������...",
						Toast.LENGTH_SHORT).show();
				exitTime = System.currentTimeMillis();

			} else {
				// �˳�����
				MainViewActivity.killCurrentApp(this);
			}
			// ������Ҫ��!��Ȼ�����Ĭ�ϵĻ��˵���һ����
			return true;
		}

		default:
			break;
		}

		return super.onKeyDown(keyCode, event);
	}


	/*
	 * (�� Javadoc,��д�ķ���) 
	 * @Title: onStop
	 * @Description: ҳ��ֹͣ�Ļص�����
	 * @see android.app.Activity#onStop()
	 */
	@Override
	protected void onStop() {
		super.onStop();

		if (mHandler != null) {
			mHandler.removeCallbacks(runnable);
		}

		if (loginThread != null) {
			loginThread.interrupt();
			loginThread = null;
		}
	}
		

	/*
	 * (�� Javadoc,��д�ķ���)   �����˵�
	 * <p>Title: onCreateOptionsMenu</p> 
	 * <p>Description: </p> 
	 * @param menu
	 * @return 
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		menu.add(Menu.NONE, Menu.FIRST + 1, 1, "����").setIcon(
				android.R.drawable.ic_menu_set_as);
		menu.add(Menu.NONE, Menu.FIRST + 2, 2, "�˳�").setIcon(
				android.R.drawable.ic_lock_power_off);

		return true;
	}

	/*
	 * (�� Javadoc,��д�ķ���) �˵�ѡ���¼� 
	 * Title: onOptionsItemSelected
	 * @Description: �˵�ѡ���¼�
	 * @param item
	 * @return
	 * 
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {

		case Menu.FIRST + 1: // �ҵ�λ��
			Intent intent = new Intent(this, SettingActivity.class);
			startActivity(intent);
			return true;

		case Menu.FIRST + 2:
			MainViewActivity.killCurrentApp(this);
			return true;

		default:
			break;
		}
		return false;

	} // end of onOptionsItemSelected

	}