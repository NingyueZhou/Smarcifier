# Smarcifier

## Projects

### Android Projects

The top-level directory contains (is) the primary project for the Thermo Bo-Bo.

`arduino/BluetoothTest` is a project that was used to figure out how the Android SDK's Bluetooth LE
stack works.

### Microcontroller code

`arduino/bluetooth/` contains the code that runs on the Thermo Bo-Bo. It is a simple Bluetooth LE
server that defines a single characteristic for the temperature value. Clients are notified when the
value changes.

The arduino code also contains the UUIDs for the Bluetooth LE service and the characteristic.

## Tools

`arduino/read_serial.py` is a small python script that reads lines from a serial port. Usage:

    python read_serial.py /dev/ttyACM0

## Bootloader reset

 1. Download [the bootloader
software](https://raw.githubusercontent.com/adafruit/Adafruit-Feather-ESP32-S2-PCB/main/Factory-Reset/feather-esp32-s2-factory-reset-and-bootloader.bin)

 2. Install esptool: `$ pip install esptool`

 3. (optional) Test the esptool installation: `$ esptool.py --port <PORT> chip_id`

 4. Enter ROM bootloader mode on the chip by pressing the BOOT button and, *while keeping the BOOT button pressed*,
press the RESET button. After you've released the RESET button, you can release the BOOT button as well. The port should
now be visible again.

 5. Flash the bootloader to the chip: `$ esptool.py --port <PORT> write_flash 0x0 <BOOTLOADER_FILE>` where
`<BOOTLOADER_FILE>` is the file you've downloaded in step 1.

Hint [Linux]: In order to write to the serial port, you must be root or belong to the `uucp` group.
