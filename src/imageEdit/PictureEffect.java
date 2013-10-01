package imageEdit;

/**
 * author : �κ�� 		����:	ͼƬ������,ͼ��ĸ���ɫ�ʵ���.
 * 
 * 1.��תͼ��			Bitmap rotateBitmap(Bitmap bmp, float degree);
 * 2.ͼ��Ļ���Ч��		oldRemeberEffect(Bitmap bmp);
 * 3.ͼ�����Ч��		sharpenEffect(Bitmap bmp);
 * 4.ͼ��ĵ�ƬЧ��		filmEffect(Bitmap bmp);
 * 5.ͼ��Ĺ���Ч��		sunshineEffect(Bitmap bmp);
 * 6.ͼ��ĸ���Ч��		embossEffect(Bitmap bmp);
 * 7.ͼ���ģ��Ч��		blurImage(Bitmap bmp)
 * 8.ͼ��ĸ�˹ģ��Ч��	blurImageAmeliorate(Bitmap bmp);
 * 
 */

public class PictureEffect{

	
	/**
	 * ���ܣ� ͼƬ��ת 
	 * @param bmp
	 *      Ҫ��ת��ͼƬ
	 * @param degree
	 *      ͼƬ��ת�ĽǶȣ���ֵΪ��ʱ����ת����ֵΪ˳ʱ����ת
	 * @return
	 */
	public static Bitmap rotateBitmap(Bitmap bmp, float degree) {
		if ( bmp != null )
		{
			Matrix matrix = new Matrix();
			matrix.postRotate(degree);
			return Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
		}
		else
		{
			return null;
		}
	}

	
	/**
	 * ���� �� ����Ч��
	 * @param bmp
	 * @return
	 * �㷨�����޸�RGBֵ���ɣ�
	 * ��ʽ��   r��g��bΪԭRGBֵ.
	 * R = 0.393r + 0.769g + 0.189b;
	 * G = 0.349r + 0.686g + 0.168b;
	 * B = 0.272r + 0.534g + 0.131b;
	 * 
	 */
	public static Bitmap oldRemeberEffect(Bitmap bmp)
	{
		
		int width = bmp.getWidth();
		int height = bmp.getHeight();
		Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
		
		int oldColor = 0;
		int oldR = 0;
		int oldG = 0;
		int oldB = 0;
		int newR = 0;
		int newG = 0;
		int newB = 0;
		
		int[] pixels = new int[width * height];
		bmp.getPixels(pixels, 0, width, 0, 0, width, height);
		for (int i = 0; i < height; i++)
		{
			for (int k = 0; k < width; k++)
			{
				oldColor = pixels[width * i + k];
				oldR = Color.red(oldColor);
				oldG = Color.green(oldColor);
				oldB = Color.blue(oldColor);
				newR = (int) (0.393 * oldR + 0.769 * oldG + 0.189 * oldB);
				newG = (int) (0.349 * oldR + 0.686 * oldG + 0.168 * oldB);
				newB = (int) (0.272 * oldR + 0.534 * oldG + 0.131 * oldB);
				int newColor = Color.argb(255, newR > 255 ? 255 : newR,
											newG > 255 ? 255 : newG, newB > 255 ? 255 : newB);
				pixels[width * i + k] = newColor;
			}
		}
		
		bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
	
		return bitmap;
	}		// end of oldRemeber()
	
	
	/**
	 *  ���ܣ� ͼƬ�񻯣�������˹�任��:��������˹�����е�������Ӧ���RGBֵ֮���ٳ�����Ӧ��ϵ��ĺ���Ϊ��ǰ���RGBֵ��
	 * @param bmp
	 * @return
	 * 
	 */
	public static Bitmap sharpenEffect(Bitmap bmp)
	{
		// ������˹����
		int[] laplacian = new int[] { -1, -1, -1, -1, 9, -1, -1, -1, -1 };
		
		int width = bmp.getWidth();
		int height = bmp.getHeight();
		Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
		
		int oldColor = 0;
		int oldR = 0;
		int oldG = 0;
		int oldB = 0;
		int newR = 0;
		int newG = 0;
		int newB = 0;
		
		int idx = 0;
		float alpha = 0.3F;
		int[] pixels = new int[width * height];
		bmp.getPixels(pixels, 0, width, 0, 0, width, height);
		for (int i = 1, length = height - 1; i < length; i++)
		{
			for (int k = 1, len = width - 1; k < len; k++)
			{
				idx = 0;
				for (int m = -1; m <= 1; m++)
				{
					for (int n = -1; n <= 1; n++)
					{
						oldColor = pixels[(i + n) * width + k + m];
						oldR = Color.red(oldColor);
						oldG = Color.green(oldColor);
						oldB = Color.blue(oldColor);
						
						newR = newR + (int) (oldR * laplacian[idx] * alpha);
						newG = newG + (int) (oldG * laplacian[idx] * alpha);
						newB = newB + (int) (oldB * laplacian[idx] * alpha);
						idx++;
					}
				}
				
				newR = Math.min(255, Math.max(0, newR));
				newG = Math.min(255, Math.max(0, newG));
				newB = Math.min(255, Math.max(0, newB));
				
				pixels[i * width + k] = Color.argb(255, newR, newG, newB);
				newR = 0;
				newG = 0;
				newB = 0;
			}
		}
		
		bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
		return bitmap;
	}	// end of sharpenImageAmeliorate()
	
	
	/**
	 * ���ܣ� ����Ч��:��ǰһ�����ص��RGBֵ�ֱ��ȥ��ǰ���ص��RGBֵ������127��Ϊ��ǰ���ص��RGBֵ
	 * ��B��ĸ���Ч�����£�
	 * B.r = C.r - B.r + 127; 	B.g = C.g - B.g + 127;	B.b = C.b - B.b + 127;
	 * @param bmp
	 * @return
	 * 
	 */
	public static Bitmap embossEffect(Bitmap bmp)
	{
		int width = bmp.getWidth();
		int height = bmp.getHeight();
		Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
		
		int pixR = 0;
		int pixG = 0;
		int pixB = 0;
		int pixColor = 0;
		int newR = 0;
		int newG = 0;
		int newB = 0;
		
		int[] pixels = new int[width * height];
		bmp.getPixels(pixels, 0, width, 0, 0, width, height);
		int pos = 0;
		for (int i = 1, length = height - 1; i < length; i++)
		{
			for (int k = 1, len = width - 1; k < len; k++)
			{
				pos = i * width + k;
				pixColor = pixels[pos];
				
				pixR = Color.red(pixColor);
				pixG = Color.green(pixColor);
				pixB = Color.blue(pixColor);
				
				pixColor = pixels[pos + 1];
				newR = Color.red(pixColor) - pixR + 127;
				newG = Color.green(pixColor) - pixG + 127;
				newB = Color.blue(pixColor) - pixB + 127;
				
				newR = Math.min(255, Math.max(0, newR));
				newG = Math.min(255, Math.max(0, newG));
				newB = Math.min(255, Math.max(0, newB));
				
				pixels[pos] = Color.argb(255, newR, newG, newB);
			}
		}
		
		bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
		return bitmap;
	}	// end of embossEffect()
	
	
	/**
	 * ���ܣ� ��ƬЧ���㷨ԭ�?����ǰ���ص��RGBֵ�ֱ���255֮����ֵ��Ϊ��ǰ���RGBֵ��
	 * ��ABC ��B��ĵ�ƬЧ��
	 * B.r = 255 - B.r; 	B.g = 255 - B.g; 	B.b = 255 - B.b;
	 * @param bmp
	 * @return
	 * 
	 */
	public static Bitmap filmEffect(Bitmap bmp)
	{
		// RGBA�����ֵ
		final int MAX_VALUE = 255;
		int width = bmp.getWidth();
		int height = bmp.getHeight();
		Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
		
		int pixR = 0;
		int pixG = 0;
		int pixB = 0;
		
		int pixColor = 0;
		
		int newR = 0;
		int newG = 0;
		int newB = 0;
		
		int[] pixels = new int[width * height];
		bmp.getPixels(pixels, 0, width, 0, 0, width, height);
		int pos = 0;
		for (int i = 1, length = height - 1; i < length; i++)
		{
			for (int k = 1, len = width - 1; k < len; k++)
			{
				pos = i * width + k;
				pixColor = pixels[pos];
				
				pixR = Color.red(pixColor);
				pixG = Color.green(pixColor);
				pixB = Color.blue(pixColor);
				
				newR = MAX_VALUE - pixR;
				newG = MAX_VALUE - pixG;
				newB = MAX_VALUE - pixB;
				
				newR = Math.min(MAX_VALUE, Math.max(0, newR));
				newG = Math.min(MAX_VALUE, Math.max(0, newG));
				newB = Math.min(MAX_VALUE, Math.max(0, newB));
				
				pixels[pos] = Color.argb(MAX_VALUE, newR, newG, newB);
			}
		}
		
		bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
		return bitmap;
	}	// end of filmEffect()
	
	
	/**
	 * ���ܣ� ����Ч��:�㷨ԭ�?ͼƬ��������ص㰴�ո�Բ�ģ�����Բ�뾶�ı仯�����ص��RGBֵ�ֱ������Ӧ��ֵ��Ϊ��ǰ���RGBֵ��
	 * ��
		ABCDE
		FGHIJ
		KLMNO
		���ָ��H��Ϊ����Ч������ģ��뾶Ϊ�������ص㣬��ôG��RGBֵ�ֱ���ϵ�ֵ���F���Ҫ��
		��ΪRGBֵԽ�󣬾�Խ�ӽ��ɫ������G�㿴������F��Ҫ�ף�Ҳ���Ǿ��������Խ��Ч��ͼ
	 * @param bmp
	 * @return
	 * 
	 */
	public static Bitmap sunshineEffect(Bitmap bmp)
	{
		final int width = bmp.getWidth();
		final int height = bmp.getHeight();
		Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
		
		int pixR = 0;
		int pixG = 0;
		int pixB = 0;
		
		int pixColor = 0;
		
		int newR = 0;
		int newG = 0;
		int newB = 0;
		
		int centerX = width / 2;
		int centerY = height / 2;
		int radius = Math.min(centerX, centerY);
		
		final float strength = 150F; // ����ǿ�� 100~150
		int[] pixels = new int[width * height];
		bmp.getPixels(pixels, 0, width, 0, 0, width, height);
		int pos = 0;
		for (int i = 1, length = height - 1; i < length; i++)
		{
			for (int k = 1, len = width - 1; k < len; k++)
			{
				pos = i * width + k;
				pixColor = pixels[pos];
				
				pixR = Color.red(pixColor);
				pixG = Color.green(pixColor);
				pixB = Color.blue(pixColor);
				
				newR = pixR;
				newG = pixG;
				newB = pixB;
				
				// ���㵱ǰ�㵽�������ĵľ��룬ƽ�����ϵ��������֮��ľ���
				int distance = (int) (Math.pow((centerY - i), 2) + Math.pow(centerX - k, 2));
				if (distance < radius * radius)
				{
					// ���վ����С�������ӵĹ���ֵ
					int result = (int) (strength * (1.0 - Math.sqrt(distance) / radius));
					newR = pixR + result;
					newG = pixG + result;
					newB = pixB + result;
				}
				
				newR = Math.min(255, Math.max(0, newR));
				newG = Math.min(255, Math.max(0, newG));
				newB = Math.min(255, Math.max(0, newB));
				
				pixels[pos] = Color.argb(255, newR, newG, newB);
			}
		}
		
		bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
		return bitmap;
	}
	
	
	/**
	 * ���ܣ� ģ��Ч��
	 * @param bmp
	 * @return
	 * 
	 */
	public static Bitmap blurImage(Bitmap bmp)
	{
		int width = bmp.getWidth();
		int height = bmp.getHeight();
		Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
		
		int pixColor = 0;
		
		int newR = 0;
		int newG = 0;
		int newB = 0;
		
		int newColor = 0;
		
		int[][] colors = new int[9][3];
		for (int i = 1, length = width - 1; i < length; i++)
		{
			for (int k = 1, len = height - 1; k < len; k++)
			{
				for (int m = 0; m < 9; m++)
				{
					int s = 0;
					int p = 0;
					switch(m)
					{
					case 0:
						s = i - 1;
						p = k - 1;
						break;
					case 1:
						s = i;
						p = k - 1;
						break;
					case 2:
						s = i + 1;
						p = k - 1;
						break;
					case 3:
						s = i + 1;
						p = k;
						break;
					case 4:
						s = i + 1;
						p = k + 1;
						break;
					case 5:
						s = i;
						p = k + 1;
						break;
					case 6:
						s = i - 1;
						p = k + 1;
						break;
					case 7:
						s = i - 1;
						p = k;
						break;
					case 8:
						s = i;
						p = k;
					}
					pixColor = bmp.getPixel(s, p);
					colors[m][0] = Color.red(pixColor);
					colors[m][1] = Color.green(pixColor);
					colors[m][2] = Color.blue(pixColor);
				}
				
				for (int m = 0; m < 9; m++)
				{
					newR += colors[m][0];
					newG += colors[m][1];
					newB += colors[m][2];
				}
				
				newR = (int) (newR / 9F);
				newG = (int) (newG / 9F);
				newB = (int) (newB / 9F);
				
				newR = Math.min(255, Math.max(0, newR));
				newG = Math.min(255, Math.max(0, newG));
				newB = Math.min(255, Math.max(0, newB));
				
				newColor = Color.argb(255, newR, newG, newB);
				bitmap.setPixel(i, k, newColor);
				
				newR = 0;
				newG = 0;
				newB = 0;
			}
		}
		
		return bitmap;
	}
	
	/**
	 * ���ܣ� �ữЧ��(��˹ģ��)
	 * @param bmp
	 * @return
	 * 
	 */
	public static Bitmap blurImageAmeliorate(Bitmap bmp)
	{
		// ��˹����
		int[] gauss = new int[] { 1, 2, 1, 2, 4, 2, 1, 2, 1 };
		
		int width = bmp.getWidth();
		int height = bmp.getHeight();
		Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
		
		int pixR = 0;
		int pixG = 0;
		int pixB = 0;
		
		int pixColor = 0;
		
		int newR = 0;
		int newG = 0;
		int newB = 0;
		
		int delta = 16; // ֵԽСͼƬ��Խ����Խ����Խ��
		
		int idx = 0;
		int[] pixels = new int[width * height];
		bmp.getPixels(pixels, 0, width, 0, 0, width, height);
		for (int i = 1, length = height - 1; i < length; i++)
		{
			for (int k = 1, len = width - 1; k < len; k++)
			{
				idx = 0;
				for (int m = -1; m <= 1; m++)
				{
					for (int n = -1; n <= 1; n++)
					{
						pixColor = pixels[(i + m) * width + k + n];
						pixR = Color.red(pixColor);
						pixG = Color.green(pixColor);
						pixB = Color.blue(pixColor);
						
						newR = newR + (pixR * gauss[idx]);
						newG = newG + (pixG * gauss[idx]);
						newB = newB + (pixB * gauss[idx]);
						idx++;
					}
				}
				
				newR /= delta;
				newG /= delta;
				newB /= delta;
				
				newR = Math.min(255, Math.max(0, newR));
				newG = Math.min(255, Math.max(0, newG));
				newB = Math.min(255, Math.max(0, newB));
				
				pixels[i * width + k] = Color.argb(255, newR, newG, newB);
				
				newR = 0;
				newG = 0;
				newB = 0;
			}
		}
		
		bitmap.setPixels(pixels, 0, width, 0, 0, width, height);

		return bitmap;
	}

}
