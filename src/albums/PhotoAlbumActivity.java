package albums;

import network.HttpThread;

import album.entry.LoginActivity;
import album.entry.MainViewActivity;
import album.entry.MyProgressDialog;
import album.entry.R;
import camera.CameraActivity;


/**
 * @ClassName: PhotoAlbumActivity 
 * @Description:  ��������������棬����Tab��Ĭ����������ᣬ��һ���Ǳ������
 * @Author: xxjgood
 * @E-mail: bboyfeiyu@gmail.com 
 * @Date 2012-11-17 ����6:42:44 
 *
 */

public class PhotoAlbumActivity extends TabActivity{
	
	private MyHandler1 mHandler1 = null;			// �����������
	private MyHandler2 mHandler2 = null;			// �½��������
	private MyHandler3 mHandler3 = null;			// ɾ�����
	private MyHandler4 mHandler4 = null;			// �޸����
	private Thread tNetAlbum = null;				// �������������߳�
	private Thread tNewAlbum = null;				// �½�����ִ���߳�
	private Thread tDeleteAlbum = null;				// ɾ�������߳�
	private Thread tModifyAlbum = null;				// �޸������߳�
	
	private TabHost myTabhost;     					// TabHost
	private TabWidget tabWidget;   					// ����tab����ʽ
	private TextView TabTV1,TabTV2;					// TabText
	private View TabView1,TabView2;					// TabView
	private ListView netAlbumListView,localAlbumListView = null;
	private SimpleAdapter tabNetAdapter,tabLocalAdapter;
	
	private ImageButton titleNetAddButton;
	private ImageButton titleLocalTakePhotoButton;
	private ImageButton titleNetRefreshButton;
	private ImageButton titleLocalRefreshButton;
	private TextView refreshTips = null ;
	private TextView localRefTips = null;
	
	private String[] albumArray = null;				// ����� ����
	private List<Map<String, Object>> netAlbumsList= new ArrayList<Map<String, Object>>();
	private List<Map<String, Object>> localAlbumsList= new ArrayList<Map<String, Object>>();
	
	public static List<String> AlbumsNameList = new ArrayList<String>();
	public static Map<String, List<String>> AlbumsFloderPath = new HashMap<String, List<String>>();	// �������ļ�
	public static Map<String, List<String>> AlbumsFloderName = new HashMap<String, List<String>>();	// �������ļ�������չ��
	public static Map<String, List<String>> AlbumsFloderTitle = new HashMap<String, List<String>>();	// �������ļ�
	public static Map<String, List<String>> AlbumsFloderTime = new HashMap<String, List<String>>();
	
	private Cursor cursor; 			// �α꣬��ý��������
	private int photoIndex; 		// �����������Ҫ��������Media.DATA,Media.TITLE,Media.DISPLAY_NAME������ţ�����ȡÿ�е���� 
	private int titleIndex; 
	private int nameIndex;	
	private int timeIndex;
	private int colorGray;
	private int colorBlue;
	private String ischecked;
	private String newAlbumName;
	private String ChooseAlbumName;
	private String ChooseAlbumNum;
	private int ChooseAlbumPosition;
	private String userId = "";					// Ҫ�鿴�ĺ�������ID�����Լ���ID
	private static boolean isReload = false;
	
	private MyProgressDialog loadingDialog ;	// �����������ʱ�Ľ������ʾ��
	public static boolean bRefresh = false;		// �Ƿ�����������������б�
	public static Map<String, List<Long>> AlbumsFloderID = new HashMap<String, List<Long>>();
	private int idIndex;
	
	
	/**
	 * (�� Javadoc,��д�ķ���) 
	 * @Title: onCreate
	 * @Description:  ҳ������
	 * @param savedInstanceState 
	 * @see android.app.ActivityGroup#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
	
		super.onCreate(savedInstanceState);
		//�����ޱ���  
        requestWindowFeature(Window.FEATURE_NO_TITLE); 
        
		userId = getIntent().getStringExtra("id");			// ��ȡ���ѵ�id,�鿴�������������
		if (userId == null)									// �����쳣�����ҳ��,��ID��Ϊ��ָ��
		{
			finish();
		}
		
		myTabhost=this.getTabHost();						// TabHost
		tabWidget = myTabhost.getTabWidget();				// TabWidget
		
    	loadingDialog = new MyProgressDialog(this,
    					"��������,���Ժ򡤡���") ;					// ��ʼ����������б�ʱ�Ľ������ʾ��	
    	
       LayoutInflater.from(this).inflate(
        		R.layout.tablistview, myTabhost.getTabContentView(), true);										// ��ȡview
        
        try {
        	// ����tabHost
			SetTabHost();
		} catch (Exception e1) {
			e1.printStackTrace();
		}										// ����TabHost
    	initAdapter();										// ��ʼ��ListView��������
    	// ��ʼ����������ͱ���
    	initComponents() ;
    	
    	// �Լ��鿴�Լ������ʱ
    	if(userId == LoginActivity.mineID){
    		try {
    			// ���������Լ�������б�
    			getMyNetAlbums();								
    		} catch (Exception e) {
    			Log.d("�������", e.toString());
    			Toast.makeText(PhotoAlbumActivity.this, "��,����б�����ʧ��,�������»�ȡ~~~", 0).show();
    			loadingDialog.show();
    			SetOnlineAlbum();
    		}
    	}
    	else{
    		loadingDialog.show() ;
    		SetOnlineAlbum();
    	}
        
        setTitle("���");
        
        if(userId.equals(LoginActivity.mineID)){
			try {
				SearchLocalAlbum();
			} catch (Exception e) {
				e.printStackTrace();
			}
        }
        
	}
	
	
	/**
	 * @Method: initAdapter
	 * @Description: ��������ʼ��
	 */
	private void initAdapter(){

		// userName: �û���  ,   Name ���ǳ�   , img : �û�ͷ��
		tabNetAdapter = new SimpleAdapter(this, netAlbumsList, R.layout.insidelistview,
				new String[]{"albumName","picnum","date","img1"},				// ��������ʽ 
				new int[]{R.id.userName2,R.id.ipInfo2,R.id.ipInfo3,R.id.userImg2});		// ����һ���������ݶ�Ӧ�������������������
		
		netAlbumListView = (ListView) findViewById(R.id.tab1listview);
		/*ʵ��ViewBinder()����ӿ�*/  
	    tabNetAdapter.setViewBinder(new ViewBinder() {  
	    	@Override  
	    	public boolean setViewValue(View view, Object data, String textRepresentation) {  
	    		if(view instanceof ImageView && data instanceof Bitmap){  
	    			ImageView i = (ImageView)view;  
	    			i.setImageBitmap((Bitmap) data);  
	    			return true;  
	    		}  
	    		return false;
	    	}
	    });
	    
		netAlbumListView.setAdapter( tabNetAdapter );
		tabNetAdapter.notifyDataSetChanged();
		
		tabLocalAdapter = new SimpleAdapter(this, localAlbumsList, R.layout.insidelistview,
				new String[]{"albumName","picnum","date","img1"},				// ��������ʽ 
				new int[]{R.id.userName2,R.id.ipInfo2,R.id.ipInfo3,R.id.userImg2});		// ����һ���������ݶ�Ӧ�������������������
		
		localAlbumListView = (ListView) findViewById(R.id.tab2listview);
		/*ʵ��ViewBinder()����ӿ� */ 
	    tabLocalAdapter.setViewBinder(new ViewBinder() {  
	    	@Override  
	    	public boolean setViewValue(View view, Object data, String textRepresentation) {  
	     
	    		if(view instanceof ImageView && data instanceof Bitmap){  
	    			ImageView i = (ImageView)view;  
	    			i.setImageBitmap((Bitmap) data);  
	    			return true;  
	    		}  
	    		return false;
	    	}
	    });
		localAlbumListView.setAdapter( tabLocalAdapter );
		tabLocalAdapter.notifyDataSetChanged();
		// ����ListView�Ķ���
	}
	
	
	/**
	 * @Method: initComponents
	 * @Description: ��ʼ����������ͱ���
	 */
	private void initComponents(){
		// ���������°�ť
    	titleNetRefreshButton = (ImageButton)findViewById(R.id.downnetlistbutton1);
    	titleNetRefreshButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				
				refreshTips.setVisibility(View.VISIBLE) ;
				refreshTips.setText("�б������...") ;
				// �����̸߳����б�
				new Thread(rNetAlbum).start();
			}
		});
    	
    	// ��������½���ť
    	titleNetAddButton = (ImageButton)findViewById(R.id.downnetlistbutton2);
    	titleNetAddButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				
				refreshTips.setVisibility(View.VISIBLE) ;
				refreshTips.setText("���������...") ;
				CreatAlbumDialog();
			}
		});
    	
    	// ���������°�ť
    	titleLocalRefreshButton = (ImageButton)findViewById(R.id.downlocallistbutton1);
    	titleLocalRefreshButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					localRefTips.setText("������������...") ;
					localRefTips.setVisibility(View.VISIBLE) ;
					// ��������б�
					SearchLocalAlbum();
					loadListAnimation(false);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
    	
    	// ���հ�ť
    	titleLocalTakePhotoButton = (ImageButton)findViewById(R.id.downlocallistbutton2);
    	titleLocalTakePhotoButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(PhotoAlbumActivity.this,CameraActivity.class);
				startActivity(intent);
			}
		});
    	
    	
    	refreshTips = (TextView)findViewById(R.id.refreshTips) ;
    	refreshTips.setVisibility(View.GONE) ;
    	
    	localRefTips = (TextView)findViewById(R.id.localRefreshTips) ;
    	localRefTips.setVisibility(View.GONE) ;

    	delayHandler = new Handler();
	}
	
	
	/**
	 * @Method: SetTabHost
	 * @Description: ����TabHost �޸������ʽ
	 */
	void SetTabHost() throws Exception{
		
		colorGray = this.getResources().getColor(R.drawable.gray);//�ı�������ɫ
        colorBlue = this.getResources().getColor(R.drawable.blue);
		
		myTabhost.addTab(myTabhost.newTabSpec("tab1")			// ����һ��Tab1
                .setIndicator("�������",getResources().getDrawable(R.drawable.world1))
                .setContent(R.id.tab1listviewl));
		tabWidget.getChildAt(0).getLayoutParams().height = 90;	// ����Tab�߶�
		TabTV1 = (TextView) tabWidget.getChildAt(0).findViewById(android.R.id.title);
        TabTV1.setTextColor(colorBlue);
        TabView1 = tabWidget.getChildAt(0);   					// ���ñ���
        TabView1.setBackgroundResource(R.drawable.backw);
        
        if(userId.equals(LoginActivity.mineID))
        {
	        myTabhost.addTab(myTabhost.newTabSpec("tab2")			// ����һ��Tab2
	                .setIndicator("�������",getResources().getDrawable(R.drawable.bank))
	                .setContent(R.id.tab2localalbum));
	        tabWidget.getChildAt(1).getLayoutParams().height = 90;	// ����Tab�߶�
	        TabTV2 = (TextView) tabWidget.getChildAt(1).findViewById(android.R.id.title);
	        TabTV2.setTextColor(colorGray);
	        TabView2 = tabWidget.getChildAt(1);						// ���ñ���
	        TabView2.setBackgroundResource(R.drawable.backb);
        }
        
        tabWidget.setStripEnabled(false);	 					// ȥ���±߰�ɫ
        setContentView(myTabhost);								// ��ʾTab

        if(userId.equals(LoginActivity.mineID))
        {
			myTabhost.setOnTabChangedListener(new OnTabChangeListener(){		// ����Tab�л���Ӧ
				
	            @Override
				public void onTabChanged(String tabId) {

	                if(tabId.equals("tab1")){									// �����tab1������ɫ���
	                	Log.d("3","tab1");
	                	TabView1.setBackgroundResource(R.drawable.backw);
	                	TabView2.setBackgroundResource(R.drawable.backb);
	                	
	                	ImageView imageView = (ImageView)tabWidget.getChildAt(0).findViewById(android.R.id.icon); 
	                    imageView.setImageDrawable(getResources().getDrawable(R.drawable.world1)); 
	                	imageView = (ImageView)tabWidget.getChildAt(1).findViewById(android.R.id.icon); 
	                    imageView.setImageDrawable(getResources().getDrawable(R.drawable.bank)); 
	                    
	                	TabTV1.setTextColor(colorBlue);
	                	TabTV2.setTextColor(colorGray);
	                	// �Ƿ���Ҫˢ������б�
	                	if ( bRefresh )	{        
	                		// ��ȡ����б���߳�
	                		new Thread(rNetAlbum).start();
	                		bRefresh = false;
	                	}
	                	loadListAnimation(true); 	// ListView����
	                }
	                else if(tabId.equals("tab2")){							// �����tab2������ɫ��ף����������̣߳����ر���ͼƬ
	                	Log.d("3","tab2");
	                	TabView2.setBackgroundResource(R.drawable.backw);
	                	TabView1.setBackgroundResource(R.drawable.backb);
	
	                	ImageView imageView = (ImageView)tabWidget.getChildAt(0).findViewById(android.R.id.icon); 
	                    imageView.setImageDrawable(getResources().getDrawable(R.drawable.world)); 
	                	imageView = (ImageView)tabWidget.getChildAt(1).findViewById(android.R.id.icon); 
	                    imageView.setImageDrawable(getResources().getDrawable(R.drawable.bank1)); 
	                	
	                	TabTV2.setTextColor(colorBlue);
	                	TabTV1.setTextColor(colorGray);
	                	
	                	if( isReload )										// �Ƿ����¼���ͼ��
	                	{
	                		//LocalAlbum();									// ���ñ������
	                		isReload = false;
	                		try {
								SearchLocalAlbum();
							} catch (Exception e) {
								e.printStackTrace();
							}
	                	}
	                	loadListAnimation(false); 	// ListView����
	                }
	            }           
	        });
        }// END IF
	}
	

	/**
	 * @Method: SearchLocalAlbum
	 * @Description: ���ұ���ͼ���·��
	 * @throws Exception
	 */
	void SearchLocalAlbum() throws Exception
	{
		String columns[] = new String[]{ 					// ָ����ȡ���� 
				MediaColumns.DATA,
				MediaColumns.DATE_MODIFIED,
 				BaseColumns._ID,
 				MediaColumns.TITLE,
 				MediaColumns.DISPLAY_NAME };
 
		cursor = getContentResolver().query(
				 Media.EXTERNAL_CONTENT_URI, 
				 columns, null, null, null); 
		idIndex = cursor.getColumnIndexOrThrow(BaseColumns._ID);
		photoIndex = cursor.getColumnIndexOrThrow(MediaColumns.DATA); 			// ��ȡ��
		titleIndex = cursor.getColumnIndexOrThrow(MediaColumns.TITLE); 
		nameIndex = cursor.getColumnIndexOrThrow(MediaColumns.DISPLAY_NAME); 
		timeIndex = cursor.getColumnIndexOrThrow(MediaColumns.DATE_MODIFIED);
		cursor.moveToFirst();							// ���α��ƶ����ʼ
		
		AlbumsFloderPath.clear();
		AlbumsFloderName.clear();
		AlbumsFloderTitle.clear();
		AlbumsFloderTime.clear();
		AlbumsNameList.clear();
		AlbumsFloderID.clear();
		do{
			String tempPath = cursor.getString(photoIndex);
			String tempName = cursor.getString(nameIndex);
			String tempTitle = cursor.getString(titleIndex);
			String tempTime = cursor.getString(timeIndex);
			long tempID = cursor.getLong(idIndex);
			
			tempPath.trim();
			String[] ArrayTemp = tempPath.split("/");
			String FolderName = ArrayTemp[ArrayTemp.length - 2];
			
			if(AlbumsFloderPath.get(FolderName) == null)
			{
				AlbumsFloderPath.put(FolderName, new ArrayList<String>());
				AlbumsFloderName.put(FolderName, new ArrayList<String>());
				AlbumsFloderTitle.put(FolderName, new ArrayList<String>());
				AlbumsFloderTime.put(FolderName, new ArrayList<String>());
				AlbumsNameList.add(FolderName);
				AlbumsFloderID.put(FolderName, new ArrayList<Long>());
			}
			AlbumsFloderPath.get(FolderName).add(tempPath);
			AlbumsFloderName.get(FolderName).add(tempName);
			AlbumsFloderTitle.get(FolderName).add(tempTitle);
			AlbumsFloderTime.get(FolderName).add(tempTime);
			AlbumsFloderID.get(FolderName).add(tempID);
			
		}while(cursor.moveToNext());
		
		localAlbumsList.clear();
		for(int i=0; i<AlbumsNameList.size(); i++){
			addFolder(AlbumsNameList.get(i), 
					AlbumsFloderPath.get(AlbumsNameList.get(i)).size(),false);
		}
		
		setOnClickListener(false);
		Log.d("debug",AlbumsFloderPath.size()+"");
		Log.d("debug",AlbumsFloderPath.get(AlbumsNameList.get(0)).size()+"");
		
		// ����"����б����..."����ʾ�ı�
		localRefTips.setVisibility(View.GONE) ;
	}
	

	/**
	 * @Method: SetOnlineAlbum
	 * @Description: �����������
	 */
	void SetOnlineAlbum()
	{		
		tNetAlbum = new Thread(rNetAlbum);
		tNetAlbum.start();
		
	}

	
	/**
	 * @Method: getMyNetAlbums
	 * @Description: ������������Լ��б��¼���,��MainViewActivity�е������ȡ
	 * @throws Exception
	 */
	private void getMyNetAlbums() throws Exception{
	
		// ��ȡ�б��е�һ��Ԫ��
		String title = MainViewActivity.sAlbumList.get(0);
		int size = MainViewActivity.sAlbumList.size();
		
		if( title.equals("fail") )
		{
			setTitle("û�����ӷ�����");
		}
		else
		{
			setTitle( title );
			if(size == 1)
			{
				if(userId.equals(LoginActivity.mineID))
					Toast.makeText(PhotoAlbumActivity.this, "��,�㻹û����ᣬ������һ����~~~", 1).show();
				else
					Toast.makeText(PhotoAlbumActivity.this, "��һﻹû�����~~~", 1).show();
			}
				
		}

		albumArray = new String[size];
		albumArray[0] = title;
		for(int i = 1; i < size; i=i+2){
			// ��MainViewActivity�б������������б���뵽ListView��
			int itemp = 0;
			try {
				itemp = Integer.parseInt(MainViewActivity.sAlbumList.get(i+1));
			} catch (Exception e) {
				continue;
			}
			addFolder(MainViewActivity.sAlbumList.get(i), itemp, true);
			albumArray[i] = MainViewActivity.sAlbumList.get(i);
			albumArray[i+1] = MainViewActivity.sAlbumList.get(i+1);
		}
		
		
		// ���������
		setOnClickListener(true);
		Log.d("1","111");
		loadListAnimation(true);
		
		if(userId.equals(LoginActivity.mineID)){
			registerForContextMenu(netAlbumListView);			// �趨�����Ĳ˵���ListView
		}
		
	}
	

	/**
	 * @Method: loadListAnimation
	 * @Description:  ����ListView��ʾ����
	 * @param NetOrLocal
	 */
	private void loadListAnimation(boolean NetOrLocal){
		// ������
        AnimationSet set = new AnimationSet(true);

        // Alpha����,Duration����Ϊ150����
        Animation animation = new AlphaAnimation(0.0f, 1.0f);
        animation.setDuration(150);
        // ��ӵ���������
        set.addAnimation(animation);

        // ����ת�ƶ���
        animation = new TranslateAnimation(
            Animation.RELATIVE_TO_SELF, 0.0f,Animation.RELATIVE_TO_SELF, 0.0f,
            Animation.RELATIVE_TO_SELF, -1.0f,Animation.RELATIVE_TO_SELF, 0.0f
        );
        animation.setDuration(300);
        // ��ӵ���������
        set.addAnimation(animation);

        // ���ò��ֶ���������,0.5fΪ��ʱʱ��
        LayoutAnimationController controller = new LayoutAnimationController(set, 0.5f);    
        // ��ListView�����ö���
        if(NetOrLocal){
        	netAlbumListView.setLayoutAnimation(controller);
        }
        else{
        	localAlbumListView.setLayoutAnimation(controller);
        }
	}
	

	/**
	 * @Method: OnlineAlbum
	 * @Description: ������������Լ��б��¼���
	 */
	private void OnlineAlbum() throws Exception{

		int n = albumArray.length;
		
		if(albumArray[0].equals("fail"))
		{
			setTitle("û�����ӷ�����");
		}
		else
		{
			setTitle(albumArray[0]);
			if(n == 1)
			{
				
				if(userId.equals(LoginActivity.mineID))
					Toast.makeText(PhotoAlbumActivity.this, "��,�㻹û����ᣬ������һ����~~~", 1).show();
				else
					Toast.makeText(PhotoAlbumActivity.this, "��һﻹû�����Ϊ~~~", 1).show();
			}
				
		}
		
		// �����������б�,�����������б�
		netAlbumsList.clear();
		// ����ȡ��������б���뵽ListView��
		for(int i = 1; i < n; i=i+2){
			int itemp;
			try {
				itemp = Integer.parseInt(albumArray[i+1]);
			} catch (Exception e) {
				continue;
			}
			try {
				addFolder(albumArray[i], itemp,true);
			} catch (Exception e) {
				Log.d("�������б�", "���ʧ��") ;
				e.printStackTrace();
			}
		}
		tabNetAdapter.notifyDataSetChanged();
		
		loadListAnimation(true);
		
		netAlbumListView.setOnItemClickListener(new OnItemClickListener() {  
	        @Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,  long arg3) {
	        	
	        	Log.d("3","arg2=" + arg2);
	        	Intent intent = new Intent(PhotoAlbumActivity.this, InPhotoAlbumActivity.class);
	        	intent.putExtra("id", userId);
	    		intent.putExtra("username",  albumArray[0]);
	    		intent.putExtra("albumname", albumArray[arg2*2 + 1]);
	    		intent.putExtra("num", albumArray[arg2*2 + 2]);  		
	    		startActivity(intent);
	        }  
	    });
		
		if(userId.equals(LoginActivity.mineID))
			registerForContextMenu(netAlbumListView);			// �趨�����Ĳ˵���ListView
		
	}
	
	/**
	 * @Method: setOnClickListener
	 * @Description: ���ü�����
	 * @param NetOrLocal
	 */
	void setOnClickListener(Boolean NetOrLocal)
	{
		if(NetOrLocal)
		{
			// ���ListView�ϵļ�����,�������뵽��Ӧ���������
			netAlbumListView.setOnItemClickListener(new OnItemClickListener() {  
		        @Override
				public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,  long arg3) {
		        	
		        	Log.d("3","arg2=" + arg2);
		        	Intent intent = new Intent(PhotoAlbumActivity.this, InPhotoAlbumActivity.class);
		        	intent.putExtra("id", userId);
		    		intent.putExtra("username",  albumArray[0]);
		    		intent.putExtra("albumname", albumArray[arg2*2 + 1]);
		    		intent.putExtra("num", albumArray[arg2*2 + 2]);  		
		    		startActivity(intent);
		        }  
		    });
		}
		else {
			// ���ListView�ϵļ�����,�������뵽��Ӧ���������
			localAlbumListView.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {

					Intent intent = new Intent(PhotoAlbumActivity.this,
							InLocalSmallPhotoActivity.class);
					intent.putExtra("albumname", AlbumsNameList.get(arg2));
					intent.putExtra("albumArray", albumArray);
					//Toast.makeText(PhotoAlbumActivity.this, albumArray.length+"����", 1).show();
					startActivity(intent);
				}
			});
		}
	}
	
	
	/**
	 * @Method: addFolder
	 * @Description:     ���һ����ᵽ�б�
	 * @param albumName  �����
	 * @param picnum     ��Ƭ����
	 * @param netOrLacal
	 */
	void addFolder(String albumName, int picnum, Boolean netOrLacal) throws Exception {

		Map<String, Object> map = new HashMap<String, Object>();
		if (netOrLacal) {
			map.put("albumName", albumName);
			map.put("picnum", picnum + "��");
			map.put("img1", R.drawable.ablum_03);
		} else {
			// �õ�����·������stemp1
			String[] stemp = AlbumsFloderPath.get(albumName).get(0).split("/");
			String stemp1 = "/";
			for (int i = 1; i < stemp.length - 2; i++)
				stemp1 += stemp[i] + "/";

			// �õ�ʱ��
			long itemp = 0;
			if (AlbumsFloderTime.get(albumName).get(0) == null) {
				Date d = new Date();
				d.getTime();
				String sd = d.toString();
				AlbumsFloderTime.get(albumName).remove(0);
				AlbumsFloderTime.get(albumName).add(0, sd);
			} else {
				try {
					itemp = Integer.parseInt(AlbumsFloderTime.get(albumName)
							.get(0));
				} catch (Exception e) {
				}
			}

			long itemp1 = 0;
			for (int i = 1; i < picnum; i++, itemp1 = 0) {
				try {
					itemp1 = Integer.parseInt(AlbumsFloderTime.get(albumName)
							.get(i));
				} catch (Exception e) {
				}
				if (itemp < itemp1)
					itemp = itemp1;
			}

			String stemp2;
			try {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");
				// ǰ���lSysTime�������ȳ�1000�õ���������תΪjava.util.Date����
				java.util.Date dt = new Date(itemp * 1000);
				// �õ���ȷ����ı�ʾ��08/31/2006 21:08:00
				stemp2 = sdf.format(dt);
			} catch (Exception e) {
				stemp2 = "2012-11-15 17:40";
			}

			map.put("albumName", albumName + " (" + picnum + ")");
			map.put("picnum", stemp1);
			map.put("date", stemp2);
			map.put("img1",
					decodeBitmap(AlbumsFloderPath.get(albumName).get(0)));

		}

		if (netOrLacal) {
			netAlbumsList.add(map);
			tabNetAdapter.notifyDataSetChanged();
		} else {
			localAlbumsList.add(map);
			tabLocalAdapter.notifyDataSetChanged();
		}
	}
	
	
	/**
	 * 
	 * @Method: decodeBitmap 
	 * @Description:  ��ȡSD���е�ͼƬ
	 * @param path
	 * @return    ����
	 * @return Bitmap  �������� 
	 * @throws
	 */
	Bitmap decodeBitmap(String path){ 
    	BitmapFactory.Options op = new BitmapFactory.Options(); 
    	op.inJustDecodeBounds = true; 
    	Bitmap bmp = BitmapFactory.decodeFile(path, op); 
    	int wRatio = (int)Math.ceil(op.outWidth/100); 	//��ȡ�����С 
    	int hRatio = (int)Math.ceil(op.outHeight/100); 
    	if(wRatio > 1 && hRatio > 1){ 					//����ָ����С������С��Ӧ�ı���
    		if(wRatio > hRatio){ 
    			op.inSampleSize = wRatio; 
    		}else{ 
    			op.inSampleSize = hRatio; 
    		} 
    	} 
    	op.inJustDecodeBounds = false; 
    	bmp = BitmapFactory.decodeFile(path, op);
    	return bmp; 
    }
	
	
	/**
	 * @Method: CreatAlbumDialog
	 * @Description: �½����ĶԻ���
	 */
	public void CreatAlbumDialog(){
		
		LinearLayout CreateAlbumDialogLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.createalbumdialog, null);
		final EditText editname = (EditText) CreateAlbumDialogLayout.findViewById(R.id.newalbumname);
		final CheckBox checkshare = (CheckBox) CreateAlbumDialogLayout.findViewById(R.id.newalbumshare);
		
		Builder builder = new AlertDialog.Builder(this);
		builder.setIcon(R.drawable.newalbums);
		builder.setTitle("���������");
		builder.setView(CreateAlbumDialogLayout);
		builder.setPositiveButton("ȷ��",
				new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialog,
							int whichButton)
					{
						// ��д�����û���¼�Ĵ���
						if(editname.length() == 0)
							Toast.makeText(PhotoAlbumActivity.this, "�����Ϊ�գ������ᴴ��ʧ��,�����´���..", 1).show();
						else{
							newAlbumName = editname.getText().toString();
							if(checkshare.isChecked())
								ischecked = "yes";
							else
								ischecked = "no";
							tNewAlbum = new Thread(rNewAlbum);
							tNewAlbum.start();
						}
					}
				});
		builder.setNegativeButton("ȡ��",
				new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialog,
							int whichButton){
						
						refreshTips.setText("ȡ����");
						delayHandler.postDelayed(delayRunnable, 2000);
					}
				});
		
		
		builder.setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
				Log.d("debug","return11");
				if(keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0)
				{
					refreshTips.setText("ȡ����");
					delayHandler.postDelayed(delayRunnable, 2000);
				}
				return false;
			}
		});

		builder.show();
	}
	
	
	/**
	 * @Method: AlterAlbumDialog
	 * @Description:  �޸����
	 */
	public void AlterAlbumDialog(){
		
		LinearLayout AlterAlbumDialogLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.createalbumdialog, null);
		final EditText editname = (EditText) AlterAlbumDialogLayout.findViewById(R.id.newalbumname);
		final CheckBox checkshare = (CheckBox) AlterAlbumDialogLayout.findViewById(R.id.newalbumshare);
		
		editname.setText(ChooseAlbumName);
		
		Builder builder = new AlertDialog.Builder(this);
		builder.setIcon(R.drawable.newalbums);
		builder.setTitle("�޸����");
		builder.setView(AlterAlbumDialogLayout);
		builder.setPositiveButton("ȷ��",
				new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialog,
							int whichButton)
					{
						// ��д�����û���¼�Ĵ���
						if(editname.length() == 0)
							Toast.makeText(PhotoAlbumActivity.this, "��,�����Ϊ�գ������ᴴ�޸�ʧ��...", 1).show();
						else{
							newAlbumName = editname.getText().toString();
							if(checkshare.isChecked())
								ischecked = "yes";
							else
								ischecked = "no";
							tModifyAlbum = new Thread(rModifyAlbum);
							tModifyAlbum.start();
						}
					}
				});
		builder.setNegativeButton("ȡ��",
				new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialog,
							int whichButton)
					{
						// ȡ���û���¼���˳�����

					}
				});
		builder.show();
	}
	
	
	
	/**
	 * ���ܣ�HTTP�����������
	 */
	Runnable rNetAlbum = new Runnable()								
	{
		String msg;
		@Override
		public void run()
		{
            ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            if(userId.equals(LoginActivity.mineID)){
            	nameValuePairs.add(new BasicNameValuePair("protocol","getAlbums"));
            }
            else{
            	nameValuePairs.add(new BasicNameValuePair("protocol","getShareAlbum"));
            }
            nameValuePairs.add(new BasicNameValuePair("id", userId));
			HttpThread h = new HttpThread(nameValuePairs,11);	// 11--��������б�
			msg = h.sendInfo().toString(); 						// ���շ������ķ���ֵ
			Log.d("������������б�", msg);
			sendMessage();
		}
		
		public void sendMessage(){								// �̼߳���ݴ���
			Looper mainLooper = Looper.getMainLooper ();		// �õ����߳�loop
            mHandler1 = new MyHandler1(mainLooper);				// �������̵߳�handler
            mHandler1.removeMessages(0);						// �Ƴ����ж����е���Ϣ
            Message m = mHandler1.obtainMessage(1, 1, 1, msg);	// ����Ϣ����message
            mHandler1.sendMessage(m);							// ����message
		}
	};
	private class MyHandler1 extends Handler{       
        public MyHandler1(Looper looper){
               super (looper);
        }
        @Override
        public void handleMessage(Message msg) {
        	String s = msg.obj.toString();
        	s = s.trim();	
        	try{
        		albumArray = s.split(";;");
        	}catch(Exception e){
        		Toast.makeText(PhotoAlbumActivity.this, "����,�����б��������~~~", 0).show();
        	}
        	
        	loadingDialog.dismiss();
        	// ����ʾ����
        	refreshTips.setVisibility(View.GONE) ;

        	try {
            	// �����µ����
				OnlineAlbum();
			} catch (Exception e) {
				e.printStackTrace();
			}
        }            
	}

	/**
	 * ���� �� �½����
	 */
	Runnable rNewAlbum = new Runnable()								
	{
		String msg;
		@Override
		public void run()
		{
            ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            nameValuePairs.add(new BasicNameValuePair("protocol","addAlbum"));
            nameValuePairs.add(new BasicNameValuePair("id", LoginActivity.mineID));
            nameValuePairs.add(new BasicNameValuePair("albumName", newAlbumName));
            nameValuePairs.add(new BasicNameValuePair("share", ischecked));
			HttpThread h = new HttpThread(nameValuePairs, 12);	// 12--�½����
			msg = h.sendInfo().toString(); 						// ���շ������ķ���ֵ
			sendMessage();
		}
		public void sendMessage(){								// �̼߳���ݴ���
			Looper mainLooper = Looper.getMainLooper ();		// �õ����߳�loop
            mHandler2 = new MyHandler2(mainLooper);				// �������̵߳�handler
            mHandler2.removeMessages(0);						// �Ƴ����ж����е���Ϣ
            Message m = mHandler2.obtainMessage(1, 1, 1, msg);	// ����Ϣ����message
            mHandler2.sendMessage(m);							// ����message
		}
	};
	private class MyHandler2 extends Handler{       
        public MyHandler2(Looper looper){
               super (looper);
        }
        @Override
        public void handleMessage(Message msg) {
        	String s = msg.obj.toString();
        	s = s.trim();	
        	if(s.equals("success")){
        		
        		try {
        			// �������б�
					addFolder(newAlbumName, 0,true);
				} catch (Exception e) {
					e.printStackTrace();
				}
        		
        		String ArrayTemp[] = new String[albumArray.length+2];
        		for(int i=0;i<albumArray.length;i++)
        			ArrayTemp[i] = albumArray[i];
        		ArrayTemp[albumArray.length] = newAlbumName;
        		ArrayTemp[albumArray.length+1] = "0";
        		albumArray = new String[ArrayTemp.length];
        		albumArray = ArrayTemp;
        		
        		refreshTips.setText("�ɹ�������ᣡ");
        		delayHandler.postDelayed(delayRunnable, 2000);
  
        	}
        	else if(s.equals("fail")){
        		Toast.makeText(PhotoAlbumActivity.this, "�������ܾ�����Ĵ�����������������...", 1).show();
        	}
        	else{
        		Toast.makeText(PhotoAlbumActivity.this, "�������ò���е�����~~~" + s, 1).show();
        	}
        }            
	}
	
	/**
	 *  ��ʱ��
	 */
	private Handler delayHandler;
	Runnable delayRunnable=new Runnable(){
		@Override
		public void run() {
			refreshTips.setVisibility(View.GONE);
			delayHandler.removeCallbacks(delayRunnable);
		}
	};
	
	
	/**
	 * ���� �� ɾ�����
	 */
	Runnable rDeleteAlbum = new Runnable()								
	{
		String msg;
		@Override
		public void run()
		{
            ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            nameValuePairs.add(new BasicNameValuePair("protocol","deleteAlbum"));
            nameValuePairs.add(new BasicNameValuePair("id", LoginActivity.mineID));
            nameValuePairs.add(new BasicNameValuePair("albumName", ChooseAlbumName));
			HttpThread h = new HttpThread(nameValuePairs,13);	// 13--ɾ�����
			msg = h.sendInfo().toString(); 						// ���շ������ķ���ֵ
			sendMessage();
		}
		public void sendMessage(){							// �̼߳���ݴ���
			Looper mainLooper = Looper.getMainLooper ();	// �õ����߳�loop
            mHandler3 = new MyHandler3(mainLooper);			// �������̵߳�handler
            mHandler3.removeMessages(0);								// �Ƴ����ж����е���Ϣ
            Message m = mHandler3.obtainMessage(1, 1, 1, msg);	// ����Ϣ����message
            mHandler3.sendMessage(m);								// ����message
		}
	};
	private class MyHandler3 extends Handler{       
        public MyHandler3(Looper looper){
               super (looper);
        }
        @Override
        public void handleMessage(Message msg) {
        	String s = msg.obj.toString();
        	s = s.trim();	
        	if(s.equals("success")){
        		
        		netAlbumsList.remove(ChooseAlbumPosition);
        		
        		for(int i=ChooseAlbumPosition*2; i<albumArray.length-4; i+=2)
        		{
        			albumArray[i+1] = albumArray[i+3];
        			albumArray[i+2] = albumArray[i+4];
        		}
        		albumArray[albumArray.length-2] = null;
        		albumArray[albumArray.length-1] = null;
        		String[] arrayTemp = new String[albumArray.length - 2];
        		for(int i=0;i<albumArray.length-2;i++) 
					arrayTemp[i] = albumArray[i];
				albumArray = new String[arrayTemp.length];
        		albumArray = arrayTemp;
        		arrayTemp = null;
        		System.gc();
        		
        		tabNetAdapter.notifyDataSetChanged();
        		
        		refreshTips.setVisibility(View.GONE) ;
        	}
        	else if(s.equals("fail")){
        		Toast.makeText(PhotoAlbumActivity.this, "�������ܾ�����Ĳ���...", 1).show();
        	}
        	else{
        		Toast.makeText(PhotoAlbumActivity.this, "����������������Ƿ���ȷ...", 1).show();
        	}
        }            
	}
	
	/**
	 * ���� �� �޸����
	 */
	Runnable rModifyAlbum = new Runnable()								
	{
		String msg;
		@Override
		public void run()
		{
            ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            nameValuePairs.add(new BasicNameValuePair("protocol","changeAlbum"));
            nameValuePairs.add(new BasicNameValuePair("id", LoginActivity.mineID));
            nameValuePairs.add(new BasicNameValuePair("albumName", ChooseAlbumName));
            nameValuePairs.add(new BasicNameValuePair("newName", newAlbumName));
            nameValuePairs.add(new BasicNameValuePair("share", ischecked));
			HttpThread h = new HttpThread(nameValuePairs,14);	// 14--�޸����
			msg = h.sendInfo().toString(); 						// ���շ������ķ���ֵ
			sendMessage();
		}
		public void sendMessage(){								// �̼߳���ݴ���
			Looper mainLooper = Looper.getMainLooper ();		// �õ����߳�loop
            mHandler4 = new MyHandler4(mainLooper);				// �������̵߳�handler
            mHandler4.removeMessages(0);						// �Ƴ����ж����е���Ϣ
            Message m = mHandler4.obtainMessage(1, 1, 1, msg);	// ����Ϣ����message
            mHandler4.sendMessage(m);							// ����message
		}
	};
	private class MyHandler4 extends Handler{       
        public MyHandler4(Looper looper){
               super (looper);
        }
        @Override
        public void handleMessage(Message msg) {
        	String s = msg.obj.toString();
        	s = s.trim();	
        	if(s.equals("success")){
        		Map<String, Object> map = new HashMap<String, Object>();
        		map.put("albumName", newAlbumName);
        		map.put("picnum", ChooseAlbumNum);
        		map.put("img", R.drawable.ablum_03);
        		netAlbumsList.remove(ChooseAlbumPosition);
        		netAlbumsList.add(ChooseAlbumPosition, map);
        		albumArray[ChooseAlbumPosition*2+1] = newAlbumName;
        		tabNetAdapter.notifyDataSetChanged();
        	}
        	else if(s.equals("fail")){
        		Toast.makeText(PhotoAlbumActivity.this, "�������ܾ�������޸ģ�������������...", 1).show();
        	}
        	else{
        		Toast.makeText(PhotoAlbumActivity.this, "�������ò���е�����~~~", 1).show();
        	}
        }            
	}
	
	
	
	/**
	 * (�� Javadoc,��д�ķ���) 
	 * @Title: onCreateOptionsMenu
	 * @Description:  �����˵�
	 * @param menu
	 * @return 
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if(userId.equals(LoginActivity.mineID))
		{
			menu.add(Menu.NONE, Menu.FIRST + 1, 1, "�½����").setIcon( android.R.drawable.ic_menu_add);
		}
        return super.onCreateOptionsMenu(menu);
	}
	
	@Override
    public boolean onMenuOpened(int featureId, Menu menu) {
		
		if(myTabhost.getCurrentTab() == 0)
			return true;												// ����Ϊtrue ����ʾϵͳmenu 
		else
			return false;												// ����Ϊtrue ����ʾϵͳmenu   
    }
		
		
	/**
	 * (�� Javadoc,��д�ķ���) 
	 * @Title: onOptionsItemSelected
	 * @Description:   �˵�ѡ����Ӧ
	 * @param item
	 * @return 
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	@Override  
    public boolean onOptionsItemSelected(MenuItem item) {  
        switch (item.getItemId()) {  
  
	        case Menu.FIRST + 1:  					// �������
	            CreatAlbumDialog();
		        break;  
	            
	        }    
        return false;  
    }   
	

	/**
	 * (�� Javadoc,��д�ķ���) 
	 * @Title: onCreateContextMenu
	 * @Description:   ���������Ĳ˵�
	 * @param menu
	 * @param v
	 * @param menuInfo 
	 * @see android.app.Activity#onCreateContextMenu(android.view.ContextMenu, android.view.View, android.view.ContextMenu.ContextMenuInfo)
	 */
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		
		    menu.setHeaderTitle("������");
	        //��Ӳ˵���
	        menu.add(0, Menu.FIRST, 0, "�޸����");
	        menu.add(0, Menu.FIRST + 1, 0, "ɾ�����");
	        menu.add(0, Menu.FIRST + 2, 0, "ȡ��");
	}
	
	
	/**
	 * (�� Javadoc,��д�ķ���) 
	 * @Title: onContextItemSelected
	 * @Description:  �����Ĳ˵�ѡ��
	 * @param item
	 * @return 
	 * @see android.app.Activity#onContextItemSelected(android.view.MenuItem)
	 */
   @Override
	public boolean onContextItemSelected(MenuItem item) {
	   
	    // ��ȡ��ǰ��ѡ��Ĳ˵������Ϣ
        AdapterContextMenuInfo info = (AdapterContextMenuInfo)item.getMenuInfo();     
        ChooseAlbumName = netAlbumsList.get(info.position).get("albumName").toString();
        ChooseAlbumNum = netAlbumsList.get(info.position).get("picnum").toString();
        ChooseAlbumPosition = info.position;
        
        switch(item.getItemId()){
        
	        case Menu.FIRST:				// �޸����
	        	AlterAlbumDialog();
	        	
	        	break;
	        	
	        case Menu.FIRST + 1:			// ɾ�����
	        	// ȷ��ɾ��
				new AlertDialog.Builder(PhotoAlbumActivity.this)
	        	.setIcon(R.drawable.beaten)
				.setTitle("ȷ��ɾ��?")  
				.setPositiveButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener() {
		            @Override
					public void onClick(DialogInterface dialog, int whichButton) {
		            	
						refreshTips.setVisibility(View.VISIBLE) ;
						refreshTips.setText("���ɾ����...") ;
						
			        	tDeleteAlbum = new Thread(rDeleteAlbum);
			        	tDeleteAlbum.start();
		            }
		        })
		        .setNegativeButton(R.string.alert_dialog_cancel, new DialogInterface.OnClickListener() {
		            @Override
					public void onClick(DialogInterface dialog, int whichButton) {

		            }
		        }).show();

	            break;
	            
	        default:
	            break;
        }
        return true;
	}
   

   /**
    * (�� Javadoc,��д�ķ���) 
    * @Title: onStop
    * @Description:   �ͷŶ���
    * @see android.app.ActivityGroup#onStop()
    */
	@Override
	protected void onStop() {

		try{
			if(tNetAlbum != null)
			{
				if (rNetAlbum != null && mHandler1 != null)
				{
					mHandler1.removeCallbacks(rNetAlbum);
				}
				tNetAlbum.interrupt();
				tNetAlbum = null;
			}
			if(tNewAlbum!=null)
			{
				if (rNetAlbum != null)
				{
					mHandler2.removeCallbacks(rNewAlbum);
				}
			
				tNewAlbum.interrupt();
				tNewAlbum = null;
			}
			if(tDeleteAlbum!=null)
			{
				if (rNetAlbum != null)
				{
					mHandler3.removeCallbacks(rDeleteAlbum);
				}
	
				tDeleteAlbum.interrupt();
				tDeleteAlbum = null;
			}
			if(tModifyAlbum!=null)
			{
				if (rNetAlbum != null)
				{
					mHandler4.removeCallbacks(rModifyAlbum);
				}
	
				tModifyAlbum.interrupt();
				tModifyAlbum = null;
			}
			
			isReload = true;
			// ����ҳ���Set���Ƴ�
			MainViewActivity.removeFromSet( this );
		}catch(Exception e){
			e.printStackTrace() ;
		}
		
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
		
		Log.d("��������", "���������������");
		super.onRestart();
	}  
	
}