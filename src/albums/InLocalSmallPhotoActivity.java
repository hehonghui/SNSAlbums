package albums;

import album.entry.R;

/**
 * Copyright (c) 2012,UIT-ESPACE( TEAM: UIT-GEEK)
 * All rights reserved.
 *
 * @Title: InLocalSmallPhotoActivity.java 
 * @Package albums 
 * @Author ������(Mr.Abert) 
 * @E-mail:uit_xuxiaojia@163.com
 * @Version V1.0
 * @Date��2012-11-9 ����5:11:43
 * @Description:
 *
 */

public class InLocalSmallPhotoActivity extends Activity{

	private String AlbumName = null;
	private String[] albumArray = null;
	public static ImageAdapter localGridAdapter = null;
	private GridView gv = null;
	private MyHandler mHandler = null;
	private Thread tLocalAlbum = null;
	private ImageButton titleReturnButton = null;
	private TextView titleTextView = null;
	
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
		AlbumName = intent.getStringExtra("albumname");
		albumArray = intent.getStringArrayExtra("albumArray");
		setTitle("����" + "/" + AlbumName);				// ���ñ���
		
		// ��ʼ���������
		initComponents() ;
		
		tLocalAlbum = new Thread(rLocalAlbum);			// ����һ�����߳�
		tLocalAlbum.start();
	}
	
	
	/**
	 * @Method: init
	 * @Description: ��ʼ������
	 */
	private void initComponents(){
		
		titleReturnButton = (ImageButton)findViewById(R.id.upgridbutton1);
		titleReturnButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		titleTextView = (TextView)findViewById(R.id.upgridtextview);
		titleTextView.setText("����/" + AlbumName + "(" + 
				PhotoAlbumActivity.AlbumsFloderName.get(AlbumName).size() + ")");

		
		localGridAdapter = new ImageAdapter(this);
		localGridAdapter.mImageList = new ArrayList<Bitmap>();
		
		gv = (GridView) findViewById(R.id.gridview); 	// GridView
        gv.setAdapter(localGridAdapter);
        // ���ͼ���еĵ����Ӧ����ת��������Ƭ�Ĵ�ͼ���Activity
		gv.setOnItemClickListener(new OnItemClickListener() {  			
	     
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {  
	        
				try{
			        Intent i = new Intent(InLocalSmallPhotoActivity.this, LocalImageView.class);  
			        i.putExtra("currentpic", arg2);
			        i.putExtra("albumArray", albumArray);
			        i.putExtra("photopath", PhotoAlbumActivity.AlbumsFloderPath.get(AlbumName).get(arg2));
			        i.putExtra("AlbumName", AlbumName);
			        startActivity(i);  
		        }catch(Exception e){
		        	Toast.makeText(InLocalSmallPhotoActivity.this, "ͼ�����������~~~~", 0).show();
		        }
			}  
		});//ClickListener
	}
	
	
	/**
	 * �������Сͼ
	 */
	Runnable rLocalAlbum = new Runnable()								
	{
		Bitmap bm;
		@Override
		public void run()
		{
			Looper mainLooper = Looper.getMainLooper ();		// �õ����߳�loop
            mHandler = new MyHandler(mainLooper);				// �������̵߳�handler
	        
	        int albumSize = PhotoAlbumActivity.AlbumsFloderPath.get(AlbumName).size();
			for(int i=0; i<albumSize && !Thread.interrupted(); i++)
			{
				bm = decodeBitmap(PhotoAlbumActivity.AlbumsFloderPath.get(AlbumName).get(i));			// ȡ��ͼƬ
	        	sendMessage(bm,i);
			}
		}
		public void sendMessage(Bitmap bitmap,int i){				// �̼߳���ݴ���
            mHandler.removeMessages(0);								// �Ƴ����ж����е���Ϣ
            Message m = mHandler.obtainMessage(1, 1, i, bitmap);	// ����Ϣ����message
            mHandler.sendMessage(m);								// ����message
		}
	};
	private class MyHandler extends Handler{       
        public MyHandler(Looper looper){
               super (looper);
        }
        @Override
        public void handleMessage(Message msg) {
        	localGridAdapter.mImageList.add((Bitmap)msg.obj);	// ����Ƭ�������
        	localGridAdapter.notifyDataSetChanged();				// �������
        }            
	}
	
	
	/**
	 * @Method: decodeBitmap
	 * @Description: ���ļ��ж�ȡͼƬ
	 * @param path   ͼƬ��·��
	 * @return
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
	 * (�� Javadoc,��д�ķ���) 
	 * @Title: onRestart
	 * @Description:  
	 * @see android.app.Activity#onRestart()
	 */
	@Override
	protected void onRestart() {
		
		if(PhotoAlbumActivity.AlbumsFloderPath.get(AlbumName).size() == 0){
			finish();
		}
		super.onRestart();
	}
	
	
}

