package locationtrail.msk.com.locationtrail;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;

public class DeviceListAdapter extends ArrayAdapter<BluetoothDevice>{

    private Context context;
    private int resource;
    String[] phone_array;

    int phone  = 0;

    public DeviceListAdapter(Context context, int resource, ArrayList<BluetoothDevice> objects) {
        super(context, resource, objects);
        this.context = context;
        this.resource = resource;
        phone_array = context.getResources().getStringArray(R.array.phone_num);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        String deviceNme = getItem(position).getName();
        String deviceHardwareAddress = getItem(position).getAddress(); // MAC address

        String phone = phone_array[position%phone_array.length];

        LayoutInflater inflater = LayoutInflater.from(context);
        convertView = inflater.inflate(resource, parent, false);

        TextView deviceNameTv = convertView.findViewById(R.id.deviceName);
        TextView deviceAddressTv = convertView.findViewById(R.id.devicAdd);

        deviceNameTv.setText(phone);
        deviceAddressTv.setText(deviceHardwareAddress);

        return convertView;
    }
}
