// Test bluetooth stuff
// This has turned into the production code running on the Thermo Bo-Bo.

// Uncomment to print debug logging to Serial:
// #define DEBUG

#include <BLEDevice.h>
#include <BLEServer.h>
#include <ESP32AnalogRead.h>

#define SERVICE_UUID "bbbaa765-0507-423a-9494-9cfd4d7e86fb"

#define TEMPERATURE_CHARACTERISTIC_UUID "673eb6f3-1af4-48db-83ac-dd9d3b0c5950"
#define TEMPERATURE_VALUE_ACCURACY (1e3)
#define TEMPERATURE_VALUE_INVALID "-"

//////////////////////////////////////////////////////
// Temperature analog <-> digital conversion constants

const int ThermistorPin = 33;
const int vout_pin = 23;
const double R1 = 10000;

//const float A = 7.6647e-04;
//const float B = 2.3051e-04;
//const float C = 7.3815e-08;

const double A = 0.001125308852122;
const double B = 0.000234711863267;
const double C = 0.000000085663516;

//const double A = 3.354016e-3;
//const double B = 2.569850e-4;
//const double C = 2.620131e-6;

ESP32AnalogRead adc;

// ---------------------------------------------------
//////////////////////////////////////////////////////

const int TICK_DELAY = 1000;

uint32_t tempVal = 0;
BLECharacteristic* tempCharacteristic = nullptr;

bool deviceConnected = false;

class ConnectionCallbacks : public BLEServerCallbacks
{
    void onConnect(BLEServer* server) override
    {
        deviceConnected = true;
    };

    void onDisconnect(BLEServer* server) override
    {
        deviceConnected = false;
        delay(500);
        server->startAdvertising();
    }
};

void setup() {
    Serial.begin(115200);

    // Set up bluetooth
    BLEDevice::init("Thermo Bo-Bo");
    BLEServer* server = BLEDevice::createServer();
    server->setCallbacks(new ConnectionCallbacks);

    BLEService* service = server->createService(SERVICE_UUID);

    // Create a characteristic for the temperature value
    tempCharacteristic = service->createCharacteristic(
        TEMPERATURE_CHARACTERISTIC_UUID,
        BLECharacteristic::PROPERTY_READ | BLECharacteristic::PROPERTY_NOTIFY
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
    tempCharacteristic->setValue(TEMPERATURE_VALUE_INVALID);
    pinMode(vout_pin, OUTPUT);
    adc.attach(33);
}

float readTemperature() {
    double Vo = adc.readVoltage() / 3.3 * 4095;
    if (Vo == 0.0) return 0.0f;  // Guard against division by zero

    const double R2 = R1 * (4095.0 / Vo - 1.0);
    const double logR2 = log(R2);

    double T = (1.0 / (A + B*logR2 + C*logR2*logR2*logR2));
    T = T - 273.15;

    // HACK
    const double correction = 2.1;

    return (float)(T + correction);
}

void loop() {
    if (deviceConnected)
    {
        digitalWrite(vout_pin, HIGH);
        delay(50);  // Wait for the voltage to stabilize
        const float currentTemp = readTemperature();
        digitalWrite(vout_pin, LOW);

        // Include 3 digits after the point
        tempVal = uint32_t(currentTemp * TEMPERATURE_VALUE_ACCURACY);

        // Update the temperature value.
        tempCharacteristic->setValue(tempVal);
        tempCharacteristic->notify();

#ifdef DEBUG
        Serial.print("Temperature: ");
        Serial.print(currentTemp);
        Serial.println(" °C");
#endif
    }

    delay(TICK_DELAY);
}
