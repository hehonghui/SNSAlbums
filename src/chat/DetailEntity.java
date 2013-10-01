package chat;


/**
 * @ClassName: DetailEntity 
 * @Description:   ������Ϣ���װ
 * @Author: Mr.Simple (�κ��)
 * @E-mail: bboyfeiyu@gmail.com 
 * @Date 2012-11-17 ����7:24:57 
 *
 */

public class DetailEntity {
	
    private String name;			// �û���
    private String date;			// ϵͳʱ��
    private String text;			// ��Ϣ����
    private int layoutID;			// ����ID��
    
    
    /**
     * 
     * @Constructor: 
     * @
     * @Description: ���캯��
     */
    public DetailEntity() {
		
	}
	
    
    /**
     * 
     * @Constructor: 
     * @@param name
     * @@param date
     * @@param text
     * @@param layoutID
     * @Description: ���캯��
     * @param name
     * @param date
     * @param text
     * @param layoutID
     */
	public DetailEntity(String name, String date, String text, int layoutID) {
		super();
		this.name = name;
		this.date = date;
		this.text = text;
		this.layoutID = layoutID;
	}
	
	
	/**
	 * @Method: getName
	 * @Description: ��ȡ��ǰentity�����
	 * @return
	 */
	public String getName() {
		return name;
	}
	
	
	/**
	 * @Method: setName
	 * @Description:   ���õ�ǰentity�����
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	
	/**
	 * @Method: getDate
	 * @Description:  ��ȡ��ǰentity��ʱ��
	 * @return
	 */
	public String getDate() {
		return date;
	}
	
	
	/**
	 * @Method: setDate
	 * @Description: ���õ�ǰentity��ʱ��
	 * @param date
	 */
	public void setDate(String date) {
		
		this.date = date;
		Log.d("Time : ", this.date);
	}
	
	
	/**
	 * @Method: getText
	 * @Description:  ��ȡ��ǰentity���ı�
	 * @return
	 */
	public String getText() {
		return text;
	}
	
	
	/**
	 * @Method: setText
	 * @Description: ���õ�ǰentity���ı�
	 * @param text
	 */
	public void setText(String text) {
		this.text = text;
	}
	
	

	/**
	 * @Method: getLayoutID
	 * @Description:  ��ȡ��ǰentity����ID��
	 * @return
	 */
	public int getLayoutID() {
		return layoutID;
	}
	
	
	/**
	 * @Method: setLayoutID
	 * @Description:  ���ò���ID��
	 * @param layoutID
	 */
	public void setLayoutID(int layoutID) {
		this.layoutID = layoutID;
	}
    
    
}
