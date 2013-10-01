package album.entry;

import imageCache.ImageCacheToSDCard;


/**
 * 
 * @ClassName: SettingActivity
 * @Description: ���ý���
 * @Author: Mr.Simple
 * @E-mail: bboyfeiyu@gmail.com
 * @Date 2012-11-16 ����6:15:58
 * 
 */
public class SettingActivity extends PreferenceActivity implements
		OnPreferenceChangeListener, OnPreferenceClickListener {

	static final String TAG = "PreferenceActivity";

	SharedPreferences preference = null;
	CheckBoxPreference updateCheckBoxPreference = null;
	ListPreference lististPreference = null;
	CheckBoxPreference isneilflag_CheckBoxPreference = null;
	CheckBoxPreference clearCache_CheckBoxPreference = null;
	EditTextPreference usernameEditTextPreference = null;
	EditTextPreference passwordEditTextPreference = null;

	
	/*
	 * (�� Javadoc,��д�ķ���) 
	 * <p>Title: onCreate</p> 
	 * <p>Description: </p> 
	 * @param savedInstanceState 
	 * @see android.preference.PreferenceActivity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// ������ʾPreferences
		addPreferencesFromResource(R.layout.setting);
		// ���SharedPreferences
		preference = PreferenceManager.getDefaultSharedPreferences(this);

		// �ҵ�preference��Ӧ��Key��ǩ��ת��
		updateCheckBoxPreference = (CheckBoxPreference) findPreference(
															getString(R.string.update_key));
		lististPreference = (ListPreference) findPreference(
													getString(R.string.auto_update_frequency_key));
		isneilflag_CheckBoxPreference = (CheckBoxPreference) findPreference(
													getString(R.string.isneilflag_key));
		usernameEditTextPreference = (EditTextPreference) findPreference(
													getString(R.string.username_key));
		passwordEditTextPreference = (EditTextPreference) findPreference(
													getString(R.string.password_key));
		clearCache_CheckBoxPreference = (CheckBoxPreference) findPreference(
													getString(R.string.clearcache));
		// ΪPreferenceע�����
		updateCheckBoxPreference.setOnPreferenceChangeListener(this);
		updateCheckBoxPreference.setOnPreferenceClickListener(this);
		clearCache_CheckBoxPreference.setOnPreferenceClickListener( this ) ;

		lististPreference.setOnPreferenceClickListener(this);
		lististPreference.setOnPreferenceChangeListener(this);

		isneilflag_CheckBoxPreference.setOnPreferenceChangeListener(this);
		isneilflag_CheckBoxPreference.setOnPreferenceClickListener(this);

		usernameEditTextPreference.setOnPreferenceChangeListener(this);
		passwordEditTextPreference.setOnPreferenceChangeListener(this);
	}

	/*
	 * (�� Javadoc,��д�ķ���) 
	 * <p>Title: onPreferenceClick</p> 
	 * <p>Description: </p> 
	 * @param preference
	 * @return 
	 * @see android.preference.Preference.OnPreferenceClickListener#onPreferenceClick(android.preference.Preference)
	 */
	@Override
	public boolean onPreferenceClick(Preference preference) {
		// �ж����ĸ�Preference�ı���
		if (preference.getKey().equals(getString(R.string.update_key))) {
			Log.e(TAG, getString(R.string.update_key));
		} else if (preference.getKey().equals(
				getString(R.string.isneilflag_key))) {
			Log.e(TAG, getString(R.string.isneilflag_key));
		} else if (preference.getKey().equals(getString(R.string.clearcache))) {
			
			// ����ȷ�϶Ի���
			AlertDialog.Builder  builder = new AlertDialog.Builder(SettingActivity.this); 
			AlertDialog dlg = builder.create();
			dlg.setIcon(R.drawable.beaten) ;
			dlg.setTitle("ȷ�����ͼƬ����?") ;
			dlg.setButton("ȷ��", new OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					ImageCacheToSDCard cache = ImageCacheToSDCard.getInstance();
					cache.clearImageCache() ;
				}
			});
			dlg.setButton2("ȡ��", new OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					
				}
			}) ;
			
			dlg.show() ;	
		}
		// ����true��ʾ����ı�
		return true;
	}

	/*
	 * (�� Javadoc,��д�ķ���) 
	 * <p>Title: onPreferenceChange</p> 
	 * <p>Description: </p> 
	 * @param preference
	 * @param newValue
	 * @return 
	 * @see android.preference.Preference.OnPreferenceChangeListener#onPreferenceChange(android.preference.Preference, java.lang.Object)
	 */
	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		// �ж����ĸ�Preference�ı���
		if (preference.getKey().equals(getString(R.string.username_key))) {
			// �˺�
			Log.e(TAG, getString(R.string.username_key));
		} else if (preference.getKey().equals(getString(R.string.password_key))) {
			// ����
			Log.e(TAG, getString(R.string.password_key));

		} else if (preference.getKey().equals(
				getString(R.string.auto_update_frequency_key))) {
			// �б�
			Log.e(TAG, getString(R.string.auto_update_frequency_key));
		}
		// ����true��ʾ����ı�
		return true;
	}
}
