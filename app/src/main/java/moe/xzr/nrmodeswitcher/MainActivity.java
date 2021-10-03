/*
 * Copyright (C) 2021 LibXZR <i@xzr.moe>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package moe.xzr.nrmodeswitcher;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.telephony.SubscriptionManager;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.qualcomm.qcrilmsgtunnel.IQcrilMsgTunnel;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    private Protocol mProtocol;
    private Runnable unbindService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(genLayout());
        Intent intent = new Intent();
        intent.setClassName("com.qualcomm.qcrilmsgtunnel", "com.qualcomm.qcrilmsgtunnel.QcrilMsgTunnelService");
        bindService(intent, new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                IQcrilMsgTunnel tunnel = IQcrilMsgTunnel.Stub.asInterface(service);
                if (tunnel != null)
                    mProtocol = new Protocol(tunnel);

                ServiceConnection serviceConnection = this;

                unbindService = () -> MainActivity.this.unbindService(serviceConnection);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                mProtocol = null;
            }
        }, BIND_AUTO_CREATE);
    }

    @Override
    public void finish() {
        if (unbindService != null)
            unbindService.run();
        super.finish();
    }

    private class Options {
        ArrayList<String> names;
        ArrayList<View.OnClickListener> onClickListeners;

        public Options() {
            names = new ArrayList<>();
            onClickListeners = new ArrayList<>();
        }

        public void addItem(String name, View.OnClickListener onClickListener) {
            names.add(name);
            onClickListeners.add(onClickListener);
        }

        public void addItem(int id, View.OnClickListener onClickListener) {
            addItem(getResources().getString(id), onClickListener);
        }

        public List<String> getOptionNames() {
            return names;
        }

        public void bindListView(ListView listView) {
            listView.setOnItemClickListener((parent, view, position, id) -> {
                onClickListeners.get(position).onClick(view);
                MainActivity.this.finish();
            });
        }
    }

    private void setNrModeChecked(Protocol.NR_5G_DISABLE_MODE_TYPE mode) {
        if (mProtocol == null) {
            Toast.makeText(this, "Service not ready", Toast.LENGTH_LONG).show();
            return;
        }
        int index = SubscriptionManager.getSlotIndex(SubscriptionManager.getDefaultDataSubscriptionId());
        if (index == SubscriptionManager.INVALID_SIM_SLOT_INDEX) {
            Toast.makeText(this, "Unable to get current mobile data slot", Toast.LENGTH_LONG).show();
            return;
        }
        new Thread(() -> mProtocol.setNrMode(index, mode)).start();
    }

    private View genLayout() {
        Options options = new Options();
        options.addItem(R.string.sa_only, v -> setNrModeChecked(Protocol.NR_5G_DISABLE_MODE_TYPE.NAS_NR5G_DISABLE_MODE_NSA));
        options.addItem(R.string.nsa_only, v -> setNrModeChecked(Protocol.NR_5G_DISABLE_MODE_TYPE.NAS_NR5G_DISABLE_MODE_SA));
        options.addItem(R.string.nsa_sa, v -> setNrModeChecked(Protocol.NR_5G_DISABLE_MODE_TYPE.NAS_NR5G_DISABLE_MODE_NONE));

        ListView listView = new ListView(this);
        listView.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, options.getOptionNames()));
        options.bindListView(listView);

        return listView;
    }
}