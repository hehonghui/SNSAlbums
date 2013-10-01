package camera;

import help_dlg.HelpDialog;

import album.entry.LoginActivity;
import album.entry.MainViewActivity;
import album.entry.R;
import albums.PhotoAlbumActivity;


/*
* Copyright (c) 2012,UIT-ESPACE
* All rights reserved.
*
* �ļ���ƣ�CameraActivity.java
* ժ Ҫ��������
* 1.�Զ������յ�ʵ��(������Ļʵ���Զ��Խ�)
* 2.ɫ��ģʽ������
* 3.���ص�ģʽ����
* 4.��ƽ��ģʽ����
* 5.��������
* 6.����ģʽ(����������ʾ)
* 
* ��ǰ�汾��1.1
* �� �ߣ��κ��
* ������ڣ�2012��11��3��
*
* ȡ��汾��1.0
* ԭ���� ���κ��
* ������ڣ�2012��8��26��
*/

public class CameraActivity extends Activity implements OnTouchListener,TextToSpeech.OnInitListener,
		SurfaceHolder.Callback, Camera.PictureCallback {

	private SurfaceView cameraView;					// ���ٻ���
	private SurfaceHolder surfaceHolder;			// SurfaceHolder����camera��SurfaceView�Ķ���
	private Camera mCamera;							// Camera����
	private Camera.Parameters mParameters;			// ��������ͷ����Ķ���
	
	private TextView countdownTextView;				// ����ģʽ�µĵ���ʱ��ʾ�ؼ�
	private TextToSpeech mTts;						// TTS ����ʱ�Ķ���
	private Handler timerUpdateHandler;				// ����ģʽ�µ���ʱHandler
	private boolean timerRunning = false;			
	private int currentTime = 5;					// 5�붨ʱ
	
	private static int m_Effectflag = 0;			// ������������ͷ����ı���
	private boolean af = false;						// �Զ��Խ��Ĳ����ͱ���
	
	private PopupWindow popupWindow;				// �˵�������PopupWindow
	private GridView menuGrid;						// �˵��������ʾģ��
	
	// �˵�ѡ��,���յ����˵�ѡ��
	private String[] menu_name_array = null;
	// �˵�ͼ������
	private int[] menu_image_array = { android.R.drawable.ic_menu_set_as,
			android.R.drawable.ic_menu_agenda,
			android.R.drawable.ic_menu_help,
			android.R.drawable.ic_menu_info_details,
			R.drawable.timerb_48,
			android.R.drawable.ic_menu_gallery,
			android.R.drawable.ic_menu_help,
			android.R.drawable.ic_menu_close_clear_cancel
			};
	
	
	/**********************************************************************************************
	 * ���� �� ҳ�洴��
	 * (non-Javadoc)
	 * @see android.app.ActivityGroup#onCreate(android.os.Bundle)
	 **********************************************************************************************/
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		// ����Ϊ�ޱ���ģʽ
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.cameraview);
		// ������ս���Ĳ˵���
		menu_name_array = this.getResources().getStringArray(R.array.menuItems) ;
		
		cameraView = (SurfaceView) this.findViewById(R.id.CameraView);
		cameraView.setOnTouchListener( this );		// ���ô����¼�������
		
		surfaceHolder = cameraView.getHolder();		// ����holder
		surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		surfaceHolder.addCallback( this );			// ���� SurfaceHolder.Callback
		
		countdownTextView = (TextView) findViewById(R.id.CountTextView);
		
		// ��ʼ������
		mTts = new TextToSpeech(CameraActivity.this, CameraActivity.this);  
		
		// ����ǰҳ����ӵ�activitymap��
		MainViewActivity.addActivityToHashSet( this );	
	}

	
	/***************************************************************************************
	 *	���ܣ�surfaceCreated�Ĵ���,�������ʼ������ͷ
	 * (non-Javadoc)
	 * @see android.view.SurfaceHolder.Callback#surfaceCreated(android.view.SurfaceHolder)
	 ***************************************************************************************/
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		mCamera = Camera.open();
		try {
			mCamera.setPreviewDisplay(holder);
			mParameters = mCamera.getParameters();
			if (this.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
				mParameters.set("orientation", "portrait");

				// ����ͷԤ���Ļ�����ת90�ȣ�����ͼ��ת90��ʾ
				mCamera.setDisplayOrientation(90);
				mParameters.setRotation(90);
			}
			// ��������ͷ����
			mCamera.setParameters(mParameters);
		} catch (IOException exception) {
			mCamera.release();
		}
	}
	
	
	/***************************************************************************************
	 *���ܣ� ��������������ͷ����
	 * (non-Javadoc)
	 * @see android.view.SurfaceHolder.Callback#surfaceChanged(android.view.SurfaceHolder, int, int, int)
	 ***************************************************************************************/
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
		
		  mParameters = mCamera.getParameters();								// ��ȡ���в���

	       List<Size> sizes = mParameters.getSupportedPreviewSizes();
	       Size optimalSize = getOptimalPreviewSize(sizes, w, h);
	       mParameters.setPreviewSize( optimalSize.width, optimalSize.height );	// ������ʾ�Ŀ�Ⱥ͸߶�
	          
	       mCamera.setParameters( mParameters );								// ���ò���
	       mCamera.setDisplayOrientation(90);									// ������ת��ʮ��
	       mCamera.startPreview();
	}

	
	/***************************************************************************************
	 *���ܣ� �ͷ�����ͷ����
	 * (non-Javadoc)
	 * @see android.view.SurfaceHolder.Callback#surfaceDestroyed(android.view.SurfaceHolder)
	 ***************************************************************************************/
	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		mCamera.stopPreview();
		mCamera.release();
	}

	/***************************************************************************************
	 *���ܣ� ���պ�Ļص�����,�����ｫͼ��浽content provider��
	 * (non-Javadoc)
	 * @see android.hardware.Camera.PictureCallback#onPictureTaken(byte[], android.hardware.Camera)
	 ***************************************************************************************/
	@Override
	public void onPictureTaken(byte[] data, Camera mCamera) {
		
		// ��ͼ����뵽�����ṩ�ߵ���
		Uri imageFileUri = getContentResolver().insert(Media.EXTERNAL_CONTENT_URI,
							new ContentValues());
		try {
			// ��ͼ�����ʵ���д�뵽content provider
			OutputStream imageFileOS = getContentResolver().openOutputStream( imageFileUri );
			imageFileOS.write(data);
			imageFileOS.flush();
			imageFileOS.close();
			if ( imageFileOS != null){
				imageFileOS = null ;
			}

			Toast.makeText(this, "����ɹ���", Toast.LENGTH_SHORT).show();

		} catch (FileNotFoundException e) {
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
		} catch (IOException e) {
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
		}
		
		mCamera.startPreview();
	}

	
	/***************************************************************************************
	 *  ���ܣ� ��ȡ�Ż���Ԥ����С
	 *  @param List<Size>		����ֳߴ��list
	 *  @param int 				���
	 *  @param int 				�߶�
	 *  return Size				�����Ż���Ĵ�С
	 ***************************************************************************************/
	 private Size getOptimalPreviewSize(List<Size> sizes, int w, int h) {
	        final double ASPECT_TOLERANCE = 0.05;
	        double targetRatio = (double) w / h;
	        if (sizes == null) return null;

	        Size optimalSize = null;
	        double minDiff = Double.MAX_VALUE;

	        int targetHeight = h;

	        // Try to find an size match aspect ratio and size
	        for (Size size : sizes) {
	            double ratio = (double) size.width / size.height;
	            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
	            if (Math.abs(size.height - targetHeight) < minDiff) {
	                optimalSize = size;
	                minDiff = Math.abs(size.height - targetHeight);
	            }
	        }

	        // Cannot find the one match the aspect ratio, ignore the requirement
	        if (optimalSize == null) {
	            minDiff = Double.MAX_VALUE;
	            for (Size size : sizes) {
	                if (Math.abs(size.height - targetHeight) < minDiff) {
	                    optimalSize = size;
	                    minDiff = Math.abs(size.height - targetHeight);
	                }
	            }
	        }
	        return optimalSize;
	    }
	 
	
	 /**
	  * (�� Javadoc,��д�ķ���) 
	  * @Title: onTouch
	  * @Description:  ��������,��̧����ָʱ����,�����Զ��Խ�
	  * @param v
	  * @param event
	  * @return 
	  * @see android.view.View.OnTouchListener#onTouch(android.view.View, android.view.MotionEvent)
	  */
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			// ����ʱ�Զ��Խ�
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				mCamera.autoFocus( null );
				af = true;
			}
			//�ſ�������
			if (event.getAction() == MotionEvent.ACTION_UP && af ==true) {
				// ���յĺ���
				takePictureThread();		
			}   
			
			return true;
		}	
	

		/**
		 * @Method: setColorEffect
		 * @Description: ��������ͷ��ɫ��Ч��
		 * @param index  ��Ч������
		 */
	   private void setColorEffect(int index){
	    	switch(index)
	    	{
	    		case 0:
	    			Log.d("��Ч","����Ч");
		    		mParameters.setColorEffect(Camera.Parameters.EFFECT_NONE);
		    		break;
		    	case 1:
		    		Log.d("��Ч","��ɫ��Ч");
		    		mParameters.setColorEffect(Camera.Parameters.EFFECT_MONO);
		    		break;
		    	case 2:
		    		Log.d("��Ч","��Ƭ��Ч");
		    		mParameters.setColorEffect(Camera.Parameters.EFFECT_NEGATIVE);
		    		break;
		    	case 3:
		    		Log.d("��Ч","�ع���Ч");
		    		mParameters.setColorEffect(Camera.Parameters.EFFECT_SOLARIZE);
		    		break;
		    	case 4:
		    		Log.d("��Ч","����Ƭ��Ч");
		    		mParameters.setColorEffect(Camera.Parameters.EFFECT_SEPIA);
		    		break;
		    	case 5:
		    		Log.d("��Ч","�װ���Ч");
		    		mParameters.setColorEffect(Camera.Parameters.EFFECT_WHITEBOARD);
		    		break;
		    	case 6:
		    		Log.d("��Ч","�ڰ���Ч");
		    		mParameters.setColorEffect(Camera.Parameters.EFFECT_BLACKBOARD);
		    		break;
		    	case 7:
		    		Log.d("��Ч","ǳ��ɫ��Ч");
		    		mParameters.setColorEffect(Camera.Parameters.EFFECT_AQUA);
		    		break;
		    		default:
		    			break;
		    		
	    	}
	    	
	    	mCamera.setParameters(mParameters);
	    }
	    
		
	   /**
	    * @Method: setSceneMode
	    * @Description: ���ó���ģʽ
	    * @param index  ����������
	    */
	    private void setSceneMode(int index){
	    	switch(index)
	    	{
	    		case 0:
	    			Log.d("����","�Զ�");
		    		mParameters.setSceneMode(Camera.Parameters.SCENE_MODE_AUTO);
		    		break;
		    	case 1:
		    		Log.d("��Ч","����");
		    		mParameters.setSceneMode(Camera.Parameters.SCENE_MODE_ACTION);
		    		break;
		    	case 2:
		    		Log.d("��Ч","Ф��");
		    		mParameters.setSceneMode(Camera.Parameters.SCENE_MODE_PORTRAIT);
		    		break;
		    	case 3:
		    		Log.d("��Ч","�羰");
		    		mParameters.setSceneMode(Camera.Parameters.SCENE_MODE_LANDSCAPE);
		    		break;

		    	case 4:
		    		Log.d("��Ч","ҹ��");
		    		mParameters.setSceneMode(Camera.Parameters.SCENE_MODE_NIGHT);
		    		break;
		    	case 5:
		    		Log.d("��Ч","ҹ��Ф��");
		    		mParameters.setSceneMode(Camera.Parameters.SCENE_MODE_NIGHT_PORTRAIT);
		    		break;
		    	case 6:
		    		Log.d("��Ч","��Ժ");
		    		mParameters.setSceneMode(Camera.Parameters.SCENE_MODE_THEATRE);
		    		break;
		    	case 7:
		    		Log.d("��Ч","��̲");
		    		mParameters.setSceneMode(Camera.Parameters.SCENE_MODE_BEACH);
		    		break;
		
		    	case 8:
		    		Log.d("��Ч","����");
		    		mParameters.setSceneMode(Camera.Parameters.SCENE_MODE_SUNSET);
		    		break;
		    	case 9:
		    		Log.d("��Ч","ƽ��ͼ��");
		    		mParameters.setSceneMode(Camera.Parameters.SCENE_MODE_STEADYPHOTO);
		    		break;
		    	case 10:
		    		Log.d("��Ч","�̻�");
		    		mParameters.setSceneMode(Camera.Parameters.SCENE_MODE_FIREWORKS);
		    		break;
		    	case 11:
		    		Log.d("��Ч","�˶�");
		    		mParameters.setSceneMode(Camera.Parameters.SCENE_MODE_SPORTS);
		    		break;
		    	case 12:
		    		Log.d("��Ч","�ۻ�");
		    		mParameters.setSceneMode(Camera.Parameters.SCENE_MODE_PARTY);
		    		break;
		    	case 13:
		    		Log.d("��Ч","ѩ��");
		    		mParameters.setSceneMode(Camera.Parameters.SCENE_MODE_SNOW);
		    		break;
		    	default:
		    			break;
		    		
	    	}
	    	mCamera.setParameters( mParameters );
	    }
	    
		
	    /**
	     * @Method: setflashMode
	     * @Description:  �������ص�ģʽ
	     * @param index   ����Ƶ�����
	     */
	    private void setflashMode(int index){
	    	switch(index)
	    	{
		    	case 0:
		    		Log.v("Flash Mode", "On");
		    		mParameters.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
		    		break;
		    	case 1:
		    		Log.v("Flash Mode", "Auto");
		    		mParameters.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
		    		break;
		    	case 2:
		    		Log.v("Flash Mode", "red eye");
		    		mParameters.setFlashMode(Camera.Parameters.FLASH_MODE_RED_EYE);
		        	break;
		        case 3:
		    		Log.v("Flash Mode", "����");
		    		mParameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
		            break;
		        case 4:
		    		Log.v("Flash Mode", "�ر�");
		    		mParameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
		            break;
		        default:
		            	break;
	    	}
	    	
	    	mCamera.setParameters(mParameters);
	    }	// end of setFlashMode().
	    
		
	    /**
	     * @Method: setWhiteBalance
	     * @Description:  ���ð�ƽ��
	     * @param index   ��ƽ�������
	     */
	    private void setWhiteBalance(int index){
	    	switch(index)
	    	{
		    	case 0:
		    		mParameters.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_AUTO);
		    		break;
		    	case 1:
		    		mParameters.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_FLUORESCENT);
		    		break;
		    	case 2:
		    		mParameters.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_DAYLIGHT);
		        	break;
		        case 3:
		        	mParameters.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_WARM_FLUORESCENT);
		            break;
		        case 4:
		        	mParameters.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_DAYLIGHT);
		            break;
		        case 5:
		        	   mParameters.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_TWILIGHT);
		        	break;
		        case 6:
		        	   mParameters.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_CLOUDY_DAYLIGHT);
		            break;
		        case 7:
		        	   mParameters.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_SHADE);
		            break;
		         
		        default:
		            	break;
	    	}
	    	mCamera.setParameters(mParameters);
	    }
	    
		
	    /**
	     *  ��ʱ�������߳�,ʹ���������������ʾ��Ϣ,���������Զ�����
	     */
		private Runnable timerUpdateTask = new Runnable() {
			@Override
			public void run() {
			
				if (currentTime >= 1) {
					countdownTextView.setText("" + currentTime);
					sayTTS("" + currentTime);			//  *******************
					currentTime--;
					timerUpdateHandler.postDelayed(timerUpdateTask, 1000);
				} else {
					countdownTextView.setText("");
					takePictureThread();							// �����߳�
					timerRunning = false;
					currentTime = 5;
					countdownTextView.setVisibility( View.GONE );	// ���ض�ʱ���ı�
				}	
			}
		};
	 	
		
		/**
		 * @Method: setTakePictureOpition
		 * @Description: ��������Ч��Ĳ˵�ѡ��,index�Ĳ�ͬ����ʾ��ͬ��ѡ��˵�
		 * @param index  ���ܲ˵�������
		 */
		private void setTakePictureOpition(int index){
			
			String[] menuitems = null;		// �����Ĳ˵� 
			switch(index)
			{
			case 1:			// ɫ��Ч��
				m_Effectflag = 1;
				menuitems = getResources().getStringArray(R.array.colorEffectItems);
				break;
				
			case 2:			// ���������ģʽ
				 m_Effectflag = 2;
				 menuitems = getResources().getStringArray(R.array.flashModesItems);
	             break;

	         case 3:	 	 // ���ó���ѡ��
		         m_Effectflag = 3;
		         menuitems = getResources().getStringArray(R.array.sceneModesItems);
		         break;

	         case 4:	     // ���ð�ƽ��
	        	m_Effectflag = 4;
	        	menuitems = getResources().getStringArray(R.array.wBalanceModesItems);
	             break;
	 
	         case 5:	         // ����ģʽ
	             m_Effectflag = 5;
	             takePictureSelf();		// ����
	             break;

	         case 6:		// �˳�
	        	 m_Effectflag = 0; 
	         	 finish();
	             break;
				
			}
			
			Log.v("m_Effectflag",""+ m_Effectflag);
			
			new AlertDialog.Builder(this)
			.setTitle("��ѡ��Ч��...")  
			.setSingleChoiceItems(menuitems, 0, new DialogInterface.OnClickListener() {  
			    @Override
				public void onClick(DialogInterface dialog, int which) {  
			    	
			         setEffectOpition( which );							// �������յ�Ч�����
			         
			    }  
			}) .setPositiveButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener() {
                @Override
				public void onClick(DialogInterface dialog, int whichButton) {

                   
                }
            })
            .setNegativeButton(R.string.alert_dialog_cancel, new DialogInterface.OnClickListener() {
                @Override
				public void onClick(DialogInterface dialog, int whichButton) {

                    /* User clicked No so do some stuff */
                }
            }).show();
			
			//m_iOps = 0;
		}		// end of takePictureOption().
		
		
		/**
		 * @Method: setEffectOpition
		 * @Description:  ��������Ч��,which�Ĳ�ͬ�����ò�ͬ�Ĳ���
		 * @param which   ������õĲ���
		 */
		private void setEffectOpition(int which){
			// �������ѡ��	
			switch (m_Effectflag) {
			
			case 1:
					setColorEffect( which );		// ����ɫ����Ч
					break;
			case 2:
					setflashMode(which);			// �����ģʽ����
					break;
			case 3:
					setSceneMode(which);			// ��������
					break;
			case 4:
					setWhiteBalance( which);		// ��������
					break;
			default:
					break;
			}	// end of switch.
		}
		

		/**
		 * @Method: takePictureSelf
		 * @Description:  ���ĺ���
		 */
		private void takePictureSelf()
		{
			timerUpdateHandler = new Handler();		// Handler����
        	countdownTextView.setVisibility(0);
        	if (!timerRunning) {
    			timerRunning = true;
    			timerUpdateHandler.post(timerUpdateTask);
    		 }
		}
		
		
		/**
		 * @Method: takePictureThread
		 * @Description: �����߳� 
		 */
		private void takePictureThread()
		{
			new Thread(){
				@Override
				public void run() {
					super.run();
					 mCamera.takePicture(null, null, CameraActivity.this);
			         af = false;
				}
			}.start();
		}
		
		
		/**
		 * (�� Javadoc,��д�ķ���) 
		 * @Title: onKeyDown
		 * @Description:  ��ť���µ��¼�����
		 * @param keyCode
		 * @param event
		 * @return 
		 * @see android.app.Activity#onKeyDown(int, android.view.KeyEvent)
		 */
		@Override
		public boolean onKeyDown(int keyCode, KeyEvent event) {
			switch (keyCode) {
			
				case KeyEvent.KEYCODE_MENU:			// �����˵�
					openPopupwin();
					break;
					
				case KeyEvent.KEYCODE_BACK:			// ����Ч��
					if (popupWindow != null)
					{
						Intent intent = new Intent(CameraActivity.this, MainViewActivity.class);
						setResult(RESULT_OK, intent);
						finish();
						overridePendingTransition(R.anim.slide_left, R.anim.slide_right);
						return false;
					}
					break;
				default:
						break;
			}
			return super.onKeyDown(keyCode, event);
		}
		
		

		/**
		 * @Method: getMenuAdapter
		 * @Description:  ����һ��popupwindow ��������
		 * @param menuNameArray
		 * @param menuImageArray
		 * @return
		 */
			private ListAdapter getMenuAdapter(String[] menuNameArray,
					int[] menuImageArray) {
				
				ArrayList<HashMap<String, Object>> data = new ArrayList<HashMap<String, Object>>();
				for (int i = 0; i < menuNameArray.length; i++) {
					HashMap<String, Object> map = new HashMap<String, Object>();
					map.put("itemImage", menuImageArray[i]);
					map.put("itemText", menuNameArray[i]);
					data.add(map);
				}
				SimpleAdapter simperAdapter = new SimpleAdapter(this, data,
						R.layout.item_menu, new String[] { "itemImage", "itemText" },
						new int[] { R.id.item_image, R.id.item_text });
				return simperAdapter;

			}

			
			/**
			 * @Method: openPopupwin
			 * @Description: �򿪹���ѡ��˵���popupwindow
			 */
		private void openPopupwin() {
			
			LayoutInflater mLayoutInflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
			ViewGroup menuView = (ViewGroup) mLayoutInflater.inflate(R.layout.gridview_pop, null, true);
			
			menuGrid = (GridView) menuView.findViewById(R.id.gridview_popup);
			menuGrid.setAdapter(getMenuAdapter(menu_name_array, menu_image_array));
			menuGrid.requestFocus();
			
			// ����¼�
			menuGrid.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
					
					switch (arg2) {
					case 0:		// ɫ��Ч������
						popupWindow.dismiss();
						setTakePictureOpition(1);
						break;
					case 1:		// �����ģʽ����
						popupWindow.dismiss(); 
						setTakePictureOpition(2);
						break;
					case 2:	 	 // ����ѡ��
						popupWindow.dismiss();						
						setTakePictureOpition(3);
					     break;

				   case 3:	     // ��ƽ��
					   popupWindow.dismiss();
					   setTakePictureOpition(4);
				       break;
				         
				   case 4:	 	// ����ģʽ
					   	popupWindow.dismiss();
					   	takePictureSelf();
				        break;

				   case 5:		// �������
					   Intent intent = new Intent(CameraActivity.this,PhotoAlbumActivity.class);
					   intent.putExtra("id", LoginActivity.mineID);
					   startActivity(intent);
					   break;
					   
				   case 6:		// ����
					   popupWindow.dismiss();
					   HelpDialog helpDlg = new HelpDialog(CameraActivity.this, R.string.camera_help_text);
					   helpDlg.showHelp();
					   break;
					   
				    case 7:		// �˳�
				        m_Effectflag = 0; 
				        popupWindow.dismiss();
				        MainViewActivity.killCurrentApp( CameraActivity.this );	
				        break;
					default:
						break;
					}
					
				}
			});
			popupWindow = new PopupWindow(menuView, LayoutParams.WRAP_CONTENT,
							LayoutParams.WRAP_CONTENT, true);
			popupWindow.setBackgroundDrawable(new BitmapDrawable());
			popupWindow.setAnimationStyle(R.style.PopupAnimation);
			popupWindow.showAtLocation(findViewById(R.id.camera_layout), Gravity.CENTER
					| Gravity.BOTTOM, 0, 0);
			popupWindow.update();
		}

		
		/**
		 * (�� Javadoc,��д�ķ���) 
		 * @Title: onInit
		 * @Description:   ������ʼ��
		 * @param status 
		 * @see android.speech.tts.TextToSpeech.OnInitListener#onInit(int)
		 */
		@Override
		public void onInit(int status) {
			
	        if (status == TextToSpeech.SUCCESS) {
	         
	            int result = mTts.setLanguage(Locale.US);
	
	            if (result == TextToSpeech.LANG_MISSING_DATA ||
	                result == TextToSpeech.LANG_NOT_SUPPORTED) {
	            
	                Log.e("TTS", "Language is not available.");
	            } else {
	            	Log.e("TTS", "Language is available.");
	            }
	        } else {
	
	            Log.e("TTS", "Could not initialize TextToSpeech.");
	        }
			
		}		// END OF INIT
		
		
		/**
		 * @Method: sayTTS
		 * @Description: ����TTS
		 * @param words
		 */
		  private void sayTTS(String words) {
		   
		        mTts.speak(words, TextToSpeech.QUEUE_FLUSH, null);
		    }
		
		
		  /**
		   * (�� Javadoc,��д�ķ���) 
		   * @Title: onStop
		   * @Description:  ����ҳ��ֹͣ,�ͷ���Դ
		   * @see android.app.Activity#onStop()
		   */
		@Override
		protected void onStop() {
			
			if (timerUpdateHandler != null)
			{
				timerUpdateHandler.removeCallbacks(timerUpdateTask);
			}
	
			// ����ҳ���Set���Ƴ�
			MainViewActivity.removeFromSet( this );
			super.onStop();
		}


		/**
		 * (�� Javadoc,��д�ķ���) 
		 * @Title: onRestart
		 * @Description:  
		 * @see android.app.Activity#onRestart()
		 */
		@Override
		protected void onRestart() {
			super.onRestart();
		}


		/**
		 * (�� Javadoc,��д�ķ���) 
		 * @Title: onDestroy
		 * @Description: ����ҳ�����,�ͷ���Դ
		 * @see android.app.Activity#onDestroy()
		 */
		 @Override
		    public void onDestroy() {

		        if (mTts != null) {
		            mTts.stop();
		            mTts.shutdown();
		        }

		        super.onDestroy();
		    }

}
