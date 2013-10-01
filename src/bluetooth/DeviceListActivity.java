
package bluetooth;

import album.entry.R;


/**
 * @ClassName: DeviceListActivity 
 * @Description:  * 	
 * 		1. ��ʾ�ɷ��ֵ������豸
 * 		2. ��ʾ�Ѿ���Ե��豸
 *  	3. ɨ���豸
 *  	4. ʹ�豸�ɷ���
 * @Author: Mr.Simple (�κ��)
 * @E-mail: bboyfeiyu@gmail.com 
 * @Date 2012-11-18 ����6:51:11 
 *
 */

public class DeviceListActivity extends Activity {
	
    // ������Ϣ
    private static final String TAG = "DeviceListActivity";
    private static final boolean D = true;

    // ���ص�Intent�Ķ�����Ϣ
    public static String EXTRA_DEVICE_ADDRESS = "device_address";

    // ��ݳ�Ա
    private BluetoothAdapter mBtAdapter;
    private ArrayAdapter<String> mPairedDevicesArrayAdapter;
    private ArrayAdapter<String> mNewDevicesArrayAdapter;

    
    /**
     * ���� �� ҳ�洴��,��ʼ���ؼ��ͱ�����
     * (non-Javadoc)
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

       
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.device_list);

        setResult(Activity.RESULT_CANCELED);

        // ɨ�谴ť
        Button scanButton = (Button) findViewById(R.id.button_scan);
        scanButton.setOnClickListener(new OnClickListener() {
            @Override
			public void onClick(View v) {
                doDiscovery();
                v.setVisibility(View.GONE);
            }
        });

        // ��ʼ������������
        mPairedDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name);
        mNewDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name);

        // Ϊ����Ե��豸listView�����������͵���¼�
        ListView pairedListView = (ListView) findViewById(R.id.paired_devices);
        pairedListView.setAdapter(mPairedDevicesArrayAdapter);
        pairedListView.setOnItemClickListener(mDeviceClickListener);

        // �·��ֵ��豸�б�
        ListView newDevicesListView = (ListView) findViewById(R.id.new_devices);
        newDevicesListView.setAdapter(mNewDevicesArrayAdapter);
        newDevicesListView.setOnItemClickListener(mDeviceClickListener);

        // ��ɨ�����豸��ע��һ��������
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mReceiver, filter);

        // �����������豸�����ע��һ���㲥������
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(mReceiver, filter);

        // ��ȡ����������
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();

        // �����豸�ļ���
        Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();

        // �����������豸,����ӵ�mPairedDevicesArrayAdapter
        if (pairedDevices.size() > 0) {
            findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);
            for (BluetoothDevice device : pairedDevices) {
                mPairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        } else {
            String noDevices = getResources().getText(R.string.none_paired).toString();
            mPairedDevicesArrayAdapter.add(noDevices);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mBtAdapter != null) {
            mBtAdapter.cancelDiscovery();
        }

        this.unregisterReceiver(mReceiver);
    }

    /**
     * ���ܣ� ����ɨ���豸
     * 
     */
    private void doDiscovery() {
        if (D) Log.d(TAG, "doDiscovery()");

        // ָ��ҳ�����
        setProgressBarIndeterminateVisibility(true);
        setTitle(R.string.scanning);

        findViewById(R.id.title_new_devices).setVisibility(View.VISIBLE);

        // �������ɨ��.��ֹͣ
        if (mBtAdapter.isDiscovering()) {
            mBtAdapter.cancelDiscovery();
        }

        // ����������ɨ��
        mBtAdapter.startDiscovery();
    }

    /**
     * ���ܣ�  ���б��ϵĵ���¼�.
     * 
     */
    private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
        @Override
		public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
    
            mBtAdapter.cancelDiscovery();

            // ��ȡMAC��ַ,���17λ�ַ�ΪMAC��ַ
            String info = ((TextView) v).getText().toString();
            String address = info.substring(info.length() - 17);

            // ��������,����������MAC��ַ
            Intent intent = new Intent();
            intent.putExtra(EXTRA_DEVICE_ADDRESS, address);
            setResult(Activity.RESULT_OK, intent);
            finish();
        }
    };


    /**
     *  ���ܣ� �㲥������,��ɨ����Ϻ�������豸�ĺ͸ı�ҳ�����
     *  
     */
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // ��ɨ�跢����һ���豸
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
            	
                // ��Intent�л�ȡBluetoothDevice����
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // ����Ѿ����,�����
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    mNewDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                }
                
            // ��ɨ�����,�ı�ҳ�����
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                setProgressBarIndeterminateVisibility(false);
                setTitle(R.string.select_device);
                if (mNewDevicesArrayAdapter.getCount() == 0) {
                    String noDevices = getResources().getText(R.string.none_found).toString();
                    mNewDevicesArrayAdapter.add(noDevices);
                }
            }
        }
    };

}
