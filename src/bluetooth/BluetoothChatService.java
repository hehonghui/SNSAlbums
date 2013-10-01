
package bluetooth;

/**
 * @ClassName: BluetoothChatService 
 * @Description:  	1.�����������������״̬
 					2.����Զ�������豸
 					3.ͨ���߳���������Ͷ�ݱ���������״̬��Ϣ
 * @Author: Mr.Simple (�κ��)
 * @E-mail: bboyfeiyu@gmail.com 
 * @Date 2012-11-18 ����6:50:49 
 *
 */

public class BluetoothChatService {
    // ������Ϣ
    private static final String TAG = "BluetoothChatService";
    private static final boolean D = true;

    // ���������
    private static final String NAME = "BluetoothChat";

    // Unique UUID for this application.        UUID�ַ�Ψһ��.
    private static final UUID MY_UUID = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
    // private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // Member fields��		��ݳ�Ա�ֶ�
    private final BluetoothAdapter mAdapter;		// ������
    private final Handler mHandler;					// ��Ϣ�ַ��߳�,������UI�̷߳������
    private AcceptThread mAcceptThread;				// ���ڽ��ܵ��߳�
    private ConnectThread mConnectThread;			// �������ӵ��߳�
    private ConnectedThread mConnectedThread;		// ������״̬�µĴ����߳�
    private int mState;								// ����״̬
    
    private BluetoothDevice mConnectedDevice;		// �Ѿ������ϵ��豸.���ڻ�ȡ�豸��ַ,���ظ�ACTIVITY,�Ա㷢��ͼ��.

    // ����״̬����
    public static final int STATE_NONE = 0;       	// Ĭ��״̬
    public static final int STATE_LISTEN = 1;     	// ��������״̬
    public static final int STATE_CONNECTING = 2; 	// ����״̬
    public static final int STATE_CONNECTED = 3;  	// now connected to a remote device
    

    /**
     * ���� �� ���캯��,׼��һ���µ���������Ự
     * @param context  The UI Activity Context.     
     * @param handler  A Handler to send messages back to the UI Activity
     */
    public BluetoothChatService(Context context, Handler handler) {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mState = STATE_NONE;
        mHandler = handler;
    }

    
    /**
     * ���ܣ� �������������豸�ĵ�ǰ״̬
     * @param state  An integer defining the current connection state
     * 
     */
    private synchronized void setState(int state) {
        if (D) 
        	Log.d(TAG, "setState() " + mState + " -> " + state);
        mState = state;

        // ����״̬,��UI�̷߳����µ�״̬
        mHandler.obtainMessage(BluetoothChat.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
    }
       
    
    /**
     *  ���ܣ� �첽����ȡ�����豸����״̬
     *  
     */
    public synchronized int getState() {
        return mState;
    }

    
    /**
     *    ���ܣ� ��������������񣬲���ָ��һ������ͻ������ӵ��߳�,��AcceptThread,�������߳�,�����������״̬
     *    
     */
    public synchronized void start() {
        if (D) 
        	Log.d(TAG, "start");

        // ȡ���������ڳ������ӵ����������
        if (mConnectThread != null) {
        	mConnectThread.cancel();
        	mConnectThread = null;
        }

        // ȡ�������Ѿ����ӵ����������
        if (mConnectedThread != null) {
        	mConnectedThread.cancel(); 
        	mConnectedThread = null;
        }

        // �����̼߳����������ӡ����������״̬
        if (mAcceptThread == null) {
            mAcceptThread = new AcceptThread();
            mAcceptThread.start();
        }
        
        // ���������豸��ǰ״̬Ϊ����״̬
        setState(STATE_LISTEN);
    }

    
    /**
     * 	���ܣ� ���������߳�,���ӵ�ָ���������豸
     * @param device  The BluetoothDevice to connect
     */
    public synchronized void connect(BluetoothDevice device) {
        if (D) 
        	Log.d(TAG, "connect to: " + device);

        // ȡ���������ӵ�����
        if (mState == STATE_CONNECTING) {
            if (mConnectThread != null) {
            	mConnectThread.cancel();
            	mConnectThread = null;
            }
        }

        // ȡ���������е�����
        if (mConnectedThread != null) {
        	mConnectedThread.cancel();
        	mConnectedThread = null;
        }

        // �������ӵ�Զ���豸���߳�
        mConnectThread = new ConnectThread(device);
        mConnectThread.start();
        setState(STATE_CONNECTING);
    }

    
    /**
     * 	���� �� �����������߳���������������.
     * @param socket  The BluetoothSocket on which the connection was made.	
     * @param device  The BluetoothDevice that has been connected.      �����ӵ������豸
     * 
     */
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
        if (D)
        	Log.d(TAG, "connected");

        // ȡ�������߳�
        if (mConnectThread != null) {
        	mConnectThread.cancel(); 
        	mConnectThread = null;
        }

        // ȡ���������е��������߳�
        if (mConnectedThread != null) {
        	mConnectedThread.cancel(); 
        	mConnectedThread = null;
        }

        // ȡ����������߳�
        if (mAcceptThread != null) {
        	mAcceptThread.cancel(); 
        	mAcceptThread = null;
        }

        // �����߳�����������
        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();

        // �����������豸�����ָ�UI�߳�,mHandler��ϢͶ��
        Message msg = mHandler.obtainMessage(BluetoothChat.MESSAGE_DEVICE_NAME);
        Bundle bundle = new Bundle();
        bundle.putString(BluetoothChat.DEVICE_NAME, device.getName());
        msg.setData( bundle );				// ҳ��֮�䴫�����
        mHandler.sendMessage(msg);			// ��UI�̴߳�����Ϣ.UI�߳������UI 

        setState(STATE_CONNECTED);			// ��������״̬
        
        mConnectedDevice = device;			// �����ϵ�����
    }

    
    /*
     *  ���� �� ��ȡ�Ѿ������ϵ��������豸��ַ
     *  
     */
    public String getConnectedDeviceAddress()
    {
    	if(mConnectedDevice != null){
    		return mConnectedDevice.getAddress();
    	}
    	else
    		return null;
    }
    
    
    /**
     * ���ܣ� ֹͣ���е��߳�
     * 
     */
    public synchronized void stop() {
        if (D) Log.d(TAG, "stop");
        if (mConnectThread != null) {
        	mConnectThread.cancel(); 
        	mConnectThread = null;
        }
        if (mConnectedThread != null) {
        	mConnectedThread.cancel();
        	mConnectedThread = null;
        }
        if (mAcceptThread != null) {
        	mAcceptThread.cancel(); 
        	mAcceptThread = null;
        }
        setState(STATE_NONE);
        
    }

    
    /**	
     * ���ܣ� ���������߳�д�����,���������. �����첽�Ĳ���
     * @param out The bytes to write
     * @see ConnectedThread#write(byte[])
     */
    public void write(byte[] out) {	
    	
        ConnectedThread cThread;						// �����ӵ��߳�.���Է������
        // ͬ��һ���������̵߳Ŀ���.    
        synchronized (this) {
            if (mState != STATE_CONNECTED) 				// ���û������.�򷵻�
            return;
            cThread = mConnectedThread;					// �������߳�
        }
     
        cThread.write(out);						// ʹ���첽�̷߳������
    }

    
    /**
     * ���ܣ� ָ����������ʧ�ܵ�����,����֪ͨUI�߳�
     * 
     */
    private void connectionFailed() {			// ����ʧ��
        setState(STATE_LISTEN);

        // Send a failure message back to the Activity
        Message msg = mHandler.obtainMessage(BluetoothChat.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(BluetoothChat.TOAST, "û���������豸.");
        msg.setData(bundle);
        mHandler.sendMessage(msg);			// ��������ʧ�ܵ���Ϣ��UI�߳�
    }

    
    /**
     * ���� �� ָ����ʧ������,����֪ͨUI�߳�
     * Indicate that the connection was lost and notify the UI Activity.
     */
    private void connectionLost() {
        setState(STATE_LISTEN);

        // Send a failure message back to the Activity
        Message msg = mHandler.obtainMessage(BluetoothChat.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(BluetoothChat.TOAST, "�豸���Ӷ�ʧ.");
        msg.setData(bundle);
        mHandler.sendMessage(msg);			// �ַ�ʧȥ���ӵ���Ϣ��UI�߳�
    }

    /**	
     * 	���ܣ� �������ڼ���״̬ʱ�Ľ��ܿͻ������ӵ��߳�
     * 
     */
    private class AcceptThread extends Thread {
        // The local server socket
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread() {
            BluetoothServerSocket tmp = null;		// �õ��Ƿ�����ģʽ���׽���socket

            // ����һ��������socket����
            try {
                tmp = mAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);	// ����ͻ��˵���������
            } catch (IOException e) {
                Log.e(TAG, "listen() failed", e);
            }
            mmServerSocket = tmp;					// ����ʱ�ķ������׽��ָ��� ���Ա����mmServerSocket
        }

        @Override
		public void run() {
            if (D) 
            	Log.d(TAG, "BEGIN mAcceptThread" + this);
            setName("AcceptThread");
            BluetoothSocket socket = null;

            // Listen to the server socket if we're not connected.
            while (mState != STATE_CONNECTED) {
                try {
      
                    socket = mmServerSocket.accept();	// ���ܿͻ��˵���������.������,���ȴ�ͻ�������
                } catch (IOException e) {
                    Log.e(TAG, "accept() failed", e);
                    break;
                }

                // If a connection was accepted.   ���ӱ�����
                if (socket != null) {
                    synchronized (BluetoothChatService.this) {
                    	
                        switch (mState) {
                        
	                        case STATE_LISTEN:
	                        case STATE_CONNECTING:
	                            // �����������߳�
	                            connected(socket, socket.getRemoteDevice());
	                            break;
	                            
	                        case STATE_NONE:
	                        case STATE_CONNECTED:
	                            // Either not ready or already connected. Terminate new socket.
	                            try {
	                                socket.close();
	                            } catch (IOException e) {
	                                Log.e(TAG, "Could not close unwanted socket", e);
	                            }
	                            break;
                        }
                    }
                }
            }
            if (D) Log.i(TAG, "END mAcceptThread");
        }

        /*
         * ���� �� ȡ�����
         * 
         */
        public void cancel() {
            if (D) Log.d(TAG, "cancel " + this);
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of server failed", e);
            }
        }
    }


    /**
     * ���� �� �����豸���߳�
     * 
     */
    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        
        public ConnectThread(BluetoothDevice device) {
            mmDevice = device;
            BluetoothSocket tmp = null;

            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            try {
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                Log.e(TAG, "create() failed", e);
            }
            mmSocket = tmp;
        }

        @Override
		public void run() {
            Log.i(TAG, "BEGIN mConnectThread");
            setName("ConnectThread");

            // ʹ�����豸���ɷ���
            mAdapter.cancelDiscovery();

            // ���������豸
            try {

                mmSocket.connect();
            } catch (IOException e) {
                connectionFailed();
                // �ر���������
                try {
                    mmSocket.close();
                } catch (IOException e2) {
                    Log.e(TAG, "unable to close() socket during connection failure", e2);
                }
                // ����BluetoothChatService����������ģʽ
                BluetoothChatService.this.start();
                return;
            }

            // ���������߳�
            synchronized (BluetoothChatService.this) {
                mConnectThread = null;
            }

            // �����������߳�
            connected(mmSocket, mmDevice);
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }

    
    /**
     * ���ܣ� �������߳�,��������socket��ݴ���
     * 
     */
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            Log.d(TAG, "create ConnectedThread");
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // ���BluetoothSocket�����롢�����
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "temp sockets not created", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        @Override
		public void run() {						// Ĭ�ϵľ���һֱ�ڼ���Ƿ�����ݷ���.
        	
            Log.i(TAG, "BEGIN mConnectedThread");
            
            byte[] buffer = new byte[1024];
            int bytes;

            // �����������Ӻ�,�����������ļ���״̬
            while (true) {

	                try {
	                    // ���������������
	                    bytes = mmInStream.read(buffer);
	
	                    // ÿ�ӵ�1024�ֽھͷ��͸�UI�߳�
	                    mHandler.obtainMessage(BluetoothChat.MESSAGE_READ, bytes, -1, buffer)
	                            .sendToTarget();
	                } catch (IOException e) {
	                    Log.e(TAG, "disconnected", e);
	                    connectionLost();
	                    break;
	                }	
	                
            	}
            }		// end of run().

        
        /**
         * ���� �� �������ӵ������д�����
         * @param buffer  The bytes to write
         * 
         */
        public void write(byte[] buffer) {		
            try {
                mmOutStream.write(buffer);

                // ���ѷ��͵���ϢͶ�ݸ�UIҳ��
                mHandler.obtainMessage(BluetoothChat.MESSAGE_WRITE, -1, -1, buffer).sendToTarget();
            } catch (IOException e) {
                Log.e(TAG, "Exception during write", e);
            }
        }

     
        /**
         * ���� �� ȡ���ҹر�socket
         * 
         */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }
}
