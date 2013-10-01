package album.entry;

/**
 * @ClassName: SplashScreen 
 * @Description:  ��������
 * @Author: Mr.Simple (�κ��)
 * @E-mail: bboyfeiyu@gmail.com 
 * @Date 2012-11-17 ����5:44:10 
 *
 */

public class SplashScreen extends Activity {
	
	private long ms=0;
	private long splashTime = 2500;
	private boolean splashActive = true;
	private boolean paused = false;
	private ImageView splashImgView = null;
			
	
	/**
	 * (�� Javadoc,��д�ķ���) 
	 * @Title: onCreate
	 * @Description: 
	 * @param savedInstanceState 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	 @Override
	    public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);

	        // ȫ�����ã����ش�������װ��
	        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
	                WindowManager.LayoutParams.FLAG_FULLSCREEN);
	        // ����������View�ģ����Դ������е����β��ֱ����غ������Ȼ��Ч,��Ҫȥ������
	        requestWindowFeature(Window.FEATURE_NO_TITLE);

	        setContentView(R.layout.splash);
	        
	        splashImgView = (ImageView)findViewById(R.id.image);
	        // AlphaAnimation����Ч��
	        AlphaAnimation animation = new AlphaAnimation(0.7f, 1.0f);
            animation.setDuration(1500);
            splashImgView.startAnimation(animation);
            
            // ����ǰҳ����ӵ�activity�Ĺ�ϣ����
            MainViewActivity.addActivityToHashSet( this );
            
	        Thread mythread = new Thread() {
	        	@Override
				public void run() {
	        	try {
		        	while (splashActive && ms < splashTime) {
			        	if(!paused)
			        	{
			        		ms = ms + 100;
			        	}
			        	sleep(100);						// �߳�˯��ʮ����
		        	}
	        	} catch(Exception e) {
	        		e.printStackTrace();
	        	}
	        	finally {
		        	Intent intent = new Intent(SplashScreen.this, LoginActivity.class);
		        	startActivity(intent);
		        	overridePendingTransition(R.anim.slide_left, R.anim.slide_right);
	        	}
	        }
	       };
	       mythread.start();
	    }
	    
}
