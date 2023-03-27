// Test bluetooth stuff

#include <Adafruit_Si7021.h>
#include <BLEDevice.h>
#include <BLEServer.h>

#define SERVICE_UUID        "4fafc201-1fb5-459e-8fcc-c5c9c331914b"

#define TEMPERATURE_CHARACTERISTIC_UUID "673eb6f3-1af4-48db-83ac-dd9d3b0c5950"
#define TEMPERATURE_VALUE_ACCURACY (1e3)
#define TEMPERATURE_VALUE_INVALID "INVALID TEMP VALUE"

Adafruit_Si7021 temp = Adafruit_Si7021();

uint32_t tempVal = 0;
BLECharacteristic* tempCharacteristic = nullptr;

void setup() {
    Serial.begin(115200);

    // Set up bluetooth
    BLEDevice::init("My BLE test device :D");
    BLEServer* server = BLEDevice::createServer();
    BLEService* service = server->createService(SERVICE_UUID);

    // Create a characteristic for the temperature value
    tempCharacteristic = service->createCharacteristic(
        TEMPERATURE_CHARACTERISTIC_UUID,
        BLECharacteristic::PROPERTY_READ
    );
    tempCharacteristic->setValue(tempVal);

    // Start the service.
    // I think it is required to start this *after* all of the characteristics
    // have been defined.
    service->start();

    // Start advertising
    BLEAdvertising* advertising = BLEDevice::getAdvertising();
    advertising->addServiceUUID(SERVICE_UUID);
    advertising->setScanResponse(true);
    advertising->setMinPreferred(0x06);  // functions that help with iPhone connections issue
    advertising->setMinPreferred(0x12);
    BLEDevice::startAdvertising();

    // Set up temperature sensor
    if (!temp.begin()) {
        tempCharacteristic->setValue(TEMPERATURE_VALUE_INVALID);
    }
}

void loop() {
    // Include 3 digits after the point
    tempVal = uint32_t(temp.readTemperature() * TEMPERATURE_VALUE_ACCURACY);

    // Update the temperature value.
    if (tempCharacteristic != nullptr) {
        tempCharacteristic->setValue(tempVal);
    }

    delay(500);
}
