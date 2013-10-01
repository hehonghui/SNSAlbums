package map;

import network.HttpThread;

import album.entry.LoginActivity;
import album.entry.MainViewActivity;
import album.entry.R;
import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.GeoPoint;
import com.baidu.mapapi.ItemizedOverlay;
import com.baidu.mapapi.LocationListener;
import com.baidu.mapapi.MKAddrInfo;
import com.baidu.mapapi.MKDrivingRouteResult;
import com.baidu.mapapi.MKGeocoderAddressComponent;
import com.baidu.mapapi.MKLocationManager;
import com.baidu.mapapi.MKPlanNode;
import com.baidu.mapapi.MKPoiResult;
import com.baidu.mapapi.MKSearch;
import com.baidu.mapapi.MKSearchListener;
import com.baidu.mapapi.MKTransitRouteResult;
import com.baidu.mapapi.MKWalkingRouteResult;
import com.baidu.mapapi.MapActivity;
import com.baidu.mapapi.MapController;
import com.baidu.mapapi.MapView;
import com.baidu.mapapi.MyLocationOverlay;
import com.baidu.mapapi.OverlayItem;
import com.baidu.mapapi.PoiOverlay;
import com.baidu.mapapi.RouteOverlay;
import com.baidu.mapapi.TransitOverlay;


/*
* Copyright (c) 2012,UIT-ESPACE
* All rights reserved.
*
* �ļ���ƣ�BaiduMapActivity.java  (�ش�ע��,����Google���½��΢���ϵ,���¹ȸ��ͼ��ʹ�ò�̫�ȶ�,���ת�ðٶȵ�ͼ)
* ժ Ҫ����ͼģ��
* 
* ���ܣ�
* 1.��ȡ�ҵ�λ��
* 2.�����ҵ�λ��
* 3.·������ ���������ݳ������У�
* 4.��ȡͬ�Ǻ��ѵĵ�ͼ��ʾ
* 5.��ȡ������λ�õ�·��
* 6.��ͼ��ͼ�޸�
* 7.���⳵����      (����)
*  
* ��ǰ�汾��1.1
* �� �ߣ��κ��
* ������ڣ�2012��11��3��
*
* ȡ��汾��1.0
* ԭ���� ���κ��
* ������ڣ�2012��9��12��
* 
*/


public class BaiduMapActivity extends MapActivity{

	private BMapManager mBMapManager = null;									// ��ͼ�������
	private MKLocationManager mLocationManager = null;							// λ�ù�����
	private MapView mMapView = null;											// MapView����
	private final String MAP_KEY = "382D9C48CADF05B90D8CB985772514087A8DB779";	// �ٶȵ�ͼ��key
	private MapController mMapContoller = null;
	private GeoPoint mMyGeoPoint;
	private MyOverItem mOverLays = null;										// ��ͼ�ϵĸ�����
	private final String TAG = "BAIDU MAP";
	
    private MKSearch mMKSearch;  												// ��ѯ����
    private GeoPoint geoPointTo;												// ���ѵ�λ��
    private int mPoiFlag = 1; 													// ·����ʻ��ʽ
    private static View mPopView = null;										// ���markʱ����������View
	private Button findBtn = null;												// �Һ���·��
	private String mFCity = null;												// �������ڳ���
	private int mAddrFlag = 0;													// ��ȡ���е�����
	private int mIndex = 0;														// �˵�ѡ�������
	private int mFindBtnFlag = 0;												// ������ť���Ǻ���
	private String mTelephone = null;
	
	
	/**
	 * ���� �� ҳ�洴��
	 * (non-Javadoc)
	 * @see android.app.ActivityGroup#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		// ����Ϊ�ޱ���ģʽ
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.baidumap);
				
		// ��ͼ����ؼ���ʼ��
		mBMapManager = new BMapManager(this);
		mBMapManager.init(MAP_KEY, null);
		super.initMapActivity(mBMapManager);
		// ��ʼ��λ�ù�����
	 	mLocationManager = mBMapManager.getLocationManager();
	 	// ע�����,λ�øı���ʵʱ��ñ���λ��
	 	mLocationManager.requestLocationUpdates( mLocationListener );
	 	
		// ��ͼ��ʾ�ؼ�
		mMapView = (MapView)findViewById(R.id.bmapsView);
		mMapView.setBuiltInZoomControls( true );				// �������õ����ſؼ�
		mMapView.setTraffic( true );							// ����ʵʱ��ͨ��Ϣ
		
		mMapContoller = mMapView.getController();				// ��ȡ��ͼ����Ȩ
		GeoPoint point = new GeoPoint((int) (39.02 * 1E6),
									   (int) (121.44 * 1E6));   // �ø�ľ�γ�ȹ���һ��GeoPoint����λ��΢�� (�� * 1E6)
		mMapContoller.setCenter( point );  						// ���õ�ͼ���ĵ�
		mMapContoller.setZoom(12);    							// ���õ�ͼzoom����
		
 		mMKSearch = new MKSearch();								// ������Ķ���
    	mMKSearch.init(mBMapManager, new MySearchListener()); 	// ������
    	
    	initComponents();										// ��ʼ���ؼ�
		turnGPSOn();											// ����GPS
		
		mHandler.postDelayed(myLocationRunnable, 1500);			// �������ʾ�ҵ�λ��,wifiģʽ����ʱ����
		
		MainViewActivity.addActivityToHashSet( this );	// ��ҳ����ӵ�map��
		
	}

	
	/*
	 * (�� Javadoc,��д�ķ���) 
	 * <p>Title: onRestoreInstanceState</p> 
	 * <p>Description: </p> 
	 * @param savedInstanceState 
	 * @see android.app.Activity#onRestoreInstanceState(android.os.Bundle)
	 */
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		
		Log.d(TAG, "onRestoreInstanceStateȡ���ҵ�λ��" + savedInstanceState.getString("myLocation")) ;
	}

	
	/*
	 * (�� Javadoc,��д�ķ���) 
	 * <p>Title: onSaveInstanceState</p> 
	 * <p>Description: </p> 
	 * @param outState 
	 * @see android.app.Activity#onSaveInstanceState(android.os.Bundle)
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {

		outState.putString("myLocation", mMyGeoPoint.toString() ) ;
		Log.d(TAG, "onSaveInstanceState�����ҵ�λ��") ;
		super.onSaveInstanceState(outState);
	}


	/**
	 * ���� �� �Ƿ���ʾ·��
	 * (non-Javadoc)
	 * @see com.baidu.mapapi.MapActivity#isRouteDisplayed()
	 */
	@Override
	@Override
	protected boolean isRouteDisplayed() {
	
		return false;
	}
	
	/**
	 * ���� �� ҳ����� (non-Javadoc)
	 * 
	 * @see com.baidu.mapapi.MapActivity#onDestroy()
	 */
	@Override
	@Override
	protected void onDestroy() {
		if (mBMapManager != null) {
			mBMapManager.destroy();
			mBMapManager = null;
		}
		// ����ҳ���Set���Ƴ�
		MainViewActivity.removeFromSet(this);
		super.onDestroy();
	}

	/**
	 * ���� �� ҳ��ת����ͣ״̬ (non-Javadoc)
	 * 
	 * @see com.baidu.mapapi.MapActivity#onPause()
	 */
	@Override
	@Override
	protected void onPause() {
		if (mBMapManager != null) {
			mBMapManager.stop();
		}
		// �ر�GPS
		turnGPSOff();
		super.onPause();

	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see com.baidu.mapapi.MapActivity#onPause()
	 */
	@Override
	@Override
	protected void onResume() {
		if (mBMapManager != null) {
			mBMapManager.start();
		}
		super.onResume();
	}

	/**
	 * 
	 * @Method: init_components
	 * @Description: ��ʼ�����
	 * @return void ��������
	 * @throws
	 */
	private void initComponents() {

		// �����ﲻͼ��
		Drawable marker = getResources().getDrawable(R.drawable.location_48);
		// ��ͼ�ϵĸ�����
		mOverLays = new MyOverItem(marker, BaiduMapActivity.this);

		// �������markʱ�ĵ�������
		mPopView = super.getLayoutInflater().inflate(R.layout.popview, null);
		mMapView.addView(mPopView, new MapView.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, null,
				MapView.LayoutParams.TOP_LEFT));
		mPopView.setVisibility(View.GONE);

		// ���������ݴ����еİ�ť,���ʱ��ȡ������λ�õ�·��
		findBtn = (Button) mPopView.findViewById(R.id.findBtn);
		findBtn.getBackground().setAlpha(200);
		findBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				if (geoPointTo != null && mFindBtnFlag == 1) {
					mPopView.setVisibility(View.GONE);
					displayToast("·��������,���Ժ�...");
					getRoute(geoPointTo); // ���ҵ�����λ�õ�·��
				} else if (mFindBtnFlag == 2) { // ���⳵ģʽ

					displayToast("����绰���û�");
					/* ����һ���µ�Intent ����action.CALL�ĳ�����ͨ��Uri���ַ���� */
					Intent myIntentDial = new Intent(
							"android.intent.action.CALL", Uri.parse("tel:"
									+ mTelephone));
					startActivity( myIntentDial );
				}
			}
		});

	}

	
	/**
	 * ���� �� ��ʼ������UI�߳�Ͷ����Ϣ,��ʾ�ҵ�λ��,���ҵ�λ�����ͼ��
	 * 
	 */
	Handler mHandler = new Handler();
	Runnable myLocationRunnable = new Runnable() {

		@Override
		public void run() {

			showMyLocation();
			if (mMyGeoPoint != null) {
				// ���ҵı�ʶ��ӵ���ͼ��
				addFriendToMap(mMyGeoPoint, 1, LoginActivity.mineID,
						LoginActivity.mineName);
			}

		}
	};
	    
		
	/**
	 * ���� �� �ڵ�ͼ����ʾ�ҵ�λ��
	 * 
	 */
	private boolean showMyLocation() {
		// ��Ӷ�λͼ��
		MyLocationOverlay myLocation = new MyLocationOverlay(this, mMapView);
		myLocation.enableMyLocation(); 				// ���ö�λ
		mMapView.getOverlays().add( myLocation );   // �ڵ�ͼ�ϻ��Ƴ���ǰλ�õ�Overlay
		mMapView.invalidate() ;
		
		// ��ȡ�ҵ����
		mMyGeoPoint = myLocation.getMyLocation();
		if (mMapContoller != null && mMyGeoPoint != null) {
			mMapContoller.animateTo(mMyGeoPoint);
			mMapContoller.setCenter(mMyGeoPoint);
			return true;
		} else {
			Log.d(TAG, "mMapContoller ��ָ��.");
		}
		return false;

	}

	/**
	 * λ�ñ仯������,ʵʱ�����Լ���λ��
	 */
	LocationListener mLocationListener = new LocationListener() {
		
		@Override
		public void onLocationChanged(Location location) {
			// λ�øı�,����ʾ�ҵ�λ��
			showMyLocation();
		}
	};

	
	/**
	 * ���� ������GPS
	 * 
	 */
	private void turnGPSOn() {

		// ��ȡ�������GPS��provider
		String provider = Settings.Secure.getString(getContentResolver(),
				Settings.Secure.LOCATION_PROVIDERS_ALLOWED);

		if (!provider.contains("gps") && canToggleGPS()) { // ���GPS�ǹرյĺ�GPS������

			final Intent poke = new Intent();
			poke.setClassName("com.android.settings",
					"com.android.settings.widget.SettingsAppWidgetProvider");
			poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
			poke.setData(Uri.parse("3"));
			sendBroadcast(poke);
		}
	}

	/**
	 * ���� ���ر�GPS
	 */
	private void turnGPSOff() {
		String provider = Settings.Secure.getString(getContentResolver(),
				Settings.Secure.LOCATION_PROVIDERS_ALLOWED);

		if (provider.contains("gps")) { // if gps is enabled
			final Intent poke = new Intent();
			poke.setClassName("com.android.settings",
					"com.android.settings.widget.SettingsAppWidgetProvider");
			poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
			poke.setData(Uri.parse("3"));
			sendBroadcast(poke);
			Log.d("GPS", "**�ر�GPS**");
		}
	}

	/**
	 * ���� ���ж� GPS�ܷ��
	 * 
	 */
	private boolean canToggleGPS() {
		PackageManager pacman = getPackageManager();
		PackageInfo pacInfo = null;

		try {
			pacInfo = pacman.getPackageInfo("com.android.settings",
					PackageManager.GET_RECEIVERS);
		} catch (NameNotFoundException e) {
			return false;
		}

		if (pacInfo != null) {
			for (ActivityInfo actInfo : pacInfo.receivers) {
				// �Ƿ��ܿ���GPS
				if (actInfo.name
						.equals("com.android.settings.widget.SettingsAppWidgetProvider")
						&& actInfo.exported) {
					Log.d(TAG, "GPS�ܿ���");
					return true;
				}
			}
		}

		Log.d(TAG, "GPS�����ܿ���");
		return false;
	}

	
	/**
	 * ���� �� �˵� (non-Javadoc)
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// setIcon()����Ϊ�˵�����ͼ�꣬����ʹ�õ���ϵͳ�Դ��ͼ��
		menu.add(Menu.NONE, Menu.FIRST + 1, 1, "�ҵ�λ��").setIcon(
				android.R.drawable.ic_menu_mylocation);

		menu.add(Menu.NONE, Menu.FIRST + 2, 2, "����").setIcon(
				android.R.drawable.ic_menu_search);

		menu.add(Menu.NONE, Menu.FIRST + 3, 3, "���⳵").setIcon(
				android.R.drawable.ic_menu_compass);

		menu.add(Menu.NONE, Menu.FIRST + 4, 4, "��ͼ").setIcon(
				android.R.drawable.ic_menu_slideshow);
		menu.add(Menu.NONE, Menu.FIRST + 5, 5, "����λ��").setIcon(
				android.R.drawable.ic_menu_share);
		menu.add(Menu.NONE, Menu.FIRST + 6, 6, "�����ͼ").setIcon(
				android.R.drawable.ic_menu_info_details);
		menu.add(Menu.NONE, Menu.FIRST + 7, 7, "�˳�").setIcon(
				android.R.drawable.ic_lock_power_off);

		return true;
	}

	/**
	 * ���� �� �˵�ѡ���¼� (non-Javadoc)
	 * 
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case Menu.FIRST + 1: // �ҵ�λ��
			showMyLocation();
			break;
		case Menu.FIRST + 2: // �����˵�,����·���������ܱ�������ͬ�Ǻ���
			searchMenu();
			break;
		case Menu.FIRST + 3: // ���⳵����
			texiSelectMenu();
			break;
		case Menu.FIRST + 4: // ��ͼ�޸�
			changeMapViewMenu();
			break;
		case Menu.FIRST + 5: // �����ҵ�λ��
			shareMyLocation();
			break;
		case Menu.FIRST + 6: // �����ͼ
			clearMapView();
			showMyLocation();
			break;
		case Menu.FIRST + 7: // �˳�����
			MainViewActivity.killCurrentApp(BaiduMapActivity.this);
			break;
		default:
			break;

		}
		return false;

	} // end of onOptionsItemSelected
	 	
	
	/**
	 * 
	 * @Method: clearMapView
	 * @Description: ��������ͼview
	 * @throws
	 */
	private void clearMapView() {
		try {
			mMapView.getOverlays().clear();
			mMapView.getOverlays().clear();
			// ���ѵ���ͼ���
			mOverLays.clearOverLayItems();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @Method: getRoute
	 * @Description: ����·��,���ҵ�λ�õ����ѵ�λ��
	 * @param fGeoPoint
	 * @return void ��������
	 * @throws
	 */
	private void getRoute(GeoPoint fGeoPoint) {
		// ���
		MKPlanNode start = new MKPlanNode();
		start.pt = mMyGeoPoint;

		// �յ�
		MKPlanNode end = new MKPlanNode();
		end.pt = fGeoPoint;

		// ���üݳ�·���������ԣ�ʱ�����ȡ��������ٻ�������
		mMKSearch.setDrivingPolicy(MKSearch.ECAR_TIME_FIRST);
		mMKSearch.drivingSearch(null, start, null, end);
	}
	 	
	/**
	 * 
	 * @Method: getRoute
	 * @Description: ����·��,�Ե�ַ��ѯ
	 * @param from
	 *            Դ��ַ
	 * @param to
	 *            Ŀ���ַ
	 * @param city
	 *            ���ڳ���
	 * @return void ��������
	 * @throws
	 */
	private void getRoute(String from, String to, String city) {

		Log.d(TAG, city + from + to);
		if (mMapView.getOverlays().size() > 3) {
			mMapView.getOverlays().remove(0);
		}

		// ���
		MKPlanNode stNode = new MKPlanNode();
		stNode.name = from;
		// ���û��ָ������ַ��Ĭ��Ϊ�ҵ�λ��
		if (from.equals("") && mMyGeoPoint != null) {
			stNode.pt = mMyGeoPoint;
		}

		// �յ�
		MKPlanNode enNode = new MKPlanNode();
		enNode.name = to;

		// ���üݳ�·���������ԣ�ʱ�����ȡ��������ٻ�������
		mMKSearch.setDrivingPolicy(MKSearch.ECAR_TIME_FIRST);
		if (mPoiFlag == 1) {

			mMKSearch.drivingSearch(city, stNode, city, enNode);
			displayToast("�ݳ�·��������...");

		} else if (mPoiFlag == 2) {

			displayToast("����·��������...");
			mMKSearch.setTransitPolicy(MKSearch.EBUS_TRANSFER_FIRST);
			mMKSearch.transitSearch(city, stNode, enNode);

		} else if (mPoiFlag == 3) {

			displayToast("����·��������...");
			mMKSearch.walkingSearch(city, stNode, city, enNode);
		}

		mPoiFlag = 1;

	}

	/**
	 * 
	 * @Method: searchRoude
	 * @Description: ·���������������봰��
	 * @return void ��������
	 * @throws
	 */
	private void searchRoude() {
		// ��ȡ�ô��ڵ�view���ܽ��л�ȡ������е��ı�
		LayoutInflater factory = LayoutInflater.from(this);
		final View dlgView = factory.inflate(R.layout.alert_dialog_text_entry,
				null);

		new AlertDialog.Builder(BaiduMapActivity.this)
				.setIcon(R.drawable.cute_48)
				.setTitle(R.string.alert_dialog_text_entry)
				.setView(dlgView)

				.setPositiveButton(R.string.alert_dialog_ok,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int whichButton) {

								EditText fromEdit = (EditText) dlgView
										.findViewById(R.id.from_edit);
								EditText toEdit = (EditText) dlgView
										.findViewById(R.id.to_edit);
								EditText cityEdit = (EditText) dlgView
										.findViewById(R.id.city_edit);

								String fromAddr = fromEdit.getText().toString();
								String toAddr = toEdit.getText().toString();
								String city = cityEdit.getText().toString();

								getRoute(fromAddr, toAddr, city); // ��ȡ���ص�·��

							}
						})
				.setNegativeButton(R.string.alert_dialog_cancel,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int whichButton) {

							}
						}).show();

		// ��ѡ��ť,����·�������ķ�ʽ,�ݳ������������е�
		RadioGroup radioGroup = (RadioGroup) dlgView
				.findViewById(R.id.radioGroup);
		radioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {

				if (checkedId == R.id.carRadioBtn) {
					mPoiFlag = 1;
				}
				if (checkedId == R.id.busRadioBtn) {
					mPoiFlag = 2;
				}
				if (checkedId == R.id.walkRadioBtn) {
					mPoiFlag = 3;
				}
			}
		});

	}

	/****
	 * ���� �� ������������ҵ����λ��,������������
	 * 
	 ****/
	private void shareMyLocation() {
		// WIFIģʽ���޷���ȡ���
		if (mMyGeoPoint == null) {
			// ��Ӷ�λͼ��
			MyLocationOverlay myLocation = new MyLocationOverlay(this, mMapView);
			myLocation.enableMyLocation(); // ���ö�λ
			// ��ȡ�ҵ����
			mMyGeoPoint = myLocation.getMyLocation();
		}

		new Thread(new Runnable() {

			@Override
			public void run() {

				String mapX = String.valueOf(mMyGeoPoint.getLatitudeE6()); // γ��
				String mapY = String.valueOf(mMyGeoPoint.getLongitudeE6()); // ����

				ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
				nameValuePairs.add(new BasicNameValuePair("protocol",
						"sharePoint"));// ��װ��ֵ��
				nameValuePairs.add(new BasicNameValuePair("id",
						LoginActivity.mineID));
				nameValuePairs.add(new BasicNameValuePair("xpoint", mapX));
				nameValuePairs.add(new BasicNameValuePair("ypoint", mapY));

				Log.d("�����ҵ�λ��", mapX + "," + mapY);

				HttpThread h = new HttpThread(nameValuePairs, 9);
				String result = (String) h.sendInfo();
				Log.d("������", result);
			}
		}).start();

		displayToast("����ɹ�");

	}
	 	
	/**
	 * 
	 * @Method: addFriendToMap
	 * @Description: ������ͼ�����ͼ��
	 * @param geoPoint
	 * @param flag
	 * @return void ��������
	 * @throws
	 */
	private void addFriendToMap(GeoPoint geoPoint, int flag,String id,String name) {
		mAddrFlag = flag;
		if (geoPoint != mMyGeoPoint) {
			geoPointTo = geoPoint;
		}
		
		geoPointTo = geoPoint;
		mMKSearch.reverseGeocode(geoPointTo);
		// ��������ӵ���ͼ��
		mOverLays.addFriendOverLayItem(geoPoint, name, id) ;
		mMapView.getOverlays().add(mOverLays) ;
		mMapView.invalidate() ;

	}

	/**
	 * 
	 * @Method: getFriendsLocation
	 * @Description: ��������������к��ѵ������Ϣ ,������ӵ���ͼ��
	 * @return void ��������
	 * @throws
	 */
	private void getFriendsLocation() {
		// ģ�⼸�����ѵ�λ��
		if (mOverLays != null) {
			mMapView.getOverlays().add(mOverLays); // ���ItemizedOverlayʵ��mMapView
		}
		mMapView.invalidate();

		// ��ȡ���ߵ�ͬ�Ǻ���,���Ե�ʱ������,����������
		Thread friendsPointThread = new Thread( runnable1 );
		friendsPointThread.start();
	}
	 	
	 /**
	  *  �����������������߳�,������UI�߳�Ͷ����Ϣ
	  */
	private Handler mHandler1 = new Handler();
	Runnable runnable1 = new Runnable() {
		String msg = null; // Ҫ���͸����̵߳�String

		@Override
		public void run() {
			ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs
					.add(new BasicNameValuePair("protocol", "getAddress"));// ��װ��ֵ��
			HttpThread h = new HttpThread(nameValuePairs, 8); // 8--��ȡ�������
			msg = h.sendInfo().toString(); // ���շ������ķ���ֵ
			sendMessage(); // ������Ϣ�����߳�
		}

		public void sendMessage() { // �̼߳���ݴ���

			Looper mainLooper = Looper.getMainLooper(); // �õ����߳�loop
			mHandler1 = new MyHandler(mainLooper); // �������̵߳�handler
			mHandler1.removeMessages(0); // �Ƴ����ж����е���Ϣ
			Message m = mHandler1.obtainMessage(1, 1, 1, msg);// ����Ϣ����message
			mHandler1.sendMessage(m); // ����message
		}
	};

	
	/**
	 * 
	 * @ClassName: MyHandler
	 * @Description: �ڲ���,���շ������������������û���Ϣ,����UI�߳�
	 * @Author: Mr.Simple
	 * @E-mail: bboyfeiyu@gmail.com
	 * @Date 2012-11-9 ����2:31:32
	 * 
	 */
	private class MyHandler extends Handler {
		public MyHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			
			try{
				String s1 = msg.obj.toString();  // ���յ����̵߳��ַ�
				s1 = s1.trim(); 				 // ȥ�ո�
				String array[] = s1.split(";;"); // ����ַ�

				int len = array.length / 4;
				Log.d(TAG, "�ӵ��ĳ���Ϊ " + len) ;
				for (int i = 0; i < len; i++) {
					// ��������
					GeoPoint geoPoint = new GeoPoint(
							( Integer.parseInt( array[i + 2]) ),
							(  Integer.parseInt( array[i + 3]) ) );
	
					Log.d(TAG, array[i] +" ,�ǳ� : " +  array[i+1] + ",���ѵľ��� : " + array[i + 2]
											+ " γ�� : " + array[i + 3]);
					// ���յ�������,��������ӵ���ͼ��
					addFriendToMap(geoPoint, 2, array[i], array[i+1]); 
				}
			}catch (Exception e) {
				e.printStackTrace() ;
			}
		}
	}

	/**
	 * 
	 * @Method: displayToast
	 * @Description: ��ʾtoast��Ϣ
	 * @param msg    Ҫ��ʾ������
	 * @return void  ��������
	 * @throws
	 */
	private void displayToast(String msg ) {
		
		Toast.makeText(BaiduMapActivity.this, msg, 0).show() ;
	}
	
	/**
	 * 
	 * @Method: texiSelectMenu 
	 * @Description: ���⳵������ѡ�����  
	 * @return void  �������� 
	 * @throws
	 */
	private void texiSelectMenu() {
		String[] menuItems = { "�����ճ���Ϣ", "�����˳���Ϣ", "�ܱ߿ճ�","�ܱ߳˿�", "ɾ��״̬"}; // �����Ĳ˵�

		new AlertDialog.Builder(this)
				.setTitle("��ѡ��...")
				.setSingleChoiceItems(menuItems, -1,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								// ��ȡѡ��
								mIndex = which;
							}
						})
				.setPositiveButton(R.string.alert_dialog_ok,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int whichButton) {
								// ���⳵ѡ��ѡ���Ժ�
								texiSelected( mIndex );
							}
						})
				.setNegativeButton(R.string.alert_dialog_cancel,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int whichButton) {

							}
						}).show();
	}

	
	/**
	 * 
	 * @Method: texiSelected
	 * @Description: ���⳵ѡ��ĳ��Ĳ���
	 * @param index
	 *            ѡ���������
	 * @return void ��������
	 * @throws
	 */
	private void texiSelected(int index) {
		// ��ȡ�绰�������
		TelephonyManager tManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		// ��ȡ�绰����
		String number = tManager.getLine1Number();
		Log.d("�绰", number);
	
		// ����ȡʧ�����ֶ�����绰����
		if (  mIndex != 2 && mIndex != 3 ) {
			if ( number.equals("") ) {
			// �������������ֻ����
			LayoutInflater factory = LayoutInflater.from(this);
			final View dlgView = factory.inflate(R.layout.inputtel, null);

			EditText telEdit = (EditText) dlgView.findViewById(R.id.telEdit);
			mTelephone = readInfoFromLocal() ;
			telEdit.setText( mTelephone ) ;
			
			new AlertDialog.Builder(BaiduMapActivity.this)
					.setIcon(R.drawable.cute_48)
					.setTitle("��������ĵ绰����...")
					.setView(dlgView)
					.setPositiveButton(R.string.alert_dialog_ok,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int whichButton) {

									EditText telEdit = (EditText) dlgView
											.findViewById(R.id.telEdit);
									mTelephone = telEdit.getText().toString();
									if (!isPhoneNumberValid(mTelephone)) {
										displayToast("����ĺ�����Ч, ����������");
									} else {
										saveInfoToLocal( mTelephone ) ;
										// �ύ����
										new TexiAsyncTask(mIndex, 0).execute();
									}

								}
							})
					.setNegativeButton(R.string.alert_dialog_cancel,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int whichButton) {

								}
							}).show();
			} // �жϵ绰�����if 
		}else{
			// �ύ����,���鿴״̬������
			new TexiAsyncTask(mIndex, 0).execute();
		}


	}

	
	/**
	 * 
	 * @Method: saveInfoToLocal 
	 * @Description: �����û���¼��Ϣ������ ��������ס���롢�Զ���¼�ȣ�
	 * @param num   
	 * @throws
	 */
	private void saveInfoToLocal(String num)
	{
		// �����û��绰����
	   	SharedPreferences ref = getSharedPreferences("phone", 0);
	    ref.edit().putString("phoneNum", num).commit();
	    Log.d(TAG, "���绰����д�뵽xml") ;
	}
	
	
	/**
	 * 
	 * @Method: readInfoFromLocal 
	 * @Description: �ӱ����ļ��ж�ȡ�û���ݵ�
	 * @return   
	 * @throws
	 */
	private String readInfoFromLocal()
	{
		 SharedPreferences settings = getSharedPreferences("phone", 0);  // ��ȡһ������
	        
		 String phoneNum = settings.getString("phoneNum", "");
		 return phoneNum ;
	}
	
	/**
	 * 
	 * @Method: addTexiToMap 
	 * @Description: ���ӷ������õ��ĳ��⳵��Ϣ���ͼ��,�����˿ͺ�˾��  
	 * @return void  �������� 
	 * @throws
	 */
	private void addTexiToMap(GeoPoint point, String telephone, String type) {

		Drawable marker = null;
		// ʵ����⳵������
		TexiOverItem textOverLays = null;
		int titleIndex = 0;
		if (type.contains("Texi") || type.contains("Driver")) {
			// �����ﲻͼ��
			marker = getResources().getDrawable(R.drawable.location_red);
			titleIndex = 1;
		} else {
			marker = getResources().getDrawable(R.drawable.location_green);
			titleIndex = 2;
		}
		// ʵ����⳵������
		textOverLays = new TexiOverItem(marker, BaiduMapActivity.this, titleIndex);
		// �����ճ���Ϣ,��ꡢ�绰���������
		textOverLays.addTexiOverLayItem(point, telephone);
		// ��ӵ���ͼ��
		mMapView.getOverlays().add( textOverLays ); // ���ItemizedOverlayʵ��mMapView
		mMapView.invalidate();
		Log.d(TAG, "�����⳵��Ϣ���ͼ��" + point.toString());

	}
	

	/**
	 * @Method: isPhoneNumberValid
	 * @Description: ����ַ��Ƿ�Ϊ�绰����ķ���,������true or false���ж�ֵ
	 * @param phoneNumber
	 * @return
	 */
	private boolean isPhoneNumberValid(String phoneNumber) {
		boolean isValid = false;
		/*
		 * �ɽ��ܵĵ绰��ʽ��: ^\\(? : ����ʹ�� "(" ��Ϊ��ͷ (\\d{3}): ������������� \\)? : ����ʹ��")"����
		 * [- ]? : ��������ʽ�����ʹ�þ�ѡ���Ե� "-". (\\d{4}) : �ٽ������������ [- ]? : ����ʹ�þ�ѡ���Ե�
		 * "-" ����. (\\d{4})$: ���ĸ����ֽ���. ���ԱȽ��������ָ�ʽ: (123)456-78900,
		 * 123-4560-7890, 12345678900, (123)-4560-7890
		 */
		String expression = "^\\(?(\\d{3})\\)?[- ]?(\\d{3})[- ]?(\\d{5})$";
		String expression2 = "^\\(?(\\d{3})\\)?[- ]?(\\d{4})[- ]?(\\d{4})$";
		CharSequence inputStr = phoneNumber;
		/* ����Pattern */
		Pattern pattern = Pattern.compile(expression);
		/* ��Pattern �Բ�����Matcher��Regular expression */
		Matcher matcher = pattern.matcher(inputStr);
		/* ����Pattern2 */
		Pattern pattern2 = Pattern.compile(expression2);
		/* ��Pattern2 �Բ�����Matcher2��Regular expression */
		Matcher matcher2 = pattern2.matcher(inputStr);

		if (matcher.matches() || matcher2.matches()) {
			isValid = true;
		}
		return isValid;
	}
		 	

	/**
	 * 
	 * @Method: searchMenu 
	 * @Description: �����˵���ѡ�����
	 * @return void  �������� 
	 * @throws
	 */
	private void searchMenu() {
		String[] menuItems = { "·������", "�ܱ�����", "ͬ�Ǻ���" }; // �����Ĳ˵�

		new AlertDialog.Builder(this)
				.setTitle("��ѡ��...")
				.setSingleChoiceItems(menuItems, -1,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								mIndex = which;
							}
						})
				.setPositiveButton(R.string.alert_dialog_ok,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int whichButton) {
								// �����˵�ѡ����
								searchMenuSelected(mIndex);
							}
						})
				.setNegativeButton(R.string.alert_dialog_cancel,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int whichButton) {

							}
						}).show();

	}
	 	 
	 	 

	/**
	 * @Method: showPOIMenu
	 * @Description: ����POI����ѡ��˵�
	 */
	private void showPOIMenu() {
		String[] menuitems = getResources().getStringArray(R.array.poiMenu); // �����Ĳ˵�

		new AlertDialog.Builder(this)
				.setTitle("��ѡ��...")
				.setSingleChoiceItems(menuitems, -1,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {

								POISearchIndex(which); // POIѡ���������Ŀ

							}
						})
				.setPositiveButton(R.string.alert_dialog_ok,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int whichButton) {
								displayToast("������,���Ժ�...");

							}
						})
				.setNegativeButton(R.string.alert_dialog_cancel,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int whichButton) {

								/* User clicked No so do some stuff */
							}
						}).show();
	}


	private int mViewIndex = -1;
	/**
	 * 
	 * @Method: changeMapViewMenu
	 * @Description: ��ͼ��ͼ�޸�
	 * @throws
	 */
	private void changeMapViewMenu() {
		String[] menuitems = { "����ͼ", "������ͼ" }; // �����Ĳ˵�
		new AlertDialog.Builder(this)
				.setTitle("��ѡ��...")
				.setSingleChoiceItems(menuitems, 0,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								mViewIndex = which;

							}
						})
				.setPositiveButton(R.string.alert_dialog_ok,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int whichButton) {
								
								if (mViewIndex == 0) {
									mMapView.setSatellite(false);
								} else if (mViewIndex == 1) // POIѡ���������Ŀ
								{
									mMapView.setSatellite(true);
								}

							}
						})
				.setNegativeButton(R.string.alert_dialog_cancel,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int whichButton) {

								/* User clicked No so do some stuff */
							}
						}).show();
	}


	/**
	 * 
	 * @Method: searchMenuSelected 
	 * @Description: �����˵���ѡ��,�����ѡ��:·�ߡ��ܱߡ�ͬ�Ǻ���
	 * @param index   
	 * @throws
	 */
	private void searchMenuSelected(int index) {
		switch (index) {
		case 0:
			// ·������
			searchRoude();
			break;
		case 1:
			// �ܱ�����
			showPOIMenu();
			break;
		case 2:
			// ͬ�Ǻ���
			getFriendsLocation();
			break;
		default:
			break;
		}

	}
	 	 

	/**
	 * 
	 * @Method: POISearchIndex 
	 * @Description:  POIѡ����Ӧ����,��ȡ��ѡ���POI����
	 * @param i       ���ѡ���POI����,����ATM���ʾ֡���԰��
	 * @throws
	 */
	private void POISearchIndex(int i) {
		String[] menu = getResources().getStringArray(R.array.poiMenu);
		String poiType = menu[i];

		if (mMKSearch == null) {
			mMKSearch = new MKSearch();
			mMKSearch.init(mBMapManager, new MySearchListener());
		}

		mMKSearch.poiSearchNearBy(poiType, mMyGeoPoint, 5000); // �����ܱ�5KM��
		Log.d(TAG, "Poi ���� �� " + poiType);

	}

	/*
	 * (�� Javadoc,��д�ķ���) �����µ��¼� <p>Title: onKeyDown</p> <p>Description: </p>
	 * 
	 * @param keyCode
	 * 
	 * @param event
	 * 
	 * @return
	 * 
	 * @see android.app.Activity#onKeyDown(int, android.view.KeyEvent)
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && mPopView.isSelected()) {
			mPopView.setVisibility(View.GONE);
			return true;
		}
		return super.onKeyDown(keyCode, event);

	}

	@Override
	protected void onStop() {

		// ��Ӷ�λͼ��
		MyLocationOverlay myLocation = new MyLocationOverlay(this, mMapView);
		if (myLocation.isMyLocationEnabled()) {
			myLocation.disableMyLocation();
		}
		// �ر�GPS
		turnGPSOff();

		super.onStop();

	}

	/**
	 * 
	 * @ClassName: MyOverItem
	 * @Description: ��ͼ�������ڲ���,���ѵĸ������ڲ���
	 * @Author: Mr.Simple
	 * @E-mail: bboyfeiyu@gmail.com
	 * @Date 2012-11-16 ����6:03:22
	 * 
	 */

	class MyOverItem extends ItemizedOverlay<OverlayItem> {

		private List<OverlayItem> mGeoList = new ArrayList<OverlayItem>();
		// �����Ѿ����ڵ��û������λ��
		private List<String> mNameList = new ArrayList<String>();
		// ���ʶ��󣬻��Ƶ�ͼ������
		Paint paint = new Paint();

		/**
		 * 
		 * @Constructor: 
		 * @@param marker
		 * @@param context
		 * @Description: ���캯��
		 * @param marker
		 * @param context
		 */
		public MyOverItem(Drawable marker, Context context) {
			super(boundCenterBottom(marker));

			if (mMyGeoPoint != null) {
				mGeoList.add(new OverlayItem(mMyGeoPoint, "MR.SIMPLE", "���ڴ���."));
			}

			populate(); // createItem(int)��������item��һ��������ݣ��ڵ�������ǰ�����ȵ����������
		}

		/**
		 * (�� Javadoc,��д�ķ���) 
		 * @Title: createItem
		 * @Description: 
		 * @param i
		 * @return 
		 * @see com.baidu.mapapi.ItemizedOverlay#createItem(int)
		 */
		@Override
		@Override
		protected OverlayItem createItem(int i) {
			return mGeoList.get(i);
		}

		/**
		 * (�� Javadoc,��д�ķ���) 
		 * @Title: size
		 * @Description: 
		 * @return 
		 * @see com.baidu.mapapi.ItemizedOverlay#size()
		 */
		@Override
		@Override
		public int size() {
			return mGeoList.size();
		}

		/**
		 * ���� �� ���?����¼� (non-Javadoc)
		 * @see com.baidu.mapapi.ItemizedOverlay#onTap(int)
		 */
		@Override
		@Override
		protected boolean onTap(int i) {

			mFindBtnFlag = 1; // ����Ϊ·������ģʽ
			geoPointTo = mGeoList.get(i).getPoint(); // ��ȡ���ѵ�λ��,Ҳ��������ʾ�����λ��
			setFocus(mGeoList.get(i)); // ��ȡ����

			// ����������λ�õ�·��
			Button dailBtn = (Button) mPopView.findViewById(R.id.findBtn);
			dailBtn.setText("����·��");

			// ���������е�����
			TextView titleView = (TextView) mPopView
					.findViewById(R.id.pop_title);
			if (LoginActivity.mineName.equals(mGeoList.get(i).getTitle())) {
				titleView.setText(mGeoList.get(i).getTitle());
				// ���ػ�ȡ·�ߵİ�ť
				dailBtn.setVisibility(View.GONE);
				dailBtn.setEnabled(false);
				displayToast("hey,���Ǳ����ʶ...");
			} else {
				titleView.setText("���� : " + mGeoList.get(i).getTitle());
				// ���ػ�ȡ·�ߵİ�ť
				dailBtn.setVisibility(View.VISIBLE);
				dailBtn.setEnabled(true);
			}

			BaiduMapActivity.this.mMapView.updateViewLayout(
					BaiduMapActivity.mPopView, new MapView.LayoutParams(
							LayoutParams.WRAP_CONTENT,
							LayoutParams.WRAP_CONTENT, geoPointTo,
							MapView.LayoutParams.BOTTOM_CENTER));

			BaiduMapActivity.mPopView.setVisibility(View.VISIBLE); // ��ʾ����

			return true;

		}

		/**
		 * ���� �� ʧȥ����,��ȥ���������� (non-Javadoc)
		 * 
		 * @see com.baidu.mapapi.ItemizedOverlay#onTap(com.baidu.mapapi.GeoPoint,
		 *      com.baidu.mapapi.MapView)
		 */
		@Override
		@Override
		public boolean onTap(GeoPoint geoPoint, MapView mapView) {

			BaiduMapActivity.mPopView.setVisibility(View.GONE);
			return super.onTap(geoPoint, mapView);
		}

		/**
		 * ���� �� ����draw����,ʵ���Զ�����ƹ��� (non-Javadoc)
		 * 
		 * @see com.baidu.mapapi.ItemizedOverlay#draw(android.graphics.Canvas,
		 *      com.baidu.mapapi.MapView, boolean)
		 */
		@Override
		public void draw(Canvas canvas, MapView mapView, boolean shadow) {

			super.draw(canvas, mMapView, shadow);

		}

		/**
		 * 
		 * @Method: setMarker
		 * @Description: �����µĸ�����ͼ��
		 * @param newMarker
		 * @throws
		 */
		public void setMarker(Drawable newMarker) {
			super.boundCenterBottom(newMarker); // �����µĸ�����ͼ��
		}

		
		/**
		 * 
		 * @Method: addFriendOverLayItem
		 * @Description: ��Ӻ��ѱ�ʶ�����ﵽĳ���
		 * @param gPoint  ���ѵ����
		 * @param name    �����ǳ�
		 * @param content
		 * @throws
		 */
		public void addFriendOverLayItem(GeoPoint gPoint, String name,
				String content) {
			Log.d(TAG, "��Ӻ���" + name + "����ͼ " + gPoint.toString());
			if (!mNameList.contains(name)) {
				mGeoList.add(new OverlayItem(gPoint, name, content));
				mNameList.add(name);
			}
			populate();
		}

		/**
		 * 
		 * @Method: clearOverLayItems
		 * @Description: ��������
		 * @throws
		 */
		public void clearOverLayItems() {
			mGeoList.clear();
			mNameList.clear();
		}

	} // end of class.
	 	
	 	 
	 	/**
	 	 * 
	 	 * @ClassName: TexiOverItem 
	 	 * @Description: ���⳵��صĵ�ͼ�������ڲ���
	 	 * @Author: Mr.Simple 
	 	 * @E-mail: bboyfeiyu@gmail.com 
	 	 * @Date 2012-11-16 ����6:04:00 
	 	 *
	 	 */
	 	class TexiOverItem extends ItemizedOverlay<OverlayItem> {
	 		
	 		// ������洢
	 	    private List<OverlayItem> mItemsList = new ArrayList<OverlayItem>();	
	 	    private String mTitle = null;				// �����ﱻ���ʱ�ı���
	 	    private List<String> mPhoneList = new ArrayList<String>() ;
	 	    
	 	    Paint paint = new Paint();					// ���ʶ��󣬻��Ƶ�ͼ������
	 	   
	 	    // ���캯��,����һ������,Ϊ����,1Ϊ���⳵,2Ϊ�˿�,�ڹ��������ò�ͬ�ı�ʶ��
	 	    public TexiOverItem(Drawable marker, Context context) {
	 	
	 	        super(boundCenterBottom(marker));
	 	    	initOverItem( 1 ) ;
	 
	 	    }
	 	    // ���캯��,����һ������,Ϊ����,1Ϊ���⳵,2Ϊ�˿�,�ڹ��������ò�ͬ�ı�ʶ��
	 	    public TexiOverItem(Drawable marker, Context context, int type) {
	 	       super(boundCenterBottom(marker));
	 	       initOverItem( type ) ;
	 	       Log.d(TAG, "���⳵��־�๹��  " + mTitle) ;
	 	    }
	 	 
	 	    /**
	 	     * 
	 	     * @Method: initOverItem 
	 	     * @Description: ��ʼ��������,���ò�ͬ��ͼ���Լ�����
	 	     * @param type   
	 	     * @return void  �������� 
	 	     * @throws
	 	     */
	 	    private void initOverItem(int type){
	 	    	if ( 1 == type ){
	 	    		mTitle = "���⳵˾��";
	 	    		// �ڴ�Ҳ����marker
	 	    	}else{
	 	    		mTitle = "���⳵�˿�";
	 	    	}
	 	    }
	 	    
	 	    /**
	 	     * (�� Javadoc,��д�ķ���) 
	 	     * @Title: createItem
	 	     * @Description: 
	 	     * @param i
	 	     * @return 
	 	     * @see com.baidu.mapapi.ItemizedOverlay#createItem(int)
	 	     */
	 	    @Override
			@Override
	 	    protected OverlayItem createItem(int i) {
	 	        return mItemsList.get(i);
	 	    }
	 	 
	 	    /**
	 	     * (�� Javadoc,��д�ķ���) 
	 	     * @Title: size
	 	     * @Description: 
	 	     * @return 
	 	     * @see com.baidu.mapapi.ItemizedOverlay#size()
	 	     */
	 	    @Override
			@Override
	 	    public int size() {
	 	        return mItemsList.size();
	 	    }
	 	 
	 	    
	 	    /**
	 	     *  ���� �� ������������ͼ��Ķ���
	 	     *  (non-Javadoc)
	 	     * @see com.baidu.mapapi.ItemizedOverlay#onTap(int)
	 	     */
	 	    @Override
			@Override
	 	    protected boolean onTap(int i) {
	 	    	
	 	    	mFindBtnFlag = 2;										// ����Ϊ·������ģʽ
	 	    	geoPointTo = mItemsList.get(i).getPoint();				// ��ȡ���ѵ�λ��,Ҳ��������ʾ�����λ��

	 	    	setFocus(mItemsList.get(i));							// ��ȡ����
	 	    	// ��ȡ���û��ĵ绰
	 	    	mTelephone = mItemsList.get(i).getSnippet();
	 	    	displayToast( "���û��绰Ϊ : " + mTelephone ) ;
	 	    	
	 			// ���������е�����
	 			TextView titleView = (TextView)mPopView.findViewById(R.id.pop_title);
	 			titleView.setText( mTitle );
	 			// ��绰�İ�ť
	 			Button dailBtn = (Button)mPopView.findViewById(R.id.findBtn);
	 			dailBtn.setVisibility( View.VISIBLE ) ;
	 			dailBtn.setEnabled( true ) ;
	 			dailBtn.setText("����");
	 			
	 			BaiduMapActivity.this.mMapView.updateViewLayout( BaiduMapActivity.mPopView,
	 	                new MapView.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,
	 	                		geoPointTo, MapView.LayoutParams.BOTTOM_CENTER));
	 			
	 			BaiduMapActivity.mPopView.setVisibility(View.VISIBLE);	// ��ʾ����
	
	 			return true;
	 			
	 	    }
	 	    
	 	    
			/**
			 *  ���� �� ʧȥ����,��ȥ����������
			 *  (non-Javadoc)
			 * @see com.baidu.mapapi.ItemizedOverlay#onTap(com.baidu.mapapi.GeoPoint, com.baidu.mapapi.MapView)
			 */
	 	   @Override
		@Override
	 		public boolean onTap(GeoPoint geoPoint, MapView mapView) {

	 			BaiduMapActivity.mPopView.setVisibility(View.GONE);
	 			return super.onTap(geoPoint, mapView);
	 		}
	 	    
	 	   
	 	    /**
	 	     * ���� �� ����draw����,ʵ���ڵ�ͼ�ϵ��Զ�����Ƹ����﹦��
	 	     * (non-Javadoc)
	 	     * @see com.baidu.mapapi.ItemizedOverlay#draw(android.graphics.Canvas, com.baidu.mapapi.MapView, boolean)
	 	     */
	 	    @Override
			public void draw(Canvas canvas, MapView mapView, boolean shadow) {
	 	    	
				super.draw(canvas, mMapView, shadow);
	
			}

	 	  
	 	    /**
	 	     * 
	 	     * @Method: addFriendOverLayItem 
	 	     * @Description: ��Ӹ����ﵽĳ���
	 	     * @param gPoint ����
	 	     * @param title  ���������ʱ�ı���
	 	     * @param telephone  �绰
	 	     * @return void  �������� 
	 	     * @throws
	 	     */
	 	    public void addTexiOverLayItem(GeoPoint gPoint, String telephone)
	 	    {
	 	    	if ( !mPhoneList.contains( telephone ) ){
		 	    	Log.d(TAG, "���" + mTitle  + "����ͼ " + gPoint.toString());
		 	    	mItemsList.add(new OverlayItem(gPoint, mTitle, telephone));
		 	    	mPhoneList.add( telephone ) ;
		 	    	populate();
	 	    	}
	 	    }
	 	    
	 	    /**
	 	     * 
	 	     * @Method: clearOverLayItem 
	 	     * @Description: ������ 
	 	     * @throws
	 	     */
	 	    public void clearOverLayItem(){
	 	    	mItemsList.clear() ;
	 	    	mPhoneList.clear() ;
	 	    	mTitle = "NULL";
	 	    }
	 	    
	 	}// end of class.

	 	
	/**
	 * 
	 * @ClassName: MySearchListener
	 * @Description: �ٶȵ�ͼ�ƶ���API�����������������
	 *               λ�ü������ܱ߼�������Χ�����������������ݳ˼��������м�����ͨ���ʼ��MKSearch�࣬
	 *               ע���������ļ������MKSearchListener��ʵ���첽��������
	 *               �����Զ���MySearchListenerʵ��MKSearchListener�ӿڣ�ͨ��ͬ�Ļص�����������������
	 * @Author: Mr.Simple
	 * @E-mail: bboyfeiyu@gmail.com
	 * @Date 2012-11-16 ����6:04:37
	 * 
	 */
	public class MySearchListener implements MKSearchListener {

		/****
		 * ���� �� ���ؼݳ�·��������� ����1�� ������� ����2 �� ����ţ�0��ʾ��ȷ����
		 ****/
		@Override
		@Override
		public void onGetAddrResult(MKAddrInfo result, int iError) {

			String Location = null;
			if (result == null) {
				Location = "û���������õ�ַ";
				return;
			} else {
				// ���������ַ�ľ�γ��
				Location = "γ�ȣ�" + result.geoPt.getLatitudeE6() / 1E6 + "\n"
						+ "���ȣ�" + result.geoPt.getLongitudeE6() / 1E6 + "\n";
				geoPointTo = result.geoPt; // ����ʹ��
			}

			// ��ȡ������,reverseGeocode�Ļص�����,�����û���λ����Ϣ��
			MKGeocoderAddressComponent addrInfo = result.addressComponents;
			if (mAddrFlag == 1) {
				mFCity = addrInfo.city;
				displayToast("���� �� " + mFCity + " ��꣺ " + Location);
				return;
			}

			if (addrInfo.city.contains(mFCity)) // ͬ������ӵ���ͼ��
			{
				displayToast("����" + "�� �� " + addrInfo.city + " ��꣺ " + Location);
				mOverLays.addFriendOverLayItem(geoPointTo, "Name", "title");
			}

			if (geoPointTo != null) {
				mOverLays.addFriendOverLayItem(geoPointTo, "Name", "title");
			}

		}
	 	   
	 	    /**
             * ���� �� ���ؼݳ�·���������
             *  ����1�� ������� 
             *  ����2�� ����ţ�0��ʾ��ȷ���� 
             */ 
	 	    @Override
			@Override
	 	    public void onGetDrivingRouteResult(MKDrivingRouteResult result, int iError) {
	 	    	
	 	    	 if (result == null || iError != 0) {
	 	    		 displayToast("·������ʧ��...");
	 	            return;
	 	        }
	 	    	 
	 	    	 // ·�߸�����
	 	        RouteOverlay routeOverlay = new RouteOverlay(BaiduMapActivity.this, mMapView);
	 	        routeOverlay.setData(result.getPlan(0).getRoute(0));
	 	       // mMapView.getOverlays().clear();
	 	        mMapView.getOverlays().add( routeOverlay );
	 	        mMapView.invalidate();
	 	        mMapContoller.setCenter(result.getStart().pt);
	 	        
	 	    }
	 	 
	 	    
	 	    /**
             * ����poi�������
             * ����1 ��������� ,
             * ����2 �����ؽ������: MKSearch.TYPE_POI_LIST ��MKSearch.TYPE_AREA_POI_LIST��
             * 					 MKSearch.TYPE_CITY_LIST 
             * ����3 ��  - ����ţ�0��ʾ��ȷ���� 
             */ 
	 	    @Override
			@Override
	 	    public void onGetPoiResult(MKPoiResult result, int type, int iError) {
	 	    	
	 	       if (result == null || iError != 0) {
	 	    	   displayToast("��Ǹ,δ�ҵ����.");
	 	          return;
	 	      }
	 	       
	 	      PoiOverlay poioverlay = new PoiOverlay(BaiduMapActivity.this, mMapView);
	 	      poioverlay.setData( result.getAllPoi() );
	 	      mMapView.getOverlays().add( poioverlay );
	 	      mMapView.invalidate();
	 	    }
	 
	 	   
	 	   /**
             * ���ع����������
             * ����1��  ������� 
             * ����2�� - ����ţ�0��ʾ��ȷ���أ� ������MKEvent.ERROR_ROUTE_ADDRʱ��
             * ��ʾ�����յ������壬 ����MKTransitRouteResult��getAddrResult������ȡ�Ƽ��������յ���Ϣ 
             */ 
	 	    @Override
			@Override
	 	    public void onGetTransitRouteResult(MKTransitRouteResult result, int iError) {
	 	    	
	 	    	Log.d("RoutePlan", "the res is " + result + "__" + iError);
	 	      
				if (iError != 0 || result == null) {
					displayToast("��Ǹ,δ�ҵ����");
					return;
				}
				
				TransitOverlay  routeOverlay = new TransitOverlay (BaiduMapActivity.this, mMapView);
			    // �˴���չʾһ��������Ϊʾ��
			    routeOverlay.setData(result.getPlan(0));
			    //mMapView.getOverlays().clear();
			    mMapView.getOverlays().add(routeOverlay);
			    mMapView.invalidate();
			    
			    mMapView.getController().animateTo(result.getStart().pt);
	 	       
	 	    }
	 	 
	 	    /** 
             * ���ز���·���������
             * ����1�� ������� 
 			 * ����2��- ����ţ�0��ʾ��ȷ���� 
             */ 
	 	    @Override
			@Override
	 	    public void onGetWalkingRouteResult(MKWalkingRouteResult result, int iError) {
	 	    
		 	   	if (iError != 0 || result == null) {
		 	   		displayToast("��Ǹ,δ�ҵ����");
					return;
				}
				RouteOverlay routeOverlay = new RouteOverlay(BaiduMapActivity.this, mMapView);
			    // �˴���չʾһ��������Ϊʾ��
			    routeOverlay.setData(result.getPlan(0).getRoute(0));
			    mMapView.getOverlays().clear();
			    mMapView.getOverlays().add(routeOverlay);
			    mMapView.invalidate();
			    
			    mMapView.getController().animateTo(result.getStart().pt);
	 	    } 	    
	 	}
	 	
	 	
	 /**
	  * 
	  * @ClassName: TexiAsyncTask 
	  * @Description: 
	  * @Author: Mr.Simple 
	  * @E-mail: bboyfeiyu@gmail.com 
	  * @Date 2012-11-12 ����3:45:33 
	  *
	  */
	public class TexiAsyncTask extends AsyncTask<Integer, Void, String> {

		private int mType = -1 ;
		private final String EMPTY_TEXI = "emptyTexi" ;
		private final String TAKE_TEXI = "takeTexi" ;
		private final String CHECK = "check" ;
		private final String CANCEL = "cancel" ;
		
		/**
		 * 
		 * @Constructor: 
		 * @@param type   ��������,0Ϊ�ύ�ճ�״̬,1Ϊ�ύ�˳�״̬,2Ϊ�鿴,˾����˿͵ķ�������ǲ�һ���
		 * @@param role   ��typeΪ2ʱ������,�ò���Ϊ"�鿴"ѡ��Ĳ���,���鿴����ͼ�ϵĿճ�,���߳˿�
		 * @Description:  ���⳵���첽����
		 * @param type
		 * @param role
		 */
		public TexiAsyncTask(int type, int role){
			mType = type ;
		}
		
		
		/**
		 * (�� Javadoc,��д�ķ���) 
		 * @Title: doInBackground
		 * @Description: 
		 * @param params  �ǲ鿴״̬ʱ�Ľ�ɫ,1���˾�����2���˿�
		 * @return 
		 * @see android.os.AsyncTask#doInBackground(Params[])
		 */
		@Override
		protected String doInBackground(Integer... params) {

			// HTTP����ļ�ֵ��
			ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			String url = "http://199.36.75.40/Android/receiveMessage.php";
			// ���
			String result = "NULL";
			try {
				// ���������
				String msgType = "";
				switch ( mType ) {
					case 0:
						// ˾���ճ�״̬
						msgType = EMPTY_TEXI;
						break;
					case 1:
						// �˿ͷ����˳�״̬
						msgType = TAKE_TEXI;
						break;
					case 2:
						// ��������÷���Ľ�ɫ,˾����˿�����ķ�����ݲ�һ��
						msgType = CHECK + "TexiDriver" ; 
						break;
					case 3:
						// ��������÷���Ľ�ɫ,˾����˿�����ķ�����ݲ�һ��
						msgType = CHECK + "Passenger" ; 
						break;
					case 4:
						msgType = CANCEL ;
						// ͨ��绰��ɾ��״̬
						nameValuePairs.add(new BasicNameValuePair("phone", mTelephone));
						Log.d(TAG,  "ȡ��״̬,msgType = "+ msgType + " telephone = " + mTelephone) ;
						break;
						
					default:
						break;
				}
				
				Log.d(TAG,  "msgType = "+ msgType) ;
				// ���Э������
				nameValuePairs.add(new BasicNameValuePair("protocol", msgType));
				
				// 1��2�����Ͷ����ύ���������ݵ�
				if ( mType == 0 || mType == 1 && mMyGeoPoint != null){
					// ��ȡ����λ�õľ�γ��
					String mapX = String.valueOf( mMyGeoPoint.getLatitudeE6() ); 
					String mapY = String.valueOf( mMyGeoPoint.getLongitudeE6() ); 
					
					nameValuePairs.add(new BasicNameValuePair("phone", mTelephone));
					nameValuePairs.add(new BasicNameValuePair("xpoint", mapX));
					nameValuePairs.add(new BasicNameValuePair("ypoint", mapY));

					Log.d("����ճ�״̬", "�ҵĵ���λ��Ϊ: " + mapX + "," + mapY);
				}

				// ����httpPost����
				HttpPost post = new HttpPost(url);

				post.setEntity(new UrlEncodedFormEntity(nameValuePairs, HTTP.UTF_8));
				// ����http����
				HttpResponse response = new DefaultHttpClient().execute(post);
				// ����ɹ�
				if (200 == response.getStatusLine().getStatusCode()) {
					// ��ȡ���صĽ��
					result = EntityUtils.toString(response.getEntity());
					Log.d("���⳵����������", result);
				} else {
					Log.d("���⳵������", "����ʧ��");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return result;
		}

		
		private String resType = "";
		@Override
		protected void onPostExecute(String result) {
			
			if ( !result.equals("NULL")){
				try {
					// ���صĽ��ͷ��success����ʧ��fail
					String flag = result.substring(0, result.indexOf("_")) ;
					if ( flag.equals("success") ){
						// ������ύ�����������,����emptyTexi
						resType = result.substring(result.indexOf("_") + 1 ,
														 result.lastIndexOf("_") ) ;
						if ( resType.equals(EMPTY_TEXI) || resType.equals(TAKE_TEXI) 
								|| resType.equals( CANCEL ) ){
							// ��ȡ���õ����
							result = result.substring(result.lastIndexOf("_") + 1, result.length()) ;
							Log.d(TAG, "��Ч���Ϊ : " + result ) ;
						}
						// ��ݷ��صĲ�ͬ������ͽ��в�ͬ�Ĵ���
						processResult(resType, result) ;
						displayToast( "����ɹ�..." ) ;
						
					}else{
						displayToast( "����ʧ��..." ) ;
					}
				}catch(Exception e){
					displayToast("����ʧ��...") ;
					e.printStackTrace() ;
				}

			}
		}
		
		/**
		 * 
		 * @Method: processResult 
		 * @Description:
		 * @param type
		 * @param result   
		 * @throws
		 */
		private void processResult(String type ,String result) throws Exception{
			Log.d(TAG, "��������Ϊ : " + type + ",���Ϊ: " + result ) ;
			
			if ( EMPTY_TEXI.equals( type ) ){
				Log.d(TAG, "�����ճ�״̬�ɹ�") ;
				displayToast( "�����ճ�״̬�ɹ�" ) ;
			}else if ( type.contains( CHECK )){
				// ����鿴���⳵�ķ��ؽ��
				parseCheckTexi( result ) ;
				
			}else if ( type.contains( CANCEL )){
				displayToast("���⳵״̬ȡ��ɹ�") ;
			}
		} // end of processResult
		
		
		/**
		 * @Method: parseCheckTexi
		 * @Description: �����������
		 * @param result
		 */
		private void parseCheckTexi(String result){
			try{
				if ( result.contains("::") ){
					// ÿ���û����������"::"�ָ�,��ÿ���û���ÿ�����ʹ��";;"�ָ�
					String temp[] = result.split("::") ;
					for(int i=0; i<temp.length; i++){
						String res[] = temp[i].split(";;") ;
						String tel = res[0];
						float x = Float.parseFloat( res[1]);
						float y = Float.parseFloat( res[2] ) ;
						GeoPoint point = new GeoPoint((int)(x*1E6), (int)(y*1E6) );
						// ��ӵ���ͼ��
						addTexiToMap( point , tel , resType) ;
					}
				}
			}catch(Exception e){
				e.printStackTrace() ;
			}
		}

	} // end of TexiAsyncTask
	
}	// end of activity
