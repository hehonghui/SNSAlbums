
package help_dlg;

import album.entry.R;

/**
 * @ClassName: HelpDialog 
 * @Description:  ������Ϣ����
 * @Author: Mr.Simple (�κ��)
 * @E-mail: bboyfeiyu@gmail.com 
 * @Date 2012-11-17 ����7:29:19 
 *
 */


public class HelpDialog  {

	private Context mContext;
	private int mMessageId;
	
	/**
	 * 
	 * @Constructor: 
	 * @@param context
	 * @@param messageId
	 * @Description: ���캯��
	 * @param context    ���øú����activity
	 * @param messageId ���øú����activity
	 */
	public HelpDialog(Context context, int messageId){
		mContext = context;
		mMessageId = messageId;
	}


	/**
	 * @Method: showHelp
	 * @Description:   ��ʾ������Ϣ
	 */
    public void showHelp(){
    	new AlertDialog.Builder( mContext )
    	.setIcon(R.drawable.help_64)
        .setTitle("	��   ��")
        .setMessage( mMessageId )
        .setPositiveButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener() {
            @Override
			public void onClick(DialogInterface dialog, int whichButton) {
            	
            }
        }).show();
    }
}
