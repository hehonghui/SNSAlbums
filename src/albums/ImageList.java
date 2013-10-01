
package albums;

import album.entry.R;


/**
 * @ClassName: ImageList 
 * @Description: ��дImageList��ʹ֮�ܹ������ڴ��е�Bitmap
 * @Author: Mr.Simple (�κ��)
 * @E-mail: bboyfeiyu@gmail.com 
 * @Date 2012-11-17 ����6:10:56 
 *
 */
public class ImageList extends BaseAdapter{  
      
    public Bitmap[] image;  
    Activity activity; 
    LayoutInflater mInflater;

      
    /**
     * 
     * @Constructor: 
     * @@param atv
     * @Description: ���캯��
     * @param atv
     */
    public ImageList(Activity atv) {  
        activity = atv;  
        mInflater = (LayoutInflater)atv.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }
   
    @Override
	public int getCount() {  
        
        return image.length;  
    }  
    
    @Override
	public Object getItem(int position) {  
    
        return image[position];  
    }   
    
    @Override
	public long getItemId(int position) {  
         
        return position;  
    }  
    
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
        holder.localImageView.setImageBitmap(image[position]);
        
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

