package android.device.admin;

import java.io.File;

/**
 * 设置属性定义
 */
public class SettingProperty {
    public final static String SYSTEM_volume_music = "System-volume_music";
    public final static String SYSTEM_volume_ring = "System-volume_ring";
    public final static String SYSTEM_volume_system = "System-volume_system";
    public final static String SYSTEM_volume_voice = "System-volume_voice";
    public final static String SYSTEM_volume_alarm = "System-volume_alarm";
    public final static String SYSTEM_volume_notification = "System-volume_notification";
    public final static String SYSTEM_volume_bluetooth_sco = "System-volume_bluetooth_sco";
    public final static String SYSTEM_mute_streams_affected = "System-mute_streams_affected";
    public final static String SYSTEM_vibrate_when_ringing = "System-vibrate_when_ringing";
    public final static String SYSTEM_dim_screen = "System-dim_screen";
    public final static String SYSTEM_screen_off_timeout = "System-screen_off_timeout";
    public final static String SYSTEM_dtmf_tone_type = "System-dtmf_tone_type";
    public final static String SYSTEM_hearing_aid = "System-hearing_aid";
    public final static String SYSTEM_tty_mode = "System-tty_mode";
    public final static String SYSTEM_screen_brightness = "System-screen_brightness";
    public final static String SYSTEM_screen_brightness_mode = "System-screen_brightness_mode";
    public final static String SYSTEM_window_animation_scale = "System-window_animation_scale";
    public final static String SYSTEM_transition_animation_scale = "System-transition_animation_scale";
    public final static String SYSTEM_accelerometer_rotation = "System-accelerometer_rotation";
    public final static String SYSTEM_haptic_feedback_enabled = "System-haptic_feedback_enabled";
    public final static String SYSTEM_notification_light_pulse = "System-notification_light_pulse";
    public final static String SYSTEM_dtmf_tone = "System-dtmf_tone";
    public final static String SYSTEM_sound_effects_enabled = "System-sound_effects_enabled";
    public final static String SYSTEM_lockscreen_sounds_enabled = "System-lockscreen_sounds_enabled";
    public final static String SYSTEM_pointer_speed = "System-pointer_speed";
    public final static String SYSTEM_status_bar_show_battery_percent = "System-status_bar_show_battery_percent";
    public final static String SYSTEM_mode_ringer_streams_affected = "System-mode_ringer_streams_affected";
    public final static String SYSTEM_disable_gps = "System-disable_gps";
    public final static String SYSTEM_disable_wifi = "System-disable_wifi";
    public final static String SYSTEM_disable_bluetooth = "System-disable_bluetooth";
    public final static String SYSTEM_disable_communication = "System-disable_communication";
    public final static String SYSTEM_disable_data = "System-disable_data";
    public final static String SYSTEM_disable_set_date = "System-disable_set_date";
    public final static String SYSTEM_display_fn_status = "System-display_fn_status";
    public final static String SYSTEM_default_disable_voice_call = "System-default_disable_voice_call";
    public final static String SYSTEM_preinstall_packagename = "System-preinstall_packagename";
    public final static String SYSTEM_preinstall_classname = "System-preinstall_classname";
    public final static String SYSTEM_disable_airplane = "System-disable_airplane";
    public final static String SYSTEM_alarm_alert = "System-alarm_alert";
    public final static String SYSTEM_streaming_max_udp_port = "System-streaming_max_udp_port";
    public final static String SYSTEM_streaming_min_udp_port = "System-streaming_min_udp_port";
    public final static String SYSTEM_apn = "System-apn";
    public final static String SYSTEM_notification_sound = "System-notification_sound";
    public final static String SYSTEM_volume_music_speaker = "System-volume_music_speaker";
    public final static String SYSTEM_volume_alarm_speaker = "System-volume_alarm_speaker";
    public final static String SYSTEM_volume_ring_speaker = "System-volume_ring_speaker";
    public final static String SYSTEM_time_12_24 = "System-time_12_24";
    public final static String SYSTEM_device_nfc = "System-device_nfc";

    public final static String GLOBAL_auto_pop_softinput = "Global-auto_pop_softinput";
    public final static String GLOBAL_quick_settings = "Global-quick_settings";
    public final static String GLOBAL_airplane_mode_on = "Global-airplane_mode_on";
    public final static String GLOBAL_airplane_mode_radios = "Global-airplane_mode_radios";
    public final static String GLOBAL_airplane_mode_toggleable_radios = "Global-airplane_mode_toggleable_radios";
    public final static String GLOBAL_assisted_gps_enabled = "Global-assisted_gps_enabled";
    public final static String GLOBAL_assisted_gps_configurable_list = "Global-assisted_gps_configurable_list";
    public final static String GLOBAL_assisted_gps_supl_host = "Global-assisted_gps_supl_host";
    public final static String GLOBAL_assisted_gps_supl_port = "Global-assisted_gps_supl_port";
    public final static String GLOBAL_auto_time = "Global-auto_time";
    public final static String GLOBAL_auto_time_zone = "Global-auto_time_zone";
    public final static String GLOBAL_stay_on_while_plugged_in = "Global-stay_on_while_plugged_in";
    public final static String GLOBAL_wifi_sleep_policy = "Global-wifi_sleep_policy";
    public final static String GLOBAL_mode_ringer = "Global-mode_ringer";
    public final static String GLOBAL_package_verifier_enable = "Global-package_verifier_enable";
    public final static String GLOBAL_wifi_networks_available_notification_on = "Global-wifi_networks_available_notification_on";
    public final static String GLOBAL_bluetooth_on = "Global-bluetooth_on";
    public final static String GLOBAL_cdma_cell_broadcast_sms = "Global-cdma_cell_broadcast_sms";
    public final static String GLOBAL_data_roaming = "Global-data_roaming";
    public final static String GLOBAL_mobile_data = "Global-mobile_data";
    public final static String GLOBAL_mobile_data0 = "Global-mobile_data0";
    public final static String GLOBAL_data_roaming0 = "Global-data_roaming0";
    public final static String GLOBAL_mobile_data1 = "Global-mobile_data1";
    public final static String GLOBAL_data_roaming1 = "Global-data_roaming1";
    public final static String GLOBAL_mobile_data2 = "Global-mobile_data2";
    public final static String GLOBAL_data_roaming2 = "Global-data_roaming2";
    public final static String GLOBAL_netstats_enabled = "Global-netstats_enabled";
    public final static String GLOBAL_usb_mass_storage_enabled = "Global-usb_mass_storage_enabled";
    public final static String GLOBAL_wifi_max_dhcp_retry_count = "Global-wifi_max_dhcp_retry_count";
    public final static String GLOBAL_wifi_display_on = "Global-wifi_display_on";
    public final static String GLOBAL_lock_sound = "Global-lock_sound";
    public final static String GLOBAL_unlock_sound = "Global-unlock_sound";
    public final static String GLOBAL_trusted_sound = "Global-trusted_sound";
    public final static String GLOBAL_power_sounds_enabled = "Global-power_sounds_enabled";
    public final static String GLOBAL_low_battery_sound = "Global-low_battery_sound";
    public final static String GLOBAL_dock_sounds_enabled = "Global-dock_sounds_enabled";
    public final static String GLOBAL_desk_dock_sound = "Global-desk_dock_sound";
    public final static String GLOBAL_desk_undock_sound = "Global-desk_undock_sound";
    public final static String GLOBAL_car_dock_sound = "Global-car_dock_sound";
    public final static String GLOBAL_car_undock_sound = "Global-car_undock_sound";
    public final static String GLOBAL_wireless_charging_started_sound = "Global-wireless_charging_started_sound";
    public final static String GLOBAL_dock_audio_media_enabled = "Global-dock_audio_media_enabled";
    public final static String GLOBAL_set_install_location = "Global-set_install_location";
    public final static String GLOBAL_default_install_location = "Global-default_install_location";
    public final static String GLOBAL_emergency_tone = "Global-emergency_tone";
    public final static String GLOBAL_call_auto_retry = "Global-call_auto_retry";
    public final static String GLOBAL_hide_carrier_network_settings = "Global-hide_carrier_network_settings";
    public final static String GLOBAL_preferred_network_mode = "Global-preferred_network_mode";
    public final static String GLOBAL_subscription_mode = "Global-subscription_mode";
    public final static String GLOBAL_low_battery_sound_timeout = "Global-low_battery_sound_timeout";
    public final static String GLOBAL_wifi_scan_always_enabled = "Global-wifi_scan_always_enabled";
    public final static String GLOBAL_heads_up_notifications_enabled = "Global-heads_up_notifications_enabled";
    public final static String GLOBAL_device_name = "Global-device_name";
    public final static String GLOBAL_guest_user_enabled = "Global-guest_user_enabled";
    public final static String GLOBAL_captive_portal_detection_enabled = "Global-captive_portal_detection_enabled";
    public final static String GLOBAL_wifi_watchdog_on = "Global-wifi_watchdog_on";
    public final static String GLOBAL_adb_enabled = "Global-adb_enabled";

    public final static String GLOBAL_network_scoring_provisioned = "Global-network_scoring_provisioned";
    public final static String GLOBAL_device_provisioned = "Global-device_provisioned";
    public final static String GLOBAL_multi_sim_default_sub = "Global-multi_sim_default_sub";
    public final static String GLOBAL_multi_sim_voice_call = "Global-multi_sim_voice_call";
    public final static String GLOBAL_multi_sim_sms = "Global-multi_sim_sms";
    public final static String GLOBAL_multi_sim_data_call = "Global-multi_sim_data_call";
    public final static String GLOBAL_preferred_network_mode_default = "Global-preferred_network_mode_default";
    public final static String GLOBAL_wifi_on = "Global-wifi_on";

    public final static String SECURE_location_providers_allowed = "Secure-location_providers_allowed";
    public final static String SECURE_mock_location = "Secure-mock_location";
    public final static String SECURE_backup_enabled = "Secure-backup_enabled";
    public final static String SECURE_backup_transport = "Secure-backup_transport";

    public final static String SECURE_mount_play_not_snd = "Secure-mount_play_not_snd";
    public final static String SECURE_mount_ums_autostart = "Secure-mount_ums_autostart";
    public final static String SECURE_mount_ums_prompt = "Secure-mount_ums_prompt";
    public final static String SECURE_mount_ums_notify_enabled = "Secure-mount_ums_notify_enabled";
    public final static String SECURE_accessibility_script_injection = "Secure-accessibility_script_injection";
    public final static String SECURE_accessibility_web_content_key_bindings = "Secure-accessibility_web_content_key_bindings";

    public final static String SECURE_long_press_timeout = "Secure-long_press_timeout";
    public final static String SECURE_touch_exploration_enabled = "Secure-touch_exploration_enabled";
    public final static String SECURE_speak_password = "Secure-speak_password";
    public final static String SECURE_accessibility_script_injection_url = "Secure-accessibility_script_injection_url";
    public final static String SECURE_lockscreen_disabled = "Secure-lockscreen.disabled";
    public final static String SECURE_screensaver_enabled = "Secure-screensaver_enabled";
    public final static String SECURE_screensaver_activate_on_dock = "Secure-screensaver_activate_on_dock";
    public final static String SECURE_screensaver_activate_on_sleep = "Secure-screensaver_activate_on_sleep";
    public final static String SECURE_screensaver_components = "Secure-screensaver_components";
    public final static String SECURE_screensaver_default_component = "Secure-screensaver_default_component";
    public final static String SECURE_accessibility_display_magnification_enabled = "Secure-accessibility_display_magnification_enabled";
    public final static String SECURE_accessibility_display_magnification_scale = "Secure-accessibility_display_magnification_scale";
    public final static String SECURE_accessibility_display_magnification_auto_update = "Secure-accessibility_display_magnification_auto_update";
    public final static String SECURE_immersive_mode_confirmations = "Secure-immersive_mode_confirmations";


    public final static String SECURE_install_non_market_apps = "Secure-install_non_market_apps";
    public final static String SECURE_wake_gesture_enabled = "Secure-wake_gesture_enabled";
    public final static String SECURE_lock_screen_show_notifications = "Secure-lock_screen_show_notifications";
    public final static String SECURE_lock_screen_allow_private_notifications = "Secure-lock_screen_allow_private_notifications";
    public final static String SECURE_sleep_timeout = "Secure-sleep_timeout";
    public final static String SECURE_default_input_method = "Secure-default_input_method";
    public final static String SECURE_enabled_input_methods = "Secure-enabled_input_methods";
    public final static String SECURE_accessibility_enabled = "Secure-accessibility_enabled";
    public final static String SECURE_enabled_accessibility_services = "Secure-enabled_accessibility_services";
    public final static String SECURE_show_ime_with_hard_keyboard = "Secure-show_ime_with_hard_keyboard";
    public final static String SECURE_android_id = "Secure-android_id";
    public final static String SECURE_input_methods_subtype_history = "Secure-input_methods_subtype_history";
    public final static String SECURE_selected_input_method_subtype = "Secure-selected_input_method_subtype";


    public final static String SECURE_selected_spell_checker = "Secure-selected_spell_checker";
    public final static String SECURE_selected_spell_checker_subtype = "Secure-selected_spell_checker_subtype";
    public final static String SECURE_lock_screen_owner_info_enabled = "Secure-lock_screen_owner_info_enabled";
    public final static String SECURE_enhLocationServices_on = "Secure-enhLocationServices_on";
    public final static String SECURE_user_setup_complete = "Secure-user_setup_complete";
    public final static String SECURE_show_note_about_notification_hiding = "Secure-show_note_about_notification_hiding";
    public final static String SECURE_sms_default_application = "Secure-sms_default_application";
    public final static String SECURE_trust_agents_initialized = "Secure-trust_agents_initialized";
    public final static String SECURE_bluetooth_name = "Secure-bluetooth_name";
    public final static String SECURE_bluetooth_address = "Secure-bluetooth_address";
    public final static String SECURE_bluetooth_addr_valid = "Secure-bluetooth_addr_valid";
    public final static String SECURE_accessibility_display_inversion_enabled = "Secure-accessibility_display_inversion_enabled";


    //Com.android.providers.settings  System
    public final static String UI_disable_about = "System-disable_about";
    //        |是否屏蔽ui“about phone”  0为屏蔽
    public final static String UI_disable_print = "System-disable_print";
    //|是否屏蔽ui“Printing”  0为屏蔽
    public final static String UI_disable_accessibility = "System-disable_accessibility";
    // |是否屏蔽ui“Accessibility”  0为屏蔽
    public final static String UI_disable_date_time = "System-disable_date_time";
    // |是否屏蔽ui“date &time”  0为屏蔽
    public final static String UI_disable_language = "System-disable_language";
    // |是否屏蔽ui“Language & input”  0为屏蔽
    public final static String UI_disable_account = "System-disable_account";
    //|是否屏蔽ui“Accounts”  0为屏蔽
    public final static String UI_disable_security = "System-disable_security";
    // |是否屏蔽ui“Security”  0为屏蔽
    public final static String UI_disable_storage = "System-disable_storage";
    //|是否屏蔽ui“Storage”  0为屏蔽
    public final static String UI_disable_battery = "System-disable_battery";
    // |是否屏蔽ui“Battery”  0为屏蔽
    public final static String UI_disable_user_settings = "System-disable_user_settings";
    //|是否屏蔽ui“Users”  0为屏蔽
    public final static String UI_disable_application = "System-disable_application";
    //|是否屏蔽ui“Apps”  0为屏蔽
    public final static String UI_disable_status_bar = "System-disable_status_bar";
    //|是否屏蔽ui“Status bar”  0为屏蔽
    public final static String UI_disable_display_settings = "System-disable_display_settings";
    // |是否屏蔽ui“Display”  0为屏蔽
    public final static String UI_disable_button = "System-disable_button";
    // |是否屏蔽ui“Buttons”  0为屏蔽
    public final static String UI_disable_scanner = "System-disable_scanner";
    // |是否屏蔽ui“Scanner”  0为屏蔽

    public final static String UI_disable_profile = "System-disable_profile";
    //|是否屏蔽ui“Profiles”  0为屏蔽
    public final static String UI_disable_notifications = "System-disable_notifications";
    //|是否屏蔽ui“Sound & Notifications”  0为屏蔽|是否屏蔽状态栏声音显示条
    public final static String UI_disable_privacy = "System-disable_privacy";
    //|是否屏蔽ui“Backup & reset”  0为屏蔽
    public final static String UI_disable_wireless = "System-disable_wireless";
    //|是否屏蔽ui“more ...”  0为屏蔽

    //Settings二级目录：
    public final static String UI_disable_data = "System-disable_data";
    //1，是否屏蔽(Settings >more)ui“Cellular networks”,“Mobile  plan” “Emergency broadcasts” 0为屏蔽
//2，是否屏蔽Settings一级目录“Data usage”
    //3，是否屏蔽状态栏“Emergency calls only”
    public final static String UI_disable_wifi = "System-disable_wifi";
    //1，是否屏蔽Settings一级目录“WLAN”
    //2，是否屏蔽状态栏WLAN
//3，是否屏蔽（Settings>more）“Tethering & prortable Hostpost”
    public final static String UI_disable_gps = "System-disable_gps";
    //1，屏蔽Settings一级目录 “Location”
    //2，屏蔽状态栏“Location”
    public final static String UI_disable_bluetooth = "System-disable_bluetooth";
    //1，屏蔽Settings一级目录“Bluetooth”
    //2，屏蔽状态栏“Bluetooth”
    public final static String UI_disable_airplane = "System-disable_airplane";
    //1，屏蔽Setting二级目录（Settings>more）“Airplane more”
    //2，屏蔽状态栏“Airplane more”
    public final static String UI_disable_hotspot = "System-disable_hotspot";
    // 3，是否屏蔽（Settings>more）“Tethering & prortable Hostpost”
    //2，屏蔽状态栏“Hostpost”
    public final static String UI_disable_flashlight = "System-disable_flashlight";
    //1,是否屏蔽状态栏flashlight
    public final static String UI_disable_cast = "System-disable_cast";
    //1,是否屏蔽状态栏Cast screen
    //1，是否屏蔽Settings目录（more>vpn）
    public final static String UI_disable_vpn = "System-disable_vpn";
    public final static String UI_disable_sms_application = "System-disable_sms_application";
    //1，是否屏蔽Settings目录（more>Default  SMS  app）
    //以下为Settings>Buttons下是的ui控制
    public final static String UI_key_system_power = "System-key_system_power";
    //是否屏蔽“End Call”
    public final static String UI_key_system_home_long_press = "System-key_system_home_long_press";
    //是否屏蔽“Long press  action”
    public final static String UI_key_system_home_double_tap = "System-key_system_home_double_tap";
    //是否屏蔽“Double tap action”
    public final static String UI_key_system_menu_press = "System-key_system_menu_press";
    //是否屏蔽“Short  press  action”
    public final static String UI_key_system_menu_long_press = "System-key_system_menu_long_press";
    //是否屏蔽“Long  press  action”
    public final static String UI_key_system_home_answer_call = "System-key_system_home_answer_call";
    //是否屏蔽“Answer  Call”
    public final static String UI_key_system_kaymap_settings = "System-key_system_kaymap_settings";
    //是否屏蔽“Remap  Key”
    //以下为Settings>Display下是的ui控制
    //public final static String UI_disable_cast = "System-disable_cast";
    //是否屏蔽“cast dcreen”  0为屏蔽  1为不屏蔽
    public final static String UI_display_screen_saver = "System-display_screen_saver";
    //是否屏蔽“Daydream”  0为屏蔽  1为不屏蔽
    public final static String UI_display_blur_effect = "System-display_blur_effect";
    //是否屏蔽“Blur  Effect”  0为屏蔽  1为不屏蔽
    public final static String UI_display_font_size = "System-display_font_size";
    //是否屏蔽“Font size”  0为屏蔽  1为不屏蔽
    public final static String UI_display_sleep = "System-display_sleep";
    //是否屏蔽“sleep”  0为屏蔽  1为不屏蔽
    public final static String UI_display_wallpaper = "System-display_wallpaper";
    //是否屏蔽“Wallpaper”  0为屏蔽  1为不屏蔽
    public final static String UI_display_brightness = "System-display_brightness";
    //是否屏蔽“Brightness  level ”  0为屏蔽  1为不屏蔽
    //以下为Settings>Souns& notification下是的ui控制
    public final static String UI_sound_media_volume = "System-sound_media_volume";
    //是否屏蔽“Media volume ”  0为屏蔽  1为不屏蔽
    public final static String UI_sound_alarm_volume = "System-sound_alarm_volume";
    //是否屏蔽“Alarm volume ”  0为屏蔽  1为不屏蔽
    public final static String UI_sound_ring_volume = "System-sound_ring_volume";
    //是否屏蔽“Ring volume ”  0为屏蔽  1为不屏蔽
    public final static String UI_sound_vibrate_when_ringing = "System-sound_vibrate_when_ringing";
    //是否屏蔽“Also vibrate for calls”  0为屏蔽  1为不屏蔽
    public final static String UI_sound_notification_ringtone = "System-sound_notification_ringtone";
    //是否屏蔽“Default  notification ringtone ”  0为屏蔽  1为不屏蔽
    public final static String UI_sound_phone_ringtone = "System-sound_phone_ringtone";
    //是否屏蔽“Phone ringtone”  0为屏蔽  1为不屏蔽
    public final static String UI_sound_other = "System-sound_other";
    //是否屏蔽“Other sounds ”  0为屏蔽  1为不屏蔽
    public final static String UI_sound_zen_mode = "System-sound_zen_mode";
    //是否屏蔽“Interruptions”  0为屏蔽  1为不屏蔽
    public final static String UI_notification_access = "System-notification_access";
    //是否屏蔽“Notification access ”  0为屏蔽  1为不屏蔽
    public final static String UI_notification_bluetooth = "System-notification_bluetooth";
    //是否屏蔽“Bluetooth”  0为屏蔽  1为不屏蔽
    public final static String UI_notification_app = "System-notification_app";
    //是否屏蔽“App notification_app”  0为屏蔽  1为不屏蔽
    public final static String UI_notification_screen = "System-notification_screen";
    //是否屏蔽“When device  is  locked”  0为屏蔽  1为不屏蔽
    //以下为Settings>Security下是的ui控制
    public final static String UI_security_unlock_set_or_change = "System-security_unlock_set_or_change";
    //是否屏蔽“Screen lock ”  0为屏蔽  1为不屏蔽
    public final static String UI_security_owner_info = "System-security_owner_info";
    //是否屏蔽“Owner info ”  0为屏蔽  1为不屏蔽
    public final static String UI_security_lockscreen_shortcuts = "System-security_lockscreen_shortcuts";
    //是否屏蔽“Locksreen shortcuts ”  0为屏蔽  1为不屏蔽
    public final static String UI_security_app = "System-security_app";
    //是否屏蔽“SMS  message limit ”  0为屏蔽  1为不屏蔽
    public final static String UI_security_show_password = "System-security_show_password";
    //是否屏蔽“Make passworks visible ”  0为屏蔽  1为不屏蔽
    public final static String UI_security_manage_device_admin = "System-security_show_password";
    //是否屏蔽“Device administrators ”  0为屏蔽  1为不屏蔽
    public final static String UI_security_toggle_install_applications = "System-security_toggle_install_applications";
    //是否屏蔽“Unknown sources”  0为屏蔽  1为不屏蔽
    public final static String UI_security_credential_storage_type = "System-security_credential_storage_type";
    //是否屏蔽“Storage type ”  0为屏蔽  1为不屏蔽
    public final static String UI_security_trusted_credentials = "System-security_trusted_credentials";
    //是否屏蔽“Trusted credentials ”  0为屏蔽  1为不屏蔽
    public final static String UI_security_credentials_install = "System-security_credentials_install";
    //是否屏蔽“Install from SD  card ”  0为屏蔽  1为不屏蔽

    public final static String UI_security_credentials_reset = "System-security_credentials_reset";
    //是否屏蔽“Clear cradentials ”  0为屏蔽  1为不屏蔽
    public final static String UI_security_manage_trust_agents = "System-security_manage_trust_agents";
    //是否屏蔽“Trust agents ”  0为屏蔽  1为不屏蔽
    public final static String UI_security_usage_access = "System-security_usage_access";
    //是否屏蔽“Apps with  usage access ”  0为屏蔽  1为不屏蔽
    //以下为Settings>Language & input下是的ui控制
    public final static String UI_input_language = "System-input_language";
    //是否屏蔽“Language ”  0为屏蔽  1为不屏蔽
    public final static String UI_input_spell_checkers = "System-input_spell_checkers";
    //是否屏蔽“Spell checkers”  0为屏蔽  1为不屏蔽
    public final static String UI_input_user_dictionary = "System-input_user_dictionary";
    //是否屏蔽“Personal  dictionary ”  0为屏蔽  1为不屏蔽
    public final static String UI_input_keyboard = "System-input_keyboard";
    //是否屏蔽“keyboars & input methods”  0为屏蔽  1为不屏蔽
    public final static String UI_input_voice_category = "System-input_voice_category";
    //是否屏蔽“speech”  0为屏蔽  1为不屏蔽
    public final static String UI_input_pointer = "System-input_pointer";
    //是否屏蔽“mouse/trackpad”  0为屏蔽  1为不屏蔽
    //以下为Settings>Language & input下是的ui控制
    public final static String UI_time_auto_time = "System-time_auto_time";
    //是否屏蔽“Automatic date & time”  0为屏蔽  1为不屏蔽
    public final static String UI_time_auto_zone = "System-time_auto_zone";
    //是否屏蔽“Automatic time zone”  0为屏蔽  1为不屏蔽
    public final static String UI_time_date = "System-time_date";
    //是否屏蔽“Set date”  0为屏蔽  1为不屏蔽
    public final static String UI_time_time = "System-time_time";
    //是否屏蔽“Set time”  0为屏蔽  1为不屏蔽
    public final static String UI_time_timezone = "System-time_timezone";
    //是否屏蔽“Select time zone”  0为屏蔽  1为不屏蔽
    public final static String UI_time_24_hour = "System-time_24_hour";
    //是否屏蔽“Use 24-hour format”  0为屏蔽  1为不屏蔽
    //以下为Settings>Accessibiliry下是的ui控制
    public final static String UI_accessibility_services = "System-accessibility_services";
    //是否屏蔽“Services”  0为屏蔽  1为不屏蔽
    public final static String UI_accessibility_captioning = "System-accessibility_captioning";
    //是否屏蔽“Captions ”  0为屏蔽  1为不屏蔽
    public final static String UI_accessibility_magnification = "System-accessibility_magnification";
    //是否屏蔽“Magnification gestures”  0为屏蔽  1为不屏蔽
    public final static String UI_accessibility_auto_pop_input = "System-accessibility_auto_pop_input";
    //是否屏蔽“Suto pop-up softinput”  0为屏蔽  1为不屏蔽
    public final static String UI_accessibility_toggle_large = "System-accessibility_toggle_large";
    //是否屏蔽“large text”  0为屏蔽  1为不屏蔽

    public final static String UI_accessibility_toggle_high = "System-accessibility_toggle_high";
    //是否屏蔽“High contrast text”  0为屏蔽  1为不屏蔽
    public final static String UI_accessibility_toggle_power_button = "System-accessibility_toggle_power_button";
    //是否屏蔽“Power  button  ends call”  0为屏蔽  1为不屏蔽
    public final static String UI_accessibility_toggle_speak_password = "System-accessibility_toggle_speak_password";
    //是否屏蔽“Speak  passworks”  0为屏蔽  1为不屏蔽
    public final static String UI_accessibility_enable_global_gesture = "System-accessibility_enable_global_gesture";
    //是否屏蔽“Accessibility shortcut”  0为屏蔽  1为不屏蔽
    public final static String UI_accessibility_tts_settings = "System-accessibility_tts_settings";
    //是否屏蔽“Text-to-speech output”  0为屏蔽  1为不屏蔽
    public final static String UI_accessibility_select_long_press = "System-accessibility_select_long_press";
    //是否屏蔽“Touch & hold  delay”  0为屏蔽  1为不屏蔽
    public final static String UI_accessibility_toggle_inversion = "System-accessibility_toggle_inversion";
    //是否屏蔽“Color inversion”  0为屏蔽  1为不屏蔽
    public final static String UI_accessibility_daltonizer = "System-accessibility_daltonizer";
    //是否屏蔽“Corlor  correction”  0为屏蔽  1为不屏蔽
    //以下为Settings>Accessibiliry下是的ui控制
    public final static String UI_device_system_update = "System-device_system_update";
    //是否屏蔽“System  update”  0为屏蔽  1为不屏蔽
    public final static String UI_device_status_info = "System-device_status_info";
    //是否屏蔽“Status”0为屏蔽  1为不屏蔽

    public final static String UI_device_container = "System-device_container";
    //是否屏蔽“Legal  information”  0为屏蔽  1为不屏蔽
    public final static String UI_device_device_model = "System-device_device_model";
    //是否屏蔽“Model number”  0为屏蔽  1为不屏蔽
    public final static String UI_firmware_version = "System-firmware_version";
    //是否屏蔽“Android  version”  0为屏蔽  1为不屏蔽
    public final static String UI_security_patch = "System-security_patch";
    //是否屏蔽“Android security  patch level”  0为屏蔽  1为不屏蔽
    public final static String UI_baseband_version = "System-baseband_version";
    //是否屏蔽“Baseband version”  0为屏蔽  1为不屏蔽
    public final static String UI_kernel_version = "System-kernel_version";
    //是否屏蔽“Kernel version”  0为屏蔽  1为不屏蔽
    public final static String UI_device_sn = "System-device_sn";
    //是否屏蔽“SN”  0为屏蔽  1为不屏蔽
    public final static String UI_device_32550 = "System-device_32550";
    //是否屏蔽“SE”  0为屏蔽  1为不屏蔽
    public final static String UI_build_number = "System-build_number";
    //是否屏蔽“Build number”  0为屏蔽  1为不屏蔽
    //        以下为长按POWER键跳出ui控制
    public final static String UI_key_poweraction = "System-key_poweraction";
    //是否屏蔽“Power  off”  0为屏蔽  1为不屏蔽

    public final static String UI_key_rebootaction = "System-key_rebootaction";
    //是否屏蔽“Reboot”  0为屏蔽  1为不屏蔽
    //public final static String UI_disable_airplane = "System-disable_airplane";
    //是否屏蔽“Airplane  mode”  0为屏蔽  1为不屏蔽
    public final static String UI_key_silentmodeaction = "System-key_silentmodeaction";
    //是否屏蔽底部的情景模式设置图标  0为屏蔽  1为不屏蔽

    //霸屏模式密码
    public final static String System_LockTaskModePassword = "System-LockTaskModePassword";

    //霸屏模式应用
    public final static String System_LockTaskModePackage = "System-lockTaskPackage";

    //霸屏模式应用白名单
    public final static String System_LockTaskModeWhitePackages = "System-lockTaskWhitePackages";

    //设置霸屏主界面
    public final static String System_lockTaskRootComponent = "System-lockTaskRootComponent";

    //固定屏幕
    public final static String System_UROVO_LOCK_SCREEN = "System-UROVO_LOCK_SCREEN";

    //PTT按键up事件广播
    public final static String System_UROVO_PTT_Up_ACTION = "System-UROVO_PTT_Up_ACTION";

    //PTT按键down事件广播
    public final static String System_UROVO_PTT_Down_ACTION = "System-UROVO_PTT_Down_ACTION";

    //apk安装器
    public final static String System_InstallerPckNames = "System-InstallerPckNames";

    //开机自动运行
    public final static String System_AutoRunningApp = "System-AutoRunningApp";

    //隐藏应用图标
    public final static String System_HideApplicationIcon = "System-HideApplicationIcon";

    //OTA升级服务器地址
    public final static String System_FotaServerUrl = "System-FotaServerUrl";

    //OTA升级服务器地址
    public final static String System_FotaBatteryWarningLevel = "System-FotaBatteryWarningLevel";

    //浏览器默认主页
    public final static String System_BrowserHomePage = "System-BrowserHomePage";

    //禁止按键
    public final static String System_DisallowedKeycodes = "System-UROVO_DISALLOWED_KEYCODES";

    //禁用状态栏
    public final static String System_StatusBarEnable = "System-UROVO_STATUSBAR_ENABLE";

    //自动录音
    public final static String System_EnableAutoRecord = "System-ENABLE_AUTORECORD";

    //自动录音文件路径
    public final static String System_AutoRecordPath = "System-AUTORECORD_PATH";

    //锁屏快捷方式
    public final static String System_KeyguardKey = "System-UROVO_KEYGUARDKEY_ENABLE";

    //是否传递扫描键按键事件给应用
    public final static String System_ScanKeyPass = "System-UROVO_SCAN_PASS";

    //扫描键悬浮按钮
    public final static String System_ScanSuspensionButton = "Udroid-SUSPENSION_BUTTON";

    //锁屏界面左边快捷方式
    public final static String System_KeyguardLeftKey = "System-UROVO_LEFTKEYGUARD_ENABLE";

    //锁屏界面右边快捷方式
    public final static String System_KeyguardRightKey = "System-UROVO_RIGHTKEYGUARD_ENABLE";

    //隐藏通话界面电话号码
    public final static String System_HideCallNumber = "System-HIDE_CALL_NUMBER";


    public final static String[] ALLOW_PROPS = new String[]{
            "persist.sys.allow.none",
            "persist.sys.allow.bluetooth",
            "persist.sys.allow.wifi",
            "persist.sys.allow.scanner",
            "persist.sys.allow.gps",
            "persist.sys.allow.wwan",
            "persist.sys.allow.editapn",
            "persist.sys.allow.sdcard",
            "persist.sys.allow.mtp",
            "persist.sys.allow.ptp",
            "persist.sys.allow.massstorage",
            "persist.sys.allow.editvpn",
            "persist.sys.allow.hotspot",
            "persist.sys.allow.touch",
            "persist.sys.allow.camera",
            "persist.sys.allow.keyboard",
            "persist.sys.allow.ime",
            "persist.sys.allow.torch",
            "persist.sys.allow.adb",
            "persist.sys.allow.recovery"
    };

    /**
     * NONE
     */
    public final static int ALLOW_NONE = 0;

    /**
     * 蓝牙
     */
    public final static int ALLOW_BLUETOOTH = 1;

    /**
     * wifi
     */
    public final static int ALLOW_WIFI = 2;

    /**
     * 扫描
     */
    public final static int ALLOW_SCANNER = 3;

    /**
     * GPS
     */
    public final static int ALLOW_GPS = 4;

    /**
     * 移动网络
     */
    public final static int ALLOW_WWAN = 5;

    /**
     * APN
     */
    public final static int ALLOW_EDITAPN = 6;

    /**
     * SDCARD
     */
    public final static int ALLOW_SDCARD = 7;

    /**
     * MTP
     */
    public final static int ALLOW_MTP = 8;

    /**
     * PTP
     */
    public final static int ALLOW_PTP = 9;

    /**
     * 存储模式
     */
    public final static int ALLOW_MASSSTORAGE = 10;//MassStorage

    /**
     * VPN
     */
    public final static int ALLOW_EDITVPN = 11;

    /**
     * wifi热点
     */
    public final static int ALLOW_HOTSPOT = 12;

    /**
     * 触摸
     */
    public final static int ALLOW_TOUCH = 13;

    /**
     * 相机
     */
    public final static int ALLOW_CAMERA = 14;

    /**
     * 键盘
     */
    public final static int ALLOW_KEYBOARD = 15;

    /**
     * 输入法
     */
    public final static int ALLOW_IME = 16;

    /**
     * 闪关灯
     */
    public final static int ALLOW_TORCH = 17;

    /**
     * ADB
     */
    public final static int ALLOW_ADB = 18;

    /**
     * recovery
     */
    public final static int ALLOW_RECOVERY = 19;


    /**
     * ubx config file dir
     */
    public final static String UBX_CONFIG_DIR = "/data/system";

    /**
     * customize config file dir
     */
    public final static String CUSTOMIZE_CONFIG_DIR = "/customize/etc";

    /**
     * wifi whitelist file name
     */
    public final static String WIFI_WHITELIST_FILE = UBX_CONFIG_DIR + File.separator + "wifiwhitelist.txt";

    /**
     * wifi blacklist file name
     */
    public final static String WIFI_BLACKLIST_FILE = UBX_CONFIG_DIR + File.separator + "wifiblacklist.txt";

    /**
     * bt whitelist file name
     */
    public final static String BT_WHITELIST_FILE = UBX_CONFIG_DIR + File.separator + "btwhitelist.txt";

    /**
     * bt blacklist file name
     */
    public final static String BT_BLACKLIST_FILE = UBX_CONFIG_DIR + File.separator + "btblacklist.txt";

    /**
     * customize app whitelist file name
     */
    public final static String CUSTOMIZE_WHITELIST_FILE = UBX_CONFIG_DIR + File.separator + "system_app_whitelist.txt";


    /**
     * data app remove list file name
     */
    public final static String DATA_PACKAGE_REMOVE_FILE = UBX_CONFIG_DIR + File.separator + "app_remove.txt";

    /**
     * data app white list file name
     */
    public final static String DATA_PACKAGE_WHITELIST_FILE = UBX_CONFIG_DIR + File.separator + "app_whitelist.txt";

    /**
     * customize app remove list file name
     */
    public final static String  CUSTOMIZE_PACKAGE_REMOVE_FILE = CUSTOMIZE_CONFIG_DIR + File.separator + "app_remove.txt";

    /**
     * customize app white list file name
     */
    public final static String  CUSTOMIZE_PACKAGE_WHITELIST_FILE = CUSTOMIZE_CONFIG_DIR + File.separator + "app_whitelist.txt";

    /**
     * system preinstalled app list file name
     */
    public final static String SYSTEM_PREINSTALLED_APP_FILE = UBX_CONFIG_DIR + File.separator + "system_applist.txt";

    /**
     * keep app alive white list file name
     */
    public final static String ALIVE_WHITELIST_FILE_PATH = UBX_CONFIG_DIR + File.separator + "alivewhitelist.txt";

    /**
     * keep app alive black list file name
     */
    public final static String ALIVE_BLACKLIST_FILE_PATH = UBX_CONFIG_DIR + File.separator + "aliveblacklist.txt";
}
