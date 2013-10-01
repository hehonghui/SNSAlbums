package albums;

import imageCache.ImageCacheToSDCard;

import network.HttpThread;

import album.entry.LoginActivity;
import album.entry.MyProgressDialog;
import album.entry.R;


/**
 * @ClassName: BigImageView 
 * @Description:  ��ʾ�������Ĵ�ͼ���������һ����鿴ͼƬ Activity
 * @Author: xxjgood
 * @E-mail: bboyfeiyu@gmail.com 
 * @Date 2012-11-17 ����5:44:48 
 *
 */
public class BigImageView extends Activity{
	
	private MyHandlerGetPhoto mHandlerGetPhoto = null ;	// �õ�һ��Bitmap��������Ϊ��ǰ�ɼ� setImageBitmap
	private MyHandlerDelPhoto mHandlerDelPhoto = null;	// ɾ��ͼƬ
	private Thread tGetPhoto = null;					// ��ȡͼƬ�߳�
	private Thread tDelPhoto = null;					// ɾ��ͼƬ�߳�
	private ImageView imageView = null;					// ImageView
	private String userId = null;
	private String username = null;						// User name
	private String albumname = null;					// User albumname
	private String[] photoArray = null;					// User photoArray
	private Bitmap currentPhoto = null; 				// Current Photo
	private int currentpic = 0;							// Ŀǰ��ʾ��photo���
	private GridView menuGrid;							// GridView
	private PopupWindow popupWindow;					// popupwindow
	private ImageButton titleButtonReturn;
	private ImageButton titleButtonDelete;
	private ImageButton titleButtonSave;
	private TextView titleTextView = null;
	private FrameLayout titleParent;
	private Boolean titleVisible = false;
	
	private int[] menu_image_array = { android.R.drawable.ic_menu_save ,
										android.R.drawable.ic_menu_delete, 
										android.R.drawable.ic_menu_close_clear_cancel};	// popupwindow�а�ťͼƬ
	private String[] menu_name_array = { "����","ɾ��","ȡ��"};							// popupwindow�а�ť���
	private ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(); // ���������������
	private MyProgressDialog loadingDialog;											// �ȴ��dialog
	private ImageCacheToSDCard mImageCache = ImageCacheToSDCard.getInstance() ;		// �����ʹ��

	
	/**
	 * (�� Javadoc,��д�ķ���) 
	 * @Title: onCreate
	 * @Description:   ҳ�洴������ʼ�������ص�һ����Ƭ
	 * @param savedInstanceState 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		//�����ޱ���  
        requestWindowFeature(Window.FEATURE_NO_TITLE); 
        //����ȫ��  
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,   
                WindowManager.LayoutParams.FLAG_FULLSCREEN); 
		setContentView(R.layout.bigimage);

		imageView = (ImageView)findViewById(R.id.bigimage1); 	
		imageView.setOnTouchListener(new OnTouchClick() ); 		// ����������Ӧ
		imageView.setLongClickable( true );
		
		// ����һ�����ȡ���
		Intent intent = getIntent();						
		userId = intent.getStringExtra("userid");
		username = intent.getStringExtra("username");
		albumname = intent.getStringExtra("albumname");
		photoArray = intent.getStringArrayExtra("photoArray");
		currentpic = intent.getIntExtra("currentpic", 0);
		
		// ��ʼ����������ͱ���
		initComponents() ;
		
		loadingDialog = new MyProgressDialog(BigImageView.this, "ͼƬ�����У����Ժ󡤡���");
		loadingDialog.show();

		// ���ڴ濨�ж�ȡ����
		Bitmap bmp = mImageCache.getImageFromSD(2, photoArray[currentpic]);
		try {
			Thread.sleep( 100 );
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		if (bmp.getWidth() != 5 && bmp.getHeight() != 5) {
			loadingDialog.dismiss();
			imageView.setImageBitmap(bmp);

		} else {
			tGetPhoto = new Thread(rGetPhoto); // ����һ�����߳� ��ȡһ�Ŵ�ͼ
			tGetPhoto.start();
		}
		
	}
	

	/**
	 * @Method: initComponents
	 * @Description: ��ʼ����������ͱ���
	 */
	private void initComponents(){
				
		titleButtonReturn = (ImageButton)findViewById(R.id.upbigbutton1);
		titleButtonDelete = (ImageButton)findViewById(R.id.upbigbutton2);
		titleButtonSave = (ImageButton)findViewById(R.id.upbigbutton3);
		// �����
		titleTextView = (TextView)findViewById(R.id.upbigtextview);
		titleTextView.setText(photoArray[currentpic]);
		titleParent = (FrameLayout)findViewById(R.id.upbigimageparent1);
		titleParent.setVisibility(View.INVISIBLE);

		// ���� 
		titleButtonReturn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		// ɾ��
		titleButtonDelete.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				deletePhoto();
			}
		});
		
		// ���� 
		titleButtonSave.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				savePhoto();
			}
		});
	}
	
	
	/**
	 * (�� Javadoc,��д�ķ���) 
	 * @Title: onCreateOptionsMenu
	 * @Description:   �����˵�
	 * @param menu
	 * @return 
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		
    	menu.add("menu");									// ���봴��һ��
		return super.onCreateOptionsMenu(menu);
	}
	

	/**
	 * (�� Javadoc,��д�ķ���) 
	 * @Title: onMenuOpened
	 * @Description: �˵����� 
	 * @param featureId
	 * @param menu
	 * @return 
	 * @see android.app.Activity#onMenuOpened(int, android.view.Menu)
	 */
	@Override
    public boolean onMenuOpened(int featureId, Menu menu) {
    	
    	openPopupwin();										// ��popupwindow
    	return false;										// ����Ϊtrue ����ʾϵͳmenu   
    }
	
	
	/**
	 * @Method: getMenuAdapter
	 * @Description: ���� popupwindow ������
	 * @param menuNameArray
	 * @param menuImageArray
	 * @return
	 */
	private ListAdapter getMenuAdapter(String[] menuNameArray,int[] menuImageArray) {
		ArrayList<HashMap<String, Object>> data = new ArrayList<HashMap<String, Object>>(); 
		
		for (int i = 0; i < menuNameArray.length; i++) {					// ѭ�� ���������ӦͼƬ����HashMap
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("itemImage", menuImageArray[i]);
			map.put("itemText", menuNameArray[i]);
			data.add(map);
		}
		SimpleAdapter simperAdapter = new SimpleAdapter(this, data,			// ����������
				R.layout.item_menu, new String[] { "itemImage", "itemText" },
				new int[] { R.id.item_image, R.id.item_text });
		return simperAdapter;												// ����һ��������
	}
	
	
	/**
	 * @Method: openPopupwin
	 * @Description: popupwindow�����ü���Ӧ
	 */
	private void openPopupwin() {
		
		LayoutInflater mLayoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		ViewGroup menuView = (ViewGroup) mLayoutInflater.inflate(R.layout.gridview_popx, null, true);
		menuGrid = (GridView) menuView.findViewById(R.id.popgridview);
		menuGrid.setAdapter(getMenuAdapter(menu_name_array, menu_image_array));
		// menuGrid�����ò���
		menuGrid.requestFocus();													
		
		// ��ʾpopupwindow
		popupWindow = new PopupWindow(menuView, LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT, true);
		popupWindow.setBackgroundDrawable(new BitmapDrawable());
		popupWindow.showAtLocation(findViewById(R.id.imageparent1), Gravity.BOTTOM | Gravity.BOTTOM, 0, 0);
		popupWindow.update();
		
		
		// ����popupwindow���������Ϣ
		menuGrid.setOnItemClickListener(new OnItemClickListener() {  			
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,long arg3) {
				// ����
				if(arg2 == 0){					
					try{
						android.provider.MediaStore.Images.Media.insertImage(getContentResolver(),currentPhoto,
							photoArray[currentpic],"onlinePhoto");		// ����Ƭ���浽������ݿ�
						Toast.makeText(BigImageView.this, "����ɹ�~~", 1).show();
					}catch(Exception e){
						Toast.makeText(BigImageView.this, e.toString(), 1).show();
					}
				}
				// ɾ��
				else if(arg2 == 1){				
					if(userId.equals(LoginActivity.mineID)){
						deletePhoto() ;
					}
					else{
						Toast.makeText(BigImageView.this, "��������ѵ���ᣬ��û��Ȩ��ɾ��", 1).show();
					}
				}
				// ȡ��
				else if (arg2 == 2) {			
					
				}
				popupWindow.dismiss();
			}
		});
		
		// ���㵽��gridview�ϣ�������Ҫ����˴��ļ����¼����������ֲ���Ӧ�����¼������
		menuGrid.setOnKeyListener(new OnKeyListener() {
					@Override
					public boolean onKey(View v, int keyCode, KeyEvent event) {
						switch (keyCode) {
						case KeyEvent.KEYCODE_MENU:
							if (popupWindow != null && popupWindow.isShowing()) {
								popupWindow.dismiss();
							}
							break;
						}
						System.out.println("menuGridfdsfdsfdfd");
						return true;
					}
				});
		

	}

	
	
	/**
	 * @Method: deletePhoto
	 * @Description:
	 */
	private void deletePhoto() {
		if (userId.equals(LoginActivity.mineID)) {
			// ȷ��ɾ��
			new AlertDialog.Builder(BigImageView.this)
					.setIcon(R.drawable.beaten)
					.setTitle("ȷ��ɾ��?")
					.setPositiveButton(R.string.alert_dialog_ok,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int whichButton) {
									loadingDialog.setTitle("ɾ����,���Ժ�...") ;
									loadingDialog.show() ;
									// ɾ��ͼ��
									tDelPhoto = new Thread(deleteRunnable);
									tDelPhoto.start();
								}
							})
					.setNegativeButton(R.string.alert_dialog_cancel,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int whichButton) {

								}
							}).show();

		} else {
			Toast.makeText(BigImageView.this, "��������ѵ���ᣬ��û��Ȩ��ɾ��", 1).show();
		}
	}

	
	/**
	 * @Method: savePhoto
	 * @Description: ����ͼƬ������
	 */
	private void savePhoto() {
		try {
			android.provider.MediaStore.Images.Media.insertImage(
					getContentResolver(), currentPhoto, photoArray[currentpic],
					"onlinePhoto"); // ����Ƭ���浽������ݿ�
			Toast.makeText(BigImageView.this, "����ɹ�~~", 1).show();
		} catch (Exception e) {
			Toast.makeText(BigImageView.this, e.toString(), 1).show();
		}
	}


	
	
	/**
	 * @ClassName: OnTouchClick 
	 * @Description:  �ڲ���  ��������
	 * @Author: xxjgood
	 * @E-mail: bboyfeiyu@gmail.com 
	 * @Date 2012-11-18 ����10:27:43 
	 *
	 */
class OnTouchClick implements OnTouchListener,OnGestureListener{
	// ���Ƽ���
	GestureDetector mGestureDetector = new GestureDetector(this); 	
	
	
	/**
	 * (�� Javadoc,��д�ķ���) 
	 * @Title: onFling
	 * @Description:  �����л�ͼ
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 * @param arg3
	 * @return 
	 * @see android.view.GestureDetector.OnGestureListener#onFling(android.view.MotionEvent, android.view.MotionEvent, float, float)
	 */
	@Override
	public boolean onFling(MotionEvent arg0, MotionEvent arg1, float arg2, float arg3) {
		
		Log.d("2", "Onfling");
		// ����������ٶ�
		final int FLING_MIN_DISTANCE = 100, FLING_MIN_VELOCITY = 200;  
	    if (arg0.getX() - arg1.getX() > FLING_MIN_DISTANCE && Math.abs(arg2) > FLING_MIN_VELOCITY) {  
	        // Fling left  
	        Log.d("3", "Fling left"); 
	        if(currentpic == photoArray.length-1)	// ������һ�ţ�����ת����һ��
	        	currentpic = 0;
	        else									// ��������һ��
	        	currentpic++;
	        	        
	    } else if (arg1.getX() - arg0.getX() > FLING_MIN_DISTANCE && Math.abs(arg2) > FLING_MIN_VELOCITY) {  
	        // Fling right  
	        Log.d("3", "Fling right"); 
	        if(currentpic == 0)
	        	currentpic = photoArray.length - 1;
	        else
	        	currentpic--;
	    }else{
	    	return false ;
	    }
	    imageView.setLongClickable(false);		// �رչرմ�����Ӧ

	    titleTextView.setText(photoArray[currentpic]);
	    // �ӻ�����������л�ȡ��ͼͼƬ
	    getImageFromCacheOrNet( currentpic ) ;
	    
	    return false;
	}

	
	/**
	 * (�� Javadoc,��д�ķ���) 
	 * @Title: onTouch
	 * @Description: 
	 * @param v
	 * @param event
	 * @return 
	 * @see android.view.View.OnTouchListener#onTouch(android.view.View, android.view.MotionEvent)
	 */
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		
		Log.d("2", "OnTouch");
		
	 	// ���ó���
		return mGestureDetector.onTouchEvent(event);
	}

	
	/**
	 * (�� Javadoc,��д�ķ���) 
	 * @Title: onDown
	 * @Description: 
	 * @param arg0
	 * @return 
	 * @see android.view.GestureDetector.OnGestureListener#onDown(android.view.MotionEvent)
	 */
	@Override
	public boolean onDown(MotionEvent arg0) {
		
		Log.d("2", "OnDown");
		return false;
	}
	
	
	/**
	 * (�� Javadoc,��д�ķ���) 
	 * @Title: onLongPress
	 * @Description: 
	 * @param arg0 
	 * @see android.view.GestureDetector.OnGestureListener#onLongPress(android.view.MotionEvent)
	 */
	@Override
	public void onLongPress(MotionEvent arg0) {
		
		Log.d("2", "Onlongpress");
	}

	/**
	 * (�� Javadoc,��д�ķ���) 
	 * @Title: onScroll
	 * @Description: 
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 * @param arg3
	 * @return 
	 * @see android.view.GestureDetector.OnGestureListener#onScroll(android.view.MotionEvent, android.view.MotionEvent, float, float)
	 */
	@Override
	public boolean onScroll(MotionEvent arg0, MotionEvent arg1, float arg2,
			float arg3) {
		
		Log.d("2", "Onscroll");
		return false;
	}

	/**
	 * (�� Javadoc,��д�ķ���) 
	 * @Title: onShowPress
	 * @Description: 
	 * @param arg0 
	 * @see android.view.GestureDetector.OnGestureListener#onShowPress(android.view.MotionEvent)
	 */
	@Override
	public void onShowPress(MotionEvent arg0) {
		
		Log.d("2", "Onshowpress");
	}

		/**
		 * (�� Javadoc,��д�ķ���)
		 * 
		 * @Title: onSingleTapUp
		 * @Description: �����¼�
		 * @param arg0
		 * @return
		 * @see android.view.GestureDetector.OnGestureListener#onSingleTapUp(android.view.MotionEvent)
		 */
		@Override
		public boolean onSingleTapUp(MotionEvent arg0) {

			Log.d("2", "Onsingletapup");
			if (titleVisible) {
				titleVisible = false;
				titleParent.setVisibility(View.VISIBLE);
			} else {
				titleVisible = true;
				titleParent.setVisibility(View.INVISIBLE);
			}
			return false;
		}

	} // end of inner class OnTouchClick



/**
 * @Method: getImageFromCacheOrNet
 * @Description:  �ӻ����ж�ȡͼƬ,������û�������������������ͼƬ
 */
private void getImageFromCacheOrNet( int curIndex ){
	 // �ӻ����ж�ȡ
    Bitmap bmp = null ;
    try {
    	// �ӻ����ж�ȡͼƬ
    	String filename = photoArray[curIndex] ;
    	bmp = mImageCache.getImageFromSD(2, filename) ;
    }catch(Exception e){
    	Log.d("������ȡ����", "������ȡ���������") ;
    	e.printStackTrace();
    }
    
    // �ӻ����ж�ȡ����ͼƬ,��͸߶���Ϊ5��Ϊ��ȡ������ͼƬ,�������������������ͼƬ
   if (bmp.getWidth() != 5 && bmp.getHeight() != 5){
	    
	   Animation anim = AnimationUtils.loadAnimation(BigImageView.this, 
			   				android.R.anim.fade_in) ;
	    anim.setInterpolator(new AccelerateDecelerateInterpolator()); 
	    anim.setDuration( 800 ); 
	    
	    imageView.startAnimation( anim ) ;
	    imageView.setImageBitmap(bmp);
	    imageView.setLongClickable(true);		// ����������Ӧ

   }else {  // ����û�и�ͼƬ�������߳�����
	    loadingDialog.show();
        tGetPhoto = new Thread(rGetPhoto);		// ���߳�����ͼƬ
		tGetPhoto.start();
   }
	
}


	/**
	 * @Method: packData
	 * @Description: ������
	 */
	private void packData()														
	{
		nameValuePairs.add(new BasicNameValuePair("protocol", "getImage"));	// ��װ��ֵ��
		nameValuePairs.add(new BasicNameValuePair("id", userId));
		nameValuePairs.add(new BasicNameValuePair("albumName", albumname));
		nameValuePairs.add(new BasicNameValuePair("imageName", photoArray[currentpic]));	
	}
	
	
	/**
	 * @ClassName: MyHandlerGetPhoto 
	 * @Description:  �ڲ���  MyHandlerGetPhoto �������߳���Ϣ
	 * @Author: xxjgood
	 * @E-mail: bboyfeiyu@gmail.com 
	 * @Date 2012-11-17 ����5:47:10 
	 *
	 */
	private class MyHandlerGetPhoto extends Handler{       
        public MyHandlerGetPhoto(Looper looper){
               super (looper);
        }
        @Override
        public void handleMessage(Message msg) { 									// ������Ϣ
        	String filename = photoArray[currentpic] ;
        	setTitle(username + "/" + albumname + "/" + filename); 	// ����title
        	currentPhoto = (Bitmap)msg.obj;				// ��ȡBitmap
        	imageView.setImageBitmap(currentPhoto);		// ��ʾBitmap
        	imageView.setLongClickable(true);			// ����������Ӧ
        	try {
        		// ������������ͼƬ���浽SD���л�������
				mImageCache.saveBmpToSd(currentPhoto, filename, 2) ;
			} catch (Exception e) {
				Log.d("��ͼ���", "��ͼд�뻺��ʧ��") ;
				e.printStackTrace();
			}
        	loadingDialog.dismiss();
        }            
	}

	
	/**
	 *  Run ���� ������һ�Ŵ�ͼ�����߳�
	 */
	Runnable rGetPhoto = new Runnable()								
	{
		Bitmap bitmap = null;
		@Override
		public void run()
		{
			Log.d("1", "new thread" + Thread.currentThread().getId());
			packData();
			HttpThread h = new HttpThread(nameValuePairs,101);
			bitmap = (Bitmap)h.sendInfo();
			sendMessage();
		}
		public void sendMessage(){										// �̼߳���ݴ���
            Looper mainLooper = Looper.getMainLooper ();				// �õ����߳�loop
            Bitmap msg ;
            mHandlerGetPhoto = new MyHandlerGetPhoto(mainLooper);		// �������̵߳�handler
            msg = bitmap;
            mHandlerGetPhoto.removeMessages(0);							// �Ƴ����ж����е���Ϣ
            Message m = mHandlerGetPhoto.obtainMessage(1, 1, 1, msg);	// ����Ϣ����message
            mHandlerGetPhoto.sendMessage(m);							// ����message
		}
	};

	
	/**
	 * @ClassName: MyHandlerDelPhoto 
	 * @Description:   ɾ��ͼ���handler
	 * @Author: Mr.Simple (�κ��)
	 * @E-mail: bboyfeiyu@gmail.com 
	 * @Date 2012-11-17 ����5:56:15 
	 *
	 */
	private class MyHandlerDelPhoto extends Handler{       
        public MyHandlerDelPhoto(Looper looper){
               super (looper);
        }
        
        /**
         * (�� Javadoc,��д�ķ���) 
         * @Title: handleMessage
         * @Description: 
         * @param msg 
         * @see android.os.Handler#handleMessage(android.os.Message)
         */
        @Override
        public void handleMessage(Message msg) {
        	String s = msg.obj.toString();
        	s = s.trim();	
        	if(s.equals("success")){
        		
        		Toast.makeText(BigImageView.this, "ɾ��ɹ���", 1).show();
         	   // ��mImageList�Ƴ��ͼƬ
         	   InPhotoAlbumActivity.netGridAdapter.mImageList.remove(currentpic);
         	   InPhotoAlbumActivity.netGridAdapter.mImageList.trimToSize();
         	   
         	   removeItemFromArray( currentpic ) ;
         	   
        		if(currentpic == photoArray.length - 1)	// ������һ�ţ�����ת����һ��
		        	currentpic = 0;
		        else									// ��������һ��
		        	currentpic++;
        		
        		imageView.setLongClickable(false);		// �رչرմ�����Ӧ
        		
        		loadingDialog.setTitle("ͼƬ������,���Ժ�...") ;
				loadingDialog.dismiss() ;
				
        		// ɾ��ͼƬ��,�ӻ����л��������������ȡ��һ�Ŵ�ͼͼƬ
        		getImageFromCacheOrNet( currentpic );
        		
        	   // ������1
        	   InPhotoAlbumActivity.photoCount--;	
        	  
        	}
        	else if(s.equals("fail")){
        		Toast.makeText(BigImageView.this, "ɾ��ʧ�ܣ�", 1).show();
        	}
        	else{
        		Toast.makeText(BigImageView.this, "������������������磡", 1).show();
        	}
        }            
	}
	
	/**
	 * @Method: removeItemFromArray
	 * @Description:
	 * @param index
	 */
	private void removeItemFromArray(int index){
		try{
			List<String> temp = new ArrayList<String>() ;
			for(String item : photoArray){
				temp.add( item );
				Log.d("", "ͼƬ :" + item) ;
			}
			
			Log.d("", "Ҫɾ���ͼƬ :" + photoArray[index]) ;
			temp.remove( photoArray[index] );
			
			photoArray = new String[temp.size()];
			for(int i=0; i<temp.size(); i++){
				photoArray[i] = temp.get(i) ;
				Log.d("ɾ���", temp.get(i)) ;
			}
		}catch(Exception e){
			e.printStackTrace() ;
		}
		
	}
	
	
	/**
	 * ɾ��ͼ����߳�
	 */
	Runnable deleteRunnable = new Runnable() {
		String msg;
		@Override
		public void run() {
			// ɾ��ͼ��
			ArrayList<NameValuePair> nameValuePair1 = new ArrayList<NameValuePair>();
			nameValuePair1.add(new BasicNameValuePair("protocol", "deleteImage"));
			nameValuePair1.add(new BasicNameValuePair("id", userId));
			nameValuePair1.add(new BasicNameValuePair("albumName", albumname));
			nameValuePair1.add(new BasicNameValuePair("imageName", photoArray[currentpic]));
			
			HttpThread h = new HttpThread(nameValuePair1, 15);
			msg = (String)h.sendInfo();
			sendMessage();
		}
		public void sendMessage(){							// �̼߳���ݴ���
			Looper mainLooper = Looper.getMainLooper ();	// �õ����߳�loop
            mHandlerDelPhoto = new MyHandlerDelPhoto(mainLooper);			// �������̵߳�handler
            mHandlerDelPhoto.removeMessages(0);								// �Ƴ����ж����е���Ϣ
            Message m = mHandlerDelPhoto.obtainMessage(1, 1, 1, msg);	// ����Ϣ����message
            mHandlerDelPhoto.sendMessage(m);								// ����message
		}
	};
}
