package album.entry;

/**
 * 
 * @ClassName: MyProgressDialog 
 * @Description:  �������ʾ��
 * @Author: Mr.Simple (�κ��)
 * @E-mail: bboyfeiyu@gmail.com 
 * @Date 2012-11-17 ����5:36:54 
 *
 */

public class MyProgressDialog extends ProgressDialog {
	
	
	/**
	 * 
	 * @Constructor:  
	 * @@param context
	 * @@param title
	 * @Description: ���캯��
	 * @param context
	 * @param title
	 */
	public MyProgressDialog(Context context, String title) {
	
		super(context);
		setTitle(title);
		setIcon(R.drawable.l_cn_48);
	}


	/**
	 * 
	 * @Constructor: 
	 * @@param context
	 * @@param title
	 * @@param resId
	 * @Description: ���캯��
	 * @param context
	 * @param title
	 * @param resId
	 */
	public MyProgressDialog(Context context, String title,int resId) {
		
		super(context);
		setTitle(title);		// ���ñ���
		setIcon(resId);			// ����ͼ��

	}
	
	
	/**
	 * (�� Javadoc,��д�ķ���) 
	 * @Title: setProgressStyle
	 * @Description:  ���ý��������ʵ���Ϊ���η��
	 * @param style 
	 * @see android.app.ProgressDialog#setProgressStyle(int)
	 */
	@Override
	public void setProgressStyle(int style) {
		super.setProgressStyle(ProgressDialog.STYLE_SPINNER);
	}


	/**
	 * (�� Javadoc,��д�ķ���) 
	 * @Title: setIcon
	 * @Description:  ����ͼ��
	 * @param resId 
	 * @see android.app.AlertDialog#setIcon(int)
	 */
	@Override
	public void setIcon(int resId) {

		super.setIcon(resId);
	}

	
	/**
	 * (�� Javadoc,��д�ķ���) 
	 * @Title: setTitle
	 * @Description:   ���ñ���
	 * @param title 
	 * @see android.app.AlertDialog#setTitle(java.lang.CharSequence)
	 */
	@Override
	public void setTitle(CharSequence title) {
		super.setTitle(title);
	}

}
