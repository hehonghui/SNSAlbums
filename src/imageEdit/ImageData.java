package imageEdit;

/**
 * 
 * @author �κ��		�๦�� : ͼ���ԭʼ��ݲ�����			date: 8.8
 * 
 * 1.ͼ��Ŀ�¡				clone()
 * 2.��ȡͼ���������ɫ		getPixelColor(int x, int y)
 * 3.��ȡͼ��ĺ�ɫ�ɷ�		getRComponent�Ķ�����غ���
 * 4.��ȡͼ�����ɫ�ɷ�		getGComponent�Ķ�����غ���
 * 5.��ȡͼ��ĺ�ɫ�ɷ�		getBComponent�Ķ�����غ���
 * 6.����ͼ���������ɫ		setPixelColor(int x, int y, int rgbcolor)
 * 7.��ȡ��ɫ����			getColorArray()
 * 8.��ȡĿ��ͼ��			getDstBitmap()
 * 
 */

public class ImageData {
	private Bitmap srcBitmap;
	private Bitmap dstBitmap;

	private int width;
	private int height;

	protected int[] colorArray;

	/**
	 * 
	 * @Constructor: 
	 * @@param bmp
	 * @Description: ���캯��
	 * @param bmp
	 */
	public ImageData(Bitmap bmp) {
		srcBitmap = bmp;
		width = bmp.getWidth();
		height = bmp.getHeight();
		dstBitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
		initColorArray();
	}

	
	/**
	 * (�� Javadoc,��д�ķ���) 
	 * @Title: clone
	 * @Description: 
	 * @return 
	 * @see java.lang.Object#clone()
	 */
	@Override
	public ImageData clone() {
		return new ImageData(this.srcBitmap);
	}

	
	/**
	 * @Method: initColorArray
	 * @Description: ��ʼ����ɫ����
	 */
	private void initColorArray() {
		colorArray = new int[width * height];
		srcBitmap.getPixels(colorArray, 0, width, 0, 0, width, height);
	}

	
	/**
	 * 	���� ����ȡ���ص��ϵ���ɫ
	 * @param x
	 * @param y
	 * @return int
	 */
	public int getPixelColor(int x, int y) {
		return colorArray[y * srcBitmap.getWidth() + x];
	}

	public int getPixelColor(int x, int y, int offset) {
		return colorArray[y * srcBitmap.getWidth() + x + offset];
	}

	
	/**
	 * 	���� ����ȡ��ɫ�ɷֵ�ɫ��
	 * @param x
	 * @param y
	 * @return int
	 * 
	 */
	public int getRComponent(int x, int y) {
		return Color.red(colorArray[y * srcBitmap.getWidth() + x]);
	}

	
	/**
	 * @Method: getRComponent
	 * @Description:
	 * @param x
	 * @param y
	 * @param offset
	 * @return
	 */
	public int getRComponent(int x, int y, int offset) {
		return Color.red(colorArray[y * srcBitmap.getWidth() + x + offset]);
	}

	
	/**
	 * 	���� ����ȡ��ɫ�ɷֵ�ɫ��
	 * @param x
	 * @param y
	 * @return int
	 * 
	 */
	public int getGComponent(int x, int y) {
		return Color.green(colorArray[y * srcBitmap.getWidth() + x]);
	}

	/**
	 * @Method: getGComponent
	 * @Description:
	 * @param x
	 * @param y
	 * @param offset
	 * @return
	 */
	public int getGComponent(int x, int y, int offset) {
		return Color.green(colorArray[y * srcBitmap.getWidth() + x + offset]);
	}

	
	/**
	 * 	���� ����ȡ��ɫ�ɷֵ�ɫ��
	 * @param x
	 * @param y
	 * @return int
	 * 
	 */
	public int getBComponent(int x, int y) {
		return Color.blue(colorArray[y * srcBitmap.getWidth() + x]);
	}

	
	/**
	 * @Method: getBComponent
	 * @Description:
	 * @param x
	 * @param y
	 * @param offset
	 * @return
	 */
	public int getBComponent(int x, int y, int offset) {
		return Color.blue(colorArray[y * srcBitmap.getWidth() + x + offset]);
	}

	
	/**
	 * 	���� ���������ص���ɫ
	 * @param x
	 * @param y
	 * @param r			��ɫ
	 * @param g			��ɫ
	 * @param b			��ɫ
	 * @return int
	 * 
	 */
	public void setPixelColor(int x, int y, int r, int g, int b) {
		int rgbcolor = (255 << 24) + (r << 16) + (g << 8) + b;
		colorArray[((y * srcBitmap.getWidth() + x))] = rgbcolor;
	}

	
	/**
	 * @Method: setPixelColor
	 * @Description:
	 * @param x
	 * @param y
	 * @param rgbcolor
	 */
	public void setPixelColor(int x, int y, int rgbcolor) {
		colorArray[((y * srcBitmap.getWidth() + x))] = rgbcolor;
	}

	
	/**
	 * 	���� ����ȡ��ɫ����
	 * @return int[]
	 * 
	 */
	public int[] getColorArray() {
		return colorArray;
	}

	/**
	 * 	���� ����ȡĿ��ͼ��
	 * @return Bitmap
	 * 
	 */
	public Bitmap getDstBitmap() {
		dstBitmap.setPixels(colorArray, 0, width, 0, 0, width, height);
		return dstBitmap;
	}

	/**
	 * @Method: safeColor
	 * @Description:
	 * @param a
	 * @return
	 */
	public int safeColor(int a) {
		if (a < 0)
			return 0;
		else if (a > 255)
			return 255;
		else
			return a;
	}

	/**
	 * @Method: getWidth
	 * @Description:
	 * @return
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * @Method: getHeight
	 * @Description:
	 * @return
	 */
	public int getHeight() {
		return height;
	}

}
