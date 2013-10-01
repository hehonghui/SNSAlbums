package albums;

import network.HttpThread;

import album.entry.MyProgressDialog;
import album.entry.R;

/**
 * Copyright (c) 2012,UIT-ESPACE( TEAM: UIT-GEEK)
 * All rights reserved.
 *
 * @Title: FriendAlbumListActivity.java 
 * @Package albums 
 * @Author ������(Mr.Abert) 
 * @E-mail:uit_xuxiaojia@163.com
 * @Version V1.0
 * @Date��2012-11-13 ����8:34:45
 * @Description:
 *
 */

public class FriendAlbumListActivity extends Activity{

	private String userID = null;
	private ListView albumListView = null;
	private SimpleAdapter adapter = null;
	private List<Map<String, Object>> albumsList= new ArrayList<Map<String, Object>>();
	private Thread tAlbum = null;
	private MyHandler mHandler = null;
	private MyProgressDialog loadingDialog ;	// �����������ʱ�Ľ������ʾ��
	private String[] albumArray;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		userID = getIntent().getStringExtra("id");
		setContentView(R.layout.friendalbumlist);
		
		loadingDialog = new MyProgressDialog(this,
    			"�������У����Ժ򡤡���") ;					// ��ʼ����������б�ʱ�Ľ������ʾ��	
		
		
		// userName: �û���  ,   Name ���ǳ�   , img : �û�ͷ��
		adapter = new SimpleAdapter(this, albumsList, R.layout.insidelistview,
				new String[]{"albumName","picnum","date","img1"},				// ��������ʽ 
				new int[]{R.id.userName2,R.id.ipInfo2,R.id.ipInfo3,R.id.userImg2});		// ����һ���������ݶ�Ӧ�������������������
		albumListView = (ListView) findViewById(R.id.friendalbumlistview);
		albumListView.setAdapter( adapter );
		
		
		tAlbum = new Thread(rAlbum);
		tAlbum.start();
	}
	
	
	
	/**
	 * ���ܣ�HTTP�����������
	 */
	Runnable rAlbum = new Runnable()								
	{
		String msg;
		@Override
		public void run()
		{
            ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            nameValuePairs.add(new BasicNameValuePair("protocol","getShareAlbum"));
            nameValuePairs.add(new BasicNameValuePair("id", userID));
			HttpThread h = new HttpThread(nameValuePairs,11);	// 11--��������б�
			msg = h.sendInfo().toString(); 						// ���շ������ķ���ֵ
			Log.d("������������б�", msg);
			sendMessage();
		}
		
		public void sendMessage(){								// �̼߳���ݴ���
			Looper mainLooper = Looper.getMainLooper ();		// �õ����߳�loop
            mHandler = new MyHandler(mainLooper);				// �������̵߳�handler
            mHandler.removeMessages(0);						// �Ƴ����ж����е���Ϣ
            Message m = mHandler.obtainMessage(1, 1, 1, msg);	// ����Ϣ����message
            mHandler.sendMessage(m);							// ����message
		}
	};
	private class MyHandler extends Handler{       
        public MyHandler(Looper looper){
               super (looper);
        }
        @Override
        public void handleMessage(Message msg) {
        	String s = msg.obj.toString();
        	s = s.trim();	
        	try{
        		albumArray = s.split(";;");
        	}catch(Exception e){
        		Toast.makeText(FriendAlbumListActivity.this, "����,�����б��������~~~", 0).show();
        	}
        	
        	loadingDialog.dismiss();
        	// �����µ����
        	OnlineAlbum();
        }            
	}
	

	/**
	 * @Method: OnlineAlbum
	 * @Description:  ������������Լ��б��¼���
	 */
	void OnlineAlbum(){

		int n = albumArray.length;
		
		if(albumArray[0].equals("fail"))
		{
			setTitle("û�����ӷ�����");
		}
		else
		{
			setTitle(albumArray[0]);
			if(n == 1)
				Toast.makeText(FriendAlbumListActivity.this, "��ĺ��ѵ����Ϊ��~~~", 1).show();
				
		}
		
		// �����������б�,�����������б�
		albumsList.clear();
		// ����ȡ��������б���뵽ListView��
		for(int i = 1; i < n; i=i+2){
			addFolder(albumArray[i], albumArray[i+1],true);
		}
		adapter.notifyDataSetChanged();
		
		loadListAnimation(true);
		
		albumListView.setOnItemClickListener(new OnItemClickListener() {  
	        @Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,  long arg3) {
	        	
	        	Log.d("3","arg2=" + arg2);
	        	Intent intent = new Intent(FriendAlbumListActivity.this, InPhotoAlbumActivity.class);
	        	intent.putExtra("id", userID);
	    		intent.putExtra("username",  albumArray[0]);
	    		intent.putExtra("albumname", albumArray[arg2*2 + 1]);
	    		intent.putExtra("num", albumArray[arg2*2 + 2]);  		
	    		startActivity(intent);
	        }  
	    });
		
	}
	

	/**
	 * @Method: addFolder
	 * @Description:  ���һ����ᵽ�б�
	 * @param albumName  �����
	 * @param picnum     ��Ƭ����
	 * @param netOrLacal ������߱������
	 */
	void addFolder(String albumName, String picnum,Boolean netOrLacal)
	{
		int[] imgArray = {	R.drawable.ablum_01, R.drawable.ablum_03, 
				R.drawable.ablum_05, R.drawable.pictures, R.drawable.folder}; 

		// ���������,��ȡ���ͼ��
		Random imgIndex = new Random();
		int index = imgIndex.nextInt(4);
		
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("albumName", albumName);
		map.put("picnum", picnum + "��");
		map.put("img1", imgArray[index]);
		//map.put("share", value);
		albumsList.add( map );
		adapter.notifyDataSetChanged();
	} 
	
	
	/**
	 * @Method: loadListAnimation
	 * @Description:   ����ListView��ʾ����
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
        albumListView.setLayoutAnimation(controller);
	}
	
}

