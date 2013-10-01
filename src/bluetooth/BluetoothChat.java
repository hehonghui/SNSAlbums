
package bluetooth;

import help_dlg.HelpDialog;
import imageEdit.PictureEditActivity;

import album.entry.R;
import album.entry.MainViewActivity;
import camera.CameraActivity;
import chat.DetailAdapter;
import chat.DetailEntity;


/**
 * 
 * @ClassName: BluetoothChat 
 * @Description: �������������ͼ�����
 * @Author: Mr.Simple 
 * @E-mail: bboyfeiyu@gmail.com 
 * @Date 2012-11-9 ����2:39:59 
 *
 */

public class BluetoothChat extends Activity implements OnTouchListener,
											android.view.GestureDetector.OnGestureListener{
    // ������Ϣ
    private static final String TAG = "BluetoothChat";
    private static final boolean bDebug = true;

    // �ӶԷ���������Ϣ����.
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    // �� BluetoothChatService���յ����豸���
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "TOAST";

    // Intent������
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    private static final int PICK_PICTURE = 3;						// ѡ��ͼƬ��CODE
    
    private int column_index;										// ͼ������
    private String imagePath = "NULL";								// ͼ��·��

    // Layout ��ͼ
    private TextView mTitle;
    private ListView mConversationView;
    private EditText mOutEditText;
    private Button mSendButton;
    
    private ArrayList<DetailEntity> conversationList = null;		// �Ի��б�
	private DetailEntity mMsgEntity = null;							// ��Ϣʵ�������
	private DetailAdapter mConversationAdapter = null;				// ����������

    // �����ӵ��豸���
    private String mConnectedDeviceName = null;
    // �����߳��е��������������
    //private ArrayAdapter<String> mConversationArrayAdapter;
    // �����Ϣ��StringBuffer
    private StringBuffer mOutStringBuffer;
    // ���������豸������
    private BluetoothAdapter mBluetoothAdapter = null;
    // BluetoothChatService�Ķ���
    private BluetoothChatService mChatService = null;

    
	/**
	 * ���� �� ҳ�洴��
	 * (non-Javadoc)
	 * @see android.app.ActivityGroup#onCreate(android.os.Bundle)
	 */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(bDebug) Log.e(TAG, "+++ ON CREATE +++");

        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.bluetooth);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title);

        // �����Զ���ı�����
        mTitle = (TextView) findViewById(R.id.title_left_text);
        mTitle.setText("����");
        mTitle = (TextView) findViewById(R.id.title_right_text);

        // ��ȡĬ�ϵ�����������
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // ���������ΪNull������ʾ������
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "����������.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
     		
        MainViewActivity.addActivityToHashSet( this );	// ����ǰҳ����ӵ�activitymap��
        final LinearLayout layout = (LinearLayout) findViewById(R.id.bluetoothLayout);
	    MainViewActivity.checkSkin(layout);
      
    }

    
    /**
     * ���ܣ� activity Onstart
     * 
     */
    @Override
    public void onStart() {
        super.onStart();
        if(bDebug) Log.e(TAG, "++ ON START ++");

        // �������û������,����������
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);

        } else {
            if (mChatService == null) 
            	setupChat();
        }
    }

    
    /**
     * ���ܣ� onResume
     * 
     */
    @Override
    public synchronized void onResume() {
        super.onResume();
        if(bDebug) Log.e(TAG, "+ ON RESUME +");

       // ���BluetoothChatService����Ϊ��,���ȡBluetoothChatService�����״̬
        if (mChatService != null) {
            if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
            	
            // ����BluetoothChatService������� 
              mChatService.start();
            }
        }
    }

    
    /**
     * ���ܣ� ��ʼ�������豸
     * 
     */
    private void setupChat() {
        Log.d(TAG, "setupChat()");
     	        
        // ��ʼ����Ϣ�༭��,�������ü�����
        mOutEditText = (EditText) findViewById(R.id.edit_text_out);
        mOutEditText.setOnEditorActionListener(mWriteListener);

        // ��ʼ�����Ͱ�ť
        mSendButton = (Button) findViewById(R.id.button_send);
        mSendButton.setOnClickListener(new OnClickListener() {
            @Override
			public void onClick(View v) {
               // ������Ϣ��Է�
                TextView view = (TextView) findViewById(R.id.edit_text_out);
                String message = view.getText().toString();
                sendMessage(message);
            }
        });

        // ��ʼ��BluetoothChatService������ִ����������
        mChatService = new BluetoothChatService(this, mHandler);

        // ��ʼ�������Ϣ��StringBufferΪ��
        mOutStringBuffer = new StringBuffer("");
    }

    
    /**
     * ���ܣ� ҳ��onPause
     * 
     */
    @Override
    public synchronized void onPause() {
        super.onPause();
        if(bDebug)
        	Log.e(TAG, "- ON PAUSE -");
    }
    

    /**
     * ���ܣ� ҳ��ONSTOP
     * 
     */
    @Override
    public void onStop() {
        super.onStop();
        if(bDebug) 
        	Log.e(TAG, "-- ON STOP --");
        // �ر�����
        mBluetoothAdapter.disable() ;
		// ����ҳ���Set���Ƴ�
		MainViewActivity.removeFromSet( this );
    }

    
    /**
     * ���ܣ�ҳ�����,ֹͣ���������߳�
     * 
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        // Stop the Bluetooth chat services
        if (mChatService != null)
        	mChatService.stop();
        if(bDebug) 
        	Log.e(TAG, "--- ON DESTROY ---");
    }

    
    /**
     * ���ܣ� ʹ�������Է���
     * 
     */
    private void ensureDiscoverable() {			
        if(bDebug)
        	Log.d(TAG, "ʹ�����ɷ���");
        // ��������豸���ǿ����ӺͿɷ��ֵ�,�����intent��ת���������ý���
        if (mBluetoothAdapter.getScanMode() !=
            BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
        	
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

    
    /**
     * 
     * @Method: sendMessage 
     * @Description: ͨ���������� ������Ϣ
     * @param message   
     * @return void  �������� 
     * @throws
     */
    private void sendMessage(String message) {
    	
        // �ж�����״̬,û���������˳�
        if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }

        // �ж���Ϣ�ĳ���
        if (message.length() > 0) {
         
            byte[] byteData = message.getBytes();	// ת�����ֽ���
            mChatService.write( byteData );			// ������Ϣ
            
            // ����StringBuffer����
            mOutStringBuffer.setLength(0);
            mOutEditText.setText(mOutStringBuffer);
            
            addConversationMsg("��˵: ", message, 1);// ��ӵ������б�
        }
    }

    /**
     * ���� ����ʼ�������������Ϣ�б����������
     * 
     */
    private void initConversationList()
    {
        // �������ȡϵͳʱ��
   		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd  HH:mm:ss");     
   		Date curDate = new Date(System.currentTimeMillis());		//��ȡ��ǰʱ��     
   		String date = formatter.format(curDate); 
   		Log.d(TAG, "ʱ�� : " + date);
   		   
        conversationList = new ArrayList<DetailEntity>();
           
        DetailEntity tips = new DetailEntity("��ʾ��Ϣ","2010-04-26","������,�ɽ�������.",
   											R.layout.list_say_me_item);
        conversationList.add(tips);
           
        // ������
        mConversationAdapter = new DetailAdapter(this, conversationList);
        mConversationView = (ListView)findViewById(R.id.conversationList); 	// ������������Ĳ���
        mConversationView.setAdapter(mConversationAdapter);
        // �����б�Ĵ����¼�������,�������Ʒ���ͼƬ.
        mConversationView.setOnTouchListener(this);
        
    }
    
    
    /**
	 *  ���ܣ� ����Ϣ��ӵ������б�
	 *  
	 */
   private void addConversationMsg(String name, String content , int layoutId)
   {
	   // �������ȡϵͳʱ��
	   SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");     
	   Date curDate = new Date(System.currentTimeMillis());		//��ȡ��ǰʱ��     
	   String date = formatter.format(curDate); 
	   Log.d(TAG, "ʱ�� : " + date);
	   
	   // ���Լ�����ȥ����Ϣ
	   if ( layoutId == 1)
	   {
		// ��ӶԻ�
			mMsgEntity = new DetailEntity(name, date,content, R.layout.list_say_me_item);
	       
	   }
	   else
	   {
		   mMsgEntity = new DetailEntity(name, date, content, R.layout.list_say_he_item);
	   }
	   
	   conversationList.add( mMsgEntity );
       mConversationView.setAdapter(new DetailAdapter(BluetoothChat.this, conversationList));
       
   }
   
    /**
     *  ����: ��Ϣ�༭���ļ���,�ɰ��س�����.
     *  
     */
    private TextView.OnEditorActionListener mWriteListener =
        new TextView.OnEditorActionListener() {
        @Override
		public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
            // ���س�ʱ������Ϣ
            if (actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_UP) {
                String message = view.getText().toString();
                sendMessage(message);			// ������Ϣ
            }
            if(bDebug) Log.i(TAG, "END onEditorAction");
            return true;
        }
    };
    
    
    /**
     *  ���ܣ� ���ö��߳̽���������������BluetoothChatService�з���������Ϣ
     *  
     */
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            
            case MESSAGE_STATE_CHANGE:								// ����״̬�ı�
                if(bDebug)
                	Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                
                switch (msg.arg1) {
                
	                case BluetoothChatService.STATE_CONNECTED:		// �����Ѿ�����
	                    mTitle.setText(R.string.title_connected_to);
	                    mTitle.append(mConnectedDeviceName);
	                    
	                    Log.d(TAG, "�����Ѿ�����");
	                    //mConversationAdapter.clear();				// �����ԭ������Ϣ�б�
	                    initConversationList();						// �������Ӻ��ʼ����Ϣ�б��
	                    
	                    break;
	                    
	                case BluetoothChatService.STATE_CONNECTING:		// ������������
	                    mTitle.setText(R.string.title_connecting);
	                    break;
	                    
	                case BluetoothChatService.STATE_LISTEN:			// �������ڼ���
	                case BluetoothChatService.STATE_NONE:
	                    mTitle.setText(R.string.title_not_connected);
	                    break;
	                }
                break;
                
            case MESSAGE_WRITE:										// ������д����,�������ֽ���,������Ϣ
                byte[] writeBuf = (byte[]) msg.obj;
                String writeMessage = new String (writeBuf );
         
                break;
                
            case MESSAGE_READ:										// �����Ķ�����,��������Ϣ
                byte[] readBuf = (byte[]) msg.obj;					// ��Handler�ύ������Ϣ�ж�ȡ��Ϣ.ת�����ֽ���
                // ��buffer�ﹹ��һ���ַ�
                String readMessage = new String(readBuf, 0, msg.arg1);
                addConversationMsg(mConnectedDeviceName + " ˵ : ", readMessage, 2);
                //mConversationArrayAdapter.add(mConnectedDeviceName + ":  " + readMessage);
                break;
                
            case MESSAGE_DEVICE_NAME:
                // �����Ѿ����ӵ��豸��
                mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                Toast.makeText(getApplicationContext(), "�����ӵ� "
                               + mConnectedDeviceName + "!", Toast.LENGTH_SHORT).show();
                break;
                
            case MESSAGE_TOAST:
                Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
                               								Toast.LENGTH_SHORT).show();
                break;
            }
        }
    };

    
    /**
     * ���ܣ� �ص�����,�������Activity�¼�
     * (non-Javadoc)
     * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
     * 
     */
    @Override
	public void onActivityResult(int requestCode, int resultCode, Intent intentData) {
        if(bDebug) 
        	Log.d(TAG, "onActivityResult " + resultCode);
        
        switch (requestCode) {
        
        case REQUEST_CONNECT_DEVICE:		// ��������
         
            if (resultCode == Activity.RESULT_OK) {
                // ��ȡ�����豸��MAC��ַ
                String address = intentData.getExtras()
                                     .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                // ��ȡԶ�������豸�ĵ�ַ
                BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
                // ����Զ�������豸
                mChatService.connect(device);
            }
            break;
            
        case REQUEST_ENABLE_BT:			// ʹ��������

            if (resultCode == Activity.RESULT_OK) {
                // �����Ѿ�����,���ʼ��
                setupChat();
            } else {
                // ����û�п���
                Log.d(TAG, "BT not enabled");
                Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                finish();
            }
            break;
            
        case PICK_PICTURE:		// ��ͼ���ȡ��ͼƬ���
        	
        	if (intentData == null)
        	{
        		break;
        	}
        	Uri imageFileUri = intentData.getData();
        	
        	try {
	        	
	        	imagePath = getPath( imageFileUri );			// �����ѡͼ���·��
	        	Log.v(TAG, "��ͼ����ѡ����: " + imagePath);
            
        	}catch (Exception e) {
				e.printStackTrace();
			}
        	break;
        }
    }

    
    	/**
    	 * ���� ����ȡͼƬ��·��
    	 * @pamara Uri ͼ���uri
    	 * return  ͼ���·��
    	 */
 		private String getPath(Uri uri) {
 			
 			String[] projection = { MediaColumns.DATA };
 			Cursor cursor = managedQuery(uri, projection, null, null, null);
 			column_index = cursor.getColumnIndexOrThrow(MediaColumns.DATA);
 			
 			cursor.moveToFirst();
 			imagePath = cursor.getString(column_index);
 		
 			return cursor.getString(column_index);
 		}
 		
 		
    /**
     *  ���ܣ� �����˵�
     *  (non-Javadoc)
     * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
     * 
     */
 	@Override
     public boolean onCreateOptionsMenu(Menu menu) {

         // setIcon()����Ϊ�˵�����ͼ�꣬����ʹ�õ���ϵͳ�Դ��ͼ��
 		menu.add(Menu.NONE, Menu.FIRST + 1, 1, "ɨ��").setIcon(
 	    android.R.drawable.ic_menu_search);

 		menu.add(Menu.NONE, Menu.FIRST + 2, 2, "�ɷ���").setIcon(
 		android.R.drawable.ic_menu_mylocation);
 		
 		menu.add(Menu.NONE, Menu.FIRST + 3, 3, "��������").setIcon(
 		 		android.R.drawable.ic_menu_share);
 		
 		menu.add(Menu.NONE, Menu.FIRST + 4, 4, "�ر�����").setIcon(
 		 		android.R.drawable.ic_menu_close_clear_cancel);
 		menu.add(Menu.NONE, Menu.FIRST + 5 , 5, "����").setIcon(android.R.drawable.ic_menu_help);
 		menu.add(Menu.NONE, Menu.FIRST + 6, 6, "�˳�").setIcon(android.R.drawable.ic_lock_power_off);
 		
         return true;
     }
 	
 	
 	/**
 	 *  ���ܣ� �˵�ѡ���¼�����
 	 *  (non-Javadoc)
 	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
 	 * 
 	 */
 	@Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {

	         case Menu.FIRST + 1:			// ɨ������
	        	 Intent serverIntent = new Intent(this, DeviceListActivity.class);
		         startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
		         return true;
	  
	         case Menu.FIRST + 2:		// �����ɷ���
	        	 ensureDiscoverable();
	         	return true;
	         	
	         case Menu.FIRST + 3:		// ����ͼƬ�ļ�
	        	 gotoGallery();
	         	return true;
	         	
	         case Menu.FIRST + 4:		// �ر�����
	        	 mBluetoothAdapter.disable();
	         	return true;
	         	
	         case Menu.FIRST + 5:		// ����
	        	 HelpDialog helpDlg = new HelpDialog(BluetoothChat.this, R.string.camera_help_text);
			     helpDlg.showHelp();
			   return true;
			   
	         case Menu.FIRST + 6:		// �˳�����
	        	 MainViewActivity.killCurrentApp(BluetoothChat.this);
	        	 break;
	        default:
	         		break;

         }
         return false;

     }		// end of onOptionsItemSelected
 	
 	
 	/**
 	 * ���� ��  ����ϵͳͼ��ѡ��Ҫ�����Է���ͼƬ
 	 * 
 	 */
 	private void gotoGallery()
 	{
 		Toast.makeText(this,"ѡ��ͼƬ֮������Ļ���ϻ����ɷ��ͣ�",1).show();
 		Intent intent = new Intent();
 		intent.setType("image/*");
 		intent.setAction(Intent.ACTION_GET_CONTENT);
 		startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_PICTURE);
 	
 	}
    
 	
 	Handler mSendHandler = new Handler();	
    /***
	 * ���� �� ͨ����������ͼ����ļ�,����ϵͳ�������ͳ���      ����ѡ��ʹ��������socket���ͣ�		
	 * 
	 **/
	private void sendPictureByBluetooth() {
	
		mSendHandler.post( mSendThread );
	}
	
	
	/**
	 * ���� ��  ����ͼƬ���߳�
	 *
	 */
	Runnable mSendThread = new Runnable() {
		
		@Override
		public void run() {

			final String deviceAddress = mChatService.getConnectedDeviceAddress();
			Log.v(TAG, "�豸��ַΪ : " + deviceAddress);
			
			if( deviceAddress != null){		// �����ѡ��ͼ��
				//����ϵͳ�������ļ�
				ContentValues cv = new ContentValues();
				String uri = "file://" + imagePath;			// �ļ���ȫ·��
				cv.put("uri", uri);
				// Ҫ���͸�������豸��ַ
				cv.put("destination", deviceAddress);		// ���Ҫ���͵���Ŀ���豸��ַ
				cv.put("direction", 0);
				Long ts = System.currentTimeMillis();
				cv.put("timestamp", ts);
				getContentResolver().insert(Uri.parse("content://com.android.bluetooth.opp/btopp"), cv);			
						
				Toast.makeText(BluetoothChat.this, "ͼ������...", 1).show();
			}
			else {
				Toast.makeText(BluetoothChat.this, "����ʧ��!(����δ���ӻ�ͼƬδѡ��).", Toast.LENGTH_SHORT).show();
			}
			
		}	// end of run().
	};		// end of Runnable()
 	
	
 	/**
 	 * ���ܣ� �����µ��¼�����
 	 * (non-Javadoc)
 	 * @see android.app.Activity#onKeyDown(int, android.view.KeyEvent)
 	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		
		switch (keyCode) {
			
		case KeyEvent.KEYCODE_BACK:				// ����������
				Intent intent = new Intent(BluetoothChat.this, MainViewActivity.class);
				setResult(RESULT_OK, intent);
				finish();
				overridePendingTransition(R.anim.slide_left, R.anim.slide_right);
				return false;
		default:
				break;
	}
		return super.onKeyDown(keyCode, event);

	}		// end of keydown.

	
	float downx = 0;		// ����ʱ��X���
	float downy = 0;		// ����ʱ��Y���
	float upx = 0;			// ̧��ʱ��X���
	float upy = 0;			// ̧��ʱ��Y���
	/**
	 * ���ܣ� ͨ����Ļ�Ϸ�������ָ,��ѡ�е���Ƭ������Ѿ����ӵ������豸
	 *  �����¼�(non-Javadoc)
	 * @see android.view.View.OnTouchListener#onTouch(android.view.View, android.view.MotionEvent)
	 * 
	 */
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		int action = event.getAction();
		switch (action) {
			case MotionEvent.ACTION_DOWN:
				downx = event.getX();
				downy = event.getY();
				Log.v(TAG, "downy : " + downy);
				break;
				
			case MotionEvent.ACTION_UP:
				upx = event.getX();
				upy = event.getY();
				Log.v(TAG, "upy : " + upy);
				if(downy - upy > 250 && imagePath != "NULL")
				{
					Log.v(TAG, "Touch and Send image." + (downy - upy));
		        	sendPictureByBluetooth();							// ��Է�������Ƭ
				}
				break;
				
			case MotionEvent.ACTION_CANCEL:
				break;
			default:
				break;
		}
		return true;
	}
	

	/**
	 *  ���ܣ� �������,�����л�ҳ��
	 *  (non-Javadoc)
	 * @see android.view.GestureDetector.OnGestureListener#onDown(android.view.MotionEvent)
	 * 
	 */
		@Override
		public boolean onDown(MotionEvent e) {
			return false;
		}

		
		/**
		 * ���� ������Ļ�ϻ���,�л�activity
		 * ����: ��������൱��һ����������Ȼ�п������������ߣ���e1Ϊ��������㣬e2Ϊ�������յ㣬
		 * 		velocityXΪ����ˮƽ������ٶȣ�velocityYΪ������ֱ������ٶ�
		 * 
		 */
		private int verticalMinDistance = 200;  		// ��ֱ����С����
		private int minVelocity = 0;  					// ��С����
		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			if (e1.getX() - e2.getX() > verticalMinDistance && Math.abs(velocityX) > minVelocity) {  
				  
				 // ���һ���������,�л�Activity  
		    	Intent cIntent = new Intent(BluetoothChat.this, PictureEditActivity.class);
				startActivityForResult(cIntent, 6); 
		        overridePendingTransition(R.anim.slide_left, R.anim.slide_right);
		       
		    } else if (e2.getX() - e1.getX() > verticalMinDistance && Math.abs(velocityX) > minVelocity) {  
		    	 // ���󻮶�������
				Intent bIntent = new Intent(BluetoothChat.this, CameraActivity.class);
				startActivityForResult(bIntent, 7); 
		        overridePendingTransition(R.anim.slide_left, R.anim.slide_right);
		       
		    }  
		  
		    return false;  
		}

		@Override
		public void onLongPress(MotionEvent e) {
			
		}


		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2,
				float distanceX, float distanceY) {
			return false;
		}


		@Override
		public void onShowPress(MotionEvent e) {
			
		}

		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			return false;
		}

}