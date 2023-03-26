// Test bluetooth stuff

#include <BLEDevice.h>
#include <BLEServer.h>

#define SERVICE_UUID        "4fafc201-1fb5-459e-8fcc-c5c9c331914b"
#define CHARACTERISTIC_UUID "beb5483e-36e1-4688-b7f5-ea07361b26a8"

void setup() {
    Serial.begin(115200);

    BLEDevice::init("My BLE test device :D");
    BLEServer* server = BLEDevice::createServer();
    BLEService* service = server->createService(SERVICE_UUID);

    //BLECharacteristic* characteristic = service->createCharacteristic(
    //    CHARACTERISTIC_UUID,
    //    BLECharacteristic::PROPERTY_READ | BLECharacteristic::PROPERTY_WRITE
    //);
    //characteristic->setValue("Hello World!");

    service->start();

    BLEAdvertising* advertising = BLEDevice::getAdvertising();
    advertising->addServiceUUID(SERVICE_UUID);
    advertising->setScanResponse(true);
    advertising->setMinPreferred(0x06);  // functions that help with iPhone connections issue
    advertising->setMinPreferred(0x12);
    BLEDevice::startAdvertising();
}

void loop() {
    delay(500);
}
