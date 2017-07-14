/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.example.android.bluetoothlegatt;

import java.util.HashMap;

/**
 * This class includes a small subset of standard GATT attributes for demonstration purposes.
 */
public class BotGattAttributes {

    /**
     *  brief      UUID cho các services và characteristic
     */
    //Bot service gồm 5 characteristics
    public static final String bot_service_uuid         = "0000FFF0-0000-1000-8000-00805F9B34FB";
    public static final String bot_motion_char_uuid     = "0000FFF1-0000-1000-8000-00805F9B34FB";  //WRITE-uchar
    public static final String bot_relay1_char_uuid     = "0000FFF2-0000-1000-8000-00805F9B34FB";  //WRITE-uchar
    public static final String bot_relay2_char_uuid     = "0000FFF3-0000-1000-8000-00805F9B34FB";  //WRITE-uchar
    public static final String bot_relay3_char_uuid     = "0000FFF4-0000-1000-8000-00805F9B34FB";  //WRITE-uchar
    public static final String bot_relay4_char_uuid     = "0000FFF5-0000-1000-8000-00805F9B34FB";  //WRITE-uchar

    public static final int STOP = 0;
    public static final int FORWARD = 1;
    public static final int BACK = 2;
    public static final int LEFT = 3;
    public static final int RIGHT = 4;

    public static final int RELAY_ON = 0;
    public static final int RELAY_OFF = 1;

    private static HashMap<String, String> attributes = new HashMap();
    public static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";

    static {
        attributes.put(bot_service_uuid, "VNG_BOT_SERVICE");
        attributes.put(bot_motion_char_uuid, "Bot motion");
        attributes.put(bot_relay1_char_uuid, "Control relay 1");
        attributes.put(bot_relay2_char_uuid, "Control relay 2");
        attributes.put(bot_relay3_char_uuid, "Control relay 3");
        attributes.put(bot_relay4_char_uuid, "Control relay 4");
    }

    public static String lookup(String uuid, String defaultName) {
        String name = attributes.get(uuid);
        return name == null ? defaultName : name;
    }
}
