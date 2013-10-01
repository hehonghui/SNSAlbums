package albums;

import album.entry.R;


/**
 * @ClassName: ImageAdapter 
 * @Description:  ��дImageList��ʹ֮�ܹ������ڴ��е�Bitmap
 * @Author: xxjgood
 * @E-mail: bboyfeiyu@gmail.com 
 * @Date 2012-11-17 ����6:02:00 
 *
 */
public class ImageAdapter extends BaseAdapter{  
      
    public Bitmap[] image;  
    public ArrayList<Bitmap> mImageList = null;
    Activity activity; 
    LayoutInflater mInflater;
      
    /**
     * 
     * @Constructor: 
     * @@param atv
     * @Description: ���캯��
     * @param atv
     */
    public ImageAdapter(Activity atv) {  
        activity = atv;  
        mInflater = (LayoutInflater)atv.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mImageList = new ArrayList<Bitmap>();

    }
   
    /**
     * (�� Javadoc,��д�ķ���) 
     * @Title: getCount
     * @Description: 
     * @return 
     * @see android.widget.Adapter#getCount()
     */
    @Override
	public int getCount() {  
        
    	return mImageList.size();
    }  
    
    /**
     * (�� Javadoc,��д�ķ���) 
     * @Title: getItem
     * @Description: 
     * @param position
     * @return 
     * @see android.widget.Adapter#getItem(int)
     */
    @Override
	public Object getItem(int position) {  
    
    	return mImageList.get(position);
    }   
    
    
    /**
     * (�� Javadoc,��д�ķ���) 
     * @Title: getItemId
     * @Description: 
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
     * @Title: getView
     * @Description:  ���view,�ڴ���ViewHolder��������
     * @param position
     * @param convertView
     * @param parent
     * @return 
     * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
     */
    @Override
	public View getView(int position, View convertView, ViewGroup parent) {  
 
    	ViewHolder holder = null;
        if (convertView == null) {
        	
            convertView = mInflater.inflate(R.layout.image, null);
            holder = new ViewHolder();
            holder.localImageView = (ImageView)convertView.findViewById(R.id.bigimage);
            convertView.setLayoutParams(new GridView.LayoutParams(100, 100));
            holder.localImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            convertView.setTag(holder);
            
        } else {
            holder = (ViewHolder)convertView.getTag();
        }
        holder.localImageView.setImageBitmap(mImageList.get(position));
        
        return convertView;
    } 
    
    private class ViewHolder   
    {  
        public ImageView localImageView = null;  

    }  
    

    /**
     * @Method: drawableToBitmap
     * @Description:  ��drawableת����Bitmap
     * @param drawable
     * @return
     */
    public static Bitmap drawableToBitmap(Drawable drawable) {    
            
        Bitmap bitmap = Bitmap.createBitmap(    
                                        drawable.getIntrinsicWidth(),    
                                        drawable.getIntrinsicHeight(),    
                                        drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888    
                                                        : Bitmap.Config.RGB_565);    
        Canvas canvas = new Canvas(bitmap);    
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());    
        drawable.draw(canvas);    
        return bitmap;    
    }  
}  
