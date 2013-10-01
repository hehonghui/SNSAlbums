package album.entry;

import network.HttpThread;


/**
 * 
 * @ClassName: RegisterActivity 
 * @Description: 
 * @Author: Mr.Simple (�κ��) & xxjgood
 * @E-mail: bboyfeiyu@gmail.com 
 * @Date 2012-11-17 ����5:40:21 
 *
 */

public class RegisterActivity extends Activity{

	private MyHandler mHandler = null ;				// UI�߳��е� Handler
	private Thread regThread = null;				// ����һ�����߳�	
	private Button regBtn = null;					// ע�ᰴť
    private MyProgressDialog mProgressDlg = null;	// ��½ʱ�Ľ����
	

    /**
     * (�� Javadoc,��д�ķ���) 
     * @Title: onCreate
     * @Description: 
     * @param savedInstanceState 
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
	@Override
	protected void onCreate(Bundle savedInstanceState) {		

		super.onCreate(savedInstanceState);
		setContentView(R.layout.register);
		
		regBtn = (Button)findViewById(R.id.eregisterbutton);	
		regBtn.setOnClickListener(new RegisterButtonClick());		// ���ð�ť����
		
		// ������Ի����ʼ��
		mProgressDlg = new MyProgressDialog(this, "ע����,���Ժ�...");
		setTitle("ע��") ;
	}
	
	
	/**
	 * 
	 * @ClassName: MyHandler 
	 * @Description:  �ڲ��� MyHandler������ע��ɹ����
	 * @Author: Mr.Simple (�κ��)
	 * @E-mail: bboyfeiyu@gmail.com 
	 * @Date 2012-11-17 ����5:40:45 
	 *
	 */
	private class MyHandler extends Handler{       
        public MyHandler(Looper looper){
               super (looper);
        }
        @Override
        public void handleMessage(Message msg) { 							// ������Ϣ
        	String s1 = "";
        	try {
        		s1 = msg.obj.toString();
            	s1 = s1.trim();	
			} catch (Exception e) {
				
			}
        													// ȥ�ո�
        	if(s1.equals("success")){										// ���ɹ����������Activity
        		Toast.makeText(RegisterActivity.this, "��ϲ��ע��ɹ���", 0).show();
        		mProgressDlg.dismiss();
        		finish();
        	}
        	else
        	{
        		Log.d("1",s1);
        		Toast.makeText(RegisterActivity.this, "�Բ���ע��ʧ�ܣ�", 0).show();
        		mProgressDlg.dismiss();
        	}
        }            
	}
	
	
	/**
	 * @ClassName: RegisterButtonClick 
	 * @Description:  �ڲ��࣬ע�ᰴť��Ӧ
	 * @Author: Mr.Simple (�κ��)
	 * @E-mail: bboyfeiyu@gmail.com 
	 * @Date 2012-11-17 ����5:41:33 
	 *
	 */
	class RegisterButtonClick implements OnClickListener{
		
		private ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();	
		private boolean isPacked = false;
		@Override
		public void onClick(View v) {									// ������Ӧ

			isPacked = packData();										// ������
			if (isPacked )
			{
				mProgressDlg.show();									// ������Ի�����ʾ
				regThread = new Thread(runnable);						// ����һ�����߳�
				regThread.start();
			}
			else
			{
				displayToast("��Ǹ,����ύʧ��,��˲�ע����Ϣ...");
			}
		}
		
		Runnable runnable = new Runnable()								// Runable							
		{
			String msgToSend = null;									// ���͸����̵߳�String
			
			@Override
			public void run()
			{
				Looper.prepare();
				
				HttpThread h = new HttpThread(nameValuePairs,1);		// Э�� 1--ע��
				msgToSend = (String)h.sendInfo();						// ��������������󣬲��������
				sendMessage();											// �����̵߳���ݷ��͵����߳�
				
				Looper.loop();
			}
			
			public void sendMessage(){									// �̼߳���ݴ���
				
                Looper mainLooper = Looper.getMainLooper ();			// �õ����߳�loop
                String msg ;

                mHandler = new MyHandler(mainLooper);					// �������̵߳�handler
                msg = msgToSend;
                mHandler.removeMessages(0);								// �Ƴ����ж����е���Ϣ
                Message m = mHandler.obtainMessage(1, 1, 1, msg);		// ����Ϣ����message
                mHandler .sendMessage(m);								// ����message
			}
		};
		

		/**
		 * @Method: packData
		 * @Description: ���Ҫ�ύ��ע����Ϣ
		 * @return
		 */
		private boolean packData()
		{
			boolean flag = true;
			Log.d("", "��ݴ��");
		
			String id = ( (EditText)findViewById(R.id.eid) ).getText().toString();				
			String name = ( (EditText)findViewById(R.id.ename) ).getText().toString();			// �û��ǳ�
			String password = ( (EditText)findViewById(R.id.epassword) ).getText().toString();	// �û�����
			String pwd2 = ((EditText)findViewById(R.id.epwd2)).getText().toString();			// ȷ������
			String email = ( (EditText)findViewById(R.id.email) ).getText().toString();			// �ʼ�
			
			Log.d("������ : ", id + " pwd : " + password +" Name : " +  name + pwd2 + email);
			
			if (id.length() == 0) {
				displayToast("�û�����Ϊ��,��������...");
				flag = false;
			}
			if (pwd2.trim().length() == 0 || password.length() == 0) {
				displayToast("���벻��Ϊ��,����������...");
				flag = false;
			}
			if (!password.equals(pwd2)) {
				displayToast("�����������벻ƥ��,����������...");
				flag = false;
			}
			if (name.length() == 0) {
				displayToast("�ǳƲ���Ϊ�գ��������ǳ�...");
				flag = false;
			}
			
			nameValuePairs.add(new BasicNameValuePair("protocol", "regist"));// ��װ��ֵ��
			nameValuePairs.add(new BasicNameValuePair("id", id));
			nameValuePairs.add(new BasicNameValuePair("password", password));
			nameValuePairs.add(new BasicNameValuePair("name", name));
			nameValuePairs.add(new BasicNameValuePair("email", email));
			return flag;
		}
	}
	
	
	/**
	 * @Method: displayToast
	 * @Description: ��ʾToast��Ϣ
	 * @param tips
	 */
	private void displayToast(String tips)
	{
		Toast.makeText(RegisterActivity.this, tips, 0).show();
	}
	
}
