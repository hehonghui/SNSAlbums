package albums;

import imageEdit.PictureEditActivity;

import network.Base64;
import network.HttpThread;

import album.entry.LoginActivity;
import album.entry.R;


/**
 * @ClassName: LocalImageView 
 * @Description:  ���������Ƭ��ͼ��Activity
 * @Author: Mr.Simple (�κ��)
 * @E-mail: bboyfeiyu@gmail.com 
 * @Date 2012-11-17 ����6:34:11 
 *
 */

public class LocalImageView extends Activity implements OnGestureListener{
	
	private String[] albumArray;			// User albumArray
	private String choosedAlbum;			// �ϴ�ʱѡ�е����
	private int currentNum;					// ��ǰͼƬ�����
	private String photoPath;				// ��Ƭ��·��
	private String AlbumName;
	private Bitmap currentBitmap;			// ��ǰBitmap
	private MyHandler mHandler = null ;		// UI�߳��е� MyHandler
	private Thread uploadThread = null;		// �ϴ���Ƭ�߳�
	private GridView menuGrid;				// GridView
	private PopupWindow menuPopupWindow;	// popupwindow
	private PopupWindow albumPopupWindow;	// popupwindow
	private int[] menu_image_array = { android.R.drawable.ic_menu_upload,
										android.R.drawable.ic_menu_edit, 
										android.R.drawable.ic_menu_delete,
										android.R.drawable.ic_menu_close_clear_cancel};	// popupwindow�а�ťͼƬ
	private String[] menu_name_array = { "�ϴ�","�༭", "ɾ��","ȡ��" };						// popupwindow�а�ť���
	private ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();	// �����ݷ��͵�������
	private ProgressDialog mProgressDialog;												// ��½ʱ�Ľ����
	
	public ViewFlipper flipper;					// ʵ�ֶ����Ŀؼ�
	private ArrayList<ImageView> ImageViewList;	// 3��ImageView��List
	private int middleView;						// �м��Ǹ�View����ʵ���
	private int lastView;						// ����Ǹ�View����ʵ���
	private int firstView;
	private boolean LeftIsTrue = false;			// ��������󻬣���Ϊtrue������Ϊfalse
	private boolean once = true;				// �жϵ�һ�λ���
	private int photoCount = 0;					// ��������Ƭ����
	private GestureDetector detector;			// ���ƻ����ļ����
	SoftReference<Bitmap> bitmapcache;			// ������
	private boolean touchEnable = true;			// �ж��Ƿ�Դ���������Ӧ
	private Handler handler;					// ��ʱ��
	private Bitmap[] BitmapList;
	
	private ImageButton titleButtonReturn;		// ���ذ�ť,���ϲ��toolButton
	private ImageButton titleButtonShare = null;// ���?ť
	private ImageButton titleButtonDelete;		// ɾ��ť
	private TextView titleTextView;				// �����Զ����title
	private FrameLayout titleParent;
	private Boolean titleVisible = false;		// �����Ƿ�ɼ�

	/**
	 * (�� Javadoc,��д�ķ���) 
	 * @Title: onCreate
	 * @Description:  ����Activity�����Ҹ��ֳ�ʼ��
	 * @param savedInstanceState 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		//�����ޱ���  
        requestWindowFeature(Window.FEATURE_NO_TITLE);  
        //����ȫ��  
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,   
                WindowManager.LayoutParams.FLAG_FULLSCREEN); 
        
		setContentView(R.layout.photo_viewfilpper);
		
		// ��ȡintent��Ϣ,ͨ��Intent���ݵ����
		Intent intent = getIntent();					
		currentNum = intent.getIntExtra("currentpic", 0);
		albumArray = intent.getStringArrayExtra("albumArray");
		photoPath = intent.getStringExtra("photopath");
		AlbumName = intent.getStringExtra("AlbumName");
		
		detector = new GestureDetector(this);					
		flipper = (ViewFlipper)findViewById(R.id.ViewFlipper);	
		
		try {
			// ��ʼ����������ͱ���
			initCompenents() ;
		} catch (Exception e) {
			e.printStackTrace();
		}
				

	}	
	

	/**
	 * @Method: initCompenents
	 * @Description: ��ʼ����������ͱ���
	 */
	private void initCompenents() throws Exception {

		titleButtonReturn = (ImageButton) findViewById(R.id.upfilpperbutton1);
		titleButtonShare = (ImageButton) findViewById(R.id.upfilpperbutton3);
		titleButtonDelete = (ImageButton) findViewById(R.id.upfilpperbutton2);
		titleButtonDelete.setBackgroundColor(Color.TRANSPARENT);
		titleTextView = (TextView) findViewById(R.id.upfilppertextview);
		titleTextView.setText(PhotoAlbumActivity.AlbumsFloderTitle.get(
				AlbumName).get(currentNum));
		titleParent = (FrameLayout) findViewById(R.id.upfilpperparent);
		titleParent.setVisibility(View.INVISIBLE);

		// ������һ��Activity
		titleButtonReturn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		// ɾ��ǰ��Ƭ
		titleButtonDelete.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				sureToDelete();
			}
		});
		
		// ���?ǰ��Ƭ
		titleButtonShare.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				shareToWeiBo(PhotoAlbumActivity.AlbumsFloderPath.get(AlbumName)
						.get(currentNum));
			}
		});

		photoCount = PhotoAlbumActivity.AlbumsFloderPath.get(AlbumName).size();
		middleView = 1;
		lastView = 2;
		firstView = 0;

		BitmapList = new Bitmap[3];

		ImageViewList = new ArrayList<ImageView>();
		for (int i = 0; i < 3; i++)
			ImageViewList.add(new ImageView(this));

		BitmapList[0] = decodeBitmap(
				PhotoAlbumActivity.AlbumsFloderPath.get(AlbumName).get(
						currentNum), 400);
		ImageViewList.get(0).setImageBitmap(BitmapList[0]);

		if (currentNum == photoCount - 1)
			BitmapList[1] = decodeBitmap(PhotoAlbumActivity.AlbumsFloderPath
					.get(AlbumName).get(0), 400);
		else
			BitmapList[1] = decodeBitmap(PhotoAlbumActivity.AlbumsFloderPath
					.get(AlbumName).get(currentNum + 1), 400);
		ImageViewList.get(1).setImageBitmap(BitmapList[1]);

		if (currentNum == 0)
			BitmapList[2] = decodeBitmap(PhotoAlbumActivity.AlbumsFloderPath
					.get(AlbumName).get(photoCount - 1), 400);
		else
			BitmapList[2] = decodeBitmap(PhotoAlbumActivity.AlbumsFloderPath
					.get(AlbumName).get(currentNum - 1), 400);
		ImageViewList.get(2).setImageBitmap(BitmapList[2]);

		flipper.addView(ImageViewList.get(0));
		flipper.addView(ImageViewList.get(1));
		flipper.addView(ImageViewList.get(2));

		System.gc();
		VMRuntime.getRuntime().setTargetHeapUtilization(0.75f);

		handler = new Handler();
	}
	
	
	/**
	 *  ��ʱ��
	 */
	Runnable runnable=new Runnable(){
		@Override
		public void run() {
			touchEnable = true;
			handler.removeCallbacks(runnable);
		}
	};
	

	/**
	 * @ClassName: MyHandler 
	 * @Description:  �ڲ���  MyHandler �����ϴ�ͼƬ�ĳɹ����
	 * @Author: xxjgood
	 * @E-mail: bboyfeiyu@gmail.com 
	 * @Date 2012-11-17 ����6:35:13 
	 *
	 */
	private class MyHandler extends Handler{      
        public MyHandler(Looper looper){
               super (looper);
        }
        @Override
        public void handleMessage(Message msg) { 
        	String s = msg.obj.toString();
        	if(s.equals("success")){
        		Toast.makeText(LocalImageView.this, " ��ϲ��ɹ��ϴ�ͼƬ", 0).show();
        		// ͼ���ϴ��ɹ�,������б���Ҫˢ��,���ñ�ʶ��
        		PhotoAlbumActivity.bRefresh = true;
        	}
        	else if(s.equals("fail")){
        		Toast.makeText(LocalImageView.this, "�ϴ�ͼƬʧ�ܣ�����", 0).show();
        	}
        	else
        		Toast.makeText(LocalImageView.this, "������", 0).show();	
        	
        	mProgressDialog.dismiss();
        } 
        
	}
	

	/**
	 *  Run ������ �ϴ���Ƭ
	 */
	Runnable uploadRunnable = new Runnable()							
	{
		String s = null;	// �����Ҫ���͵�msg
		@Override
		public void run()
		{
			packData();										// ������
			HttpThread h = new HttpThread(nameValuePairs,6);// 6--�ϴ�
			Log.d("�ϴ�","shangchuan");
			s = (String)h.sendInfo();						// ��ȡs
			sendMessage();									// ����msg
		}
		
		public void sendMessage(){	
			nameValuePairs.clear();
            Looper mainLooper = Looper.getMainLooper ();	// �õ����߳�loop
            mHandler = new MyHandler(mainLooper);			// �������̵߳�handler
            mHandler.removeMessages(0);						// �Ƴ����ж����е���Ϣ
            Message m = mHandler.obtainMessage(1, 1, 1, s);	// ����Ϣ����message
            mHandler.sendMessage(m);						// ����message
		}
	};
	

	/**
	 * @Method: packData
	 * @Description:  ���Ҫ���͵����
	 */
	private void packData(){
		
		currentBitmap = BitmapList[firstView];
		ByteArrayOutputStream stream1 = new ByteArrayOutputStream();	// stream1
		currentBitmap.compress(Bitmap.CompressFormat.PNG, 1, stream1); 	// compress to which format you want
		byte[] byte_arr = stream1.toByteArray();						// stream1 to byte
		try {
			stream1.close();
			stream1 = null;
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		String image_str = Base64.encodeBytes(byte_arr);				// byte to string
		
		Bitmap smallcurrentBitmap;
		float wRatio = (float) (currentBitmap.getWidth()/100.0);
		float hRatio = (float) (currentBitmap.getHeight()/100.0);
		if(wRatio > 1 && hRatio > 1) 									//����ָ����С������С��Ӧ�ı��� 
		{
			float scaleTemp;
			if(wRatio>hRatio)
				scaleTemp = hRatio;
			else
				scaleTemp = wRatio;
			wRatio = currentBitmap.getWidth()/scaleTemp;
			hRatio = currentBitmap.getHeight()/scaleTemp;
			int h = (int)wRatio;
			int w = (int)hRatio;
    		smallcurrentBitmap = Bitmap.createScaledBitmap(currentBitmap, h, w, false); 
		}
		else
			smallcurrentBitmap = currentBitmap;
		
		ByteArrayOutputStream stream2 = new ByteArrayOutputStream();						// stream2
		smallcurrentBitmap.compress(Bitmap.CompressFormat.PNG, 50, stream2);				// compress to which format you want
		smallcurrentBitmap.recycle();
		byte_arr = stream2.toByteArray();													// stream2 to byte
		try {
			stream2.close();
			stream2 = null;
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		String smallimage_str = Base64.encodeBytes(byte_arr);								// byte to string
		
		nameValuePairs.add(new BasicNameValuePair("protocol","upload"));					// ��װ��ֵ��
		nameValuePairs.add(new BasicNameValuePair("id", LoginActivity.mineID));
		nameValuePairs.add(new BasicNameValuePair("albumName", choosedAlbum));			
		nameValuePairs.add(new BasicNameValuePair("imageName", 
				PhotoAlbumActivity.AlbumsFloderName.get(AlbumName).get(currentNum)));
		Log.d("debug",PhotoAlbumActivity.AlbumsFloderName.get(AlbumName).get(currentNum));
		nameValuePairs.add(new BasicNameValuePair("image", image_str));
		nameValuePairs.add(new BasicNameValuePair("smallImage", smallimage_str));
		
		System.gc();
	}
	
	/**
	 * @Method: shareToWeiBo
	 * @Description: ����ͼƬ��΢��
	 * @param path   ͼƬ��·��
	 */
	private void shareToWeiBo(String path){
		Log.d("���?΢�� -ͼƬ��ַ : ", path);
		Intent intent = new Intent(Intent.ACTION_SEND);
		// imagePath:����·����Ҫ���ļ���չ��  
		String url = "file:///" + path;  				
		intent.putExtra(Intent.EXTRA_STREAM, Uri.parse(url));
		intent.setType("image/jpeg");
		intent.putExtra(Intent.EXTRA_TITLE,"���?΢��");
		intent.putExtra(Intent.EXTRA_STREAM,Uri.parse(url));        
		startActivity(Intent.createChooser(intent, "���?ʽ"));
	}

	
	/**
	 * @Method: showProgress
	 * @Description:  ��½ʱ�������ʾ.
	 */
	private void showProgress(){
		mProgressDialog = new ProgressDialog( this );
		mProgressDialog.setIcon(R.drawable.l_cn_48);
		mProgressDialog.setTitle("�ϴ���,���Ժ�...");
		mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		
		mProgressDialog.show();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

    	menu.add("menu");											// ���봴��һ��
		return super.onCreateOptionsMenu(menu);
	}
	@Override
    public boolean onMenuOpened(int featureId, Menu menu) {

    	openPopupwin();
    	return false;												// ����Ϊtrue ����ʾϵͳmenu   
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
		
		// ѭ�� ���������ӦͼƬ����HashMap
		for (int i = 0; i < menuNameArray.length; i++) {					
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("itemImage", menuImageArray[i]);
			map.put("itemText", menuNameArray[i]);
			data.add(map);
		}
		
		// ����������
		SimpleAdapter simperAdapter = new SimpleAdapter(this, data,			
				R.layout.item_menu, new String[] { "itemImage", "itemText" },
				new int[] { R.id.item_image, R.id.item_text });
		
		// ����һ��������
		return simperAdapter;												
	}
	
	
	/**
	 * @Method: openPopupwin
	 * @Description:  popupwindow�����ü���Ӧ
	 */
	private void openPopupwin() {
		
		LayoutInflater mLayoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		ViewGroup menuView = (ViewGroup) mLayoutInflater.inflate(R.layout.gridview_popx, null, true);
		menuGrid = (GridView) menuView.findViewById(R.id.popgridview);
		menuGrid.setAdapter(getMenuAdapter(menu_name_array, menu_image_array));
		// menuGrid�����ò���
		menuGrid.requestFocus();													
		
		// ����popupwindow���������Ϣ
		menuGrid.setOnItemClickListener(new OnItemClickListener() {  			
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,long arg3) {
				menuPopupWindow.dismiss();
				if(arg2 == 0){		// �ϴ�
					albumPopupwin();
				}
				else if(arg2 == 1)	// �༭
				{
					Intent intent = new Intent(LocalImageView.this,PictureEditActivity.class);
					intent.putExtra("photopath", photoPath);
					startActivity(intent);
				}
				else if(arg2 == 2)	// ɾ��
				{
					sureToDelete();
				}

			}
		});
		
		// ���㵽��gridview�ϣ�������Ҫ����˴��ļ����¼����������ֲ���Ӧ�����¼������
		menuGrid.setOnKeyListener(new OnKeyListener() {
					@Override
					public boolean onKey(View v, int keyCode, KeyEvent event) {
						switch (keyCode) {
						case KeyEvent.KEYCODE_MENU:
							if (menuPopupWindow != null && menuPopupWindow.isShowing()) {
								menuPopupWindow.dismiss();
							}
							break;
						}
						System.out.println("menuGridfdsfdsfdfd");
						return true;
					}
				});
		
		// ��ʾpopupwindow
		menuPopupWindow = new PopupWindow(menuView, LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT, true);
		menuPopupWindow.setBackgroundDrawable(new BitmapDrawable());
		menuPopupWindow.setAnimationStyle(R.style.PopupAnimation);
		menuPopupWindow.showAtLocation(findViewById(R.id.filpperparent), Gravity.BOTTOM | Gravity.BOTTOM, 0, 0);
		menuPopupWindow.update();
	}
	

	/**
	 * @Method: sureToDelete
	 * @Description:  ɾ����Ƭǰ��ȷ�϶Ի���
	 */
	private void sureToDelete(){
		new AlertDialog.Builder(this)
		.setIcon(R.drawable.beaten)
		.setTitle("ȷ��ɾ��")
		.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {

				try {
					// ɾ����Ƭ
					deleteCurrentPhoto() ;
				} catch (Exception e) {
					Toast.makeText(LocalImageView.this, "ɾ�����...", 0).show() ;
					e.printStackTrace();
				}
			}
		})
		.setNegativeButton("ȡ��", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
			}
		}).create().show();
		
	}
	
	
	/**
	 * @Method: deleteCurrentPhoto
	 * @Description: ɾ��ǰ����Ƭ
	 */
	private void deleteCurrentPhoto() throws Exception
	{
		// �����ݿ��е�ID�Ż�ȡURI
		Uri uri = ContentUris.withAppendedId(Media.EXTERNAL_CONTENT_URI, 
				PhotoAlbumActivity.AlbumsFloderID.get(AlbumName).get(currentNum));
		// ��URIɾ��ͼƬ
		getContentResolver().delete(uri, null,null); 
		
		// ����adaper
		InLocalSmallPhotoActivity.localGridAdapter.mImageList.remove(currentNum);
		InLocalSmallPhotoActivity.localGridAdapter.notifyDataSetChanged();
		
		if(photoCount == 1)
		{
			;
		}
		else if(photoCount == 2)
		{
			if(currentNum == 0)
			{
				BitmapList[firstView] = decodeBitmap(
						PhotoAlbumActivity.AlbumsFloderPath.get(AlbumName).get(1), 400);
				BitmapList[middleView] = decodeBitmap(
						PhotoAlbumActivity.AlbumsFloderPath.get(AlbumName).get(1), 400);
				BitmapList[lastView] = decodeBitmap(
						PhotoAlbumActivity.AlbumsFloderPath.get(AlbumName).get(1), 400);
			}
			else
			{
				BitmapList[firstView] = decodeBitmap(
						PhotoAlbumActivity.AlbumsFloderPath.get(AlbumName).get(0), 400);
				BitmapList[middleView] = decodeBitmap(
						PhotoAlbumActivity.AlbumsFloderPath.get(AlbumName).get(0), 400);
				BitmapList[lastView] = decodeBitmap(
						PhotoAlbumActivity.AlbumsFloderPath.get(AlbumName).get(0), 400);
			}
		}
		else
		{
			// ����flipper��������
			if(currentNum == photoCount-1)
			{
				BitmapList[firstView] = decodeBitmap(
						PhotoAlbumActivity.AlbumsFloderPath.get(AlbumName).get(0), 400);
				BitmapList[middleView] = decodeBitmap(
						PhotoAlbumActivity.AlbumsFloderPath.get(AlbumName).get(1), 400);
			}
			else if(currentNum == photoCount-2)
			{
				BitmapList[firstView] = decodeBitmap(
						PhotoAlbumActivity.AlbumsFloderPath.get(AlbumName).get(currentNum+1), 400);
				BitmapList[middleView] = decodeBitmap(
						PhotoAlbumActivity.AlbumsFloderPath.get(AlbumName).get(0), 400);
			}
			else
			{
				BitmapList[firstView] = decodeBitmap(
						PhotoAlbumActivity.AlbumsFloderPath.get(AlbumName).get(currentNum+1), 400);
				BitmapList[middleView] = decodeBitmap(
						PhotoAlbumActivity.AlbumsFloderPath.get(AlbumName).get(currentNum+2), 400);
			}
			if(currentNum == 0)
				BitmapList[lastView] = decodeBitmap(
						PhotoAlbumActivity.AlbumsFloderPath.get(AlbumName).get(photoCount-1), 400);
			else
				BitmapList[lastView] = decodeBitmap(
						PhotoAlbumActivity.AlbumsFloderPath.get(AlbumName).get(currentNum-1), 400);
		}
		
		
		// �����ڴ��е����
		PhotoAlbumActivity.AlbumsFloderID.get(AlbumName).remove(currentNum);
		PhotoAlbumActivity.AlbumsFloderName.get(AlbumName).remove(currentNum);
		PhotoAlbumActivity.AlbumsFloderPath.get(AlbumName).remove(currentNum);
		PhotoAlbumActivity.AlbumsFloderTime.get(AlbumName).remove(currentNum);
		PhotoAlbumActivity.AlbumsFloderTitle.get(AlbumName).remove(currentNum);
		
		// �л�����һ��ͼƬ
		ImageViewList.get(firstView).destroyDrawingCache();
		ImageViewList.get(middleView).destroyDrawingCache();
		ImageViewList.get(firstView).setImageBitmap(BitmapList[firstView]);
		ImageViewList.get(middleView).setImageBitmap(BitmapList[lastView]);
		
		photoCount--;
		once = true;
		if(photoCount == 0)
			finish();
		else
			titleTextView.setText(PhotoAlbumActivity.AlbumsFloderTitle.get(AlbumName).get(currentNum));
	}

	
	/**
	 * @Method: albumPopupwin
	 * @Description: menuPopupwindow�����ü���Ӧ
	 */
	private void albumPopupwin(){
		
    	ListView popList;
    	ImageView popImage;
    	SimpleAdapter adapter;			
    	List<Map<String, Object>> foldersList = new ArrayList<Map<String, Object>>();
    	
		LayoutInflater mLayoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		ViewGroup menuView = (ViewGroup) mLayoutInflater.inflate(R.layout.listview_pop, null, true);
		popList = (ListView)menuView.findViewById(R.id.poplistview);
		popImage = (ImageView)menuView.findViewById(R.id.poplistviewimage);
		// popwindow �ı���
		popImage.setImageDrawable(getResources().getDrawable(R.drawable.pop_titleimg));
		
		// ��������ʽ 
		adapter = new SimpleAdapter(this, foldersList, R.layout.smallinsidelistview,
				new String[]{"albumName","picnum","img"},				
				new int[]{R.id.smalluserName,R.id.smallipInfo,R.id.smalluserImg});	
		popList.setAdapter(adapter);
		
		foldersList.clear();
		
    	for (int i = 1; i < albumArray.length; i+=2) {
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("albumName", albumArray[i]);
			map.put("picnum", albumArray[i+1] + "��");
			map.put("img", R.drawable.folder);
			foldersList.add(map);
			adapter.notifyDataSetChanged();
		}
		
		popList.requestFocus();
		popList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,long arg3) {
				Log.d("debug",albumArray.length + "");
				choosedAlbum = albumArray[arg2*2+1];
				uploadThread = new Thread( uploadRunnable );// ����һ�����߳�
        		uploadThread.start();
				albumPopupWindow.dismiss();	// ���ѡ���
				showProgress();				// �����
			}
		});
		popList.setOnKeyListener(new OnKeyListener() {// ���㵽��gridview�ϣ�������Ҫ����˴��ļ����¼����������ֲ���Ӧ�����¼������
					@Override
					public boolean onKey(View v, int keyCode, KeyEvent event) {
						switch (keyCode) {
						case KeyEvent.KEYCODE_MENU:
							if (albumPopupWindow != null && albumPopupWindow.isShowing()) {
								albumPopupWindow.dismiss();
							}
							break;
						}
						System.out.println("menuGridfdsfdsfdfd");
						return true;
					}
				});
		
		int poplength = (albumArray.length - 1)*53 + 65;
		Log.d("10",albumArray.length + "");
		if(poplength > 595)
			poplength = 595;
		albumPopupWindow = new PopupWindow(menuView, 300,poplength, true);
		albumPopupWindow.setBackgroundDrawable(new BitmapDrawable());
		albumPopupWindow.setAnimationStyle(R.style.PopupAnimation);
		albumPopupWindow.showAtLocation(findViewById(R.id.filpperparent), Gravity.CENTER, 0, 0);
		albumPopupWindow.update();
		
	}
	
	
	/**
	 * @Method: decodeBitmap
	 * @Description: ��ȡpath·���µ�ͼƬ��������
	 * @param path
	 * @param rect
	 * @return
	 */
	Bitmap decodeBitmap(String path,int rect){ 
    	BitmapFactory.Options op = new BitmapFactory.Options(); 
    	op.inJustDecodeBounds = true; 
    	op.inPreferredConfig = Bitmap.Config.ALPHA_8;
    	Bitmap bmp = BitmapFactory.decodeFile(path, op);
    	//��ȡ�����С									 
    	int wRatio = (int)Math.ceil(op.outWidth/rect); 		
    	int hRatio = (int)Math.ceil(op.outHeight/rect); 
    	
    	//����ָ����С������С��Ӧ�ı��� 
    	if(wRatio > 1 && hRatio > 1){ 			
    		if(wRatio > hRatio){ 
    			op.inSampleSize = wRatio; 
    		}else{ 
    			op.inSampleSize = hRatio; 
    		} 
    	} 
    	op.inPreferredConfig = Bitmap.Config.ALPHA_8;
    	op.inJustDecodeBounds = false; 
    	bmp = BitmapFactory.decodeFile(path, op); 
    	return bmp; 
    }
	
	
	/**
	 * (�� Javadoc,��д�ķ���) 
	 * @Title: onFling
	 * @Description:  �����л�ͼ
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
		if(!touchEnable)
			return false;
		touchEnable = false;
		handler.postDelayed(runnable, 500);
		
		try{
			if (e1.getX() - e2.getX() > 120) {
				// ��һ�β�ִ�У��Ժ�ÿ�ζ�ִ��
				if(!once)
				{
					if(BitmapList[middleView].isRecycled()==false) //���û�л���  
						BitmapList[middleView].recycle(); 
					System.gc();
					
					// �ϴ������󻬶�
					if(LeftIsTrue)
					{
						if(currentNum == photoCount-1)
							BitmapList[middleView] = decodeBitmap(PhotoAlbumActivity.AlbumsFloderPath.get(AlbumName).get(0), 400);
						else
							BitmapList[middleView] = decodeBitmap(PhotoAlbumActivity.AlbumsFloderPath.get(AlbumName).get(currentNum+1), 400);
						
					}
					// �ϴ����һ���
					else
					{
						if(currentNum == 0)
							BitmapList[middleView] = decodeBitmap(PhotoAlbumActivity.AlbumsFloderPath.get(AlbumName).get(photoCount-1), 400);
						else
							BitmapList[middleView] = decodeBitmap(PhotoAlbumActivity.AlbumsFloderPath.get(AlbumName).get(currentNum-1), 400);
					}
					ImageViewList.get(middleView).destroyDrawingCache();
					ImageViewList.get(middleView).setImageBitmap(BitmapList[middleView]);
					
				}
				
				// ���������¼��ǰ״̬
				if(firstView == 2)
					firstView = 0;
				else
					firstView++;
				
				if(middleView == 2)
					middleView = 0;
				else
					middleView++;
				
				if(lastView == 2)
					lastView = 0;
				else
					lastView++;
				
				if(currentNum == photoCount-1)
					currentNum = 0;
				else
					currentNum ++;
				
				once = false;
				LeftIsTrue = true;
	
				flipper.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.push_left_in));
				flipper.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.push_left_out));
				flipper.showNext();
				
			} else if (e1.getX() - e2.getX() < -120) {
				if(!once)
				{	
					if(BitmapList[lastView].isRecycled()==false) //���û�л���  
						BitmapList[lastView].recycle();  
					System.gc();
					
					if(LeftIsTrue)
					{
						if(currentNum == photoCount-1)
							BitmapList[lastView] = decodeBitmap(PhotoAlbumActivity.AlbumsFloderPath.get(AlbumName).get(0), 400);
						else
							BitmapList[lastView] = decodeBitmap(PhotoAlbumActivity.AlbumsFloderPath.get(AlbumName).get(currentNum+1), 400);
						
					}
					else
					{
						if(currentNum == 0)
							BitmapList[lastView] = decodeBitmap(PhotoAlbumActivity.AlbumsFloderPath.get(AlbumName).get(photoCount-1), 400);
						else
							BitmapList[lastView] = decodeBitmap(PhotoAlbumActivity.AlbumsFloderPath.get(AlbumName).get(currentNum-1), 400);
					}
	
					ImageViewList.get(lastView).destroyDrawingCache();
					ImageViewList.get(lastView).setImageBitmap(BitmapList[lastView]);
				}
				
				if(firstView == 0)
					firstView = 2;
				else
					firstView--;
				
				if(middleView == 0)
					middleView = 2;
				else
					middleView--;
				
				if(lastView == 0)
					lastView = 2;
				else
					lastView--;
				
				if(currentNum == 0)
					currentNum = photoCount-1;
				else
					currentNum --;
				
				LeftIsTrue = false;
				once = false;
				
				flipper.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.push_right_in));
				flipper.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.push_right_out));
				flipper.showPrevious();
				
			
			}
			
			titleTextView.setText(PhotoAlbumActivity.AlbumsFloderTitle.get(AlbumName).get(currentNum));
		}catch(Exception e){
			e.printStackTrace() ;
		}
		
		return false;
	}

	
	/**
	 * (�� Javadoc,��д�ķ���) 
	 * @Title: onTouchEvent
	 * @Description: 
	 * @param event
	 * @return 
	 * @see android.app.Activity#onTouchEvent(android.view.MotionEvent)
	 */
	@Override
    public boolean onTouchEvent(MotionEvent event) {
		
    	return this.detector.onTouchEvent(event);
    }
    
	
	/**
	 * (�� Javadoc,��д�ķ���) 
	 * @Title: onDown
	 * @Description: 
	 * @param e
	 * @return 
	 * @see android.view.GestureDetector.OnGestureListener#onDown(android.view.MotionEvent)
	 */
    @Override
	public boolean onDown(MotionEvent e) {

		return false;
	}
	
    /**
     * (�� Javadoc,��д�ķ���) 
     * @Title: onLongPress
     * @Description: 
     * @param e 
     * @see android.view.GestureDetector.OnGestureListener#onLongPress(android.view.MotionEvent)
     */
	@Override
    public void onLongPress(MotionEvent e) {

    	
    }
    
	/**
	 * (�� Javadoc,��д�ķ���) 
	 * @Title: onScroll
	 * @Description: 
	 * @param e1
	 * @param e2
	 * @param distanceX
	 * @param distanceY
	 * @return 
	 * @see android.view.GestureDetector.OnGestureListener#onScroll(android.view.MotionEvent, android.view.MotionEvent, float, float)
	 */
    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
    		float distanceY) {

    	return false;
    }
    
    /**
     * (�� Javadoc,��д�ķ���) 
     * @Title: onShowPress
     * @Description: 
     * @param e 
     * @see android.view.GestureDetector.OnGestureListener#onShowPress(android.view.MotionEvent)
     */
    @Override
    public void onShowPress(MotionEvent e) {
    
    	
    }
    
    /**
     * (�� Javadoc,��д�ķ���) 
     * @Title: onSingleTapUp
     * @Description: 
     * @param e
     * @return 
     * @see android.view.GestureDetector.OnGestureListener#onSingleTapUp(android.view.MotionEvent)
     */
    @Override
    public boolean onSingleTapUp(MotionEvent e) {
    	
    	if(titleVisible)
    	{
    		titleVisible = false;
    		titleParent.setVisibility(View.VISIBLE);
    	}
    	else
    	{
    		titleVisible = true;
    		titleParent.setVisibility(View.INVISIBLE);
    	}
    	return false;
    }
}
