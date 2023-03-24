#include <Adafruit_Si7021.h>
#include <Adafruit_NeoPixel.h>

const int LED_COUNT = 23;
const int LED_PIN = 5;

Adafruit_Si7021 temp = Adafruit_Si7021();
Adafruit_NeoPixel strip(LED_COUNT, LED_PIN, NEO_RGB + NEO_KHZ800);

void setup() {
    pinMode(LED_BUILTIN, OUTPUT);

    if (!temp.begin()) {
        Serial.println("Unable to find temperature sensor!");
    }

    strip.begin();
    strip.setPixelColor(0, strip.Color(255, 0, 0));
    strip.setPixelColor(1, strip.Color(0, 255, 0));
    strip.setPixelColor(2, strip.Color(0, 0, 255));
    strip.show();
    strip.setBrightness(20);
}

float normalizeTemp(float temp, const float low = 20, const float high = 40)
{
    const float norm = (temp - low) / (high - low);
    return norm < 0.0f ? 0.0f : (norm > 1.0f ? 1.0f : norm);  // clamp in [0, 1]
}

void setFullStrip(uint32_t color)
{
    for (int i = 0; i < LED_COUNT; ++i) {
        strip.setPixelColor(i, color);
    }
}

void loop() {
    static bool blink = true;

    const int DELAY = 100;   // Update delay in milliseconds

    const int LOW_TEMP = 21;  // Temperature at which the light is green
    const int HIGH_TEMP = 31;  // Temperature at which the light is red

    // Calculate color based on temperature
    const float currentTemp = temp.readTemperature();
    const float norm = normalizeTemp(currentTemp, LOW_TEMP, HIGH_TEMP);
    setFullStrip(strip.Color((int)(255.0f * (1.0f - norm)), (int)(255.0f * norm), 0));

    // Finalize
    strip.show();
    Serial.println(currentTemp, 2);

    // Let the built-in light blink, just to get a confirmation of life
    digitalWrite(LED_BUILTIN, (int)blink);
    blink = !blink;

    delay(DELAY);
}
