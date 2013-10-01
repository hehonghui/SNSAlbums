package chat;

import album.entry.MainViewActivity;

/**
 * Copyright (c) 2012,UIT-ESPACE( TEAM: UIT-GEEK)
 * All rights reserved.
 *
 * @Title: PhoneReceiver.java 
 * @Package: chat 
 * @Author: �κ��(Mr.Simple) 
 * @E-mail:bboyfeiyu@gmail.com
 * @Version: V1.0
 * @Date��2012-11-18 ����5:08:37
 * @Description:  �绰������,�е绰�������ֱ�ӹص�����.���������
 *
 */

public class PhoneReceiver extends BroadcastReceiver {
	 
	private Context mContext = null;
	/**
	 * (�� Javadoc,��д�ķ���) 
	 * @Title: onReceive
	 * @Description: 
	 * @param context
	 * @param intent 
	 * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
	 */
	 @Override
	 public void onReceive(Context context, Intent intent) {
		 
		System.out.println("action" + intent.getAction());
		mContext = context ;
		
		if (intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL)) {
			Toast.makeText(context, "�绰����", 0).show();
			Log.d("�绰���ȥ", "�رճ���") ;
			// �˳�����
			MainViewActivity.killCurrentApp(context);
		} else {
			// ������android�ĵ���ò��û��ר�����ڽ��������action,���ԣ���ȥ�缴����
			TelephonyManager tm = (TelephonyManager) context
					.getSystemService(Context.TELEPHONY_SERVICE);
			// ���ü�����
			tm.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);
			Log.d("�绰�������", "�رճ���") ;
			// �˳�����
			MainViewActivity.killCurrentApp(context);

		}
	}
	 
	 /**
	  * �绰״̬������
	  */
	 PhoneStateListener listener = new PhoneStateListener(){
	 
	  @Override
	  public void onCallStateChanged(int state, String incomingNumber) {
	   //state ��ǰ״̬ incomingNumber,ò��û��ȥ���API
	   super.onCallStateChanged(state, incomingNumber);
	   switch( state ){
    	   case TelephonyManager.CALL_STATE_IDLE:
    		   System.out.println("�Ҷ�");
    		   break;
    	   case TelephonyManager.CALL_STATE_OFFHOOK:
    		   System.out.println("����");
    		   break;
    	   case TelephonyManager.CALL_STATE_RINGING:
    		   System.out.println("����:�������"+incomingNumber);
    		   MainViewActivity.killCurrentApp( mContext ) ;
    		   Log.d("����", "����ر�") ;
    		   break;
    	    default:
    	    	break;
	   }
	   
	  } // enf of onCallStateChanged
	 
	 };
	}
