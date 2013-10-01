package chat;

import map.BaiduMapActivity;
import network.HttpThread;
import network.NetworkUtils;

import album.entry.LoginActivity;
import album.entry.MainViewActivity;
import album.entry.MyProgressDialog;
import album.entry.R;
import albums.FriendAlbumListActivity;

/*
* Copyright (c) 2012,UIT-ESPACE
* All rights reserved.
*
* �ļ���ƣ�FriendsListActivity.java
* ժ Ҫ�������б�
*  1.�����б��ʵ�� : ʹ�õ���ListView �� SimpleAdapter.
*  2.����ý���,�����ĳ����ʱ�������������û���IP��ַ.�����ȡ���û�IP��ʹ��Socket���е�Ե�ͨ��.
*  3.��̨������ѷ���������Ϣ
* 
* ��ǰ�汾��1.1
* �� �ߣ��κ��
* ������ڣ�2012��11��3��
*
* ȡ��汾��1.0
* ԭ���� ���κ��
* ������ڣ�2012��7��20��
*/

/**
 * 
 * @ClassName: FriendsListActivity 
 * @Description: �����б�,�����ĳ����ʱ������ú��ѵ��������.
 * @Author: Mr.Simple (�κ��)
 * @E-mail: bboyfeiyu@gmail.com 
 * @Date 2012-11-17 ����5:07:00 
 *
 */
public class FriendsListActivity extends ListActivity implements OnTouchListener,
									android.view.GestureDetector.OnGestureListener,OnScrollListener{
	
	// ��������б���û�ͷ��id���ǳƵȵ�����
	private ArrayList<Map<String, Object>> mFriendsList = new ArrayList<Map<String, Object>>();
	
	private SimpleAdapter mAdapter;						// ������
	//private String array[];								// ����IP�б�
	private MyProgressDialog loadingDialog = null;		// ����ʱ�Ľ����
	private GestureDetector mGestureDetector = null;	// ���ƴ���̽����
	private MyBroadcastReciver mDataReceiver = null;

	
	/**
	 * (�� Javadoc,��д�ķ���) 
	 * @Title: onCreate
	 * @Description:   ҳ�洴��,�ؼ��ͱ�����ʼ��
	 * @param savedInstanceState 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle("�����б�");
		
		//	�����б��������  userId: �û��� ��  Name ���ǳ�   ��  img : �û�ͷ��	 
		mAdapter = new SimpleAdapter(this, mFriendsList , R.layout.friendslist,
				new String[]{"userId","Name","img"},				//	��������ʽ 
				new int[]{R.id.userName,R.id.ipInfo,R.id.userImg});	// ����һ���������ݶ�Ӧ�������������������
		setListAdapter( mAdapter );
		// ���ö�����ʾListView
		loadListAnimation();
		// �趨�����Ĳ˵���ListView
		registerForContextMenu(getListView());	
		// ��ȡ�����б�ʱ�Ľ��������
		loadingDialog = new MyProgressDialog(this, "������Ϣ�����С�����");
		// ����ʶ��
		mGestureDetector = new GestureDetector(this);    		
		
		// ������ߵĺ����б�
		mFriendsList.clear();
		mAdapter.notifyDataSetChanged();
		try {
			// ��������ĺ����б������
			addFriendFromList();
		} catch (Exception e1) {
			loadingDialog.show();
			getAllUsersInfo();
		}
		
		MainViewActivity.addActivityToHashSet( this );	// ����ǰҳ����ӵ�activitymap��
		
		// �㲥������,���պ������ߵĵ���Ϣ(ͨ��service�Ĺ㲥�����ݵ�);
		mDataReceiver = new MyBroadcastReciver();
		IntentFilter intentFilter = new IntentFilter("chat.SocketService.onlie");
		Log.d("TAG", "�������߽�����ע���� �� " + registerReceiver(mDataReceiver,  intentFilter) );
		
	}
	
	/**
	 * 
	 * @Method: loadListAnimation 
	 * @Description: ����ListView��ʾ����  
	 * @return void  �������� 
	 * @throws
	 */
	private void loadListAnimation(){
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
        // ��ListView�����ö���,���õ��Ƕ���������
        ListView listView = getListView();       
        // ���ñ���ɫ
        listView.setCacheColorHint(Color.TRANSPARENT);
        // �����б�ı���,Ҫ����ͼ�񱳾���Ҫ��CacheColorHint����Ϊ͸��
        listView.setBackgroundResource(R.drawable.frilist);
        listView.setLayoutAnimation(controller);
        listView.setOnTouchListener(this);
        
	}
	

	/**
	 * @Method: addFriendFromList 
	 * @Description:  ��MainViewActivity������غ����б�,�洢�˺����б�
	 * @throws Exception   
	 * @throws
	 */
	private void addFriendFromList() throws Exception {
		// Main��û�л�ȡ�������б�Ļ�ֱ���Զ���ȡ
		if (MainViewActivity.sFriendList.size() == 0) {
			throw new ArrayIndexOutOfBoundsException();
		}

		// ���List�Լ�֪ͨAdapter��ݼ��ı�
		mFriendsList.clear();
		mAdapter.notifyDataSetChanged();
		Log.d("��������", "�����б�");
		for (int i = 0; i < MainViewActivity.sFriendList.size(); i += 3) {

			// ������б���Ӻ���
			addFriend(MainViewActivity.sFriendList.get(i + 1),
					MainViewActivity.sFriendList.get(i),
					MainViewActivity.sFriendList.get(i + 2));
			Log.d("LIST", "���б�����������б�");
		}

	}
	

	/**
	 * @Method: addFriend 
	 * @Description:  ���µ��û�����ʱ����û����б� 
	 * @param Name    �û��ǳ�
	 * @param userId  �û�ID
	 * @param ip	  �û���IP
	 * @throws Exception   
	 */
	private void addFriend(String Name,String userId, String ip) throws Exception
	{
		if (LoginActivity.mineName.equals(Name) || LoginActivity.mineID.equals(userId))
		{
			return ;
		}
		
		int[] imgArray = {	R.drawable.bad_smile_96,R.drawable.laugh_96,
							R.drawable.fire_96,R.drawable.money_96,
							R.drawable.grimace_96,R.drawable.girl_96,
							R.drawable.face_96, R.drawable.o_96
						 }; 
		
		// ���������,��ȡ���ͼ��
		Random imgIndex = new Random();
		int index = imgIndex.nextInt(7);
		
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("userId", userId);
		map.put("Name", Name);		// �ǳ�			
		map.put("img", imgArray[index]);
		map.put("ip", ip);
		
		mFriendsList.add( map );
		mAdapter.notifyDataSetChanged();
		
	}
	
	
	/**
	 * (�� Javadoc,��д�ķ���) 
	 * @Title: onListItemClick
	 * @Description:  �����б���������,���ĳ�����ѽ����������
	 * @param l
	 * @param v
	 * @param position
	 * @param id 
	 * @see android.app.ListActivity#onListItemClick(android.widget.ListView, android.view.View, int, long)
	 */
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		
			Log.d("my IP :", NetworkUtils.getLocalIpAddress());
			
			String name = mFriendsList.get(position).get("Name").toString();			// �ǳ�
			String ip = mFriendsList.get(position).get("ip").toString();				// IP
			
			Intent intent = new Intent(this, ChatActivity.class);
			intent.putExtra("friendName", name);
		    intent.putExtra("friendIp", ip);
		    
		    Log.d("����б��е�", " �����ǳ� : " + name + ",IP : " + ip);
		    startActivity(intent);
		    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
	}
	
	

	/**
	 * @Method: getAllUsersInfo ����
	 * @Description:  ���������������,��ȡ���к��ѵ��б�
	 */
	private void getAllUsersInfo()
	{
		new Thread( new Runnable() {
			
			@Override
			public void run() {
				
				ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
				nameValuePairs.add(new BasicNameValuePair("protocol","getIP"));// ��װ��ֵ��
				HttpThread h = new HttpThread(nameValuePairs,7);				// ��ȡ7 �����б�
				String msg = (String)h.sendInfo(); 								// ���շ������ķ���ֵ
				
				Looper mainLooper = Looper.getMainLooper ();					// �õ����߳�loop
	            mHandler = new MyHandler(mainLooper);							// �������̵߳�handler
	            mHandler.removeMessages(0);										// �Ƴ����ж����е���Ϣ
	            Message m = mHandler.obtainMessage(1, 1, 1, msg);				// ����Ϣ����message
	            mHandler.sendMessage(m);
			}           
		}).start();
	}

	
	/**
	 *  ���շ��������ص����ߺ������,������ʾ�ں����б���
	 */
	private Handler mHandler = new Handler();

	/**
	 * 
	 * @ClassName: MyHandler 
	 * @Description:  ���շ��������ص����ߺ������,������ʾ�ں����б���
	 * @Author: XXJGOOD
	 * @E-mail: bboyfeiyu@gmail.com 
	 * @Date 2012-11-17 ����5:11:33 
	 *
	 */
	private class MyHandler extends Handler {

		public MyHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {

			String array[] = null;
			String s1 = msg.obj.toString(); // ���յ����̵߳��ַ�
			s1 = s1.trim(); // ȥ�ո�

			if (s1.contains("error")) {
				Toast.makeText(FriendsListActivity.this,
								"�����б����ʧ��,����������߷������쳣...", 1).show();
				// ���»�ȡ�����б�
				getAllUsersInfo() ;
				return;
			} 

			loadingDialog.dismiss();
			try {
				// ����ַ�,�����Ч��ݵ��ַ�����
				array = s1.split(";;");
				int len = array.length;
				if ( len > 0 ){
					mFriendsList.clear();
					mAdapter.notifyDataSetChanged();
				}
				for (int i = 0; i < len; i += 3) {
					// ������б���Ӻ���
					addFriend(array[i + 1], array[i], array[i + 2]);
					// ����ݴ浽MainViewActivity��
					MainViewActivity.sFriendList.add(array[i + 1]);
					MainViewActivity.sFriendList.add(array[i]);
					MainViewActivity.sFriendList.add(array[i + 2]);

				}
			} catch (Exception e) {
				Toast.makeText(FriendsListActivity.this, "��Ǹ,�����б��ȡʧ��~~~", 0).show();
			}
		}

	}
	

	/**
	 * (�� Javadoc,��д�ķ���) 
	 * @Title: onCreateOptionsMenu
	 * @Description:  ˢ�²˵�
	 * @param menu
	 * @return 
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
 	@Override
     public boolean onCreateOptionsMenu(Menu menu) {

         // setIcon()����Ϊ�˵�����ͼ�꣬����ʹ�õ���ϵͳ�Դ��ͼ��
 		menu.add(Menu.NONE, Menu.FIRST + 1, 1, "ˢ��").setIcon(
 				android.R.drawable.ic_menu_send);
        
 		return true;
     }
 	
 	
 	/**
 	 *  ���� �� ˢ�²˵�ѡ���¼�
 	 *  (non-Javadoc)
 	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
 	 */
 	@Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
	         case Menu.FIRST + 1:		// �ҵ�λ��
	        	 loadingDialog.show();
        	 	 getAllUsersInfo();
        	 	 break;	  
	        
	        default:
	         		break;

         }
         return false;

     }		// end of onOptionsItemSelected


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
			
			    menu.setHeaderTitle("��������");
		        //��Ӳ˵���
		        menu.add(0, Menu.FIRST, 0, "����");
		        menu.add(0, Menu.FIRST + 1, 0, "���");
		}

	   
		/**
		 * (�� Javadoc,��д�ķ���) 
		 * @Title: onContextItemSelected
		 * @Description:   �����Ĳ˵�ѡ��
		 * @param item
		 * @return 
		 * @see android.app.Activity#onContextItemSelected(android.view.MenuItem)
		 */
	   @Override
		public boolean onContextItemSelected(MenuItem item) {
		   
		   // ��ȡ��ǰ��ѡ��Ĳ˵������Ϣ
	        AdapterContextMenuInfo info = (AdapterContextMenuInfo)item.getMenuInfo();        
	        switch(item.getItemId()){
	        
		        case Menu.FIRST:		   
		        	ViewDataDialog(mFriendsList.get(info.position).get("userId").toString(),
		        					mFriendsList.get(info.position).get("Name").toString());
		                	break;  
		            
		        case Menu.FIRST + 1:
		        	// �����ҳ�洫�����,ͨ��ID�Ż�ȡ�û������Ϣ
					String userid = mFriendsList.get(info.position).get("userId").toString();
					Log.d("�б�", "id: "+ userid );				
					// �������
					Intent intent = new Intent(FriendsListActivity.this, FriendAlbumListActivity.class);
		    		intent.putExtra("id", userid);
		    		startActivity(intent);	    		
		            break;
		              
		        default:
		            break;
	        }
	        return true;
		}


	   /**
	    * @Method: ViewDataDialog ����
	    * @Description:  �鿴������Ϣ�ĶԻ���
	    * @param viewfriendid
	    * @param viewfriendname
	    */
	   private void ViewDataDialog(String viewfriendid,String viewfriendname){
			
			LinearLayout ViewDataDialogLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.viewdata_dialog, null);
			final TextView viewid = (TextView) ViewDataDialogLayout.findViewById(R.id.ViewDataDialogID);
			final TextView viewname = (TextView) ViewDataDialogLayout.findViewById(R.id.ViewDataDialogName);
			viewid.setText("�û���:   " + viewfriendid);
			viewname.setText("�ǳ�:   " + viewfriendname);
			
			Builder builder = new AlertDialog.Builder(this);
			builder.setView(ViewDataDialogLayout);
			builder.setPositiveButton("ȷ��",
					new DialogInterface.OnClickListener()
					{
						@Override
						public void onClick(DialogInterface dialog,
								int whichButton)
						{
							// ��д�����û���¼�Ĵ���
						}
					});
			builder.show();
		}
	   
	   
	   /**
	    * 
	    * @ClassName: MyBroadcastReciver 
	    * @Description:  �㲥������, ����socket����������Ϣ,���ҽ���Ϣ��ӵ������б�
	    * @Author: Mr.Simple (�κ��)
	    * @E-mail: bboyfeiyu@gmail.com 
	    * @Date 2012-11-17 ����5:13:06 
	    *
	    */
	  private class MyBroadcastReciver extends BroadcastReceiver {  
	    	
	    	@Override
	    	public void onReceive(Context context, Intent intent) {
	    		
	    	   String action = intent.getAction();
	    	   if(action.equals("chat.SocketService.online")) {
	    		   
	    		   // ��ù㲥����Ϣ,�����յ�����Ϣ
		    	    String msg = intent.getStringExtra("broadCast");
		    	    String fName = "";			// �����ǳ�
		    	    String fid = "";			// ���ѵ�ID
		    	    String fip = "";			// ����IP
		    	    if ( msg.contains(";;") ){
		    	    	try {
			    	    	// �����Ѽ��뵽�б���
							addFriend(fName, fid, fip);
						} catch (Exception e) {
							e.printStackTrace();
						}
		    	    }
	    	   }
	    	 }
	    
	    }	// end of MyBroadcastReciver


	  /**
	   * (�� Javadoc,��д�ķ���) 
	   * @Title: onRestart
	   * @Description:  ҳ�����»�ȡ����
	   * @see android.app.Activity#onRestart()
	   */
		@Override
		protected void onRestart() {
			
			Log.d("Friend list", "onRestart");
			super.onRestart();
		}

		
		/**
		 * (�� Javadoc,��д�ķ���) 
		 * @Title: onStop
		 * @Description:  ��ǰҳ��ʧȥ����,�ж��Ƿ���Ҫ�ͷ�����
		 * @see android.app.Activity#onStop()
		 */
		@Override
		protected void onStop() {
			super.onStop();
		}
		

		/**
		 * (�� Javadoc,��д�ķ���) 
		 * @Title: onDestroy
		 * @Description:  
		 * @see android.app.ListActivity#onDestroy()
		 */
		@Override
		protected void onDestroy() {
			try{
				unregisterReceiver(mDataReceiver);
			}catch(Exception e){
				
			}
			super.onDestroy();
		}

	@Override
	public boolean onDown(MotionEvent e) {
		return false;
	}


	private int verticalMinDistance = 150;  
	private int minVelocity = 0;  
	/**
	 * (�� Javadoc,��д�ķ���) 
	 * @Title: onFling
	 * @Description: �����л�activity .��������൱��һ����������Ȼ�п������������ߣ���e1Ϊ��������㣬e2Ϊ�������յ㣬
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
		if (e1.getX() - e2.getX() > verticalMinDistance && Math.abs(velocityX) > minVelocity) {  
			  
			 // �������һ���������,�л�Activity  
	    	Intent cIntent = new Intent(FriendsListActivity.this, BaiduMapActivity.class);
			startActivityForResult(cIntent, 6); 
	        overridePendingTransition(R.anim.slide_left, R.anim.slide_right);
	       
	    } else if (e2.getX() - e1.getX() > verticalMinDistance && Math.abs(velocityX) > minVelocity) {  
	    	 // �������󻮶�������
			Intent bIntent = new Intent(FriendsListActivity.this, MainViewActivity.class);
			startActivityForResult(bIntent, 7); 
	        overridePendingTransition(R.anim.slide_left, R.anim.slide_right);
	       
	    } else if ( e2.getY() - e1.getY() > verticalMinDistance && Math.abs(velocityY) > minVelocity){

	    }
	  
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

	@Override
	public void onShowPress(MotionEvent e) {
		
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		return false;
	}

	int lastItem = 0;
	// ��������
	int totalCount = mFriendsList.size();
	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		// ���һ���б��λ��
		lastItem = firstVisibleItem + visibleItemCount;
		
	}


	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		if ( lastItem == totalCount && scrollState == SCROLL_STATE_IDLE){
			//Toast.makeText(FriendsListActivity.this, "������������", 0).show();
		}
		
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		return mGestureDetector.onTouchEvent(event);
	}
	
	

}
