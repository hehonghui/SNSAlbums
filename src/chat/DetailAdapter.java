package chat;

import album.entry.R;


/**
 * @ClassName: DetailAdapter 
 * @Description:  ������Ϣ�б�������  �����߿��Բο������б�,ʹ��ListActivity��ʵ�����ݶ��Ź��� ��
 * @Author: Mr.Simple (�κ��)
 * @E-mail: bboyfeiyu@gmail.com 
 * @Date 2012-11-17 ����7:22:56 
 *
 */

public class DetailAdapter implements ListAdapter{
	
	private ArrayList<DetailEntity> mMsgList;			// ��Ϣ�б�
	private Context mContext;							// ��ǰ��activity
	
	int[] imgArray = {	R.drawable.bad_smile_96,R.drawable.laugh_96,
						R.drawable.fire_96,R.drawable.money_96,		// ��ѡͷ���б�
						R.drawable.grimace_96,R.drawable.girl_96,
						R.drawable.face_96, R.drawable.o_96
						}; 
	
	
	/**
	 * @Constructor: 
	 * @@param context
	 * @@param msgList
	 * @Description: ���캯��
	 * @param context
	 * @param msgList
	 */
	public DetailAdapter(Context context ,ArrayList<DetailEntity> msgList) {
		
		mContext = context;
		mMsgList = msgList;
	}
	
	/**
	 * (�� Javadoc,��д�ķ���) 
	 * @Title: areAllItemsEnabled
	 * @Description: 
	 * @return 
	 * @see android.widget.ListAdapter#areAllItemsEnabled()
	 */
	@Override
	public boolean areAllItemsEnabled() {
		return false;
	}
	
	/**
	 * (�� Javadoc,��д�ķ���) 
	 * @Title: isEnabled
	 * @Description: 
	 * @param arg0
	 * @return 
	 * @see android.widget.ListAdapter#isEnabled(int)
	 */
	@Override
	public boolean isEnabled(int arg0) {
		return false;
	}
	
	
	/**
	 * (�� Javadoc,��д�ķ���) 
	 * @Title: getCount
	 * @Description:  ��ȡ��ǰadapter���б���ݵ�����
	 * @return 
	 * @see android.widget.Adapter#getCount()
	 */
	@Override
	public int getCount() {
		return mMsgList.size();
	}
	
	
	/**
	 * (�� Javadoc,��д�ķ���) 
	 * @Title: getItem
	 * @Description:  ��ȡ��ǰadapter��һ��
	 * @param position
	 * @return 
	 * @see android.widget.Adapter#getItem(int)
	 */
	@Override
	public Object getItem(int position) {
		return mMsgList.get(position);
	}
	

	/**
	 * (�� Javadoc,��д�ķ���) 
	 * @Title: getItemId
	 * @Description:  ��ȡ��ǰadapter��һ���ID��
	 * @param position
	 * @return 
	 * @see android.widget.Adapter#getItemId(int)
	 */
	@Override
	public long getItemId(int position) {
		return position;
	}
	
	/**
	 * (�� Javadoc,��д�ķ���) 
	 * @Title: getItemViewType
	 * @Description: 
	 * @param position
	 * @return 
	 * @see android.widget.Adapter#getItemViewType(int)
	 */
	@Override
	public int getItemViewType(int position) {
		return position;
	}
	
	
	/**
	 * @Method: clear
	 * @Description:
	 */
	public void clear(){
		mMsgList.clear();
	}
	

	/**
	 * (�� Javadoc,��д�ķ���) 
	 * @Title: getView
	 * @Description:   ��ȡ��Ϣ�б��е�view  (ÿ�λ�ȡһ�����)
	 * @param position
	 * @param convertView
	 * @param parent
	 * @return 
	 * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		// �õ���ǰentity
		DetailEntity entity = mMsgList.get(position);
		// ��ȡ��ǰentity�Ĳ���ID��
		int itemLayout = entity.getLayoutID();				
		
		// ��ctx����һ�����Բ���
		LinearLayout layout = new LinearLayout(mContext);
		
		// ��LayoutInflater����layout�ļ����µ�xml�����ļ�������ʵ��
		LayoutInflater vi = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		vi.inflate(itemLayout, layout, true);
		
		// �������ڸ�����Ϣ��ʾ�ؼ��и�ֵ
		TextView tvName = (TextView) layout.findViewById(R.id.messagedetail_row_name);
		tvName.setText(entity.getName());
		
		TextView tvDate = (TextView) layout.findViewById(R.id.messagedetail_row_date);
		tvDate.setText(entity.getDate());
		
		TextView tvText = (TextView) layout.findViewById(R.id.messagedetail_row_text);
		tvText.setText(entity.getText());
		
		return layout;
	}
	
	/**
	 * (�� Javadoc,��д�ķ���) 
	 * @Title: getViewTypeCount
	 * @Description: 
	 * @return 
	 * @see android.widget.Adapter#getViewTypeCount()
	 */
	@Override
	public int getViewTypeCount() {
		
		return mMsgList.size();
	}
	
	/**
	 * (�� Javadoc,��д�ķ���) 
	 * @Title: hasStableIds
	 * @Description: 
	 * @return 
	 * @see android.widget.Adapter#hasStableIds()
	 */
	@Override
	public boolean hasStableIds() {
		return false;
	}
	
	/**
	 * (�� Javadoc,��д�ķ���) 
	 * @Title: isEmpty
	 * @Description: 
	 * @return 
	 * @see android.widget.Adapter#isEmpty()
	 */
	@Override
	public boolean isEmpty() {
		return false;
	}
	
	/**
	 * (�� Javadoc,��д�ķ���) 
	 * @Title: registerDataSetObserver
	 * @Description: 
	 * @param observer 
	 * @see android.widget.Adapter#registerDataSetObserver(android.database.DataSetObserver)
	 */
	@Override
	public void registerDataSetObserver(DataSetObserver observer) {
	}
	
	/**
	 * (�� Javadoc,��д�ķ���) 
	 * @Title: unregisterDataSetObserver
	 * @Description: 
	 * @param observer 
	 * @see android.widget.Adapter#unregisterDataSetObserver(android.database.DataSetObserver)
	 */
	@Override
	public void unregisterDataSetObserver(DataSetObserver observer) {
	}
    
	
}
