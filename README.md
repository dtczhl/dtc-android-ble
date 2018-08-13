
## About this repo

Bluetootch Low Energy (BLE) implementations with Android studio.




## Braches:

Refer to braches for codes and details

1. Receiving functions

    * `ble_scanner`

      Simple BLE scanner with device name, address, timestamp, and RSSI

    * `ble_scan_logger`

      Based on `ble_scanner`, data logging function is added

    * `ble_scan_log_plotter`,

      Based on `ble_scan_logger`, rssi plotting function is added

2. Sending functions

    * `ble_broadcastor`
      
      Simple BLE broadcasting 

For projects 

* `ble_scan_log_plot_autofitter`,

  Linear fitting function is added
  
* `ble_scan_log_plot_labler`,

   Adapted from `ble_scan_log_plot_autofitter`, hold button for timestamp logging
