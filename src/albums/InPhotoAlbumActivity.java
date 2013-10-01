package albums;

import imageCache.ImageCacheToSDCard;

import network.HttpThread;

import album.entry.MyProgressDialog;
import album.entry.R;


/**
 * 
 * @ClassName: InPhotoAlbumActivity 
 * @Description:  �����ʾ ����������С��Ƭ Activity
 * @Author: xxjgood 
 * @E-mail: bboyfeiyu@gmail.com 
 * @Date 2012-11-16 ����6:39:04 
 *
 */

public class InPhotoAlbumActivity extends Activity{
	
	private MyHandler mHandler = null ;		// UI�߳��е� Handler
	private Thread smallImgThread = null;				// ����һ�����߳�
	private int deal = 3;					// Э�� 3--��������е�ͼƬ��
	private String userId = null;
	private String albumname = null;		// User albumname
	private String username = null;			// User name					
	public static int photoCount = 0;		// �������Ƭ������
	private String[] photoArray = null;		// ��Ƭ������, ����ط�����ɾ���
	public static ImageAdapter netGridAdapter = null;	// ����������ʾ��Ƭ	
	int gridNum = 0;						// ������в�����Ƭ����¼��ǰ�ǵڼ���
	private MyProgressDialog loadingDialog;
	private ImageCacheToSDCard mImgCache = ImageCacheToSDCard.getInstance();	// ����������ڻ����
	private String TAG = "�������Сͼ����";
	private List<String> mNoCacheList = new ArrayList<String>() ; // ������û�е�ͼƬ
	private List<String> mNewPhotoList = new ArrayList<String>() ; // ��ȡ������µ�ͼƬ����
	private List<String> mPhotoArray = new ArrayList<String>();// ��������ͼƬ���
	private GridView gv = null ;
	
	private TextView titleTextView;
	private ImageButton titleReturnButton;
	
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
		//�����ޱ���  
        requestWindowFeature(Window.FEATURE_NO_TITLE);  
		setContentView(R.layout.grid);
		
		Intent intent = getIntent();					// ��ȡintent ��Ϣ
		userId = intent.getStringExtra("id");
		photoCount = Integer.parseInt(intent.getStringExtra("num"));
		albumname = intent.getStringExtra("albumname");
		username = intent.getStringExtra("username");
		
		// newһ������������ʹ���Լ���ImageList
		netGridAdapter = new ImageAdapter(InPhotoAlbumActivity.this);   		   
        netGridAdapter.mImageList = new ArrayList<Bitmap>();  	// ���������ΪphotoCount
        
        // �����Զ����title

        titleTextView = (TextView)findViewById(R.id.upgridtextview);
		titleTextView.setText(albumname + "(" + photoCount + ")");
		
		titleReturnButton = (ImageButton)findViewById(R.id.upgridbutton1);
		titleReturnButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});

		
		LayoutGridView();								// ����
		
		if ( photoCount != 0){
			loadingDialog = new MyProgressDialog(this, "ͼƬ������,���Ժ򡤡���");
			loadingDialog.show();
			// �����߳�,����СͼƬ
			NewThreadForAsk newthreadforask = new NewThreadForAsk();
			newthreadforask.StartNewThread();
		}else{
			Toast.makeText(this, "���Ϊ��...", 0).show() ;
			finish() ;
		}
		

	}
	

	/**
	 * @ClassName: MyHandler 
	 * @Description:  �ڲ���  MyHandler���������߳���Ϣ
	 * @Author: xxjgood 
	 * @E-mail: bboyfeiyu@gmail.com 
	 * @Date 2012-11-16 ����6:39:41 
	 *
	 */
	private class MyHandler extends Handler{       
        public MyHandler(Looper looper){
               super (looper);
        }
        @Override
        public void handleMessage(Message msg) { 	// ������Ϣ
        	Log.d("1","handle");
        	if(deal == 3){							// 3Ϊ��������е�ͼƬ��
        		String s1 = msg.obj.toString();		// �������е�������Ƭ��
        		s1 = s1.trim();						// ȥ�ո�
        		photoArray = s1.split(";;");		// ����ַ�����
        		// �������е����ȫ�����List��
        		addToList();
        		// �ӻ����ж�ȡСͼ
        		try {
					getCacheImageFromSD( 1 ) ;
				} catch (Exception e) {
					Log.d(TAG, "�ӻ����ж�ȡͼƬʧ��") ;
					e.printStackTrace();
				}
        		deal = 100;							// ����deal=100���´�ִ��else����ͼ ��
        	}
        	else{												// 4Ϊ��ͼ
        		try {
        			netGridAdapter.mImageList.add( (Bitmap)msg.obj );		// ��С��Ƭ���������
            		netGridAdapter.notifyDataSetChanged();					// ������������ʹ�������Ƭ������ʾ����
            		gridNum++;
            		if(gridNum == 1){
            			loadingDialog.dismiss();
            		}
            		
	        		Log.d("", "ͼƬ�Ѿ�����");
	        		// ��mPhotoArray��ס
	        		synchronized ( mPhotoArray ) {
	        			// ������������ͼƬд��SD����
		        		if ( ! (mPhotoArray.contains("error") || photoArray[0].equals("fail") ) ){
		        			// �ļ���
		        			String filename = photoArray[gridNum-1];
		            		try {
		                		// ��ͼƬ���浽SD����
								mImgCache.saveBmpToSd((Bitmap)msg.obj, filename , 1);
							} catch (Exception e) {
								Log.d(TAG, filename + "д��SD��ʧ��!" ) ;
								e.printStackTrace();
							}
		        		} // end if
					}
	        		
	    		}catch(Exception e){
	    			Log.d(TAG, "ͼƬת��ʧ��") ;
	    			e.printStackTrace();
	    		}
        	}
        }            
	}
	
	
	/**
	 * 
	 * @Method: addToList 
	 * @Description: ���ļ���ȫ�����List��
	 * @return void  �������� 
	 * @throws
	 */
	private void addToList(){
		
		for (String item : photoArray){
			// 
			mPhotoArray.add( item );
			Log.d("ͼƬ��", item);
		}
	}
	
	
	/** ʹ���������Ҫ��packData(int i,int tag)�е�photoArray�ĳ�mPhotoArray
	 * 
	 * @Method: getCacheImageFromSD 
	 * @Description: �ӻ����ж�ȡͼƬ
	 * @param type   ͼƬ����,1ΪСͼ����,2Ϊ��ͼ����
	 * @return void  �������� 
	 * @throws
	 */
	private void getCacheImageFromSD(int type) throws Exception{
		Log.d("��ȡСͼ����", "���ڴ濨�л�ȡ");

		if ("error".equals(mPhotoArray.get(0))) {
			// �ٴ�����СͼƬ���߳�
			NewThreadForAsk newthreadforask = new NewThreadForAsk();	
			newthreadforask.StartNewThread();
			Toast.makeText(this, "���粻̫�ȶ�,�б��ȡʧ��,Ԫ��,��˵��...", 1).show() ;
			return;
		}

		// ���,���δӻ����ж�ȡͼƬ
		Iterator<String> iter = mPhotoArray.iterator();
		while ( iter.hasNext() ) {
			// ��ȡ��ǰ�ļ����ļ���
			String filename = iter.next().trim();
			// �����첽�������ͼƬ����
			new CacheAsycTask().execute(filename, "small");
			Thread.sleep(100) ;

		} // end of while()

	}
	

	/**
	 * 
	 * @Method: LayoutGridView 
	 * @Description:  ��񲼾� ,�������ü�����
	 * @throws
	 */
	private void LayoutGridView(){											
		gv = (GridView) findViewById(R.id.gridview); 		// GridView
        gv.setAdapter(netGridAdapter);										// ����������
  
        gv.setOnItemClickListener(new OnItemClickListener() {  		// ���ü���
            @Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {  
                
                Intent i=new Intent(InPhotoAlbumActivity.this, BigImageView.class);  
                i.putExtra("userid", userId);
                i.putExtra("albumname", albumname);
                i.putExtra("username", username);
                i.putExtra("photoArray", photoArray);
                i.putExtra("currentpic", arg2);
                startActivity(i);  							// �������е�ĳ����Ƭ����ת�����Ӧ�Ĵ�ͼActivity
            }  
        });
	}
	

	/**
	 * 
	 * @ClassName: NewThreadForAsk 
	 * @Description: �������������Ƭ��Ͷ����С��Ƭ
	 * @Author: xxjgood 
	 * @E-mail: bboyfeiyu@gmail.com 
	 * @Date 2012-11-16 ����4:18:40 
	 *
	 */
	class NewThreadForAsk{
		private ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		
		/**
		 * 
		 * @Constructor: 
		 * @Description:
		 */
		public NewThreadForAsk() {
			
		}
		
		/**
		 * @Method: StartNewThread 
		 * @Description: �����߳�   
		 * @throws
		 */
		public void StartNewThread(){
			smallImgThread = new Thread( smallImgRbl );
			smallImgThread.start();
		}
		
		
		/**
		 *  Runnable�ĸ�д
		 */
		Runnable smallImgRbl = new Runnable()							
		{
			@Override
			public void run()
			{
				Log.d("1", "new thread" + Thread.currentThread().getId());
				packData(0,0);													// ������
				HttpThread h = new HttpThread(nameValuePairs,3);		// �������緢����Ķ��󣬴������
				String s = (String)h.sendInfo();								// ��������������󣬲��������
				Log.d("1","return" + s);
				sendMessage(s);												// �����̵߳���ݷ��͵����߳�
				
				try {
					Thread.sleep(200);// 4.24�����޸�
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
		
			/**
			 * 
			 * @Method: sendMessage 
			 * @Description: ����String �����߳�MyHandler
			 * @param s   
			 * @throws
			 */
			public void sendMessage(String s){
                Looper mainLooper = Looper.getMainLooper ();		// �õ����߳�loop
                String msg ;
                mHandler = new MyHandler(mainLooper);				// �������̵߳�handler
                msg = s;
                mHandler.removeMessages(0);							// �Ƴ����ж����е���Ϣ
                Message m = mHandler.obtainMessage(1, 1, 1, msg);	// ����Ϣ����message
                mHandler.sendMessage(m);							// ����message
                Log.d("1","sendstring");
			}
			
	
		};
		
		
		/**
		 * @Method: packData 
		 * @Description: ������
		 * @param i
		 * @param tag   
		 * @throws
		 */
		void packData(int i,int tag)
		{
			Log.d("1", "the data is packed");
			if(tag == 0){
				nameValuePairs.add(new BasicNameValuePair("protocol", "getImageName"));// ��װ��ֵ��
				nameValuePairs.add(new BasicNameValuePair("id", userId));
				nameValuePairs.add(new BasicNameValuePair("albumName", albumname));
			}
			if(tag == 1){
				Log.d("1","the data is packed tag1");
				nameValuePairs.add(new BasicNameValuePair("protocol", "getSmallImage"));// ��װ��ֵ��
				Log.d("1","fengzhuang");
				nameValuePairs.add(new BasicNameValuePair("id", userId));
				nameValuePairs.add(new BasicNameValuePair("albumName", albumname));
				Log.d("1","albumname");
				nameValuePairs.add(new BasicNameValuePair("imageName", photoArray[i]));
				Log.d("1","packend");
			}
			
		}
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
		
		netGridAdapter.notifyDataSetChanged();			// ˢ��ҳ��,������ͼ
	}
	
	
	/**
	 * 
	 * @ClassName: CacheAsycTack 
	 * @Description: ��ִ��executeʱ��Ҫ������������,��һ��ΪҪ��ȡ������ļ���,�ڶ���ΪͼƬ��С,"big"Ϊ��ͼ,����ΪСͼ
	 * @Author: Mr.Simple 
	 * @E-mail: bboyfeiyu@gmail.com 
	 * @Date 2012-11-10 ����8:20:23 
	 *
	 */
	private int temp = 0;

	private class CacheAsycTask extends AsyncTask<String, Void, Map<String, Bitmap> > {
		// ����·��
		private String cachePath = Environment.getExternalStorageDirectory()
											+ File.separator + "a_sns_small_cache";
		private int size = 0;

		
		/*
		 * (�� Javadoc,��д�ķ���) <p>
		 * Title: onPreExecute</p> 
		 * <p>Description: </p>
		 * @see android.os.AsyncTask#onPreExecute()
		 */
		@Override
		protected void onPreExecute() {
			size = mPhotoArray.size();
			Log.d(TAG, "mPhotoArray.size() =" + size);
		}

		
		/*
		 * (�� Javadoc,��д�ķ���) <p>
		 * Title: doInBackground</p> 
		 * <p>Description: </p>
		 * @param params
		 * @return
		 * @see android.os.AsyncTask#doInBackground(Params[])
		 */
		@SuppressWarnings("finally")
		@Override
		protected Map<String, Bitmap> doInBackground(String... params) {

			Map<String, Bitmap> map = new HashMap<String, Bitmap>();
			synchronized (params[0]) {
				temp++;
				Log.d(TAG, "������ִ����" + temp);
				// �ļ���
				String fileName = params[0];
				// ��ȡ��ͼƬ����
				String type = params[1];
				Bitmap mBitmap = Bitmap.createBitmap(5, 5,
						Bitmap.Config.ARGB_4444);

				if ("big" == type) {
					cachePath = Environment.getExternalStorageDirectory()
							+ File.separator + "a_sns_cache";
				}

				try {

					File file = new File(cachePath, fileName);
					InputStream input = new FileInputStream(file);
					// ͨ���ļ�������ͼƬ
					mBitmap = BitmapFactory.decodeStream(input);
					input.close();
					// ��ͼƬ���뵽map��
					map.put(fileName, mBitmap);

				} catch (Exception e) {
					// ��û�л����ͼƬ�����ӵ�mNoCacheList
					synchronized (mNoCacheList) {
						mNoCacheList.add(fileName);
						Log.d(TAG, fileName + " ****û�л���");
					}
					e.printStackTrace();
				} finally {
					return map;
				}
			}

		}

		/*
		 * (�� Javadoc,��д�ķ���)
		 * Title: onPostExecute
		 * Description: 
		 * @param result
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */
		@Override
		protected void onPostExecute(Map<String, Bitmap> result) {

			synchronized (result) {
				Set<Map.Entry<String, Bitmap>> bitmap = result.entrySet();
				Iterator<Map.Entry<String, Bitmap>> iter = bitmap.iterator();
				while (iter.hasNext()) {
					Map.Entry<String, Bitmap> map = iter.next();

					if (result != null && map.getValue().getWidth() != 5
							&& map.getValue().getHeight() != 5) {
						// ��ȡ��ȡ����Ľ��,��ͼƬ��ӵ�list��
						netGridAdapter.mImageList.add(map.getValue());
						netGridAdapter.notifyDataSetChanged();
						loadingDialog.dismiss();

						// ����ͼƬ��ͼƬ�б����Ƴ� , ����ͼƬ�����Ƴ�
						mPhotoArray.remove(map.getKey());
						mNewPhotoList.add(map.getKey());
						Log.d(TAG, "�첽����--ͼƬ��ȡ�ɹ�");
					} else {
						Log.d(TAG, "--�첽�����ȡʧ��");
					}
				}
			} // end of synchronized

			// ����ֻ����ȡʧ�ܵ�,���������첽����ӷ�������ȡ
			if (mNoCacheList.size() > 0 && temp == size) {
				Log.d(TAG, "������ȡСͼƬ��**�첽�����߳�");
				temp = 0;
				// �����������û�л����ͼƬ
				//new ImageAsycTask().execute();
				mSize = mNoCacheList.size() ;
				Log.d(TAG, "mNoCacheList size = " + mSize ) ;
				for(int i=0; i<mSize; i++){
					new GetImageAsycTask().execute( i ) ;
				}
				
			} else {
				// ���µ�ͼƬ�������鵼�뵽photoArray��
				try {
					mNewPhotoList.toArray(photoArray);
					loadingDialog.dismiss();
				} catch (Exception e) {
					Log.d(TAG, "����photoArrayʧ��");
					e.printStackTrace();
				}
			}

		}

	} // end of asycTack
	
	

	/**
	 * 
	 * @ClassName: GetImageAsycTask 
	 * @Description: �����������û�л����СͼƬ���첽����
	 * @Author: Mr.Simple 
	 * @E-mail: bboyfeiyu@gmail.com 
	 * @Date 2012-11-10 ����8:59:51 
	 *
	 */
	private int mSize = 0 ;
	private int mCount = 0;
	private class GetImageAsycTask extends AsyncTask<Integer, Void, Map<String,Bitmap> >{
		
		ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		private ImageCacheToSDCard mImgCache2SD = ImageCacheToSDCard.getInstance() ;
		
		@Override
		protected void onPreExecute() {
			nameValuePairs.clear();
			loadingDialog.show() ;
		}

		/*
		 * (�� Javadoc,��д�ķ���) 
		 * <p>Title: doInBackground</p> 
		 * <p>Description: ��û�б?���л�ȡû�л����ͼƬ���֣�����������������ͼƬ
		 * @param params
		 * @return 
		 * @see android.os.AsyncTask#doInBackground(Params[])
		 */
		@Override
		protected Map<String,Bitmap> doInBackground(Integer... params) {
			
			Bitmap bmp = null;
			Map<String, Bitmap> map = new HashMap<String, Bitmap>();
			try {
				// ��û�л�����б��л�ȡ�ļ���
				String imgName = mNoCacheList.get(params[0]);
				nameValuePairs.add(new BasicNameValuePair("protocol",
						"getSmallImage"));// ��װ��ֵ��
				nameValuePairs.add(new BasicNameValuePair("id", userId));
				nameValuePairs.add(new BasicNameValuePair("albumName",
						albumname));
				// �����������Ҫ��ȡ��ͼƬ,������List����mPhotoArray��
				nameValuePairs
						.add(new BasicNameValuePair("imageName", imgName));

				// �����������СͼƬ���߳�,һ��һ�Ż�ȡ
				HttpThread h = new HttpThread(nameValuePairs, 100);
				bmp = (Bitmap) h.sendInfo();
				Log.d("ASYC", "�����������Сͼ" + imgName);
				// ��������ŵ�map��
				map.put(imgName, bmp);

				loadingDialog.dismiss();

			} catch (Exception e) {
				Log.d(TAG, "����Сͼʧ��");
				e.printStackTrace();
			}

			return map;
		}

		/**
		 * (�� Javadoc,��д�ķ���) 
		 * @Title: onPostExecute
		 * @Description: 
		 * @param result 
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */
		@Override
		protected void onPostExecute(Map<String,Bitmap> result) {
			
			synchronized (result) {
				Set<Map.Entry<String,Bitmap> > bitmap = result.entrySet();
				Iterator<Map.Entry<String,Bitmap> > iter = bitmap.iterator() ;
				while ( iter.hasNext() ){
					Map.Entry<String,Bitmap> map = iter.next() ;
					// ��ȡ��ȡ����Ľ��,��ͼƬ��ӵ�list��
					netGridAdapter.mImageList.add( map.getValue() );
					netGridAdapter.notifyDataSetChanged();
					loadingDialog.dismiss() ;
					
					Log.d(TAG, "*******����ɹ�********") ; 
					// ���µ��ļ���ŵ�newPhotoList
					mNewPhotoList.add( map.getKey() ) ;
					synchronized (map) {
						try {
							// ��ͼƬд�뵽SD����, Сͼģʽ
							mImgCache2SD.saveBmpToSd(map.getValue(), map.getKey(), 1) ;
							Thread.sleep( 100 ) ;
						}catch(Exception e){
							Log.d(TAG, "д�뻺��ʧ��--ImageAsycTack") ; 
						}
						
						iter.remove() ;
					}

					// ���µ����������ε��뵽photo������
					if ( mSize == ++mCount){
						try {
							mNewPhotoList.toArray( photoArray ) ;
							mNoCacheList.clear() ;
							loadingDialog.dismiss() ;
							Log.d(TAG, "����photoArray�ɹ�") ;
						}catch(Exception e){
							Log.d(TAG, "����photoArrayʧ��") ;
							e.printStackTrace() ;
						}
					}
					Log.d(TAG, "mCount=" + mCount) ;

				} // end of while
			} // end of synchronized
		} // end of onPostExecute
		
	} // end of GetImageAsycTask
	
}
